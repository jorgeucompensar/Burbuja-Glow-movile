package com.example.burbujaglow.feature.shop

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.burbujaglow.data.model.totalesCarrito

// Carrito interactivo: +/− con tope de stock, eliminar, vaciar y totales COP
@Composable
fun CarritoScreen(viewModel: ShopViewModel, nombreComprador: String, onRegister: () -> Unit) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val carrito = state.carrito
    val totales = totalesCarrito(carrito)
    val totalItems = carrito.sumOf { it.cantidad }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (carrito.isEmpty()) {
            Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(Icons.Filled.ShoppingBag, contentDescription = "Carrito vacío", tint = Gold, modifier = Modifier.size(28.dp)) }
                    Spacer(Modifier.height(12.dp)); Text("Tu carrito está vacío", fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                    Spacer(Modifier.height(4.dp)); Text("Descubre belleza en el catálogo.", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(16.dp)); PrimaryButton("IR AL CATÁLOGO", { viewModel.navegar(CompradorRoute.Catalogo) }, Modifier.width(200.dp))
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${carrito.size} producto(s) · $totalItems ítem(s)", fontSize = 12.sp, color = Color.Gray)
                TextButton(onClick = { viewModel.onConfirmarVaciarChange(true) }) { Icon(Icons.Filled.Delete, contentDescription = "Vaciar carrito", tint = Rose, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("Vaciar carrito", color = Rose, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(carrito, key = { it.producto.id }) { item ->
                    BoutiqueCard(elevation = 2) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProductImage(item.producto, Modifier.size(52.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.producto.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                                Spacer(Modifier.height(4.dp))
                                Text(item.producto.precio, color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Subtotal: ${fmtCOP(item.producto.precioValor * item.cantidad)}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.setCantidad(item, item.cantidad - 1) }) { Icon(Icons.Filled.Remove, contentDescription = "Restar", tint = Charcoal, modifier = Modifier.size(16.dp)) }
                                    Text("${item.cantidad}", fontWeight = FontWeight.Bold, color = Charcoal)
                                    IconButton(onClick = {
                                        if (item.cantidad < item.producto.stock) viewModel.setCantidad(item, item.cantidad + 1)
                                        else Toast.makeText(context, "Solo hay ${item.producto.stock} unidades", Toast.LENGTH_SHORT).show()
                                    }) { Icon(Icons.Filled.Add, contentDescription = "Sumar", tint = Charcoal, modifier = Modifier.size(16.dp)) }
                                }
                                TextButton(onClick = { viewModel.removeItem(item); Toast.makeText(context, "${item.producto.nombre} eliminado del carrito", Toast.LENGTH_SHORT).show() }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar ítem", tint = Rose, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("Eliminar", color = Rose, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (nombreComprador.isBlank()) {
                BoutiqueCard(elevation = 2) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Registrarse", tint = Gold, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Regístrate antes de comprar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                            Text("Crea tu cuenta con nombre, apellido y correo.", fontSize = 10.sp, color = Color.Gray)
                        }
                        Box(Modifier.clip(RoundedCornerShape(50.dp)).background(Charcoal).clickable(onClick = onRegister).padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("REGISTRARSE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            } else {
                Text("Comprando como: $nombreComprador", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold)
                Spacer(Modifier.height(10.dp))
            }
            BoutiqueCard(elevation = 2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Subtotal", fontSize = 12.sp, color = Color.Gray); Text(fmtCOP(totales.subtotal), fontSize = 12.sp, color = Charcoal) }
                if (totales.descuento > 0) { Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Ahorro por ofertas", fontSize = 12.sp, color = Success); Text(fmtCOP(totales.descuento), fontSize = 12.sp, color = Success) } }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Envío", fontSize = 12.sp, color = Color.Gray); Text(if (totales.envio == 0) "Gratis" else fmtCOP(totales.envio), fontSize = 12.sp, color = if (totales.envio == 0) Success else Charcoal) }
                if (totales.envio > 0) { Spacer(Modifier.height(4.dp)); Text("Te faltan ${fmtCOP(150000 - totales.totalSinEnvio)} para envío gratis", fontSize = 10.sp, color = Gold) }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = LineSoft)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total a pagar", fontSize = 13.sp, color = Color.Gray)
                    Text(fmtCOP(totales.total), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Charcoal)
                }
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton("FINALIZAR COMPRA", { viewModel.navegar(CompradorRoute.Pago) }, Modifier.fillMaxWidth(), Icons.Filled.Payment)
        }
    }
}
