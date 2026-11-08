package com.example.burbujaglow.feature.admin

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.burbujaglow.core.components.*

// Pedidos: filtro por estado y cambio de estado inline
@Composable
fun PedidosScreen(viewModel: AdminViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pedidos = viewModel.pedidos
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Todos") + viewModel.estadosPedido) { e ->
                Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (state.filtroPedido == e) GoldLight else Color.White).clickable { viewModel.onFiltroPedidoChange(e) }.padding(horizontal = 12.dp, vertical = 7.dp)) {
                    Text(e, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (state.filtroPedido == e) Gold else InkSoft)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        pedidos.filter { state.filtroPedido == "Todos" || it.estado == state.filtroPedido }.forEach { ped ->
            BoutiqueCard(elevation = 2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(ped.numero, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                        Text("${ped.cliente} · ${ped.fecha}", fontSize = 10.sp, color = Color.Gray)
                        Text(ped.productos, fontSize = 10.sp, color = InkSoft)
                        Text("${ped.pago} · ${fmtCOP(ped.total)}", fontSize = 10.sp, color = Gold, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (ped.estado == "Cancelado") Color(0xFFF9ECEC) else GoldLight).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(ped.estado, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (ped.estado == "Cancelado") Danger else Gold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Cambiar estado:", fontSize = 10.sp, color = Color.Gray)
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
