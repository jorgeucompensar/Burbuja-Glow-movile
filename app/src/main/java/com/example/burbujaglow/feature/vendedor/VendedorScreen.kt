package com.example.burbujaglow.feature.vendedor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.burbujaglow.core.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Panel del vendedor: contenedor con barra superior, secciones y diálogos CRUD.
// Secciones: Productos (CRUD) · Pedidos recibidos · Mi perfil.
@Composable
fun VendedorScreen(onBack: () -> Unit, uidVendedor: String = "", viewModel: VendedorViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uidVendedor) {
        if (uidVendedor.isNotBlank()) viewModel.configurarVendedor(uidVendedor)
    }
    val fechaHoy = remember { SimpleDateFormat("EEEE, d MMM yyyy", Locale("es", "CO")).format(Date()) }
    val pendientes = viewModel.pedidos.count { it.estado == "Pendiente" }

    // Confirmación de eliminación de producto (acción crítica)
    state.productoAEliminar?.let { p ->
        AlertDialog(
            onDismissRequest = { viewModel.onEliminarChange(null) },
            title = { Text("Eliminar producto", fontFamily = Serif, fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres eliminar «${p.nombre}» del catálogo? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProducto(p)
                    Toast.makeText(context, "${p.nombre} eliminado", Toast.LENGTH_SHORT).show()
                }) { Text("Eliminar", color = Rose, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { viewModel.onEliminarChange(null) }) { Text("Cancelar") } },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Crear / editar producto (CRUD)
    if (state.productoAEditar != null || state.mostrarAgregar) {
        val original = state.productoAEditar
        var nombre by remember(original) { mutableStateOf(original?.nombre ?: "") }
        var categoria by remember(original) { mutableStateOf(original?.categoria ?: "Labiales") }
        var precio by remember(original) { mutableStateOf(original?.precio ?: "$") }
        var stock by remember(original) { mutableStateOf(original?.stock ?: "") }
        var estado by remember(original) { mutableStateOf(original?.estado ?: "NUEVO") }
        var imagenUrl by remember(original) { mutableStateOf(original?.imagenUrl ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.onEditarChange(null); viewModel.onAgregarChange(false) },
            title = { Text(if (original == null) "Nuevo producto" else "Editar producto", fontFamily = Serif, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampoDialogo("Nombre", nombre, { nombre = it })
                    CampoDialogo("Categoría", categoria, { categoria = it })
                    CampoDialogo("Precio (ej. \$49.900)", precio, { precio = it })
                    CampoDialogo("Stock", stock, { stock = it })
                    CampoDialogo("Estado: OFERTA / NUEVO / MÁS VENDIDO", estado, { estado = it })
                    ImageUrlField(valor = imagenUrl, onValueChange = { imagenUrl = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nombre.isBlank()) {
                        Toast.makeText(context, "Ingresa el nombre del producto", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (imagenUrl.isNotBlank() && !urlImagenValida(imagenUrl)) {
                        Toast.makeText(context, "Ingresa una URL HTTPS válida o el enlace Raw de GitHub", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    viewModel.guardarProducto(original, nombre, categoria, precio, stock, estado, normalizarUrlImagen(imagenUrl))
                    Toast.makeText(context, if (original != null) "$nombre actualizado" else "$nombre agregado al catálogo", Toast.LENGTH_SHORT).show()
                }) { Text("Guardar", color = Gold, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEditarChange(null); viewModel.onAgregarChange(false) }) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(Modifier.fillMaxSize().background(Ivory)) {
        Box(Modifier.fillMaxWidth().height(4.dp).background(Gold))
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("BURBUJA GLOW · PANEL VENDEDOR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp)
                Text("Panel Vendedor", fontFamily = Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                Text(fechaHoy.replaceFirstChar { it.uppercase() }, fontSize = 10.sp, color = Color.Gray)
            }
            Box(Modifier.clip(RoundedCornerShape(50.dp)).background(Charcoal).clickable(onClick = onBack).padding(horizontal = 16.dp, vertical = 9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Logout, contentDescription = "Salir del panel", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("SALIR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                }
            }
        }
        HorizontalDivider(color = LineSoft)

        val secciones = listOf(
            "productos" to "Productos",
            "pedidos" to "Pedidos recibidos ($pendientes)",
            "perfil" to "Mi perfil"
        )
        LazyRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(secciones) { (key, lbl) ->
                Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (state.seccion == key) Charcoal else Color.White).clickable { viewModel.onSeccionChange(key) }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (state.seccion == key) Color.White else InkSoft)
                }
            }
        }

        when (state.seccion) {
            "productos" -> VendedorProductosScreen(viewModel)
            "pedidos" -> VendedorPedidosScreen(viewModel)
            "perfil" -> VendedorPerfilScreen(viewModel)
        }
    }
}

@Composable
private fun CampoDialogo(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold,
            focusedLabelColor = Gold
        )
    )
}
