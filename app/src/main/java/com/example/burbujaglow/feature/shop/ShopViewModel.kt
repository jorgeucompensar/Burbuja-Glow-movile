package com.example.burbujaglow.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burbujaglow.Repositorios
import com.example.burbujaglow.data.PaymentRepository
import com.example.burbujaglow.data.SolicitudPago
import com.example.burbujaglow.data.burbujaRepository
import com.example.burbujaglow.data.model.ItemCarrito
import com.example.burbujaglow.data.model.PedidoAdmin
import com.example.burbujaglow.data.model.ProductoComprador
import com.example.burbujaglow.data.model.totalesCarrito
import com.example.burbujaglow.data.remote.FirestoreDataRepository
import com.google.firebase.firestore.ListenerRegistration
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

sealed interface CompradorRoute {
    object Inicio : CompradorRoute
    object Catalogo : CompradorRoute
    data class Detalle(val producto: ProductoComprador) : CompradorRoute
    object Carrito : CompradorRoute
    object Pago : CompradorRoute
}

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
    val metodoPagoSel: String = "Nequi",
    val erroresCheckout: Set<String> = emptySet(),
    val numeroPedido: String = "MAK-10482",
    val siguientePedido: Int = 10482,
    val confirmarVaciar: Boolean = false,
    val confirmarCompra: Boolean = false,
    val mostrarExito: Boolean = false,
    val uid: String = "",
    val correo: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val sesionPago: String = "",
    val pagando: Boolean = false,
    val pagoEstado: String = "PENDIENTE"
)

sealed interface ShopUiEffect {
    data class Toast(val mensaje: String) : ShopUiEffect
}

// Regex compilados una sola vez (antes se creaban en cada validación)
private val PHONE_REGEX = Regex("^[0-9+\\s]{7,15}$")
private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")

// ViewModel de la tienda: filtros, carrito, checkout y navegación interna
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

    private fun toast(mensaje: String) { _uiEffect.tryEmit(ShopUiEffect.Toast(mensaje)) }

    // Los totales se calculan en la UI desde state.carrito con totalesCarrito()
    // El carrito vive en el repositorio; se refleja en el estado vía sync
    private fun syncCarrito() { _uiState.value = _uiState.value.copy(carrito = repository.carrito.toList()) }

    fun configurarSesion(uid: String, correo: String) {
        _uiState.value = _uiState.value.copy(uid = uid, correo = correo)
        carritoListener?.remove()
        carritoListener = datos.observarCarrito(uid) { items ->
            repository.reemplazarCarrito(items)
            syncCarrito()
        }
    }

    fun onUbicacionChange(latitud: Double, longitud: Double) {
        val s = _uiState.value
        _uiState.value = s.copy(latitud = latitud, longitud = longitud)
        viewModelScope.launch {
            datos.actualizarUbicacion(s.uid, s.address, s.city, latitud, longitud)
        }
    }

    fun prepararPago(onListo: (String) -> Unit, onError: (String) -> Unit) {
        val s = _uiState.value
        if (!pagos.configurado) {
            onError("Configura EPAYCO_SESSION_URL y EPAYCO_PUBLIC_KEY en local.properties")
            return
        }
        _uiState.value = s.copy(pagando = true)
        viewModelScope.launch {
            pagos.crearSesion(
                SolicitudPago(
                    numeroPedido = "MAK-${s.siguientePedido}",
                    descripcion = "Pedido Burbuja Glow",
                    valor = totalesCarrito(s.carrito).total,
                    nombre = s.fullName,
                    correo = s.email
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

    fun pagoResultado(estado: String) {
        val normalizado = estado.uppercase()
        _uiState.value = _uiState.value.copy(pagoEstado = normalizado, sesionPago = "")
        if (normalizado == "APROBADO") finalizarPedido("ePayco")
        else toast("El pago no se completó. Estado: $normalizado")
    }

    fun cerrarPago() { _uiState.value = _uiState.value.copy(sesionPago = "") }

    fun htmlPago(sessionId: String): String = pagos.htmlCheckout(sessionId)

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

    fun validarCheckout(): Boolean {
        val s = _uiState.value
        val errs = mutableSetOf<String>()
        if (s.fullName.trim().length < 5) errs.add("fullName")
        if (!s.phone.matches(Regex("^[0-9+\\s]{7,15}$"))) errs.add("phone")
        if (!s.email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$"))) errs.add("email")
        if (s.address.trim().length < 6) errs.add("address")
        if (s.city.trim().length < 2) errs.add("city")
        if (s.department.trim().length < 2) errs.add("department")
        if (s.postal.trim().length < 4) errs.add("postal")
        _uiState.value = s.copy(erroresCheckout = errs)
        return errs.isEmpty()
    }

    fun procesarCompra() {
        finalizarPedido(_uiState.value.metodoPagoSel)
    }

    private fun finalizarPedido(metodo: String) {
        val s = _uiState.value
        val numero = "MAK-${s.siguientePedido}"
        val total = totalesCarrito(s.carrito).total
        val pedido = PedidoAdmin(
            numero = numero,
            cliente = s.fullName,
            correo = s.email,
            fecha = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")).format(Date()),
            productos = s.carrito.joinToString(" · ") { "${it.producto.nombre} x${it.cantidad}" },
            total = total,
            pago = metodo,
            estado = "Confirmado",
            uid = numero,
            compradorUid = s.uid,
            vendedorIds = s.carrito.map { it.producto.vendedorId }.filter { it.isNotBlank() }.distinct(),
            latitud = s.latitud,
            longitud = s.longitud,
            estadoPago = if (metodo == "ePayco") s.pagoEstado else "PENDIENTE",
            direccion = "${s.address}, ${s.city}, ${s.department}"
        )
        _uiState.value = s.copy(
            numeroPedido = numero,
            siguientePedido = s.siguientePedido + 1,
            mostrarExito = true,
            pagoEstado = "PENDIENTE"
        )
        repository.descontarStock()
        repository.vaciarCarrito()
        syncCarrito()
        viewModelScope.launch {
            datos.guardarPedido(pedido)
            if (s.uid.isNotBlank()) datos.vaciarCarrito(s.uid)
        }
    }

    fun onConfirmarVaciarChange(v: Boolean) { _uiState.value = _uiState.value.copy(confirmarVaciar = v) }
    fun onConfirmarCompraChange(v: Boolean) { _uiState.value = _uiState.value.copy(confirmarCompra = v) }
    fun onMostrarExitoChange(v: Boolean) { _uiState.value = _uiState.value.copy(mostrarExito = v) }
}
