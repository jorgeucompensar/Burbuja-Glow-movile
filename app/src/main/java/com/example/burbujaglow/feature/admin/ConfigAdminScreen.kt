package com.example.burbujaglow.feature.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.burbujaglow.core.components.*

// Configuración de la tienda (valores por defecto VELVETTE/Burbuja Glow)
@Composable
fun ConfigAdminScreen(viewModel: AdminViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        BoutiqueCard(elevation = 2) {
            Text("Configuración de la tienda", fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Charcoal)
            Spacer(Modifier.height(12.dp))
            StyledField("Nombre de la tienda", state.cfgNombre, { v -> viewModel.onCfgChange { s -> s.copy(cfgNombre = v) } }, "Burbuja Glow", Icons.Filled.Storefront)
            Spacer(Modifier.height(12.dp))
            StyledField("Correo", state.cfgCorreo, { v -> viewModel.onCfgChange { s -> s.copy(cfgCorreo = v) } }, "hola@burbujaglow.com", Icons.Filled.Mail)
            Spacer(Modifier.height(12.dp))
            StyledField("Teléfono", state.cfgTelefono, { v -> viewModel.onCfgChange { s -> s.copy(cfgTelefono = v) } }, "+57 300 123 4567", Icons.Filled.LocalShipping)
            Spacer(Modifier.height(12.dp))
            StyledField("Dirección", state.cfgDireccion, { v -> viewModel.onCfgChange { s -> s.copy(cfgDireccion = v) } }, "Bogotá D.C., Colombia", Icons.Filled.LocalShipping)
            Spacer(Modifier.height(12.dp))
            StyledField("Costo de envío", state.cfgEnvio, { v -> viewModel.onCfgChange { s -> s.copy(cfgEnvio = v) } }, "9900", Icons.Filled.LocalShipping)
            Spacer(Modifier.height(12.dp))
            StyledField("Moneda", state.cfgMoneda, { v -> viewModel.onCfgChange { s -> s.copy(cfgMoneda = v) } }, "COP", Icons.Filled.Payment)
            Spacer(Modifier.height(16.dp))
            PrimaryButton("GUARDAR CONFIGURACIÓN", { Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
        }
    }
}
