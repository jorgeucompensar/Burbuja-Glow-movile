package com.example.burbujaglow.data

import androidx.compose.runtime.mutableStateListOf
import com.example.burbujaglow.data.model.ClienteAdmin
import com.example.burbujaglow.data.model.ItemCarrito
import com.example.burbujaglow.data.model.PedidoAdmin
import com.example.burbujaglow.data.model.ProductoAdmin
import com.example.burbujaglow.data.model.ProductoComprador
import com.example.burbujaglow.data.model.UsuarioAdmin

// Repositorio en memoria (antes: listas dispersas en MainActivity.kt).
// Punto único de datos para ViewModels; persistencia futura: DataStore.
// Las listas se exponen como List inmutables; toda mutación pasa por métodos.
class BurbujaRepository {
    private val _productos = mutableStateListOf<ProductoComprador>().apply { addAll(catalogoBurbujaGlow()) }
    val productos: List<ProductoComprador> get() = _productos

    private val _carrito = mutableStateListOf<ItemCarrito>()
    val carrito: List<ItemCarrito> get() = _carrito

    private val _productosAdmin = mutableStateListOf<ProductoAdmin>().apply {
        catalogoBurbujaGlow().forEach { p ->
            add(ProductoAdmin(p.id, p.nombre, p.categoria, p.precio, p.stock.toString(), if (p.sello.isBlank()) "ACTIVO" else p.sello))
        }
    }
    val productosAdmin: List<ProductoAdmin> get() = _productosAdmin

    private val _pedidos = mutableStateListOf(
        PedidoAdmin("MAK-10460", "Laura Gómez", "laura.gomez@correo.com", "Hace 0 días", "Tinta para Labios, Paleta Nude", 159700, "Nequi", "Entregado"),
        PedidoAdmin("MAK-10461", "Laura Gómez", "laura.gomez@correo.com", "Hace 1 días", "Polvos Nailen, Iluminador Glow", 219700, "Daviplata", "Entregado"),
        PedidoAdmin("MAK-10462", "Andrea Ruiz", "andrea.ruiz@correo.com", "Hace 2 días", "Paleta Nude Velvet, Gloss Llavero", 249700, "Transferencia bancaria", "Enviado"),
        PedidoAdmin("MAK-10463", "Andrea Ruiz", "andrea.ruiz@correo.com", "Hace 3 días", "Jabón Líquido Dolce Bella", 129900, "Contraentrega", "Preparando"),
        PedidoAdmin("MAK-10464", "Sofía Martínez", "sofia.m@correo.com", "Hace 4 días", "Libro Engol", 149900, "Nequi", "Confirmado"),
        PedidoAdmin("MAK-10465", "Camila Herrera", "camila.h@correo.com", "Hace 5 días", "Fresitas Bálsamos, Tinta Labios", 119700, "Daviplata", "Pendiente"),
        PedidoAdmin("MAK-10466", "Camila Herrera", "camila.h@correo.com", "Hace 6 días", "Pestañas Engol, Encrespador", 144700, "Transferencia bancaria", "Entregado"),
        PedidoAdmin("MAK-10467", "Valentina León", "vale.leon@correo.com", "Hace 7 días", "Iluminador Burbuja Glow", 99900, "Contraentrega", "Cancelado")
    )
    val pedidos: List<PedidoAdmin> get() = _pedidos

    val clientes = listOf(
        ClienteAdmin("Laura Gómez", "laura.gomez@correo.com", "300 456 7890", 3, 389700, "Hace 2 días"),
        ClienteAdmin("Andrea Ruiz", "andrea.ruiz@correo.com", "310 223 5566", 2, 264800, "Hace 5 días"),
        ClienteAdmin("Sofía Martínez", "sofia.m@correo.com", "315 998 1122", 1, 149900, "Hace 7 días"),
        ClienteAdmin("Camila Herrera", "camila.h@correo.com", "320 774 3399", 4, 612500, "Hace 1 día"),
        ClienteAdmin("Valentina León", "vale.leon@correo.com", "301 665 8844", 1, 99900, "Hace 12 días")
    )
    val estadosPedido = listOf("Pendiente", "Confirmado", "Preparando", "Enviado", "Entregado", "Cancelado")

