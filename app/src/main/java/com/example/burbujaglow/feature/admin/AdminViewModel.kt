package com.example.burbujaglow.feature.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burbujaglow.Repositorios
import com.example.burbujaglow.data.burbujaRepository
import com.example.burbujaglow.data.model.ProductoAdmin
import com.example.burbujaglow.data.model.UsuarioAdmin
import com.example.burbujaglow.data.remote.FirestoreDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

private const val TAG = "AdminViewModel"

sealed interface AdminUiEffect {
    data class Toast(val mensaje: String) : AdminUiEffect
}

data class AdminUiState(
    val seccion: String = "dashboard",
    val filtroPedido: String = "Todos",
    val busquedaAdmin: String = "",
    val productoAEditar: ProductoAdmin? = null,
    val productoAEliminar: ProductoAdmin? = null,
    val mostrarAgregar: Boolean = false,
    val usuarioAEditar: UsuarioAdmin? = null,
    val usuarioAEliminar: UsuarioAdmin? = null,
    val mostrarAgregarUsuario: Boolean = false,
    val periodoReporte: String = "Todos",
    val cfgNombre: String = "Burbuja Glow",
    val cfgCorreo: String = "hola@burbujaglow.com",
    val cfgTelefono: String = "+57 300 123 4567",
    val cfgDireccion: String = "Bogotá D.C., Colombia",
    val cfgEnvio: String = "9900",
    val cfgMoneda: String = "COP"
)

