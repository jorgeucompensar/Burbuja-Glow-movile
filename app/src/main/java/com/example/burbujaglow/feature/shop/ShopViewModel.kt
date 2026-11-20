package com.example.burbujaglow.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burbujaglow.Repositorios
import com.example.burbujaglow.data.ClientePago
import com.example.burbujaglow.data.ItemPago
import com.example.burbujaglow.data.PaymentRepository
import com.example.burbujaglow.data.SolicitudPago
import com.example.burbujaglow.data.burbujaRepository
import com.example.burbujaglow.data.model.ItemCarrito
import com.example.burbujaglow.data.model.ItemPedido
import com.example.burbujaglow.data.model.PedidoAdmin
import com.example.burbujaglow.data.model.ProductoComprador
import com.example.burbujaglow.data.model.totalesCarrito
import com.example.burbujaglow.data.remote.FirestoreDataRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Rutas internas del flujo comprador dentro del NavHost de la tienda. */
sealed interface CompradorRoute {
    object Inicio : CompradorRoute
    object Catalogo : CompradorRoute
    data class Detalle(val producto: ProductoComprador) : CompradorRoute
    object Carrito : CompradorRoute
    object Pago : CompradorRoute
}

/** Estado de la tienda: filtros, carrito, checkout y estado del pago ePayco. */
data class ShopUiState(
    val ruta: CompradorRoute = CompradorRoute.Inicio,
    val categoriaSel: String = "Todos",
    val busqueda: String = "",
    val precioMin: String = "",
    val precioMax: String = "",
    val soloOfertas: Boolean = false,
    val soloBest: Boolean = false,
    val soloNuevos: Boolean = false,
    val ordenSel: String = "recientes",
    val mostrarFiltros: Boolean = false,
    val carrito: List<ItemCarrito> = emptyList(),
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "Bogotá",
    val department: String = "Cundinamarca",
    val postal: String = "110111",
    val metodoPagoSel: String = "ePayco",
    val erroresCheckout: Set<String> = emptySet(),
    val numeroPedido: String = "",
    val confirmarVaciar: Boolean = false,
    val confirmarCompra: Boolean = false,
    val mostrarExito: Boolean = false,
    val uid: String = "",
    val correo: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val sesionPago: String = "",
    val pedidoIdPago: String = "",
    val pagando: Boolean = false,
    val verificandoPago: Boolean = false,
    val pagoEstado: String = "PENDIENTE"
)

/** Efectos puntuales de la tienda, como mensajes Toast. */
sealed interface ShopUiEffect {
    data class Toast(val mensaje: String) : ShopUiEffect
}

private val PHONE_REGEX = Regex("^[0-9+\\s]{7,15}$")
private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")

