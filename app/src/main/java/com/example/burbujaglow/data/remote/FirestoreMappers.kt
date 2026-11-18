package com.example.burbujaglow.data.remote

import com.example.burbujaglow.data.model.PedidoAdmin
import com.example.burbujaglow.data.model.ProductoAdmin
import com.example.burbujaglow.data.model.ProductoComprador
import com.example.burbujaglow.data.model.UsuarioAdmin

private fun Any?.asText(): String = when (this) {
    is String -> this
    is Number -> toString()
    else -> ""
}

private fun Any?.asInt(): Int = when (this) {
    is Number -> toInt()
    is String -> filter { it.isDigit() || it == '-' }.toIntOrNull() ?: 0
    else -> 0
}

private fun Any?.asDouble(): Double = when (this) {
    is Number -> toDouble()
    is String -> toDoubleOrNull() ?: 0.0
    else -> 0.0
}

private fun Any?.asBool(): Boolean = this as? Boolean ?: false

private fun Any?.asStringList(): List<String> = when (this) {
    is List<*> -> map { it.asText() }
    else -> emptyList()
}

fun ProductoComprador.aMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "nombre" to nombre,
    "categoria" to categoria,
    "precio" to precio,
    "descripcion" to descripcion,
    "precioValor" to precioValor,
    "rating" to rating,
    "stock" to stock,
    "sello" to sello,
    "precioAnterior" to precioAnterior,
    "reviews" to reviews,
    "vendidos" to vendidos,
    "descripcionCorta" to descripcionCorta,
    "ingredientes" to ingredientes,
    "creado" to creado,
    "minStock" to minStock,
    "imagen" to imagen,
    "imagenUrl" to imagenUrl,
    "vendedorId" to vendedorId,
    "uid" to uid.ifBlank { "p_$id" },
    "activo" to activo
)

fun Map<String, Any?>.aProducto(): ProductoComprador = ProductoComprador(
    id = (this["id"]?.asInt() ?: 0),
    nombre = this["nombre"].asText(),
    categoria = this["categoria"].asText(),
    precio = this["precio"].asText(),
    descripcion = this["descripcion"].asText(),
    precioValor = this["precioValor"]?.asInt() ?: 0,
    rating = this["rating"].asText().ifBlank { "4.9" },
    stock = this["stock"]?.asInt() ?: 0,
    sello = this["sello"].asText(),
    precioAnterior = this["precioAnterior"]?.asInt() ?: 0,
    reviews = this["reviews"]?.asInt() ?: 0,
    vendidos = this["vendidos"]?.asInt() ?: 0,
    descripcionCorta = this["descripcionCorta"].asText(),
    ingredientes = this["ingredientes"].asStringList(),
    creado = this["creado"].asText().ifBlank { "2026-01-01" },
    minStock = this["minStock"]?.asInt() ?: 8,
    imagen = this["imagen"]?.asInt() ?: 0,
    imagenUrl = this["imagenUrl"].asText(),
    vendedorId = this["vendedorId"].asText(),
    uid = this["uid"].asText(),
    activo = this["activo"]?.asBool() ?: true
)

fun ProductoAdmin.aMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "nombre" to nombre,
    "categoria" to categoria,
    "precio" to precio,
    "stock" to stock,
    "estado" to estado,
    "imagenUrl" to imagenUrl,
    "vendedorId" to vendedorId,
    "uid" to uid.ifBlank { "p_$id" },
    "activo" to activo
)

fun Map<String, Any?>.aProductoAdmin(): ProductoAdmin = ProductoAdmin(
    id = this["id"]?.asInt() ?: 0,
    nombre = this["nombre"].asText(),
    categoria = this["categoria"].asText(),
    precio = this["precio"].asText(),
    stock = this["stock"].asText(),
    estado = this["estado"].asText(),
    imagenUrl = this["imagenUrl"].asText(),
    vendedorId = this["vendedorId"].asText(),
    uid = this["uid"].asText(),
    activo = this["activo"]?.asBool() ?: true
)

fun UsuarioAdmin.aMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "nombre" to nombre,
    "correo" to correo,
    "telefono" to telefono,
    "rol" to rol,
    "estado" to estado,
    "uid" to uid,
    "usuarioAlias" to usuarioAlias.ifBlank { correo.substringBefore("@") },
    "direccion" to direccion,
    "ciudad" to ciudad,
    "latitud" to latitud,
    "longitud" to longitud
)

fun Map<String, Any?>.aUsuarioAdmin(): UsuarioAdmin = UsuarioAdmin(
    id = this["id"]?.asInt() ?: 0,
    nombre = this["nombre"].asText(),
    correo = this["correo"].asText(),
    telefono = this["telefono"].asText(),
    rol = this["rol"].asText().ifBlank { "COMPRADOR" },
    estado = this["estado"].asText().ifBlank { "Activo" },
    uid = this["uid"].asText(),
    usuarioAlias = this["usuarioAlias"].asText(),
    direccion = this["direccion"].asText(),
    ciudad = this["ciudad"].asText().ifBlank { "Bogotá" },
    latitud = this["latitud"]?.asDouble() ?: 0.0,
    longitud = this["longitud"]?.asDouble() ?: 0.0
)

fun PedidoAdmin.aMap(): Map<String, Any?> = mapOf(
    "numero" to numero,
    "cliente" to cliente,
    "correo" to correo,
    "fecha" to fecha,
    "productos" to productos,
    "total" to total,
    "pago" to pago,
    "estado" to estado,
    "uid" to uid.ifBlank { numero },
    "compradorUid" to compradorUid,
    "vendedorIds" to vendedorIds,
    "latitud" to latitud,
    "longitud" to longitud,
    "estadoPago" to estadoPago,
    "direccion" to direccion
)

fun Map<String, Any?>.aPedidoAdmin(): PedidoAdmin = PedidoAdmin(
    numero = this["numero"].asText(),
    cliente = this["cliente"].asText(),
    correo = this["correo"].asText(),
    fecha = this["fecha"].asText(),
    productos = this["productos"].asText(),
    total = this["total"]?.asInt() ?: 0,
    pago = this["pago"].asText(),
    estado = this["estado"].asText().ifBlank { "Pendiente" },
    uid = this["uid"].asText(),
    compradorUid = this["compradorUid"].asText(),
    vendedorIds = this["vendedorIds"].asStringList(),
    latitud = this["latitud"]?.asDouble() ?: 0.0,
    longitud = this["longitud"]?.asDouble() ?: 0.0,
    estadoPago = this["estadoPago"].asText().ifBlank { "PENDIENTE" },
    direccion = this["direccion"].asText()
)
