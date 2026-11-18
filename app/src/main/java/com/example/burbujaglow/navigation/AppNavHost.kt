package com.example.burbujaglow.navigation

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.example.burbujaglow.core.components.Ivory
import com.example.burbujaglow.data.SessionStore
import com.example.burbujaglow.feature.admin.AdminScreen
import com.example.burbujaglow.feature.auth.LoginScreen
import com.example.burbujaglow.feature.auth.RecuperarPasswordScreen
import com.example.burbujaglow.feature.auth.RegistroCompradorScreen
import com.example.burbujaglow.feature.shop.CompradorNavegacion
import com.example.burbujaglow.feature.splash.SplashScreen
import com.example.burbujaglow.feature.vendedor.VendedorScreen
import kotlinx.coroutines.launch

// Rutas principales (Navigation Compose)
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"
    const val COMPRADOR = "comprador"
    const val ADMIN = "admin"
    const val VENDEDOR = "vendedor"
}

private fun destinoPorRol(rol: String): String = when (rol.uppercase()) {
    "ADMIN", "ADMINISTRADOR" -> Routes.ADMIN
    "VENDEDOR" -> Routes.VENDEDOR
    else -> Routes.COMPRADOR
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val sessionStore = remember { SessionStore(context) }
    val scope = rememberCoroutineScope()
    var nombreComprador by remember { mutableStateOf("") }
    var correoComprador by remember { mutableStateOf("") }
    var uidComprador by remember { mutableStateOf("") }
    var rolGuardado by remember { mutableStateOf("") }
    fun cerrarSesion() {
        rolGuardado = ""
        uidComprador = ""
        scope.launch { sessionStore.cerrarSesion() }
    }
    LaunchedEffect(Unit) {
        sessionStore.nombreComprador.collect { guardado -> if (guardado.isNotBlank()) nombreComprador = guardado }
    }
    LaunchedEffect(Unit) {
        sessionStore.correoRegistro.collect { guardado -> correoComprador = guardado }
    }
    LaunchedEffect(Unit) {
        sessionStore.uid.collect { guardado -> uidComprador = guardado }
    }
    LaunchedEffect(Unit) {
        sessionStore.rol.collect { guardado -> rolGuardado = guardado }
    }
    Surface(modifier = Modifier, color = Ivory) {
        NavHost(navController = navController, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = {
                    val destino = if (rolGuardado.isNotBlank()) destinoPorRol(rolGuardado) else Routes.LOGIN
                    navController.navigate(destino) { popUpTo(Routes.SPLASH) { inclusive = true } }
                })
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = { rol, uid, correo, nombre ->
                        val rolNormalizado = rol.uppercase()
                        rolGuardado = rolNormalizado
                        uidComprador = uid
                        if (nombre.isNotBlank()) nombreComprador = nombre
                        if (correo.isNotBlank()) correoComprador = correo
                        scope.launch { sessionStore.guardarSesion(nombreComprador, correoComprador, rolNormalizado, uid, correo) }
                        navController.navigate(destinoPorRol(rolNormalizado)) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onBackToStore = { navController.navigate(Routes.COMPRADOR) },
                    onForgotPassword = { navController.navigate(Routes.RECUPERAR) },
                    onRegister = { navController.navigate(Routes.REGISTRO) }
                )
            }
            composable(Routes.REGISTRO) {
                RegistroCompradorScreen(
                    onBack = { navController.popBackStack() },
                    onRegistered = { nombre, rol, uid, correo ->
                        nombreComprador = nombre
                        correoComprador = correo
                        uidComprador = uid
                        rolGuardado = rol.uppercase()
                        scope.launch { sessionStore.guardarSesion(nombre, correo, rol.uppercase(), uid, correo) }
                        navController.navigate(destinoPorRol(rol)) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    }
                )
            }
            composable(Routes.RECUPERAR) {
                RecuperarPasswordScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.COMPRADOR) {
                CompradorNavegacion(
                    onBack = { cerrarSesion(); navController.navigate(Routes.LOGIN) { popUpTo(Routes.COMPRADOR) { inclusive = true } } },
                    nombreComprador = nombreComprador,
                    uidComprador = uidComprador,
                    correoComprador = correoComprador,
                    onRegister = { navController.navigate(Routes.REGISTRO) }
                )
            }
            composable(Routes.ADMIN) {
                AdminScreen(onBack = { cerrarSesion(); navController.navigate(Routes.LOGIN) { popUpTo(Routes.ADMIN) { inclusive = true } } })
            }
            composable(Routes.VENDEDOR) {
                VendedorScreen(
                    onBack = { cerrarSesion(); navController.navigate(Routes.LOGIN) { popUpTo(Routes.VENDEDOR) { inclusive = true } } },
                    uidVendedor = uidComprador
                )
            }
        }
    }
}
