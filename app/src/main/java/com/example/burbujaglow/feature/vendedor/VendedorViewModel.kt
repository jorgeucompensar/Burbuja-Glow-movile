package com.example.burbujaglow.feature.vendedor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burbujaglow.Repositorios
import com.example.burbujaglow.data.burbujaRepository
import com.example.burbujaglow.data.model.ProductoAdmin
import com.example.burbujaglow.data.remote.FirestoreDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VendedorUiState(
    val seccion: String = "productos",
    val busqueda: String = "",
    val filtroPedido: String = "Todos",
    val productoAEditar: ProductoAdmin? = null,
    val productoAEliminar: ProductoAdmin? = null,
    val mostrarAgregar: Boolean = false,
    val perfilNombre: String = "Vendedor Burbuja Glow",
    val perfilCorreo: String = "vendedor@burbujaglow.com",
    val perfilTelefono: String = "+57 300 123 4567",
    val perfilCiudad: String = "Bogotá D.C., Colombia",
    val perfilTienda: String = "BURBUJA GLOW · Boutique cruelty-free",
    val vendedorUid: String = ""
)

class VendedorViewModel(
    private val datos: FirestoreDataRepository = Repositorios.datos
) : ViewModel() {
    private val repository = burbujaRepository
    val productos = repository.productosAdmin
    val pedidos = repository.pedidos
    val estadosPedido = repository.estadosPedido

    private val _uiState = MutableStateFlow(VendedorUiState())
    val uiState: StateFlow<VendedorUiState> = _uiState.asStateFlow()
    private var proximoId = 200

    fun onSeccionChange(v: String) { _uiState.value = _uiState.value.copy(seccion = v) }
    fun onBusquedaChange(v: String) { _uiState.value = _uiState.value.copy(busqueda = v) }
    fun onFiltroPedidoChange(v: String) { _uiState.value = _uiState.value.copy(filtroPedido = v) }
    fun onEditarChange(v: ProductoAdmin?) { _uiState.value = _uiState.value.copy(productoAEditar = v) }
    fun onEliminarChange(v: ProductoAdmin?) { _uiState.value = _uiState.value.copy(productoAEliminar = v) }
    fun onAgregarChange(v: Boolean) { _uiState.value = _uiState.value.copy(mostrarAgregar = v) }
    fun configurarVendedor(uid: String) { _uiState.value = _uiState.value.copy(vendedorUid = uid) }

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
            val id = proximoId++
            ProductoAdmin(id, nombre, categoria, precio, stock, estado.uppercase(), imagenUrl = imagenUrl, uid = "p_$id", vendedorId = vendedorUid)
        }
        if (original != null) repository.updateProductoAdmin(producto) else repository.addProductoAdmin(producto)
        viewModelScope.launch { datos.guardarProductoAdmin(producto) }
        _uiState.value = _uiState.value.copy(productoAEditar = null, mostrarAgregar = false)
    }

    fun eliminarProducto(p: ProductoAdmin) {
        repository.removeProductoAdmin(p)
        viewModelScope.launch { datos.eliminarProductoAdmin(p) }
        _uiState.value = _uiState.value.copy(productoAEliminar = null)
    }

    fun restaurarDemo() {
        repository.restaurarCatalogoAdmin()
        viewModelScope.launch { datos.sembrarCatalogoDemo() }
    }

    fun cambiarEstadoPedido(numero: String, nuevoEstado: String) {
        val ped = pedidos.find { it.numero == numero } ?: return
        repository.updatePedido(ped.copy(estado = nuevoEstado))
        viewModelScope.launch { datos.cambiarEstadoPedido(numero, nuevoEstado) }
    }

    fun guardarPerfil(nombre: String, correo: String, telefono: String, ciudad: String, tienda: String) {
        _uiState.value = _uiState.value.copy(
            perfilNombre = nombre,
            perfilCorreo = correo,
            perfilTelefono = telefono,
            perfilCiudad = ciudad,
            perfilTienda = tienda
        )
        viewModelScope.launch {
            datos.actualizarUbicacion(_uiState.value.vendedorUid, ciudad, ciudad, 0.0, 0.0)
        }
    }
}