class AdminViewModel(
    private val datos: FirestoreDataRepository = Repositorios.datos
) : ViewModel() {
    private val repository = burbujaRepository
    val productos = repository.productosAdmin
    val pedidos = repository.pedidos
    val clientes = repository.clientes
    val usuarios = repository.usuarios
    val estadosPedido = repository.estadosPedido

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<AdminUiEffect>(extraBufferCapacity = 8)
    val uiEffect = _uiEffect.asSharedFlow()

    private fun nuevoId(): Int {
        val valor = UUID.randomUUID().hashCode()
        return if (valor == Int.MIN_VALUE) 0 else abs(valor)
    }

    private fun nuevoUid(prefixo: String): String = "${prefixo}_${UUID.randomUUID()}"

    fun asegurarCatalogoRemoto() {
        viewModelScope.launch {
            sembrarCatalogoDemo(mensajeSiVacio = false)
        }
    }

    fun onSeccionChange(v: String) { _uiState.value = _uiState.value.copy(seccion = v) }
    fun onFiltroPedidoChange(v: String) { _uiState.value = _uiState.value.copy(filtroPedido = v) }
    fun onBusquedaAdminChange(v: String) { _uiState.value = _uiState.value.copy(busquedaAdmin = v) }
    fun onEditarChange(v: ProductoAdmin?) { _uiState.value = _uiState.value.copy(productoAEditar = v) }
    fun onEliminarChange(v: ProductoAdmin?) { _uiState.value = _uiState.value.copy(productoAEliminar = v) }
    fun onAgregarChange(v: Boolean) { _uiState.value = _uiState.value.copy(mostrarAgregar = v) }
    fun onCfgChange(cfg: (AdminUiState) -> AdminUiState) { _uiState.value = cfg(_uiState.value) }
    fun notificar(mensaje: String) { _uiEffect.tryEmit(AdminUiEffect.Toast(mensaje)) }

    fun guardarProducto(original: ProductoAdmin?, nombre: String, categoria: String, precio: String, stock: String, estado: String, imagenUrl: String = "") {
        val producto = if (original != null) {
            original.copy(
                nombre = nombre, categoria = categoria, precio = precio,
                stock = stock, estado = estado.uppercase(),
                uid = original.uid.ifBlank { "p_${original.id}" },
                imagenUrl = imagenUrl.ifBlank { original.imagenUrl }
            )
        } else {
            ProductoAdmin(
                nuevoId(), nombre, categoria, precio, stock, estado.uppercase(),
                imagenUrl = imagenUrl, uid = nuevoUid("p")
            )
        }
        viewModelScope.launch {
            val uid = original?.uid?.ifBlank { "p_${original.id}" } ?: producto.uid.ifBlank { "p_${producto.id}" }
            val vendedor = producto.vendedorId.ifBlank { repositorioUid() }
            val remoto = producto.copy(uid = uid, vendedorId = vendedor)
            val resultado = datos.guardarProductoAdmin(remoto)
            resultado
                .onSuccess {
                    notificar(if (original != null) "$nombre actualizado" else "$nombre agregado al catálogo")
                }
                .onFailure { error ->
                    Log.e(TAG, "Error al guardar producto", error)
                    notificar("No se pudo guardar el producto: ${error.message.orEmpty()}")
                }
            _uiState.value = _uiState.value.copy(productoAEditar = null, mostrarAgregar = false)
        }
    }

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

    fun restaurarDemo() {
        viewModelScope.launch { sembrarCatalogoDemo(mensajeSiVacio = true) }
    }

    private suspend fun sembrarCatalogoDemo(mensajeSiVacio: Boolean) {
        datos.sembrarCatalogoDemo(repositorioUid())
            .onSuccess { sembrados ->
                if (sembrados > 0) {
                    repository.restaurarCatalogoAdmin()
                    notificar("Catálogo demo restaurado ($sembrados productos)")
                } else if (mensajeSiVacio) {
                    notificar("El catálogo remoto ya tenía productos")
                }
            }
            .onFailure { error ->
                Log.e(TAG, "Error al sembrar catálogo demo", error)
                notificar("No se pudo restaurar el catálogo: ${error.message.orEmpty()}")
            }
    }

    private fun repositorioUid(): String = Repositorios.auth.currentUser?.uid.orEmpty()

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

    fun onEditarUsuarioChange(v: UsuarioAdmin?) { _uiState.value = _uiState.value.copy(usuarioAEditar = v) }
    fun onEliminarUsuarioChange(v: UsuarioAdmin?) { _uiState.value = _uiState.value.copy(usuarioAEliminar = v) }
    fun onAgregarUsuarioChange(v: Boolean) { _uiState.value = _uiState.value.copy(mostrarAgregarUsuario = v) }
    fun onPeriodoReporteChange(v: String) { _uiState.value = _uiState.value.copy(periodoReporte = v) }

    fun guardarUsuario(original: UsuarioAdmin?, nombre: String, correo: String, telefono: String, rol: String, estado: String) {
        val rolFinal = when (rol.uppercase()) {
            "ADMIN", "ADMINISTRADOR" -> "ADMIN"
            "VENDEDOR" -> "VENDEDOR"
            else -> "COMPRADOR"
        }
        val usuario = if (original != null) {
            original.copy(
                nombre = nombre, correo = correo, telefono = telefono, rol = rolFinal, estado = estado,
                uid = original.uid.ifBlank { "u_${original.id}" }
            )
        } else {
            UsuarioAdmin(nuevoId(), nombre, correo, telefono, rolFinal, estado, uid = nuevoUid("u"))
        }
        viewModelScope.launch {
            datos.guardarUsuario(usuario)
                .onSuccess {
                    notificar(if (original != null) "$nombre actualizado" else "$nombre creado")
                }
                .onFailure { error ->
                    Log.e(TAG, "Error al guardar usuario", error)
                    notificar("No se pudo guardar el usuario: ${error.message.orEmpty()}")
                }
            _uiState.value = _uiState.value.copy(usuarioAEditar = null, mostrarAgregarUsuario = false)
        }
    }

    fun eliminarUsuario(u: UsuarioAdmin) {
        viewModelScope.launch {
            datos.desactivarUsuario(u)
                .onSuccess {
                    repository.updateUsuario(u.copy(estado = "Inactivo"))
                    notificar("${u.nombre} eliminado")
                }
                .onFailure { error ->
                    Log.e(TAG, "Error al desactivar usuario", error)
                    notificar("No se pudo desactivar el usuario: ${error.message.orEmpty()}")
                }
            _uiState.value = _uiState.value.copy(usuarioAEliminar = null)
        }
    }
}
