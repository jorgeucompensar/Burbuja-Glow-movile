package com.example.burbujaglow.core.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import java.text.NumberFormat
import java.util.Locale

// Identidad visual Burbuja Glow — boutique de cosmética y cuidado capilar
// Fondo marfil #F9F9F8, tarjetas blancas, dorado #8C7355, rosa #B25353 y negro #1A1A1A
val Ivory = Color(0xFFF9F9F8)
val IvoryDark = Color(0xFFF1EDE6)
val Gold = Color(0xFF8C7355)
val Champagne = Color(0xFFE4CD9C)
val GoldLight = Color(0xFFEDE4D8)
val Nude = Color(0xFFF4E9E1)
val Rose = Color(0xFFB25353)
val RoseLight = Color(0xFFF6E6E4)
val Danger = Color(0xFFB25353)
val Success = Color(0xFF3F8A63)
val Charcoal = Color(0xFF1A1A1A)
val InkSoft = Color(0xFF6B6360)
val LineSoft = Color(0xFFE6E0D8)
val Serif = FontFamily.Serif

// NumberFormat no es seguro entre hilos: se reutiliza uno por hilo
private val formatoCOP = ThreadLocal.withInitial { NumberFormat.getInstance(Locale("es", "CO")) }

fun fmtCOP(valor: Int): String = "$" + formatoCOP.get().format(valor) + " COP"
