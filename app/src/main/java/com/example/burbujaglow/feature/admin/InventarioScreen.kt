package com.example.burbujaglow.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.burbujaglow.core.components.*

// Inventario: alerta de stock bajo y medidor por producto
@Composable
fun InventarioScreen(viewModel: AdminViewModel) {
    val productos = viewModel.productos
    val stockBajo = productos.count { (it.stock.toIntOrNull() ?: 0) <= 8 }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        if (stockBajo > 0) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFFFF8EC)).padding(12.dp)) {
                Text("$stockBajo productos tienen inventario bajo. Revisa: ${productos.filter { (it.stock.toIntOrNull() ?: 0) <= 8 }.take(3).joinToString(", ") { it.nombre }}", fontSize = 11.sp, color = Color(0xFF8A6A24))
            }
            Spacer(Modifier.height(12.dp))
        }
        productos.forEach { item ->
            val stock = item.stock.toIntOrNull() ?: 0
            val pct = minOf(100f, stock / 24f * 100)
            BoutiqueCard(elevation = 2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.nombre, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Charcoal)
                    Text("Stock: $stock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (stock <= 8) Danger else Success)
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50.dp)).background(LineSoft)) {
                    Box(Modifier.fillMaxWidth(pct / 100f).height(7.dp).clip(RoundedCornerShape(50.dp)).background(if (stock <= 8) Danger else if (stock <= 20) Gold else Success))
                }
                Spacer(Modifier.height(4.dp))
                Text(if (stock <= 0) "Agotado" else if (stock <= 8) "Nivel bajo" else "Nivel OK", fontSize = 9.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
