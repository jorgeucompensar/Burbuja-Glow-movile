package com.example.burbujaglow.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.burbujaglow.core.components.*
import com.example.burbujaglow.data.CATEGORIAS

// Dashboard principal: estadísticas, gráfico de barras y ventas por categoría
@Composable
fun DashboardScreen(viewModel: AdminViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pedidos = viewModel.pedidos
    val productos = viewModel.productos
    val pedidosActivos = pedidos.filter { it.estado != "Cancelado" }
    val ventasMes = pedidosActivos.sumOf { it.total }
    val pendientes = pedidos.count { it.estado == "Pendiente" }
    val stockBajo = productos.count { (it.stock.toIntOrNull() ?: 0) <= 8 }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Ventas hoy", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text(fmtCOP(pedidosActivos.firstOrNull()?.total ?: 0), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Gold) }
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Ventas del mes", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text(fmtCOP(ventasMes), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Gold) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Pedidos", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text("${pedidos.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal); Text("Pendientes: $pendientes", fontSize = 9.sp, color = Rose) }
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Productos", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text("${productos.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Usuarios", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text("${viewModel.usuarios.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal) }
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Stock bajo", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text("$stockBajo productos", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Rose) }
        }
        Spacer(Modifier.height(14.dp))
        Text("Ventas últimos 7 días", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
        Spacer(Modifier.height(8.dp))
        val ventas7 = listOf(159700, 219700, 249700, 129900, 149900, 119700, 144700)
        val maxV = (ventas7.maxOrNull() ?: 1).toFloat()
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            ventas7.forEachIndexed { i, v ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.width(26.dp).height((v / maxV * 90).dp).clip(RoundedCornerShape(8.dp)).background(Gold))
                    Spacer(Modifier.height(4.dp))
                    Text(listOf("L", "M", "X", "J", "V", "S", "D")[i], fontSize = 9.sp, color = Color.Gray)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Ventas por categoría", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
        Spacer(Modifier.height(8.dp))
        val coloresDonut = listOf(Color(0xFFB8923F), Color(0xFFE9B7C4), Color(0xFFE4CD9C), Color(0xFFD8BEA6), Color(0xFFC9B39C), Color(0xFFB9CFC4), Color(0xFFD4868F), Color(0xFFB4ACA6), Color(0xFFDEA6B6))
        CATEGORIAS.forEachIndexed { i, cat ->
            val n = productos.count { it.categoria == cat }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(coloresDonut[i % coloresDonut.size]))
                Spacer(Modifier.width(8.dp))
                Text(cat, fontSize = 11.sp, color = InkSoft, modifier = Modifier.weight(1f))
                Text("$n productos", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
