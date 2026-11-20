package com.example.burbujaglow.feature.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.burbujaglow.core.components.*
import com.example.burbujaglow.data.CATEGORIAS

// Pantalla de Inicio (index): hero con logo + botones, categorías, más vendidos, nosotros, opiniones
@Composable
fun HomeScreen(viewModel: ShopViewModel) {
    val conteos = remember(viewModel.productos) {
        CATEGORIAS.associateWith { cat -> viewModel.productos.count { it.categoria == cat } }
    }
    val masVendidos = remember(viewModel.productos) {
        viewModel.productos.filter { it.sello.contains("MÁS VENDIDO") }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Charcoal).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(150.dp).clip(CircleShape).background(Color.White).shadow(4.dp, CircleShape), contentAlignment = Alignment.Center) {
                    LogoBurbujaGlow(Modifier.size(142.dp).clip(CircleShape))
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(50.dp)).background(Champagne).clickable { viewModel.navegar(CompradorRoute.Catalogo) }.padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
                        Text("COMPRAR AHORA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Charcoal, letterSpacing = 1.sp)
                    }
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(50.dp)).background(Color.White.copy(alpha = .15f)).clickable { viewModel.onSoloNuevosChange(true); viewModel.navegar(CompradorRoute.Catalogo) }.padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
                        Text("VER COLECCIÓN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(50.dp)).background(Charcoal).padding(horizontal = 14.dp, vertical = 9.dp)) {
                Text("Envíos a toda Colombia ✦ Pagos con Nequi y Daviplata ✦ Hipoalergénicos ✦ Cambios sin costo ✦", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        item {
            Text("CATEGORÍAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Text("Explora la colección", fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Charcoal)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(CATEGORIAS) { cat ->
                    Column(Modifier.width(92.dp).clip(RoundedCornerShape(18.dp)).background(Color.White).clickable { viewModel.onCategoriaChange(cat); viewModel.navegar(CompradorRoute.Catalogo) }.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        BubbleGlowArt(cat.first().toString(), Modifier.size(52.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(cat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Charcoal, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 13.sp)
                        Text("${conteos[cat] ?: 0} productos", fontSize = 8.sp, color = Color.Gray)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("MÁS VENDIDOS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp); Text("Los favoritos", fontFamily = Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Charcoal) }
                TextButton(onClick = { viewModel.onSoloBestChange(true); viewModel.navegar(CompradorRoute.Catalogo) }) { Text("Ver todos →", color = Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(masVendidos, key = { it.id }) { prod ->
                    Column(Modifier.width(170.dp).clip(RoundedCornerShape(18.dp)).background(Color.White).clickable { viewModel.navegar(CompradorRoute.Detalle(prod)) }.padding(12.dp)) {
                        ProductImage(prod, Modifier.fillMaxWidth().height(110.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(prod.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                        Text(prod.precio, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Gold)
                    }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Nude).padding(20.dp)) {
                Text("NOSOTROS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                Text("Belleza con propósito", fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                Spacer(Modifier.height(6.dp))
                Text("En Burbuja Glow creamos cosméticos de autor, hipoalergénicos y con ingredientes limpios, pensados para tu momento.", fontSize = 12.sp, color = InkSoft, lineHeight = 17.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Envío gratis desde $150.000", "Pago seguro", "Fórmulas limpias", "Asesoría experta").forEach { perk ->
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(10.dp), contentAlignment = Alignment.Center) {
                        Text(perk, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Charcoal, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 12.sp)
                    }
                }
            }
        }
        item {
            Text("OPINIONES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Text("Lo que dicen nuestras clientas", fontFamily = Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Charcoal)
            Spacer(Modifier.height(10.dp))
            listOf(Triple("Laura Gómez", "LG · Bogotá", "La calidad es increíble, la Tinta para Labios dura todo el día."), Triple("Andrea Ruiz", "AR · Medellín", "El empaque y los tonos son preciosos, llegó rapidísimo."), Triple("Camila Méndez", "CM · Cali", "La Paleta Nude Velvet dejó mi mirada como porcelana. La amo.")).forEach { (nombre, ini, texto) ->
                BoutiqueCard(elevation = 2) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) { Text(ini.take(2), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold) }
                        Spacer(Modifier.width(10.dp))
                        Column { Text(nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal); Text(texto, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp) }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
            }
}
