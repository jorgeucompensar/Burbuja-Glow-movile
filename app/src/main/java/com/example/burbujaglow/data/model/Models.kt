package com.example.burbujaglow.data.model

/** Modelos de dominio compartidos por UI, repositorios y mappers de Firestore. */
data class ProductoAdmin(
    val id: Int, val nombre: String, val categoria: String, val precio: String, val stock: String, val estado: String,
    val imagenUrl: String = "", val vendedorId: String = "", val uid: String = "", val activo: Boolean = true
)

/** Producto completo del catálogo: `precioValor` se usa en cálculos y `precio` es la etiqueta visual. */
data class ProductoComprador(
    val id: Int, val nombre: String, val categoria: String, val precio: String,
    val descripcion: String, val precioValor: Int, val rating: String = "4.9",
    val stock: Int = 20, val sello: String = "MÁS VENDIDO",
    val precioAnterior: Int = 0, val reviews: Int = 0, val vendidos: Int = 0,
    val descripcionCorta: String = "", val ingredientes: List<String> = emptyList(),
    val creado: String = "2026-01-01", val minStock: Int = 8,
    val imagen: Int = 0,
    val imagenUrl: String = "", val vendedorId: String = "", val uid: String = "", val activo: Boolean = true
)

/** Ítem inmutable del carrito; se reemplaza con `copy()` para una recomposición fiable. */
data class ItemCarrito(val producto: ProductoComprador, val cantidad: Int)

/** Línea de un pedido enviada al backend y guardada dentro del documento `pedidos`. */
data class ItemPedido(
    val productoId: String,
    val cantidad: Int,
    val nombre: String = "",
    val precioValor: Int = 0
)

/** Pedido con estado de entrega y de pago; `vendedorIds` controla la visibilidad por rol. */
data class PedidoAdmin(
    val numero: String, val cliente: String, val correo: String, val fecha: String, val productos: String,
    val total: Int, val pago: String, val estado: String,
    val uid: String = "", val compradorUid: String = "", val vendedorIds: List<String> = emptyList(),
    val latitud: Double = 0.0, val longitud: Double = 0.0, val estadoPago: String = "PENDIENTE",
    val direccion: String = "", val items: List<ItemPedido> = emptyList()
)

/** Resumen de cliente mostrado en el panel administrativo. */
data class ClienteAdmin(val nombre: String, val correo: String, val telefono: String, val pedidos: Int, val total: Int, val ultimaCompra: String)

/** Perfil de usuario; `rol` es la fuente de autoridad de las reglas de Firestore. */
data class UsuarioAdmin(
    val id: Int, val nombre: String, val correo: String, val telefono: String, val rol: String, val estado: String,
    val uid: String = "", val usuarioAlias: String = "", val direccion: String = "",
    val ciudad: String = "Bogotá", val latitud: Double = 0.0, val longitud: Double = 0.0
)

/** Desglose del carrito; el descuento es informativo y el envío se gratis desde $150.000. */
data class TotalesCarrito(val subtotal: Int, val descuento: Int, val envio: Int, val total: Int, val totalSinEnvio: Int)

/** Calcula subtotal, ahorro frente al precio anterior, envío y total del carrito. */
fun totalesCarrito(carrito: List<ItemCarrito>): TotalesCarrito {
    // Regla de la tienda: envío $9.900, gratis desde $150.000.
    // Subtotal = suma de los precios actuales (igual que la suma de los ítems),
    // descuento = ahorro frente al precio anterior (solo informativo).
    val totalSinEnvio = carrito.sumOf { it.producto.precioValor * it.cantidad }
    val subtotal = totalSinEnvio
    val descuento = carrito.sumOf { (maxOf(0, it.producto.precioAnterior - it.producto.precioValor)) * it.cantidad }
    val envio = if (totalSinEnvio > 0 && totalSinEnvio < 150000) 9900 else 0
    return TotalesCarrito(subtotal, descuento, envio, totalSinEnvio + envio, totalSinEnvio)
}
