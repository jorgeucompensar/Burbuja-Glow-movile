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
import androidx.compose.material.icons.filled.Verified
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.burbujaglow.core.biometric.BiometricAuth
import com.example.burbujaglow.core.components.*

/** Formulario de registro público; el rol ADMIN se bloquea sin el código de invitación. */
@Composable
fun RegistroCompradorScreen(
    onBack: () -> Unit,
    onRegistered: (String, String, String, String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var enviado by remember { mutableStateOf(false) }
    var rolRegistrado by remember { mutableStateOf("COMPRADOR") }
    var nombreRegistrado by remember { mutableStateOf("") }
    var uidRegistrado by remember { mutableStateOf("") }
    var correoRegistrado by remember { mutableStateOf("") }
    var errorNombre by remember { mutableStateOf(false) }
    var errorApellido by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    var errorPassword by remember { mutableStateOf(false) }
    var errorCodigo by remember { mutableStateOf(false) }
    val roles = listOf("COMPRADOR" to "Comprador", "VENDEDOR" to "Vendedor", "ADMIN" to "Administrador")

    Box(Modifier.fillMaxSize().background(Ivory).verticalScroll(rememberScrollState()).padding(22.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BoutiqueCard(elevation = 4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(RoseLight), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Rose, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Registro Burbuja Glow", fontFamily = Serif, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Charcoal, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Elige tu rol y crea tu cuenta. El registro de administrador requiere código de invitación.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
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
                        Text("Bienvenido/a, $nombreRegistrado. Tu cuenta quedó registrada como ${roles.first { it.first == rolRegistrado }.second}.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton("CONTINUAR", { onRegistered(nombreRegistrado, rolRegistrado, uidRegistrado, correoRegistrado) }, Modifier.fillMaxWidth(), Icons.Filled.ShoppingBag)
                    }
                } else {
                    Text("TIPO DE CUENTA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        roles.forEach { (clave, etiqueta) ->
                            val sel = state.registroRol == clave
                            Box(
                                Modifier.weight(1f).clip(RoundedCornerShape(50.dp))
                                    .background(if (sel) Charcoal else Color.White)
                                    .clickable { viewModel.onRegistroRolChange(clave); errorCodigo = false }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) { Text(etiqueta, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else InkSoft, textAlign = TextAlign.Center) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    StyledField("Nombre", state.registroNombre, { viewModel.onRegistroNombreChange(it); errorNombre = false }, "Ej. María", Icons.Filled.Person)
                    if (errorNombre) { Spacer(Modifier.height(5.dp)); Text("Ingresa tu nombre", fontSize = 11.sp, color = Rose) }
                    Spacer(Modifier.height(14.dp))
                    StyledField("Apellido", state.registroApellido, { viewModel.onRegistroApellidoChange(it); errorApellido = false }, "Ej. García", Icons.Filled.Person)
                    if (errorApellido) { Spacer(Modifier.height(5.dp)); Text("Ingresa tu apellido", fontSize = 11.sp, color = Rose) }
                    Spacer(Modifier.height(14.dp))
                    StyledField("Correo electrónico", state.registroCorreo, { viewModel.onRegistroCorreoChange(it); errorCorreo = false }, "tu@email.com", Icons.Filled.Mail)
                    if (errorCorreo) { Spacer(Modifier.height(5.dp)); Text("Ingresa un correo válido (ej. tu@email.com)", fontSize = 11.sp, color = Rose) }
                    Spacer(Modifier.height(14.dp))
                    StyledField("Contraseña", state.registroPassword, { viewModel.onRegistroPasswordChange(it); errorPassword = false }, "Mínimo 6 caracteres", Icons.Filled.Lock, password = true)
                    if (errorPassword) { Spacer(Modifier.height(5.dp)); Text("La contraseña debe tener al menos 6 caracteres", fontSize = 11.sp, color = Rose) }
                    if (state.registroRol == "ADMIN") {
                        Spacer(Modifier.height(14.dp))
                        StyledField("Código de administrador", state.registroCodigo, { viewModel.onRegistroCodigoChange(it); errorCodigo = false }, "Código de invitación", Icons.Filled.Verified, password = true)
                        if (errorCodigo) { Spacer(Modifier.height(5.dp)); Text("Verifica el código de administrador", fontSize = 11.sp, color = Rose) }
                    }
                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(if (state.cargando) "REGISTRANDO…" else "REGISTRARSE", {
                        errorNombre = state.registroNombre.trim().isBlank()
                        errorApellido = state.registroApellido.trim().isBlank()
                        errorCorreo = !viewModel.correoValido(state.registroCorreo)
                        errorPassword = state.registroPassword.length < 6
                        errorCodigo = state.registroRol == "ADMIN" && state.registroCodigo.isBlank()
                        if (!errorNombre && !errorApellido && !errorCorreo && !errorPassword && !errorCodigo) {
                            viewModel.registrarRemoto { nombre, rol, uid, correo ->
                                if (rol == "ERROR") {
                                    Toast.makeText(context, "No se pudo completar el registro", Toast.LENGTH_SHORT).show()
                                } else {
                                    nombreRegistrado = nombre.ifBlank { state.registroNombre.trim() }
                                    rolRegistrado = rol
                                    uidRegistrado = uid
                                    correoRegistrado = correo
                                    enviado = true
                                    Toast.makeText(context, "¡Registro exitoso!", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Revisa los campos marcados", Toast.LENGTH_SHORT).show()
                        }
                    }, Modifier.fillMaxWidth(), Icons.Filled.PersonAdd, enabled = !state.cargando)
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

/** Solicita el correo de recuperación y muestra confirmación o error según la respuesta Firebase. */
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
                        Text("Hemos enviado instrucciones de restablecimiento a ${state.correoRecuperar}.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
                    }
                } else {
                    StyledField("Correo electrónico", state.correoRecuperar, { viewModel.onCorreoRecuperarChange(it); errorCorreo = false }, "tu@email.com", Icons.Filled.Mail)
                    if (errorCorreo) { Spacer(Modifier.height(6.dp)); Text("Ingresa un correo válido (ej. tu@email.com)", fontSize = 11.sp, color = Rose) }
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(if (state.cargando) "ENVIANDO…" else "ENVIAR INSTRUCCIONES", {
                        if (viewModel.correoValido(state.correoRecuperar)) {
                            viewModel.recuperarRemoto { ok ->
                                if (ok) enviado = true
                                else Toast.makeText(context, "No se pudo enviar el correo", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            errorCorreo = true
                            Toast.makeText(context, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
                        }
                    }, Modifier.fillMaxWidth(), Icons.Filled.Mail, enabled = !state.cargando)
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

@Composable
fun BiometricDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current
    var mensaje by remember { mutableStateOf("Verificando biometría…") }
    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity
        if (activity == null) {
            mensaje = "La biometría requiere una Activity compatible"
            return@LaunchedEffect
        }
        BiometricAuth.autenticar(
            activity = activity,
            onSuccess = onSuccess,
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                onDismiss()
            }
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ingresar con Huella / Biometría", fontFamily = Serif, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(96.dp).clip(CircleShape).background(RoseLight), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold, modifier = Modifier.size(52.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(mensaje, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 16.sp)
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(20.dp)
    )
}

/** Pantalla de login con acceso demo, autenticación remota y opción biométrica. */
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String, String) -> Unit,
    onBackToStore: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onRegister: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarBiometria by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { efecto ->
            if (efecto is AuthUiEffect.Toast) Toast.makeText(context, efecto.mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    fun manejarResultado(resultado: LoginResult) {
        when (resultado) {
            is LoginResult.Admin -> onLoginSuccess("ADMIN", "", "", "")
            is LoginResult.Vendedor -> onLoginSuccess("VENDEDOR", "", "", "")
            is LoginResult.Comprador -> onLoginSuccess("COMPRADOR", "", "", "")
            is LoginResult.Remoto -> onLoginSuccess(
                resultado.sesion.rol.uppercase(),
                resultado.sesion.uid,
                resultado.sesion.correo,
                resultado.sesion.nombre
            )
            is LoginResult.Error -> Toast.makeText(context, resultado.mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    if (mostrarBiometria) {
        BiometricDialog(
            onDismiss = { mostrarBiometria = false },
            onSuccess = {
                mostrarBiometria = false
                viewModel.loginBiometrico(::manejarResultado)
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
                    Text("Accede al panel administrativo, panel vendedor o tienda de BURBUJA GLOW", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(22.dp))
                StyledField("Usuario o correo", state.usuario, viewModel::onUsuarioChange, "admin / vendedor / tu@email.com", Icons.Filled.Person)
                Spacer(Modifier.height(14.dp))
                StyledField("Contraseña", state.password, viewModel::onPasswordChange, "••••••••", Icons.Filled.Lock, password = true)
                Spacer(Modifier.height(22.dp))
                PrimaryButton(if (state.cargando) "INGRESANDO…" else "ENTRAR AL PANEL", {
                    viewModel.loginRemoto(::manejarResultado)
                }, Modifier.fillMaxWidth(), Icons.AutoMirrored.Filled.ArrowBack, enabled = !state.cargando)
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
                GoldButton("REGISTRARSE", onRegister, Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Text("Registro disponible para comprador, vendedor y administrador con código.", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onBackToStore, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Storefront, contentDescription = null, tint = Gold, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Volver a la tienda", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text("Aliases demo: admin / vendedor. Código admin: BURBUJA-ADMIN", fontSize = 9.sp, color = Color.LightGray, textAlign = TextAlign.Center)
            }
        }
    }
}
