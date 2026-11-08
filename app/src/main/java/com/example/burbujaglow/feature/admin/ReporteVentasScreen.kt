package com.example.burbujaglow.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

// Reporte de ventas: totales, ticket promedio, por método de pago y detalle
@Composable
fun ReporteVentasScreen(viewModel: AdminViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pedidos = viewModel.pedidos
    val pedidosReporte = pedidos.filter { p ->
        when (state.periodoReporte) {
            "Hoy" -> p.fecha.contains("0 días")
            "7 días" -> { val d = p.fecha.filter { it.isDigit() }.toIntOrNull() ?: 99; d <= 7 }
            else -> true
        }
    }
    val activosReporte = pedidosReporte.filter { it.estado != "Cancelado" }
    val totalReporte = activosReporte.sumOf { it.total }
    val ticketPromedio = if (activosReporte.isNotEmpty()) totalReporte / activosReporte.size else 0
    val canceladosReporte = pedidosReporte.count { it.estado == "Cancelado" }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Text("Reporte de ventas", fontFamily = Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Charcoal)
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Todos", "Hoy", "7 días")) { per ->
                Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (state.periodoReporte == per) GoldLight else Color.White).clickable { viewModel.onPeriodoReporteChange(per) }.padding(horizontal = 12.dp, vertical = 7.dp)) {
                    Text(per, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (state.periodoReporte == per) Gold else InkSoft)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Total ventas", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text(fmtCOP(totalReporte), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Gold) }
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Ticket promedio", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text(fmtCOP(ticketPromedio), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Pedidos", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text("${pedidosReporte.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal) }
            BoutiqueCard(modifier = Modifier.weight(1f), elevation = 3) { Text("Cancelados", fontSize = 10.sp, color = Color.Gray); Spacer(Modifier.height(4.dp)); Text("$canceladosReporte", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Danger) }
        }
        Spacer(Modifier.height(14.dp))
        Text("Ventas por método de pago", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
        Spacer(Modifier.height(8.dp))
        listOf("Nequi", "Daviplata", "Transferencia bancaria", "Contraentrega").forEach { metodo ->
            val suma = activosReporte.filter { it.pago == metodo }.sumOf { it.total }
            val cantidad = activosReporte.count { it.pago == metodo }
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$metodo ($cantidad)", fontSize = 12.sp, color = InkSoft)
                Text(fmtCOP(suma), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Charcoal)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Detalle de ventas (${pedidosReporte.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
        Spacer(Modifier.height(8.dp))
        pedidosReporte.forEach { ped ->
            BoutiqueCard(elevation = 2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(ped.numero, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Charcoal)
                        Text("${ped.cliente} · ${ped.fecha} · ${ped.pago}", fontSize = 10.sp, color = Color.Gray)
                        Text(ped.productos, fontSize = 10.sp, color = InkSoft)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(fmtCOP(ped.total), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Gold)
                        Text(ped.estado, fontSize = 9.sp, color = if (ped.estado == "Cancelado") Danger else Success)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
