package com.example.burbujaglow.feature.vendedor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burbujaglow.Repositorios
import com.example.burbujaglow.data.burbujaRepository
import com.example.burbujaglow.data.model.ProductoAdmin
import com.example.burbujaglow.data.remote.FirestoreDataRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

private const val TAG = "VendedorViewModel"

/** Efectos puntuales del módulo vendedor, como mensajes Toast. */
sealed interface VendedorUiEffect {
    data class Toast(val mensaje: String) : VendedorUiEffect
}

/** Estado del módulo vendedor: sección, filtros, diálogos y perfil editable. */
data class VendedorUiState(
    val seccion: String = "productos",
    val busqueda: String = "",
    val filtroPedido: String = "Todos",
    val productoAEditar: ProductoAdmin? = null,
    val productoAEliminar: ProductoAdmin? = null,
    val mostrarAgregar: Boolean = false,
    val perfilNombre: String = "Vendedor Burbuja Glow",
    val perfilCorreo: String = "vendedor@burbujaglow.test",
    val perfilTelefono: String = "+57 300 123 4567",
    val perfilCiudad: String = "Bogotá D.C., Colombia",
    val perfilTienda: String = "BURBUJA GLOW · Boutique de cosmética y cuidado capilar",
    val vendedorUid: String = ""
)

