package com.example.burbujaglow.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.burbujaglow.data.model.ClienteAdmin
import com.example.burbujaglow.data.model.ItemCarrito
import com.example.burbujaglow.data.model.PedidoAdmin
import com.example.burbujaglow.data.model.ProductoAdmin
import com.example.burbujaglow.data.model.ProductoComprador
import com.example.burbujaglow.data.model.UsuarioAdmin

/** Reemplaza una lista observable solo cuando cambia, evitando recomposiciones innecesarias. */
private fun <T> SnapshotStateList<T>.replaceIfChanged(nuevos: List<T>) {
    if (size == nuevos.size && (0 until size).all { this[it] == nuevos[it] }) return
    clear()
    addAll(nuevos)
}

/** Restaura el drawable local cuando el producto remoto no trae URL de imagen. */
private fun productoConImagenLocal(remoto: ProductoComprador): ProductoComprador {
    if (remoto.imagenUrl.isNotBlank() || remoto.imagen != 0) return remoto
    return IMAGENES_LOCALES[remoto.id]?.let { remoto.copy(imagen = it) } ?: remoto
}

/** Convierte un producto del catálogo comprador a la vista usada por el panel administrativo. */
private fun aAdministracion(p: ProductoComprador) = ProductoAdmin(
    p.id, p.nombre, p.categoria, p.precio, p.stock.toString(),
    if (p.sello.isBlank()) "ACTIVO" else p.sello,
    p.imagenUrl, p.vendedorId, p.uid, p.activo
)

/** Recupera los datos extensos del catálogo base y aplica los cambios editados por el admin. */
internal fun productoDesdeAdmin(p: ProductoAdmin): ProductoComprador {
    val base = PRODUCTOS_BASE_POR_ID[p.id]
    val precioValor = p.precio.filter { it.isDigit() }.toIntOrNull() ?: 0
    return (base ?: ProductoComprador(p.id, p.nombre, p.categoria, p.precio, "", precioValor)).copy(
        id = p.id,
        nombre = p.nombre,
        categoria = p.categoria,
        precio = p.precio,
        precioValor = precioValor,
        stock = p.stock.toIntOrNull() ?: 0,
        sello = if (p.estado == "ACTIVO") "" else p.estado,
        imagenUrl = p.imagenUrl,
        vendedorId = p.vendedorId,
        uid = p.uid.ifBlank { "p_${p.id}" },
        activo = p.activo
    )
}

/**
 * Repositorio en memoria y punto único de datos para los ViewModels.
 * Las listas se exponen como `List` inmutables; toda mutación pasa por sus métodos.
 */
class BurbujaRepository {
    private val _productos = mutableStateListOf<ProductoComprador>().apply { addAll(catalogoBurbujaGlow()) }
    val productos: List<ProductoComprador> get() = _productos

    private val _carrito = mutableStateListOf<ItemCarrito>()
    val carrito: List<ItemCarrito> get() = _carrito

    private val _productosAdmin = mutableStateListOf<ProductoAdmin>().apply {
        catalogoBurbujaGlow().forEach { add(aAdministracion(it)) }
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
        UsuarioAdmin(1, "Administrador", "admin@burbujaglow.test", "+57 300 123 4567", "ADMIN", "Activo"),
        UsuarioAdmin(2, "Laura Gómez", "laura.gomez@correo.com", "300 456 7890", "COMPRADOR", "Activo"),
        UsuarioAdmin(3, "Andrea Ruiz", "andrea.ruiz@correo.com", "310 223 5566", "COMPRADOR", "Activo"),
        UsuarioAdmin(4, "Sofía Martínez", "sofia.m@correo.com", "315 998 1122", "COMPRADOR", "Activo"),
        UsuarioAdmin(5, "Camila Herrera", "camila.h@correo.com", "320 774 3399", "COMPRADOR", "Inactivo"),
        UsuarioAdmin(6, "Valentina León", "vale.leon@correo.com", "301 665 8844", "COMPRADOR", "Activo")
    )
    val usuarios: List<UsuarioAdmin> get() = _usuarios

    /** Agrega o incrementa un ítem validando stock; devuelve un mensaje de error o `null` si funciona. */
    fun addToCart(p: ProductoComprador, qty: Int = 1): String? {
        if (p.stock <= 0) return "Producto agotado"
        val idx = _carrito.indexOfFirst { it.producto.id == p.id }
        val nuevaCant = (_carrito.getOrNull(idx)?.cantidad ?: 0) + qty
        if (nuevaCant > p.stock) return "Solo hay ${p.stock} unidades"
        if (idx >= 0) _carrito[idx] = _carrito[idx].copy(cantidad = nuevaCant)
        else _carrito.add(ItemCarrito(p, qty))
        return null
    }

