package com.example.burbujaglow.feature.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.burbujaglow.data.model.ProductoComprador

// Tienda (shop): buscador, categorías, filtros, orden y tarjetas de productos
@Composable
fun CatalogoScreen(
    viewModel: ShopViewModel,
    nombreComprador: String,
    onAddToCart: (ProductoComprador) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categorias = listOf("Todos") + CATEGORIAS
    // Filtrados/orden calculados solo cuando cambian los campos leídos del estado o el catálogo
    val filtrados by remember(viewModel.productos) {
        derivedStateOf {
            val minF = state.precioMin.filter { it.isDigit() }.toIntOrNull()
            val maxF = state.precioMax.filter { it.isDigit() }.toIntOrNull()
            val base = viewModel.productos.filter { p ->
                (state.categoriaSel == "Todos" || p.categoria == state.categoriaSel) &&
                (state.busqueda.isBlank() || p.nombre.contains(state.busqueda, true) || p.descripcionCorta.contains(state.busqueda, true) || p.categoria.contains(state.busqueda, true)) &&
                (minF == null || p.precioValor >= minF) && (maxF == null || p.precioValor <= maxF) &&
                (!state.soloOfertas || discountPct(p) > 0) && (!state.soloBest || p.sello.contains("MÁS VENDIDO")) && (!state.soloNuevos || p.sello == "NUEVO")
            }
            when (state.ordenSel) {
                "precio-asc" -> base.sortedBy { it.precioValor }
                "precio-desc" -> base.sortedByDescending { it.precioValor }
                "best-sellers" -> base.sortedByDescending { it.vendidos }
                "rating" -> base.sortedByDescending { it.rating.toDoubleOrNull() ?: 0.0 }
                else -> base.sortedByDescending { it.creado }
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            OutlinedTextField(
                value = state.busqueda, onValueChange = viewModel::onBusquedaChange,
                placeholder = { Text("Buscar velvet, nude, glow…", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = Gold, modifier = Modifier.size(19.dp)) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = Gold, unfocusedBorderColor = LineSoft)
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categorias) { c ->
                    val sel = c == state.categoriaSel
                    Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (sel) Charcoal else Color.White).clickable { viewModel.onCategoriaChange(c) }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text(c, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else InkSoft)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Belleza y cuidado capilar · Envío en Bogotá", fontSize = 10.sp, color = Color.Gray)
            if (nombreComprador.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Hola, $nombreComprador · compra en Burbuja Glow", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${filtrados.size} productos", fontSize = 11.sp, color = Color.Gray)
                TextButton(onClick = { viewModel.onMostrarFiltrosChange(!state.mostrarFiltros) }) { Text(if (state.mostrarFiltros) "Ocultar filtros" else "Filtros", color = Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Ordenar: ", fontSize = 11.sp, color = Color.Gray)
                listOf("recientes" to "Recientes", "precio-asc" to "Precio ↑", "precio-desc" to "Precio ↓", "best-sellers" to "Más vendidos", "rating" to "Rating").forEach { (key, lbl) ->
                    Box(Modifier.padding(end = 6.dp).clip(RoundedCornerShape(50.dp)).background(if (state.ordenSel == key) GoldLight else Color.White).clickable { viewModel.onOrdenChange(key) }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (state.ordenSel == key) Gold else InkSoft)
                    }
                }
            }
            if (state.mostrarFiltros) {
                Spacer(Modifier.height(8.dp))
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(14.dp)) {
                    Text("FILTROS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = state.precioMin, onValueChange = viewModel::onPrecioMinChange, placeholder = { Text("Precio mín.", fontSize = 11.sp) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
                        OutlinedTextField(value = state.precioMax, onValueChange = viewModel::onPrecioMaxChange, placeholder = { Text("Precio máx.", fontSize = 11.sp) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.onSoloOfertasChange(!state.soloOfertas) }.padding(vertical = 4.dp)) {
                        Checkbox(checked = state.soloOfertas, onCheckedChange = viewModel::onSoloOfertasChange, colors = CheckboxDefaults.colors(checkedColor = Gold))
                        Text("Solo ofertas", fontSize = 12.sp, color = Charcoal)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.onSoloBestChange(!state.soloBest) }.padding(vertical = 4.dp)) {
                        Checkbox(checked = state.soloBest, onCheckedChange = viewModel::onSoloBestChange, colors = CheckboxDefaults.colors(checkedColor = Gold))
                        Text("Más vendidos", fontSize = 12.sp, color = Charcoal)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.onSoloNuevosChange(!state.soloNuevos) }.padding(vertical = 4.dp)) {
                        Checkbox(checked = state.soloNuevos, onCheckedChange = viewModel::onSoloNuevosChange, colors = CheckboxDefaults.colors(checkedColor = Gold))
                        Text("Nuevos", fontSize = 12.sp, color = Charcoal)
                    }
                    TextButton(onClick = viewModel::limpiarFiltros) {
                        Text("Limpiar filtros", color = Danger, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
        if (filtrados.isEmpty()) item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Sin resultados para esta búsqueda.", color = Color.Gray, fontSize = 13.sp) } }
        items(filtrados, key = { it.id }) { prod ->
            val dcto = discountPct(prod)
            val (stockTxt, stockColor) = stockInfo(prod)
            BoutiqueCard(elevation = 3, onClick = { viewModel.navegar(CompradorRoute.Detalle(prod)) }) {
                Row(verticalAlignment = Alignment.Top) {
                    ProductImage(prod, Modifier.size(76.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row {
                            Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (prod.sello == "OFERTA") Danger else Rose).padding(horizontal = 7.dp, vertical = 3.dp)) {
                                Text(if (prod.sello.isBlank()) "BURBUJA GLOW" else prod.sello, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = .6.sp)
                            }
                            if (dcto > 0) { Spacer(Modifier.width(5.dp)); Box(Modifier.clip(RoundedCornerShape(50.dp)).background(Danger).padding(horizontal = 7.dp, vertical = 3.dp)) { Text("-$dcto%", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White) } }
                            Spacer(Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Star, contentDescription = "Rating ${prod.rating}", tint = Gold, modifier = Modifier.size(13.dp)); Spacer(Modifier.width(3.dp)); Text(prod.rating, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Charcoal) }
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(prod.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Charcoal, lineHeight = 19.sp)
                        Text(prod.categoria, fontSize = 11.sp, color = Color.Gray)
                        if (prod.descripcionCorta.isNotBlank()) { Spacer(Modifier.height(3.dp)); Text(prod.descripcionCorta, fontSize = 10.sp, color = InkSoft, lineHeight = 14.sp, maxLines = 2) }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                if (dcto > 0) Text(fmtCOP(prod.precioAnterior), fontSize = 10.sp, color = Color.Gray, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                Text(prod.precio, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gold)
                                Text(stockTxt, fontSize = 10.sp, color = stockColor)
                                Text("${prod.vendidos} vendidos", fontSize = 9.sp, color = Color.Gray)
                            }
                            Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (prod.stock <= 0) Color.Gray else Charcoal).clickable(enabled = prod.stock > 0) { onAddToCart(prod) }.padding(horizontal = 14.dp, vertical = 9.dp)) {
                                Text(if (prod.stock <= 0) "AGOTADO" else "AÑADIR +", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = .8.sp)
                            }
                        }
                    }
                }
            }
        }
            }
}
