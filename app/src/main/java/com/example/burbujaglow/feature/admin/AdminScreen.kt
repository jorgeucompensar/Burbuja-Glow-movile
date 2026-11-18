package com.example.burbujaglow.feature.admin

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

// Contenedor : CRUD.
// Cada sección vive en su propia pantalla (Dashboard/Productos/Pedidos/Usuarios/Reporte/Inventario/Config).
@Composable
fun AdminScreen(onBack: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fechaHoy = remember { SimpleDateFormat("EEEE, d MMM yyyy", Locale("es", "CO")).format(Date()) }
    val pendientes = viewModel.pedidos.count { it.estado == "Pendiente" }

    state.usuarioAEliminar?.let { u ->
        AlertDialog(
            onDismissRequest = { viewModel.onEliminarUsuarioChange(null) },
            title = { Text("Eliminar usuario", fontFamily = Serif, fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres eliminar al usuario «${u.nombre}»? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminarUsuario(u); Toast.makeText(context, "${u.nombre} eliminado", Toast.LENGTH_SHORT).show() }) {
                    Text("Eliminar", color = Rose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.onEliminarUsuarioChange(null) }) { Text("Cancelar") } },
            shape = RoundedCornerShape(20.dp)
        )
    }
    if (state.usuarioAEditar != null || state.mostrarAgregarUsuario) {
        val originalU = state.usuarioAEditar
        var uNombre by remember(originalU) { mutableStateOf(originalU?.nombre ?: "") }
        var uCorreo by remember(originalU) { mutableStateOf(originalU?.correo ?: "") }
        var uTelefono by remember(originalU) { mutableStateOf(originalU?.telefono ?: "") }
        var uRol by remember(originalU) { mutableStateOf(originalU?.rol ?: "COMPRADOR") }
        var uEstado by remember(originalU) { mutableStateOf(originalU?.estado ?: "Activo") }
        AlertDialog(
            onDismissRequest = { viewModel.onEditarUsuarioChange(null); viewModel.onAgregarUsuarioChange(false) },
            title = { Text(if (originalU == null) "Nuevo usuario" else "Editar usuario", fontFamily = Serif, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = uNombre, onValueChange = { uNombre = it }, label = { Text("Nombre", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                    OutlinedTextField(value = uCorreo, onValueChange = { uCorreo = it }, label = { Text("Correo", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                    OutlinedTextField(value = uTelefono, onValueChange = { uTelefono = it }, label = { Text("Teléfono", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                    OutlinedTextField(value = uRol, onValueChange = { uRol = it }, label = { Text("Rol: ADMIN / VENDEDOR / COMPRADOR", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                    OutlinedTextField(value = uEstado, onValueChange = { uEstado = it }, label = { Text("Estado: Activo / Inactivo", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (uNombre.isBlank() || !uCorreo.contains("@")) { Toast.makeText(context, "Ingresa nombre y un correo válido", Toast.LENGTH_SHORT).show(); return@TextButton }
                    viewModel.guardarUsuario(originalU, uNombre, uCorreo, uTelefono, uRol, uEstado)
                    Toast.makeText(context, if (originalU != null) "$uNombre actualizado" else "$uNombre creado", Toast.LENGTH_SHORT).show()
                }) { Text("Guardar", color = Gold, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { viewModel.onEditarUsuarioChange(null); viewModel.onAgregarUsuarioChange(false) }) { Text("Cancelar") } },
            shape = RoundedCornerShape(20.dp)
        )
    }
    state.productoAEliminar?.let { p ->
        AlertDialog(
            onDismissRequest = { viewModel.onEliminarChange(null) },
            title = { Text("Eliminar producto", fontFamily = Serif, fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres eliminar «${p.nombre}» del catálogo? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminarProducto(p); Toast.makeText(context, "${p.nombre} eliminado", Toast.LENGTH_SHORT).show() }) {
                    Text("Eliminar", color = Rose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.onEliminarChange(null) }) { Text("Cancelar") } },
            shape = RoundedCornerShape(20.dp)
        )
    }
    if (state.productoAEditar != null || state.mostrarAgregar) {
        val original = state.productoAEditar
        var nombre by remember(original) { mutableStateOf(original?.nombre ?: "") }
        var categoria by remember(original) { mutableStateOf(original?.categoria ?: "Belleza") }
        var precio by remember(original) { mutableStateOf(original?.precio ?: "$") }
        var stock by remember(original) { mutableStateOf(original?.stock ?: "") }
        var estado by remember(original) { mutableStateOf(original?.estado ?: "NUEVO") }
        var imagenUrl by remember(original) { mutableStateOf(original?.imagenUrl ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.onEditarChange(null); viewModel.onAgregarChange(false) },
            title = { Text(if (original == null) "Nuevo producto" else "Editar producto", fontFamily = Serif, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                    OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                    OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio (ej. \$49.900)", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                    OutlinedTextField(value = estado, onValueChange = { estado = it }, label = { Text("Estado: OFERTA / NUEVO / MÁS VENDIDO", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold))
                    ImageUrlField(valor = imagenUrl, onValueChange = { imagenUrl = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nombre.isBlank()) { Toast.makeText(context, "Ingresa el nombre del producto", Toast.LENGTH_SHORT).show(); return@TextButton }
                    if (imagenUrl.isNotBlank() && !urlImagenValida(imagenUrl)) {
                        Toast.makeText(context, "Ingresa una URL HTTPS válida o el enlace Raw de GitHub", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    viewModel.guardarProducto(original, nombre, categoria, precio, stock, estado, normalizarUrlImagen(imagenUrl))
                    Toast.makeText(context, if (original != null) "$nombre actualizado" else "$nombre agregado al catálogo", Toast.LENGTH_SHORT).show()
                }) { Text("Guardar", color = Gold, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { viewModel.onEditarChange(null); viewModel.onAgregarChange(false) }) { Text("Cancelar") } },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(Modifier.fillMaxSize().background(Ivory)) {
        Box(Modifier.fillMaxWidth().height(4.dp).background(Gold))
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("BURBUJA GLOW · ADMIN PANEL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp)
                Text("Panel Administrador", fontFamily = Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                Text(fechaHoy.replaceFirstChar { it.uppercase() }, fontSize = 10.sp, color = Color.Gray)
            }
            Box(Modifier.clip(RoundedCornerShape(50.dp)).background(Charcoal).clickable(onClick = onBack).padding(horizontal = 16.dp, vertical = 9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Logout, contentDescription = "Salir del panel", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp)); Text("SALIR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                }
            }
        }
        HorizontalDivider(color = LineSoft)

        val seccionesAdmin = listOf("dashboard" to "Dashboard", "products" to "Productos", "orders" to "Pedidos ($pendientes)", "usuarios" to "Usuarios", "reporte" to "Reporte de ventas", "inventory" to "Inventario", "config" to "Configuración")
        LazyRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(seccionesAdmin) { (key, lbl) ->
                Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (state.seccion == key) Charcoal else Color.White).clickable { viewModel.onSeccionChange(key) }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (state.seccion == key) Color.White else InkSoft)
                }
            }
        }
        when (state.seccion) {
            "dashboard" -> DashboardScreen(viewModel)
            "products" -> ProductosAdminScreen(viewModel)
            "orders" -> PedidosScreen(viewModel)
            "usuarios" -> UsuariosScreen(viewModel)
            "reporte" -> ReporteVentasScreen(viewModel)
            "inventory" -> InventarioScreen(viewModel)
            "config" -> ConfigAdminScreen(viewModel)
        }
    }
}
