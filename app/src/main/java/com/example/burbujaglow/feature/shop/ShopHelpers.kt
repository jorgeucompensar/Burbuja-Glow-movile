package com.example.burbujaglow.feature.shop

import androidx.compose.ui.graphics.Color
import com.example.burbujaglow.core.components.Danger
import com.example.burbujaglow.core.components.Gold
import com.example.burbujaglow.core.components.Success
import com.example.burbujaglow.data.model.ProductoComprador

internal fun discountPct(p: ProductoComprador): Int =
    if (p.precioAnterior > p.precioValor && p.precioAnterior > 0) Math.round((1 - p.precioValor.toFloat() / p.precioAnterior) * 100).toInt() else 0

internal fun stockInfo(p: ProductoComprador): Pair<String, Color> = when {
    p.stock <= 0 -> "Agotado" to Danger
    p.stock <= p.minStock -> "¡Quedan ${p.stock} unidades!" to Gold
    else -> "Disponible (${p.stock} u.)" to Success
}