    private val _usuarios = mutableStateListOf(
        UsuarioAdmin(1, "Administrador", "admin@burbujaglow.com", "+57 300 123 4567", "Administrador", "Activo"),
        UsuarioAdmin(2, "Laura Gómez", "laura.gomez@correo.com", "300 456 7890", "Comprador", "Activo"),
        UsuarioAdmin(3, "Andrea Ruiz", "andrea.ruiz@correo.com", "310 223 5566", "Comprador", "Activo"),
        UsuarioAdmin(4, "Sofía Martínez", "sofia.m@correo.com", "315 998 1122", "Comprador", "Activo"),
        UsuarioAdmin(5, "Camila Herrera", "camila.h@correo.com", "320 774 3399", "Comprador", "Inactivo"),
        UsuarioAdmin(6, "Valentina León", "vale.leon@correo.com", "301 665 8844", "Comprador", "Activo")
    )
    val usuarios: List<UsuarioAdmin> get() = _usuarios

    fun addToCart(p: ProductoComprador, qty: Int = 1): String? {
        if (p.stock <= 0) return "Producto agotado"
        val idx = _carrito.indexOfFirst { it.producto.id == p.id }
        val nuevaCant = (_carrito.getOrNull(idx)?.cantidad ?: 0) + qty
        if (nuevaCant > p.stock) return "Solo hay ${p.stock} unidades"
        if (idx >= 0) _carrito[idx] = _carrito[idx].copy(cantidad = nuevaCant)
        else _carrito.add(ItemCarrito(p, qty))
        return null
    }

    fun setCantidad(productoId: Int, nueva: Int) {
        val idx = _carrito.indexOfFirst { it.producto.id == productoId }
        if (idx < 0) return
        val item = _carrito[idx]
        if (nueva <= 0) _carrito.removeAt(idx)
        else if (nueva <= item.producto.stock) _carrito[idx] = item.copy(cantidad = nueva)
    }

    fun removeItem(productoId: Int) { _carrito.removeAll { it.producto.id == productoId } }
    fun vaciarCarrito() { _carrito.clear() }

    fun descontarStock() {
        _carrito.forEach { item ->
            val idx = _productos.indexOfFirst { it.id == item.producto.id }
            if (idx >= 0) _productos[idx] = _productos[idx].copy(stock = (_productos[idx].stock - item.cantidad).coerceAtLeast(0), vendidos = _productos[idx].vendidos + item.cantidad)
        }
    }

    fun restaurarCatalogoAdmin() {
        _productosAdmin.clear()
        catalogoBurbujaGlow().forEach { p -> _productosAdmin.add(ProductoAdmin(p.id, p.nombre, p.categoria, p.precio, p.stock.toString(), if (p.sello.isBlank()) "ACTIVO" else p.sello)) }
    }

    fun updateProductoAdmin(p: ProductoAdmin) {
        val idx = _productosAdmin.indexOfFirst { it.id == p.id }
        if (idx >= 0) _productosAdmin[idx] = p
    }

    fun addProductoAdmin(p: ProductoAdmin) { _productosAdmin.add(p) }
    fun removeProductoAdmin(p: ProductoAdmin) { _productosAdmin.remove(p) }

    fun updatePedido(p: PedidoAdmin) {
        val idx = _pedidos.indexOfFirst { it.numero == p.numero }
        if (idx >= 0) _pedidos[idx] = p
    }

    fun updateUsuario(u: UsuarioAdmin) {
        val idx = _usuarios.indexOfFirst { it.id == u.id }
        if (idx >= 0) _usuarios[idx] = u
    }

    fun addUsuario(u: UsuarioAdmin) { _usuarios.add(u) }
    fun removeUsuario(u: UsuarioAdmin) { _usuarios.remove(u) }
}

// Instancia única compartida por ShopViewModel y AdminViewModel
val burbujaRepository = BurbujaRepository()
