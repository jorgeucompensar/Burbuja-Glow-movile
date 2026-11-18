package com.example.burbujaglow.feature.vendedor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.burbujaglow.core.components.*

// Historial de pedidos recibidos por el vendedor, con cambio de estado.
@Composable
fun VendedorPedidosScreen(viewModel: VendedorViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pedidos = viewModel.pedidos
    val pendientes = pedidos.count { it.estado == "Pendiente" }
    val entregados = pedidos.count { it.estado == "Entregado" }
    val ingresoTotal = pedidos.filter { it.estado != "Cancelado" }.sumOf { it.total }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(6.dp))
        BoutiqueCard(elevation = 2) {
            Text("Resumen de pedidos", fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Charcoal)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResumenPedido("Pendientes", "$pendientes", Rose, Modifier.weight(1f))
                ResumenPedido("Entregados", "$entregados", Success, Modifier.weight(1f))
                ResumenPedido("Ingresos", fmtCOP(ingresoTotal), Gold, Modifier.weight(1.3f))
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Todos") + viewModel.estadosPedido) { e ->
                Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (state.filtroPedido == e) GoldLight else Color.White).clickable { viewModel.onFiltroPedidoChange(e) }.padding(horizontal = 12.dp, vertical = 7.dp)) {
                    Text(e, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (state.filtroPedido == e) Gold else InkSoft)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        val filtrados = pedidos.filter { state.filtroPedido == "Todos" || it.estado == state.filtroPedido }
        if (filtrados.isEmpty()) {
            BoutiqueCard(elevation = 2) {
                Text("No hay pedidos con este estado.", fontSize = 12.sp, color = Color.Gray)
            }
        }
        filtrados.forEach { ped ->
            BoutiqueCard(elevation = 2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(ped.numero, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                        Text("${ped.cliente} · ${ped.fecha}", fontSize = 10.sp, color = Color.Gray)
                        Text(ped.productos, fontSize = 10.sp, color = InkSoft)
                        Text("${ped.pago} · ${fmtCOP(ped.total)}", fontSize = 10.sp, color = Gold, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (ped.estado == "Cancelado") RoseLight else GoldLight).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(ped.estado, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (ped.estado == "Cancelado") Danger else Gold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = LineSoft)
                Spacer(Modifier.height(8.dp))
                Text("Actualizar estado:", fontSize = 10.sp, color = Color.Gray)
                Spacer(Modifier.height(2.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(viewModel.estadosPedido) { e ->
                        Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (ped.estado == e) Charcoal else Color.White).clickable {
                            viewModel.cambiarEstadoPedido(ped.numero, e)
                            Toast.makeText(context, "Pedido ${ped.numero}: $e", Toast.LENGTH_SHORT).show()
                        }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(e, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (ped.estado == e) Color.White else InkSoft)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ResumenPedido(titulo: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(titulo, fontSize = 9.sp, color = Color.Gray)
        Spacer(Modifier.height(2.dp))
        Text(valor, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