    /** Ajusta la cantidad de un ítem; cero o menos lo elimina y nunca supera el stock disponible. */
    fun setCantidad(productoId: Int, nueva: Int) {
        val idx = _carrito.indexOfFirst { it.producto.id == productoId }
        if (idx < 0) return
        val item = _carrito[idx]
        if (nueva <= 0) _carrito.removeAt(idx)
        else if (nueva <= item.producto.stock) _carrito[idx] = item.copy(cantidad = nueva)
    }

    /** Quita un producto del carrito local. */
    fun removeItem(productoId: Int) { _carrito.removeAll { it.producto.id == productoId } }

    /** Vacía el carrito local tras confirmar una compra. */
    fun vaciarCarrito() { _carrito.clear() }

    /** Descuenta del catálogo local las unidades compradas y suma `vendidos`. */
    fun descontarStock() {
        _carrito.forEach { item ->
            val idx = _productos.indexOfFirst { it.id == item.producto.id }
            if (idx >= 0) _productos[idx] = _productos[idx].copy(stock = (_productos[idx].stock - item.cantidad).coerceAtLeast(0), vendidos = _productos[idx].vendidos + item.cantidad)
        }
    }

    /** Restaura el catálogo local desde los datos demo base. */
    fun restaurarCatalogoAdmin() {
        sincronizarCatalogo(catalogoBurbujaGlow())
    }

    /** Reemplaza catálogo comprador y admin con datos remotos, conservando imágenes locales de respaldo. */
    fun sincronizarCatalogo(nuevos: List<ProductoComprador>) {
        val conRespaldoLocal = nuevos.map(::productoConImagenLocal)
        _productos.replaceIfChanged(conRespaldoLocal)
        _productosAdmin.replaceIfChanged(conRespaldoLocal.map(::aAdministracion))
    }

    /** Actualiza la lista de pedidos visible en el panel administrativo. */
    fun reemplazarPedidos(nuevos: List<PedidoAdmin>) = _pedidos.replaceIfChanged(nuevos)

    /** Actualiza la lista de usuarios visible en el panel administrativo. */
    fun reemplazarUsuarios(nuevos: List<UsuarioAdmin>) = _usuarios.replaceIfChanged(nuevos)

    /** Actualiza el carrito local con los datos recibidos desde Firestore. */
    fun reemplazarCarrito(nuevos: List<ItemCarrito>) = _carrito.replaceIfChanged(nuevos)

    /** Inserta o actualiza un producto en ambas vistas (comprador y administrador). */
    fun upsertProducto(p: ProductoComprador) {
        val idx = _productos.indexOfFirst { it.id == p.id || it.uid == p.uid }
        if (idx >= 0) _productos[idx] = p else _productos.add(p)
        val admin = aAdministracion(p)
        val idxAdmin = _productosAdmin.indexOfFirst { it.id == p.id || it.uid == p.uid }
        if (idxAdmin >= 0) _productosAdmin[idxAdmin] = admin else _productosAdmin.add(admin)
    }

    /** Elimina un producto de ambas vistas por `id` o `uid`. */
    fun eliminarProducto(p: ProductoComprador) {
        _productos.removeAll { it.id == p.id || it.uid == p.uid }
        _productosAdmin.removeAll { it.id == p.id || it.uid == p.uid }
    }

    /** Aplica un cambio del panel admin y refleja el resultado en la vista comprador. */
    fun updateProductoAdmin(p: ProductoAdmin) {
        val idx = _productosAdmin.indexOfFirst { it.id == p.id }
        if (idx >= 0) _productosAdmin[idx] = p
        upsertProducto(productoDesdeAdmin(p))
    }

    /** Elimina un producto desde el panel administrativo. */
    fun removeProductoAdmin(p: ProductoAdmin) { eliminarProducto(productoDesdeAdmin(p)) }

    /** Actualiza un pedido existente por su número. */
    fun updatePedido(p: PedidoAdmin) {
        val idx = _pedidos.indexOfFirst { it.numero == p.numero }
        if (idx >= 0) _pedidos[idx] = p
    }

    /** Actualiza un usuario existente por su identificador local. */
    fun updateUsuario(u: UsuarioAdmin) {
        val idx = _usuarios.indexOfFirst { it.id == u.id }
        if (idx >= 0) _usuarios[idx] = u
    }

}

/** Instancia única compartida por los ViewModels de comprador, administrador y vendedor. */
val burbujaRepository = BurbujaRepository()