/** ViewModel del vendedor: CRUD de sus productos, pedidos asignados y perfil remoto. */
class VendedorViewModel(
    private val datos: FirestoreDataRepository = Repositorios.datos
) : ViewModel() {
    private val repository = burbujaRepository
    val productos = repository.productosAdmin
    val pedidos = repository.pedidos
    val estadosPedido = repository.estadosPedido

    private val _uiState = MutableStateFlow(VendedorUiState())
    val uiState: StateFlow<VendedorUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<VendedorUiEffect>(extraBufferCapacity = 8)
    val uiEffect: SharedFlow<VendedorUiEffect> = _uiEffect.asSharedFlow()

/** Emite un aviso breve para la UI del módulo vendedor. */
    private fun notificar(mensaje: String) { _uiEffect.tryEmit(VendedorUiEffect.Toast(mensaje)) }

    /** Genera un identificador numérico positivo para nuevos productos locales. */
    private fun nuevoId(): Int {
        val valor = UUID.randomUUID().hashCode()
        return if (valor == Int.MIN_VALUE) 0 else abs(valor)
    }

/** Genera un `uid` único para documentos nuevos de Firestore. */
    private fun nuevoUid(prefixo: String): String = "${prefixo}_${UUID.randomUUID()}"

    fun onSeccionChange(v: String) { _uiState.value = _uiState.value.copy(seccion = v) }
    fun onBusquedaChange(v: String) { _uiState.value = _uiState.value.copy(busqueda = v) }
    fun onFiltroPedidoChange(v: String) { _uiState.value = _uiState.value.copy(filtroPedido = v) }
    fun onEditarChange(v: ProductoAdmin?) { _uiState.value = _uiState.value.copy(productoAEditar = v) }
    fun onEliminarChange(v: ProductoAdmin?) { _uiState.value = _uiState.value.copy(productoAEliminar = v) }
    fun onAgregarChange(v: Boolean) { _uiState.value = _uiState.value.copy(mostrarAgregar = v) }

/** Guarda el UID del vendedor y carga su perfil remoto para poblar el formulario. */
    fun configurarVendedor(uid: String) {
        _uiState.value = _uiState.value.copy(
            vendedorUid = uid,
            perfilCorreo = Repositorios.auth.currentUser?.email.orEmpty().ifBlank { _uiState.value.perfilCorreo }
        )
        viewModelScope.launch {
            datos.obtenerPerfil(uid)
                .onSuccess { perfil ->
                    if (perfil == null) return@onSuccess
                    val actual = _uiState.value
                    _uiState.value = actual.copy(
                        perfilNombre = perfil.nombre.ifBlank { actual.perfilNombre },
                        perfilCorreo = perfil.correo.ifBlank { actual.perfilCorreo },
                        perfilTelefono = perfil.telefono.ifBlank { actual.perfilTelefono },
                        perfilCiudad = perfil.ciudad.ifBlank { actual.perfilCiudad }
                    )
                }
                .onFailure { error -> Log.e(TAG, "Error al cargar perfil del vendedor", error) }
        }
    }

/** Crea o actualiza un producto asignándolo siempre al UID del vendedor activo. */
    fun guardarProducto(original: ProductoAdmin?, nombre: String, categoria: String, precio: String, stock: String, estado: String, imagenUrl: String = "") {
        val vendedorUid = _uiState.value.vendedorUid
        val producto = if (original != null) {
            original.copy(
                nombre = nombre, categoria = categoria, precio = precio,
                stock = stock, estado = estado.uppercase(),
                uid = original.uid.ifBlank { "p_${original.id}" },
                vendedorId = original.vendedorId.ifBlank { vendedorUid },
                imagenUrl = imagenUrl.ifBlank { original.imagenUrl }
            )
        } else {
            ProductoAdmin(
                nuevoId(), nombre, categoria, precio, stock, estado.uppercase(),
                imagenUrl = imagenUrl, uid = nuevoUid("p"), vendedorId = vendedorUid
            )
        }
        viewModelScope.launch {
            datos.guardarProductoAdmin(producto)
                .onSuccess { notificar(if (original != null) "$nombre actualizado" else "$nombre agregado al catálogo") }
                .onFailure { error ->
                    Log.e(TAG, "Error al guardar producto", error)
                    notificar("No se pudo guardar el producto: ${error.message.orEmpty()}")
                }
            _uiState.value = _uiState.value.copy(productoAEditar = null, mostrarAgregar = false)
        }
    }

/** Elimina un producto remoto y lo retira de la lista local del vendedor. */
    fun eliminarProducto(p: ProductoAdmin) {
        viewModelScope.launch {
            datos.eliminarProductoAdmin(p)
                .onSuccess {
                    repository.removeProductoAdmin(p)
                    notificar("${p.nombre} eliminado")
                }
                .onFailure { error ->
                    Log.e(TAG, "Error al eliminar producto", error)
                    notificar("No se pudo eliminar el producto: ${error.message.orEmpty()}")
                }
            _uiState.value = _uiState.value.copy(productoAEliminar = null)
        }
    }

/** Siembra el catálogo demo asignando los productos al vendedor autenticado. */
    fun restaurarDemo() {
        viewModelScope.launch {
            datos.sembrarCatalogoDemo(_uiState.value.vendedorUid)
                .onSuccess { sembrados ->
                    notificar(
                        if (sembrados > 0) "Catálogo demo restaurado ($sembrados productos)"
                        else "El catálogo remoto ya tenía productos"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Error al sembrar catálogo demo", error)
                    notificar("No se pudo restaurar el catálogo: ${error.message.orEmpty()}")
                }
        }
    }

/** Cambia el estado de un pedido asignado al vendedor. */
    fun cambiarEstadoPedido(numero: String, nuevoEstado: String) {
        val ped = pedidos.find { it.numero == numero } ?: return
        viewModelScope.launch {
            datos.cambiarEstadoPedido(numero, nuevoEstado)
                .onSuccess { repository.updatePedido(ped.copy(estado = nuevoEstado)) }
                .onFailure { error ->
                    Log.e(TAG, "Error al cambiar estado del pedido", error)
                    notificar("No se pudo actualizar el pedido: ${error.message.orEmpty()}")
                }
        }
    }

/** Actualiza el perfil del vendedor en Firestore en una sola escritura. */
    fun guardarPerfil(nombre: String, correo: String, telefono: String, ciudad: String, tienda: String) {
        _uiState.value = _uiState.value.copy(
            perfilNombre = nombre,
            perfilCorreo = correo,
            perfilTelefono = telefono,
            perfilCiudad = ciudad,
            perfilTienda = tienda
        )
        val uid = _uiState.value.vendedorUid
        viewModelScope.launch {
            datos.actualizarPerfilVendedor(uid, nombre, correo, telefono, ciudad)
                .onSuccess { notificar("Perfil actualizado") }
                .onFailure { error ->
                    Log.e(TAG, "Error al guardar perfil del vendedor", error)
                    notificar("No se pudo actualizar el perfil: ${error.message.orEmpty()}")
                }
        }
    }
}
