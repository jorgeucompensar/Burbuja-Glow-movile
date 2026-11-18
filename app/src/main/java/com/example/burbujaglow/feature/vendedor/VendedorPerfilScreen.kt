package com.example.burbujaglow.feature.vendedor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.burbujaglow.core.components.*

// Perfil del vendedor: identidad de la tienda, datos de contacto y métricas.
@Composable
fun VendedorPerfilScreen(viewModel: VendedorViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var nombre by remember(state.perfilNombre) { mutableStateOf(state.perfilNombre) }
    var correo by remember(state.perfilCorreo) { mutableStateOf(state.perfilCorreo) }
    var telefono by remember(state.perfilTelefono) { mutableStateOf(state.perfilTelefono) }
    var ciudad by remember(state.perfilCiudad) { mutableStateOf(state.perfilCiudad) }
    var tienda by remember(state.perfilTienda) { mutableStateOf(state.perfilTienda) }

    val stockBajo = viewModel.productos.count { (it.stock.toIntOrNull() ?: 0) <= 8 }
    val entregados = viewModel.pedidos.count { it.estado == "Entregado" }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        BoutiqueCard(elevation = 3) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Gold, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(10.dp))
                Text(state.perfilNombre, fontFamily = Serif, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Charcoal, textAlign = TextAlign.Center)
                Text("Rol · Vendedor", fontSize = 11.sp, color = Gold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                CrueltyFreeBadge()
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Metrica("Productos", "${viewModel.productos.size}", Charcoal)
                    Metrica("Entregados", "$entregados", Success)
                    Metrica("Stock bajo", "$stockBajo", Rose)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        BoutiqueCard(elevation = 2) {
            Text("Datos de la tienda", fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Charcoal)
            Spacer(Modifier.height(10.dp))
            StyledField("Nombre del vendedor", nombre, { nombre = it }, "Ej: María García", Icons.Filled.Person)
            Spacer(Modifier.height(10.dp))
            StyledField("Correo de contacto", correo, { correo = it }, "vendedor@burbujaglow.com", Icons.Filled.Mail)
            Spacer(Modifier.height(10.dp))
            StyledField("Teléfono", telefono, { telefono = it }, "Ej: 300 123 4567", Icons.Filled.Phone)
            Spacer(Modifier.height(10.dp))
            StyledField("Ciudad", ciudad, { ciudad = it }, "Ej: Bogotá D.C., Colombia", Icons.Filled.LocationOn)
            Spacer(Modifier.height(10.dp))
            StyledField("Tienda", tienda, { tienda = it }, "BURBUJA GLOW", Icons.Filled.Storefront)
            Spacer(Modifier.height(16.dp))
            PrimaryButton("GUARDAR PERFIL", {
                if (nombre.isBlank() || !correo.contains("@")) {
                    Toast.makeText(context, "Ingresa tu nombre y un correo válido", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.guardarPerfil(nombre.trim(), correo.trim(), telefono.trim(), ciudad.trim(), tienda.trim())
                    Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                }
            }, Modifier.fillMaxWidth(), Icons.Filled.Person)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Burbuja Glow es una boutique de cosmética y cuidado capilar 100% libre de crueldad animal.",
            fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), lineHeight = 15.sp
        )
    }
}

@Composable
private fun Metrica(titulo: String, valor: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(titulo, fontSize = 10.sp, color = Color.Gray)
    }
}