/** Orquesta catálogo, carrito, checkout y verificación del pago de ePayco. */
class ShopViewModel(
    private val datos: FirestoreDataRepository = Repositorios.datos
) : ViewModel() {
    private val repository = burbujaRepository
    private val pagos = PaymentRepository()
    private var carritoListener: ListenerRegistration? = null
    val productos = repository.productos

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ShopUiEffect>(extraBufferCapacity = 16)
    val uiEffect: SharedFlow<ShopUiEffect> = _uiEffect.asSharedFlow()

    /** Emite un mensaje corto para la UI sin bloquear el flujo principal. */
    private fun toast(mensaje: String) { _uiEffect.tryEmit(ShopUiEffect.Toast(mensaje)) }

    /** Copia el carrito del repositorio al estado para provocar recomposición. */
    private fun syncCarrito() { _uiState.value = _uiState.value.copy(carrito = repository.carrito.toList()) }

    /** Genera un número de pedido único combinando tiempo y UUID corto. */
    private fun nuevoNumeroPedido(): String =
        "MAK-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(6).uppercase()}"

/** Convierte el carrito a ítems de pedido, resolviendo el `uid` remoto del producto. */
    private fun itemsPedido(): List<ItemPedido> = _uiState.value.carrito.map { item ->
        ItemPedido(
            productoId = item.producto.uid.ifBlank { "p_${item.producto.id}" },
            cantidad = item.cantidad,
            nombre = item.producto.nombre,
            precioValor = item.producto.precioValor
        )
    }

    /** Guarda la sesión del comprador y comienza a escuchar su carrito remoto. */
    fun configurarSesion(uid: String, correo: String) {
        _uiState.value = _uiState.value.copy(uid = uid, correo = correo)
        carritoListener?.remove()
        carritoListener = datos.observarCarrito(uid) { items ->
            repository.reemplazarCarrito(items)
            syncCarrito()
        }
    }

    /** Actualiza las coordenadas del checkout y las persiste en el perfil del usuario. */
    fun onUbicacionChange(latitud: Double, longitud: Double) {
        val s = _uiState.value
        _uiState.value = s.copy(latitud = latitud, longitud = longitud)
        viewModelScope.launch {
            datos.actualizarUbicacion(s.uid, s.address, s.city, latitud, longitud)
        }
    }

    /** Crea la sesión de pago en el backend y entrega el `sessionId` al WebView. */
    fun prepararPago(onListo: (String) -> Unit, onError: (String) -> Unit) {
        val s = _uiState.value
        if (!pagos.configurado) {
            onError("Configura EPAYCO_SESSION_URL en local.properties y reinstala la app")
            return
        }
        val pedidoId = nuevoNumeroPedido()
        _uiState.value = s.copy(
            pagando = true,
            pedidoIdPago = pedidoId,
            numeroPedido = pedidoId,
            pagoEstado = "PENDIENTE"
        )
        viewModelScope.launch {
            pagos.crearSesion(
                SolicitudPago(
                    pedidoId = pedidoId,
                    compradorUid = s.uid,
                    items = itemsPedido().map { ItemPago(it.productoId, it.cantidad) },
                    cliente = ClientePago(
                        nombre = s.fullName.trim(),
                        email = s.email.trim(),
                        telefono = s.phone.trim(),
                        direccion = s.address.trim(),
                        ciudad = s.city.trim(),
                        departamento = s.department.trim(),
                        codigoPostal = s.postal.trim(),
                        latitud = s.latitud,
                        longitud = s.longitud
                    )
                )
            ).fold(
                onSuccess = { sesion ->
                    _uiState.value = _uiState.value.copy(pagando = false, sesionPago = sesion)
                    onListo(sesion)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(pagando = false)
                    onError(error.message ?: "No se pudo iniciar el pago")
                }
            )
        }
    }

/** Interpreta el resultado del puente JS del WebView e inicia la verificación cuando procede. */
    fun pagoResultado(estado: String) {
        val normalizado = estado.uppercase()
        if (normalizado == "RESPONDIDO" || normalizado == "APROBADO") {
            _uiState.value = _uiState.value.copy(pagoEstado = normalizado, verificandoPago = true)
            verificarPago()
        } else {
            _uiState.value = _uiState.value.copy(pagoEstado = normalizado)
            toast("El pago no se completó. Estado: $normalizado")
        }
    }

/** Hace polling del estado del pago hasta 15 intentos separados por 2 segundos. */
    private fun verificarPago() {
        viewModelScope.launch {
            val pedidoId = _uiState.value.pedidoIdPago
            if (pedidoId.isBlank()) {
                _uiState.value = _uiState.value.copy(verificandoPago = false)
                toast("No se pudo identificar el pago")
                return@launch
            }
            repeat(15) { intento ->
                pagos.verificar(pedidoId).fold(
                    onSuccess = { estado ->
                        when (estado.estadoPago.uppercase()) {
                            "APROBADO" -> {
                                _uiState.value = _uiState.value.copy(
                                    numeroPedido = estado.numero.ifBlank { pedidoId },
                                    pagoEstado = "APROBADO",
                                    verificandoPago = false,
                                    sesionPago = ""
                                )
                                finalizarCompraConfirmada()
                                return@launch
                            }
                            "RECHAZADO" -> {
                                _uiState.value = _uiState.value.copy(
                                    pagoEstado = "RECHAZADO",
                                    verificandoPago = false,
                                    sesionPago = ""
                                )
                                toast("Pago rechazado: ${estado.respuesta.ifBlank { "sin detalle" }}")
                                return@launch
                            }
                        }
                    },
                    onFailure = {
                        if (intento == 14) {
                            _uiState.value = _uiState.value.copy(verificandoPago = false)
                            toast("No se pudo verificar el pago: ${it.message ?: "error de red"}")
                        }
                    }
                )
                delay(2000)
            }
            _uiState.value = _uiState.value.copy(verificandoPago = false, sesionPago = "")
            toast("Pago pendiente; podrás revisarlo en tus pedidos cuando se confirme")
        }
    }

/** Tras aprobación, descuenta stock, vacía carritos local/remoto y muestra confirmación. */
    private fun finalizarCompraConfirmada() {
        repository.descontarStock()
        repository.vaciarCarrito()
        syncCarrito()
        val uid = _uiState.value.uid
        if (uid.isNotBlank()) viewModelScope.launch { datos.vaciarCarrito(uid) }
        _uiState.value = _uiState.value.copy(mostrarExito = true, pagoEstado = "APROBADO")
    }

    /** Cierra el WebView del pago limpiando la sesión del estado. */
    fun cerrarPago() { _uiState.value = _uiState.value.copy(sesionPago = "") }

    /** Devuelve el HTML del checkout ePayco para la sesión indicada. */
    fun htmlPago(sessionId: String): String = pagos.htmlCheckout(sessionId)

    /** Libera el listener del carrito cuando el ViewModel se destruye. */
    override fun onCleared() {
        carritoListener?.remove()
        carritoListener = null
        super.onCleared()
    }

    fun navegar(ruta: CompradorRoute) { _uiState.value = _uiState.value.copy(ruta = ruta) }
    fun onCategoriaChange(v: String) { _uiState.value = _uiState.value.copy(categoriaSel = v) }
    fun onBusquedaChange(v: String) { _uiState.value = _uiState.value.copy(busqueda = v) }
    fun onPrecioMinChange(v: String) { _uiState.value = _uiState.value.copy(precioMin = v) }
    fun onPrecioMaxChange(v: String) { _uiState.value = _uiState.value.copy(precioMax = v) }
    fun onSoloOfertasChange(v: Boolean) { _uiState.value = _uiState.value.copy(soloOfertas = v) }
    fun onSoloBestChange(v: Boolean) { _uiState.value = _uiState.value.copy(soloBest = v) }
    fun onSoloNuevosChange(v: Boolean) { _uiState.value = _uiState.value.copy(soloNuevos = v) }
    fun onOrdenChange(v: String) { _uiState.value = _uiState.value.copy(ordenSel = v) }
    fun onMostrarFiltrosChange(v: Boolean) { _uiState.value = _uiState.value.copy(mostrarFiltros = v) }
    fun limpiarFiltros() { _uiState.value = _uiState.value.copy(precioMin = "", precioMax = "", soloOfertas = false, soloBest = false, soloNuevos = false, categoriaSel = "Todos", busqueda = "") }

    fun addToCart(p: ProductoComprador, qty: Int = 1): String? {
        val error = repository.addToCart(p, qty)
        syncCarrito()
        return error
    }

    fun setCantidad(item: ItemCarrito, nueva: Int) {
        repository.setCantidad(item.producto.id, nueva)
        syncCarrito()
    }

    fun removeItem(item: ItemCarrito) { repository.removeItem(item.producto.id); syncCarrito() }
    fun vaciarCarrito() { repository.vaciarCarrito(); syncCarrito() }

    fun onFullNameChange(v: String) { _uiState.value = _uiState.value.copy(fullName = v, erroresCheckout = _uiState.value.erroresCheckout - "fullName") }
    fun onPhoneChange(v: String) { _uiState.value = _uiState.value.copy(phone = v, erroresCheckout = _uiState.value.erroresCheckout - "phone") }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v, erroresCheckout = _uiState.value.erroresCheckout - "email") }
    fun onAddressChange(v: String) { _uiState.value = _uiState.value.copy(address = v, erroresCheckout = _uiState.value.erroresCheckout - "address") }
    fun onCityChange(v: String) { _uiState.value = _uiState.value.copy(city = v, erroresCheckout = _uiState.value.erroresCheckout - "city") }
    fun onDepartmentChange(v: String) { _uiState.value = _uiState.value.copy(department = v, erroresCheckout = _uiState.value.erroresCheckout - "department") }
    fun onPostalChange(v: String) { _uiState.value = _uiState.value.copy(postal = v, erroresCheckout = _uiState.value.erroresCheckout - "postal") }
    fun onMetodoPagoChange(v: String) { _uiState.value = _uiState.value.copy(metodoPagoSel = v) }

    /** Valida los campos obligatorios del checkout y guarda los errores por clave para la UI. */
    fun validarCheckout(): Boolean {
        val s = _uiState.value
        val errs = mutableSetOf<String>()
        if (s.fullName.trim().length < 5) errs.add("fullName")
        if (!s.phone.matches(PHONE_REGEX)) errs.add("phone")
        if (!s.email.matches(EMAIL_REGEX)) errs.add("email")
        if (s.address.trim().length < 6) errs.add("address")
        if (s.city.trim().length < 2) errs.add("city")
        if (s.department.trim().length < 2) errs.add("department")
        if (s.postal.trim().length < 4) errs.add("postal")
        _uiState.value = s.copy(erroresCheckout = errs)
        return errs.isEmpty()
    }

    /** Confirma una compra contraentrega o inicia el flujo de pago según el método elegido. */
    fun procesarCompra() {
        finalizarPedido(_uiState.value.metodoPagoSel)
    }

