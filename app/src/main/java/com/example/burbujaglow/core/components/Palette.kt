package com.example.burbujaglow.core.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import java.text.NumberFormat
import java.util.Locale

// Identidad visual VELVETTE/Burbuja Glow (style.css :root)
val Ivory = Color(0xFFFFFCFA)
val IvoryDark = Color(0xFFF4E9E1)
val Gold = Color(0xFFB8923F)
val Champagne = Color(0xFFE4CD9C)
val GoldLight = Color(0xFFFAE7EC)
val Nude = Color(0xFFF4E9E1)
val Rose = Color(0xFFD4868F)
val RoseLight = Color(0xFFFAE7EC)
val Danger = Color(0xFFC2453F)
val Success = Color(0xFF3F8A63)
val Charcoal = Color(0xFF12100E)
val InkSoft = Color(0xFF6B6360)
val LineSoft = Color(0xFFE9E0D8)
val Serif = FontFamily.Serif

// NumberFormat no es seguro entre hilos: se reutiliza uno por hilo
private val formatoCOP = ThreadLocal.withInitial { NumberFormat.getInstance(Locale("es", "CO")) }

fun fmtCOP(valor: Int): String = "$" + formatoCOP.get().format(valor) + " COP"
