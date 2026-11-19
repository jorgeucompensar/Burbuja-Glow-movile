package com.example.burbujaglow.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.burbujaglow.core.components.*
import com.example.burbujaglow.data.productoDesdeAdmin

// Gestión de productos: buscador, restaurar demo, crear/editar/eliminar (CRUD)
@Composable
fun ProductosAdminScreen(viewModel: AdminViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val productosFiltrados = viewModel.productos.filter { state.busquedaAdmin.isBlank() || it.nombre.contains(state.busquedaAdmin, true) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Catálogo de administración", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal)
                Row {
                    TextButton(onClick = { viewModel.restaurarDemo() }) { Text("Restaurar demo", color = Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    Box(Modifier.clip(RoundedCornerShape(50.dp)).background(Charcoal).clickable { viewModel.onAgregarChange(true) }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = "Nuevo producto", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp)); Text("+ Nuevo producto", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = state.busquedaAdmin, onValueChange = viewModel::onBusquedaAdminChange, placeholder = { Text("Buscar producto…", fontSize = 12.sp) }, leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar producto", tint = Gold, modifier = Modifier.size(18.dp)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = Gold, unfocusedBorderColor = LineSoft))
        }
        item {
            Column(Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(Color.White)) {
                GoldenTopBar()
                Row(Modifier.fillMaxWidth().background(GoldLight).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("PRODUCTO", modifier = Modifier.weight(2.2f), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = .6.sp)
                    Text("PRECIO", modifier = Modifier.weight(1.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = .6.sp)
                    Text("STOCK", modifier = Modifier.weight(.9f), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = .6.sp)
                    Text("ACCIONES", modifier = Modifier.weight(1.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = .6.sp, textAlign = TextAlign.End)
                }
                if (productosFiltrados.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text("Sin productos. Agrega el primero con + Nuevo producto.", fontSize = 12.sp, color = Color.Gray) }
                } else {
                    productosFiltrados.forEach { item ->
                        Column {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Row(Modifier.weight(2.2f), verticalAlignment = Alignment.CenterVertically) {
                                    ProductImage(productoDesdeAdmin(item), Modifier.size(36.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(item.nombre, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Charcoal, lineHeight = 14.sp)
                                        Text(item.categoria, fontSize = 8.sp, color = Color.Gray)
                                        Spacer(Modifier.height(3.dp))
                                        Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (item.estado.contains("OFERTA")) RoseLight else GoldLight).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text(item.estado, fontSize = 6.sp, fontWeight = FontWeight.Bold, color = if (item.estado.contains("OFERTA")) Rose else Gold, letterSpacing = .5.sp)
                                        }
                                    }
                                }
                                Text(item.precio, modifier = Modifier.weight(1.4f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Gold)
                                Text(item.stock, modifier = Modifier.weight(.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if ((item.stock.toIntOrNull() ?: 0) <= 8) Rose else Charcoal)
                                Row(Modifier.weight(1.4f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.onEditarChange(item) }) { Icon(Icons.Filled.Edit, contentDescription = "Editar producto", tint = Gold, modifier = Modifier.size(18.dp)) }
                                    IconButton(onClick = { viewModel.onEliminarChange(item) }) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar producto", tint = Rose, modifier = Modifier.size(18.dp)) }
                                }
                            }
                            HorizontalDivider(color = LineSoft)
                        }
                    }
                }
            }
        }
            }
}
