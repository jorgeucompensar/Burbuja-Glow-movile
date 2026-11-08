package com.example.burbujaglow.feature.shop

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.burbujaglow.core.components.*
import com.example.burbujaglow.data.model.totalesCarrito

// Checkout: contacto + envío + pago + resumen, con validaciones VELVETTE
@Composable
fun CheckoutScreen(viewModel: ShopViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val carrito = state.carrito
    val totales = totalesCarrito(carrito)
    val metodos = listOf(
        Triple("Nequi", "Pago inmediato desde tu aplicación Nequi", Icons.Filled.Payment),
        Triple("Daviplata", "Pago inmediato desde Daviplata", Icons.Filled.AccountBalance),
        Triple("Transferencia bancaria", "Datos bancarios enviados a tu correo", Icons.Filled.Verified),
        Triple("Contraentrega", "Pagas en efectivo al recibir", Icons.Filled.LocalShipping)
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("Checkout", fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Charcoal)
        Spacer(Modifier.height(4.dp))
        Text("Compra segura · Envíos a toda Colombia · Cambios sin costo", fontSize = 11.sp, color = Color.Gray)
        Spacer(Modifier.height(14.dp))
        BoutiqueCard(elevation = 2) {
            Text("1 · CONTACTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))
            StyledField("Nombre completo", state.fullName, viewModel::onFullNameChange, "Ej: María Fernanda López", Icons.Filled.Person)
            if (state.erroresCheckout.contains("fullName")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Teléfono", state.phone, viewModel::onPhoneChange, "Ej: 300 123 4567", Icons.Filled.LocalShipping)
            if (state.erroresCheckout.contains("phone")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Correo electrónico", state.email, viewModel::onEmailChange, "tu@email.com", Icons.Filled.Mail)
            if (state.erroresCheckout.contains("email")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
        }
        Spacer(Modifier.height(12.dp))
        BoutiqueCard(elevation = 2) {
            Text("2 · ENVÍO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))
            StyledField("Dirección", state.address, viewModel::onAddressChange, "Calle 12 # 34-56", Icons.Filled.LocalShipping)
            if (state.erroresCheckout.contains("address")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Ciudad", state.city, viewModel::onCityChange, "Ej: Bogotá", Icons.Filled.Storefront)
            if (state.erroresCheckout.contains("city")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Departamento", state.department, viewModel::onDepartmentChange, "Ej: Cundinamarca", Icons.Filled.Storefront)
            if (state.erroresCheckout.contains("department")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Código postal", state.postal, viewModel::onPostalChange, "Ej: 110111", Icons.Filled.LocalShipping)
            if (state.erroresCheckout.contains("postal")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
        }
        Spacer(Modifier.height(12.dp))
        BoutiqueCard(elevation = 2) {
            Text("3 · PAGO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))
            metodos.forEach { (nombre, desc, icono) ->
                val sel = state.metodoPagoSel == nombre
                Box(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp).shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp))
                        .background(if (sel) GoldLight else Color.White).clickable { viewModel.onMetodoPagoChange(nombre) }.padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icono, contentDescription = nombre, tint = if (sel) Gold else Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text(nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal); Text(desc, fontSize = 10.sp, color = Color.Gray) }
                        if (sel) Box(Modifier.size(22.dp).clip(CircleShape).background(Charcoal), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, contentDescription = "Seleccionado", tint = Color.White, modifier = Modifier.size(13.dp)) }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        BoutiqueCard(elevation = 2) {
            Text("RESUMEN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
            carrito.forEach { item -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${item.producto.nombre} ×${item.cantidad}", fontSize = 11.sp, color = InkSoft); Text(fmtCOP(item.producto.precioValor * item.cantidad), fontSize = 11.sp, color = Charcoal) }; Spacer(Modifier.height(3.dp)) }
            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = LineSoft)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Subtotal", fontSize = 12.sp, color = Color.Gray); Text(fmtCOP(totales.subtotal), fontSize = 12.sp, color = Charcoal) }
            if (totales.descuento > 0) { Spacer(Modifier.height(3.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Ahorro por ofertas", fontSize = 12.sp, color = Success); Text(fmtCOP(totales.descuento), fontSize = 12.sp, color = Success) } }
            Spacer(Modifier.height(3.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Envío", fontSize = 12.sp, color = Color.Gray); Text(if (totales.envio == 0) "Gratis" else fmtCOP(totales.envio), fontSize = 12.sp, color = Charcoal) }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal); Text(fmtCOP(totales.total), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gold) }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { if (viewModel.validarCheckout()) viewModel.onConfirmarCompraChange(true) else Toast.makeText(context, "Revisa los campos marcados", Toast.LENGTH_SHORT).show() },
            enabled = carrito.isNotEmpty(), modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Charcoal)
        ) { Text("CONFIRMAR PEDIDO", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp) }
    }
}
