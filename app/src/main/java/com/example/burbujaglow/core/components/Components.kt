package com.example.burbujaglow.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.burbujaglow.R
import com.example.burbujaglow.data.model.ProductoComprador

// (Sello cruelty-free retirado a pedido del cliente: función eliminada)

@Composable
fun GoldenTopBar() { Box(Modifier.fillMaxWidth().height(4.dp).background(Gold)) }

// Logo oficial Burbuja Glow (carpeta CATALOGO del Escritorio → Logo burbuja.jpeg)
@Composable
fun LogoBurbujaGlow(modifier: Modifier = Modifier) {
    Image(painter = painterResource(id = R.drawable.logo_burbuja_glow), contentDescription = "Logo Burbuja Glow", modifier = modifier)
}

// Imagen real del producto (carpeta CATALOGO); respaldo al arte de burbujas
@Composable
fun ProductImage(producto: ProductoComprador, modifier: Modifier = Modifier) {
    if (producto.imagen != 0) {
        Image(painter = painterResource(id = producto.imagen), contentDescription = producto.nombre, modifier = modifier.clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Crop)
    } else {
        BubbleGlowArt(producto.nombre.first().toString(), modifier)
    }
}

@Composable
fun BubbleGlowArt(initial: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(18.dp)).background(GoldLight), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = .75f), radius = size.minDimension * .34f, center = center.copy(x = size.width * .28f, y = size.height * .30f))
            drawCircle(Gold.copy(alpha = .18f), radius = size.minDimension * .20f, center = center.copy(x = size.width * .75f, y = size.height * .72f))
            drawCircle(Rose.copy(alpha = .14f), radius = size.minDimension * .14f, center = center.copy(x = size.width * .72f, y = size.height * .26f))
        }
        Text(initial, fontFamily = Serif, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Gold)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Button(
        onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        if (icon != null) { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
fun GoldButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White)) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun BoutiqueCard(modifier: Modifier = Modifier, elevation: Int = 3, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val cardMod = modifier.shadow(elevation.dp, RoundedCornerShape(20.dp), clip = false)
        .clip(RoundedCornerShape(20.dp)).background(Color.White)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Column(modifier = cardMod) { GoldenTopBar(); Column(Modifier.padding(18.dp), content = content) }
}

// (Pie con sello retirado junto con el sello, a pedido del cliente)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PreviewBoutiqueCard() {
    BoutiqueCard { Text("Vista previa Burbuja Glow", fontWeight = FontWeight.Bold) }
}

@Composable
fun StyledField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector, password: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = InkSoft, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray, fontSize = 13.sp) },
            label = { Text(label, fontSize = 11.sp) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(19.dp)) },
            visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = Gold, unfocusedBorderColor = LineSoft, cursorColor = Gold,
                focusedLabelColor = Gold, unfocusedLabelColor = Color.Gray
            )
        )
    }
}
