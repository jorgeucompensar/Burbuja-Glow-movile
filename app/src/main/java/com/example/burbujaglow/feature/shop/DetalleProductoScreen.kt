package com.example.burbujaglow.feature.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.burbujaglow.core.components.*
import com.example.burbujaglow.data.model.ProductoComprador

// Detalle de producto (PDP): galería, precio, stock, qty, acordeón, reseñas y relacionados
@Composable
fun DetalleProductoScreen(
    producto: ProductoComprador,
    viewModel: ShopViewModel,
    onAddToCart: (ProductoComprador, Int) -> Unit
) {
    val prod = viewModel.productos.find { it.id == producto.id } ?: producto
    var qty by remember(prod.id) { mutableStateOf(1) }
    var thumbSel by remember(prod.id) { mutableStateOf(0) }
    var accAbierto by remember(prod.id) { mutableStateOf("desc") }
    val dcto = discountPct(prod)
    val (stockTxt, stockColor) = stockInfo(prod)
    val relacionados = viewModel.productos.filter { it.id != prod.id && (it.categoria == prod.categoria || (it.sello.isNotBlank() && it.sello == prod.sello)) }.take(4)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        BoutiqueCard(elevation = 3) {
            ProductImage(prod, Modifier.fillMaxWidth().height(200.dp))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Vista", "Detalle", "Textura").forEachIndexed { i, lbl ->
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (thumbSel == i) GoldLight else Nude).clickable { thumbSel = i }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(lbl, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (thumbSel == i) Gold else InkSoft)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(prod.nombre, fontFamily = Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Charcoal, lineHeight = 30.sp)
            Spacer(Modifier.height(4.dp)); Text(prod.categoria, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    if (dcto > 0) Text(fmtCOP(prod.precioAnterior), fontSize = 12.sp, color = Color.Gray, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    Text(prod.precio, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gold)
                }
                if (dcto > 0) { Spacer(Modifier.width(8.dp)); Box(Modifier.clip(RoundedCornerShape(50.dp)).background(Danger).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("-$dcto%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White) } }
                Spacer(Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Star, contentDescription = "Rating ${prod.rating}", tint = Gold, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("${prod.rating} (${prod.reviews})", fontSize = 11.sp, color = Color.Gray) }
            }
            Spacer(Modifier.height(6.dp))
            Text(stockTxt, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = stockColor)
            Spacer(Modifier.height(4.dp))
            Text("Envío gratis desde $150.000 · Entrega 2–5 días a toda Colombia", fontSize = 10.sp, color = Color.Gray)
            HorizontalDivider(Modifier.padding(vertical = 14.dp), color = LineSoft)
            listOf(Triple("desc", "Descripción", prod.descripcion), Triple("ing", "Ingredientes", prod.ingredientes.joinToString("\n• ")), Triple("uso", "Cómo usarlo", "Aplica sobre piel limpia y preparada, difumina con tus dedos o con una brocha de la colección Burbuja Glow para un acabado profesional.")).forEach { (key, titulo, cuerpo) ->
                Column(Modifier.fillMaxWidth().clickable { accAbierto = if (accAbierto == key) "" else key }.padding(vertical = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                        Text(if (accAbierto == key) "−" else "+", fontWeight = FontWeight.Bold, color = Gold)
                    }
                    if (accAbierto == key) { Spacer(Modifier.height(6.dp)); Text((if (key == "ing") "• " else "") + cuerpo, fontSize = 12.sp, color = InkSoft, lineHeight = 17.sp) }
                }
                HorizontalDivider(color = LineSoft)
            }
            Spacer(Modifier.height(10.dp))
            Text("OPINIONES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(prod.rating, fontFamily = Serif, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    listOf(5 to 72, 4 to 19, 3 to 6, 2 to 2, 1 to 1).forEach { (estrellas, pct) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("$estrellas ★", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.width(30.dp))
                            Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(50.dp)).background(LineSoft)) {
                                Box(Modifier.fillMaxWidth(pct / 100f).height(6.dp).clip(RoundedCornerShape(50.dp)).background(Gold))
                            }
                            Spacer(Modifier.width(6.dp)); Text("$pct%", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            listOf(Triple("Camila R.", 5, "La textura es increíble, se siente ligera y dura todo el día. Ya pedí otro."), Triple("Valentina M.", 5, "Increíble la calidad por el precio. El empaque es precioso y elegante."), Triple("Daniela P.", 4, "Muy bueno, solo me gustaría que tuviera más tonos disponibles.")).forEach { (nombre, estr, texto) ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Text("$nombre  ${"★".repeat(estr)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                    Text(texto, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Cantidad", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(Color.White).padding(4.dp)) {
                IconButton(onClick = { qty = (qty - 1).coerceAtLeast(1) }) { Icon(Icons.Filled.Remove, contentDescription = "Restar cantidad", tint = Charcoal) }
                Text("$qty", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Charcoal, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                IconButton(onClick = { qty = (qty + 1).coerceAtMost(maxOf(prod.stock, 1)) }) { Icon(Icons.Filled.Add, contentDescription = "Sumar cantidad", tint = Charcoal) }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (prod.stock <= 0) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(50.dp)).background(Color.Gray).padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                Text("AGOTADO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, letterSpacing = 1.sp)
            }
        } else {
            PrimaryButton("Añadir al Carrito de Compras", { onAddToCart(prod, qty); viewModel.navegar(CompradorRoute.Carrito) }, Modifier.fillMaxWidth(), Icons.Filled.ShoppingBag)
            Spacer(Modifier.height(10.dp))
            GoldButton("COMPRAR AHORA", { onAddToCart(prod, qty); viewModel.navegar(CompradorRoute.Pago) }, Modifier.fillMaxWidth())
        }
        if (relacionados.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("TAMBIÉN TE PUEDE GUSTAR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(relacionados, key = { it.id }) { rel ->
                    Column(Modifier.width(150.dp).clip(RoundedCornerShape(18.dp)).background(Color.White).clickable { viewModel.navegar(CompradorRoute.Detalle(rel)) }.padding(10.dp)) {
                        ProductImage(rel, Modifier.fillMaxWidth().height(90.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(rel.nombre, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Charcoal)
                        Text(rel.precio, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold)
                    }
                }
            }
        }
    }
}
