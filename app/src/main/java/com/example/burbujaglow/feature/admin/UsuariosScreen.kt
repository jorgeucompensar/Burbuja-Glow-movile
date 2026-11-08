package com.example.burbujaglow.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.burbujaglow.core.components.*

// Gestión de usuarios: listado, creación, edición y eliminación
@Composable
fun UsuariosScreen(viewModel: AdminViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Gestión de usuarios (${viewModel.usuarios.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal)
            Box(Modifier.clip(RoundedCornerShape(50.dp)).background(Charcoal).clickable { viewModel.onAgregarUsuarioChange(true) }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo usuario", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp)); Text("+ Nuevo usuario", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        viewModel.usuarios.forEach { u ->
            BoutiqueCard(elevation = 2) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) {
                        Text(u.nombre.take(1), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(u.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                        Text("${u.correo} · ${u.telefono}", fontSize = 10.sp, color = Color.Gray)
                        Spacer(Modifier.height(3.dp))
                        Row {
                            Box(Modifier.clip(RoundedCornerShape(50.dp)).background(GoldLight).padding(horizontal = 7.dp, vertical = 2.dp)) { Text(u.rol, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Gold) }
                            Spacer(Modifier.width(5.dp))
                            Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if (u.estado == "Activo") Color(0xFFE3F2E8) else Color(0xFFF9ECEC)).padding(horizontal = 7.dp, vertical = 2.dp)) { Text(u.estado, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = if (u.estado == "Activo") Success else Danger) }
                        }
                    }
                    IconButton(onClick = { viewModel.onEditarUsuarioChange(u) }) { Icon(Icons.Filled.Edit, contentDescription = "Editar usuario", tint = Gold, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = { viewModel.onEliminarUsuarioChange(u) }) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar usuario", tint = Rose, modifier = Modifier.size(18.dp)) }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
