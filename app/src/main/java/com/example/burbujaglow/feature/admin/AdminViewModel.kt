package com.example.burbujaglow.feature.admin

import androidx.lifecycle.ViewModel
import com.example.burbujaglow.data.burbujaRepository
import com.example.burbujaglow.data.model.ProductoAdmin
import com.example.burbujaglow.data.model.UsuarioAdmin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

// ViewModel del panel administrativo: dashboard, CRUD, pedidos, inventario, config
class AdminViewModel : ViewModel() {
    private val repository = burbujaRepository
    val productos = repository.productosAdmin
    val pedidos = repository.pedidos
    val clientes = repository.clientes
    val usuarios = repository.usuarios
    val estadosPedido = repository.estadosPedido
    private var proximoUsuarioId = 100

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()
    private var proximoId = 100

    fun onSeccionChange(v: String) { _uiState.value = _uiState.value.copy(seccion = v) }
    fun onFiltroPedidoChange(v: String) { _uiState.value = _uiState.value.copy(filtroPedido = v) }
    fun onBusquedaAdminChange(v: String) { _uiState.value = _uiState.value.copy(busquedaAdmin = v) }
    fun onEditarChange(v: ProductoAdmin?) { _uiState.value = _uiState.value.copy(productoAEditar = v) }
    fun onEliminarChange(v: ProductoAdmin?) { _uiState.value = _uiState.value.copy(productoAEliminar = v) }
    fun onAgregarChange(v: Boolean) { _uiState.value = _uiState.value.copy(mostrarAgregar = v) }
    fun onCfgChange(cfg: (AdminUiState) -> AdminUiState) { _uiState.value = cfg(_uiState.value) }

    fun guardarProducto(original: ProductoAdmin?, nombre: String, categoria: String, precio: String, stock: String, estado: String) {
        if (original != null) {
            val idx = productos.indexOfFirst { it.id == original.id }
            repository.updateProductoAdmin(original.copy(nombre = nombre, categoria = categoria, precio = precio, stock = stock, estado = estado.uppercase()))
        } else {
            repository.addProductoAdmin(ProductoAdmin(proximoId++, nombre, categoria, precio, stock, estado.uppercase()))
        }
        _uiState.value = _uiState.value.copy(productoAEditar = null, mostrarAgregar = false)
    }

    fun eliminarProducto(p: ProductoAdmin) {
        repository.removeProductoAdmin(p)
        _uiState.value = _uiState.value.copy(productoAEliminar = null)
    }

    fun restaurarDemo() { repository.restaurarCatalogoAdmin() }

    fun cambiarEstadoPedido(numero: String, nuevoEstado: String) {
        val ped = pedidos.find { it.numero == numero } ?: return
        repository.updatePedido(ped.copy(estado = nuevoEstado))
    }

    // Gestión de usuarios: creación, edición y eliminación
    fun onEditarUsuarioChange(v: UsuarioAdmin?) { _uiState.value = _uiState.value.copy(usuarioAEditar = v) }
    fun onEliminarUsuarioChange(v: UsuarioAdmin?) { _uiState.value = _uiState.value.copy(usuarioAEliminar = v) }
    fun onAgregarUsuarioChange(v: Boolean) { _uiState.value = _uiState.value.copy(mostrarAgregarUsuario = v) }
    fun onPeriodoReporteChange(v: String) { _uiState.value = _uiState.value.copy(periodoReporte = v) }

    fun guardarUsuario(original: UsuarioAdmin?, nombre: String, correo: String, telefono: String, rol: String, estado: String) {
        if (original != null) {
            repository.updateUsuario(original.copy(nombre = nombre, correo = correo, telefono = telefono, rol = rol, estado = estado))
        } else {
            repository.addUsuario(UsuarioAdmin(proximoUsuarioId++, nombre, correo, telefono, rol, estado))
        }
        _uiState.value = _uiState.value.copy(usuarioAEditar = null, mostrarAgregarUsuario = false)
    }

    fun eliminarUsuario(u: UsuarioAdmin) {
        repository.removeUsuario(u)
        _uiState.value = _uiState.value.copy(usuarioAEliminar = null)
    }
}
