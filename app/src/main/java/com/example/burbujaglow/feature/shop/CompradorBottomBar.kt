package com.example.burbujaglow.feature.shop

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.burbujaglow.core.components.Gold
import com.example.burbujaglow.core.components.GoldLight
import com.example.burbujaglow.core.components.Rose

// ── MENÚ DE NAVEGACIÓN INFERIOR (Bottom Navigation) ────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompradorBottomBar(
    rutaActual: CompradorRoute,
    totalItems: Int,
    onInicio: () -> Unit,
    onTienda: () -> Unit,
    onCarrito: () -> Unit,
    onSesion: () -> Unit
) {
    val seleccionadoInicio = rutaActual == CompradorRoute.Inicio
    val seleccionadoTienda = rutaActual == CompradorRoute.Catalogo || rutaActual is CompradorRoute.Detalle
    val seleccionadoCarrito = rutaActual == CompradorRoute.Carrito || rutaActual == CompradorRoute.Pago
    NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
        NavigationBarItem(
            selected = seleccionadoInicio, onClick = onInicio,
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Gold, selectedTextColor = Gold, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = GoldLight)
        )
        NavigationBarItem(
            selected = seleccionadoTienda, onClick = onTienda,
            icon = { Icon(Icons.Filled.Storefront, contentDescription = "Tienda") },
            label = { Text("Tienda", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Gold, selectedTextColor = Gold, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = GoldLight)
        )
        NavigationBarItem(
            selected = seleccionadoCarrito, onClick = onCarrito,
            icon = {
                BadgedBox(badge = { if (totalItems > 0) Badge(containerColor = Rose, contentColor = Color.White) { Text("$totalItems", fontSize = 9.sp) } }) {
                    Icon(Icons.Filled.ShoppingBag, contentDescription = "Carrito")
                }
            },
            label = { Text("Carrito", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Gold, selectedTextColor = Gold, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = GoldLight)
        )
        NavigationBarItem(
            selected = false, onClick = onSesion,
            icon = { Icon(Icons.Filled.Person, contentDescription = "Iniciar sesión") },
            label = { Text("Iniciar sesión", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Gold, selectedTextColor = Gold, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = GoldLight)
        )
    }
}
