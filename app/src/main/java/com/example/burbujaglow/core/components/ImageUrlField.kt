package com.example.burbujaglow.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

fun normalizarUrlImagen(valor: String): String {
    val url = valor.trim().substringBefore("#").substringBefore("?")
    if (!url.contains("github.com/")) return url
    return url
        .replace("https://www.github.com/", "https://raw.githubusercontent.com/")
        .replace("http://www.github.com/", "https://raw.githubusercontent.com/")
        .replace("https://github.com/", "https://raw.githubusercontent.com/")
        .replace("http://github.com/", "https://raw.githubusercontent.com/")
        .replace("/blob/", "/")
}

fun urlImagenValida(valor: String): Boolean {
    val url = normalizarUrlImagen(valor)
    return url.startsWith("https://") && url.length > 12 && !url.contains(' ')
}

@Composable
fun ImageUrlField(valor: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val url = normalizarUrlImagen(valor)
    var fallo by remember(url) { mutableStateOf(false) }
    val valida = urlImagenValida(valor)

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = { onValueChange(it); fallo = false },
            label = { Text("URL de imagen (https://...)", fontSize = 11.sp) },
            placeholder = { Text("https://raw.githubusercontent.com/.../imagen.jpg", fontSize = 11.sp, color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = LineSoft,
                cursorColor = Gold,
                focusedLabelColor = Gold
            )
        )
        Spacer(Modifier.height(6.dp))
        when {
            valor.isBlank() -> Text(
                "Pega aquí la URL Raw de GitHub o cualquier URL pública de imagen.",
                fontSize = 10.sp,
                color = Color.Gray
            )
            !valida -> Text(
                "Usa una URL HTTPS directa. Si es de GitHub, copia el enlace Raw.",
                fontSize = 10.sp,
                color = Danger,
                fontWeight = FontWeight.Bold
            )
            fallo -> {
                FallbackImagenUrl(Modifier.size(96.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    "No se pudo cargar la imagen. Revisa la URL.",
                    fontSize = 10.sp,
                    color = Danger,
                    fontWeight = FontWeight.Bold
                )
            }
            else -> AsyncImage(
                model = url,
                contentDescription = "Vista previa de la imagen",
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
                onError = { fallo = true }
            )
        }
    }
}

@Composable
private fun FallbackImagenUrl(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GoldLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Image, contentDescription = null, tint = Gold)
    }
}
