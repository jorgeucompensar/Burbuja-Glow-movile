package com.example.burbujaglow.feature.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.burbujaglow.core.components.*
import kotlinx.coroutines.delay

// ── REGISTRO DE COMPRADOR (antes de comprar) ───────────────────────────────
@Composable
fun RegistroCompradorScreen(
    onBack: () -> Unit,
    onRegistered: (String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var enviado by remember { mutableStateOf(false) }
    var errorNombre by remember { mutableStateOf(false) }
    var errorApellido by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Ivory).verticalScroll(rememberScrollState()).padding(22.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BoutiqueCard(elevation = 4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(RoseLight), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Rose, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Registro de Comprador", fontFamily = Serif, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Charcoal, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Regístrate antes de comprar en BURBUJA GLOW. Solo necesitamos tu nombre, apellido y correo.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(20.dp))
                if (enviado) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(56.dp).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("¡Registro completado!", fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Charcoal, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(5.dp))
                        Text("Bienvenido/a, ${state.registroNombre} ${state.registroApellido}. Ya puedes comprar con tu cuenta en ${state.registroCorreo}.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton("IR A COMPRAR", { onRegistered(state.registroNombre.trim()) }, Modifier.fillMaxWidth(), Icons.Filled.ShoppingBag)
                    }
                } else {
                    StyledField("Nombre", state.registroNombre, { viewModel.onRegistroNombreChange(it); errorNombre = false }, "Ej. María", Icons.Filled.Person)
                    if (errorNombre) { Spacer(Modifier.height(5.dp)); Text("Ingresa tu nombre", fontSize = 11.sp, color = Rose) }
                    Spacer(Modifier.height(14.dp))
                    StyledField("Apellido", state.registroApellido, { viewModel.onRegistroApellidoChange(it); errorApellido = false }, "Ej. García", Icons.Filled.Person)
                    if (errorApellido) { Spacer(Modifier.height(5.dp)); Text("Ingresa tu apellido", fontSize = 11.sp, color = Rose) }
                    Spacer(Modifier.height(14.dp))
                    StyledField("Correo electrónico", state.registroCorreo, { viewModel.onRegistroCorreoChange(it); errorCorreo = false }, "tu@email.com", Icons.Filled.Mail)
                    if (errorCorreo) { Spacer(Modifier.height(5.dp)); Text("Ingresa un correo válido (ej. tu@email.com)", fontSize = 11.sp, color = Rose) }
                    Spacer(Modifier.height(20.dp))
                    PrimaryButton("REGISTRARSE", {
                        errorNombre = state.registroNombre.trim().isBlank()
                        errorApellido = state.registroApellido.trim().isBlank()
                        errorCorreo = !viewModel.correoValido(state.registroCorreo)
                        if (!errorNombre && !errorApellido && !errorCorreo) {
                            enviado = true
                            Toast.makeText(context, "¡Registro exitoso! Bienvenido/a, ${state.registroNombre.trim()} ${state.registroApellido.trim()}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Completa nombre, apellido y un correo válido", Toast.LENGTH_SHORT).show()
                        }
                    }, Modifier.fillMaxWidth(), Icons.Filled.PersonAdd)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp)); Text("Volver a iniciar sesión", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// ── RECUPERACIÓN DE CONTRASEÑA ─────────────────────────────────────────────
@Composable
fun RecuperarPasswordScreen(onBack: () -> Unit, viewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var enviado by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Ivory).verticalScroll(rememberScrollState()).padding(22.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BoutiqueCard(elevation = 4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Mail, contentDescription = null, tint = Gold, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Recuperar contraseña", fontFamily = Serif, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Charcoal, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Ingresa tu correo y te enviaremos instrucciones de restablecimiento.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(20.dp))
                if (enviado) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(56.dp).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Revisa tu correo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Charcoal)
                        Spacer(Modifier.height(5.dp))
                        Text("Hemos enviado instrucciones de restablecimiento a ${state.correoRecuperar}. Sigue el enlace del correo para crear una contraseña nueva.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
                    }
                } else {
                    StyledField("Correo electrónico", state.correoRecuperar, { viewModel.onCorreoRecuperarChange(it); errorCorreo = false }, "tu@email.com", Icons.Filled.Mail)
                    if (errorCorreo) { Spacer(Modifier.height(6.dp)); Text("Ingresa un correo válido (ej. tu@email.com)", fontSize = 11.sp, color = Rose) }
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton("ENVIAR INSTRUCCIONES", {
                        if (viewModel.correoValido(state.correoRecuperar)) {
                            enviado = true
                            Toast.makeText(context, "Instrucciones enviadas a ${state.correoRecuperar}", Toast.LENGTH_LONG).show()
                        } else {
                            errorCorreo = true
                            Toast.makeText(context, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
                        }
                    }, Modifier.fillMaxWidth(), Icons.Filled.Mail)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp)); Text("Volver a iniciar sesión", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// ── AUTENTICACIÓN CON BIOMETRÍA (simulada, conservada) ────────────────────
@Composable
fun BiometricDialog(onDismiss: () -> Unit, onSuccess: () -> Unit, viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val escaneando = state.biometricoEscaneando
    val reconocido = state.biometricoReconocido
    AlertDialog(
        onDismissRequest = { if (!escaneando) { viewModel.resetBiometrico(); onDismiss() } },
        title = { Text("Ingresar con Huella / Biometría", fontFamily = Serif, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    Modifier.size(96.dp).clip(CircleShape).background(if (reconocido) GoldLight else RoseLight)
                        .clickable(enabled = !escaneando && !reconocido) { viewModel.iniciarEscaneoBiometrico() },
                    contentAlignment = Alignment.Center
                ) {
                    if (escaneando && !reconocido) {
                        CircularProgressIndicator(color = Gold, modifier = Modifier.size(52.dp))
                    } else if (reconocido) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(44.dp))
                    } else {
                        Icon(Icons.Filled.Fingerprint, contentDescription = "Huella dactilar", tint = Rose, modifier = Modifier.size(52.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    when {
                        reconocido -> "Biometría reconocida. Acceso concedido."
                        escaneando -> "Verificando huella / FaceID…"
                        else -> "Toca la huella para simular el escaneo con tu huella dactilar o FaceID."
                    },
                    fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            if (!escaneando && !reconocido) {
                Button(onClick = { viewModel.iniciarEscaneoBiometrico() }, shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Charcoal)) {
                    Text("ESCANEAR HUELLA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            } else if (escaneando && !reconocido) {
                TextButton(onClick = {}) { Text("Escaneando…", color = Gold) }
            } else {
                Button(onClick = { viewModel.resetBiometrico(); onSuccess() }, shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Charcoal)) {
                    Text("CONTINUAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !escaneando) { Text("Cancelar") } },
        shape = RoundedCornerShape(20.dp)
    )
}

// ── LOGIN ───────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onBackToStore: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onRegister: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { efecto ->
            if (efecto is AuthUiEffect.Toast) Toast.makeText(context, efecto.mensaje, Toast.LENGTH_SHORT).show()
        }
    }
    var mostrarBiometria by remember { mutableStateOf(false) }
    if (mostrarBiometria) {
        BiometricDialog(
            onDismiss = { mostrarBiometria = false },
            onSuccess = {
                mostrarBiometria = false
                Toast.makeText(context, "Biometría reconocida. ¡Bienvenido/a a Burbuja Glow!", Toast.LENGTH_LONG).show()
                if (state.usuario.trim().lowercase() == "admin" && state.password == "admin12345") onLoginSuccess("ADMIN")
                else if (state.usuario.trim().lowercase() == "admin") Toast.makeText(context, "Para el panel admin ingresa también tu contraseña", Toast.LENGTH_SHORT).show()
                else onLoginSuccess("COMPRADOR")
            }
        )
    }
    Box(Modifier.fillMaxSize().background(Ivory).verticalScroll(rememberScrollState()).padding(22.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BoutiqueCard(elevation = 4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(110.dp).clip(CircleShape).background(Color.White).shadow(3.dp, CircleShape), contentAlignment = Alignment.Center) {
                        LogoBurbujaGlow(Modifier.size(102.dp).clip(CircleShape))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("BURBUJA GLOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 3.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Iniciar sesión", fontFamily = Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Charcoal, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Accede al panel administrativo o tienda de BURBUJA GLOW", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(22.dp))
                StyledField("Usuario", state.usuario, viewModel::onUsuarioChange, "admin / tu usuario", Icons.Filled.Person)
                Spacer(Modifier.height(14.dp))
                StyledField("Contraseña", state.password, viewModel::onPasswordChange, "••••••••", Icons.Filled.Lock, password = true)
                Spacer(Modifier.height(22.dp))
                PrimaryButton("ENTRAR AL PANEL", {
                    when (val r = viewModel.intentarLogin()) {
                        is LoginResult.Admin -> onLoginSuccess("ADMIN")
                        is LoginResult.Comprador -> onLoginSuccess("COMPRADOR")
                        is LoginResult.Error -> Toast.makeText(context, r.mensaje, Toast.LENGTH_SHORT).show()
                    }
                }, Modifier.fillMaxWidth(), Icons.AutoMirrored.Filled.ArrowBack)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onForgotPassword) { Text("¿Olvidaste tu contraseña?", color = Rose, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    Text("Acceso seguro", fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { mostrarBiometria = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(1.5.dp, Gold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold)
                ) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ingresar con Huella / Biometría", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
                GoldButton("REGISTRARSE COMO COMPRADOR", onRegister, Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Text("¿Aún no tienes cuenta? Regístrate con nombre, apellido y correo antes de comprar.", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onBackToStore, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Storefront, contentDescription = null, tint = Gold, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Volver a la tienda", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