/** Registra el pedido en Firestore, descuenta stock y vacía el carrito al confirmar. */
    private fun finalizarPedido(metodo: String) {
        val s = _uiState.value
        val numero = nuevoNumeroPedido()
        val items = itemsPedido()
        val pedido = PedidoAdmin(
            numero = numero,
            cliente = s.fullName.trim(),
            correo = s.email.trim(),
            fecha = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")).format(Date()),
            productos = s.carrito.joinToString(" · ") { "${it.producto.nombre} x${it.cantidad}" },
            total = totalesCarrito(s.carrito).total,
            pago = metodo,
            estado = "Confirmado",
            uid = numero,
            compradorUid = s.uid,
            vendedorIds = s.carrito.map { it.producto.vendedorId }.filter { it.isNotBlank() }.distinct(),
            latitud = s.latitud,
            longitud = s.longitud,
            estadoPago = "PENDIENTE",
            direccion = "${s.address}, ${s.city}, ${s.department}",
            items = items
        )
        _uiState.value = s.copy(numeroPedido = numero, pagando = true, pagoEstado = "PENDIENTE")
        viewModelScope.launch {
            datos.crearPedido(pedido, items, descontarStock = true).fold(
                onSuccess = {
                    repository.descontarStock()
                    repository.vaciarCarrito()
                    syncCarrito()
                    if (s.uid.isNotBlank()) datos.vaciarCarrito(s.uid)
                    _uiState.value = _uiState.value.copy(pagando = false, mostrarExito = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(pagando = false)
                    toast(error.message ?: "No se pudo registrar el pedido")
                }
            )
        }
    }

    fun onConfirmarVaciarChange(v: Boolean) { _uiState.value = _uiState.value.copy(confirmarVaciar = v) }
    fun onConfirmarCompraChange(v: Boolean) { _uiState.value = _uiState.value.copy(confirmarCompra = v) }
    fun onMostrarExitoChange(v: Boolean) { _uiState.value = _uiState.value.copy(mostrarExito = v) }
}
