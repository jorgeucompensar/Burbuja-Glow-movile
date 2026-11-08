package com.example.burbujaglow.navigation

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Rutas principales (Navigation Compose)
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"
    const val COMPRADOR = "comprador"
    const val ADMIN = "admin"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    // Sesión/registro persistidos con DataStore
    val context = LocalContext.current
    val sessionStore = remember { SessionStore(context) }
    var nombreComprador by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        sessionStore.nombreComprador.collect { guardado -> if (guardado.isNotBlank()) nombreComprador = guardado }
    }
    Surface(modifier = Modifier, color = Ivory) {
        NavHost(navController = navController, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } } })
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = { rol ->
                        navController.navigate(if (rol == "ADMIN") Routes.ADMIN else Routes.COMPRADOR) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onBackToStore = { navController.navigate(Routes.COMPRADOR) },
                    onForgotPassword = { navController.navigate(Routes.RECUPERAR) },
                    onRegister = { navController.navigate(Routes.REGISTRO) }
                )
            }
            composable(Routes.REGISTRO) {
                RegistroCompradorScreen(
                    onBack = { navController.popBackStack() },
                    onRegistered = { nombre ->
                        nombreComprador = nombre
                        // Persistir registro (nombre + correo) con DataStore
                        CoroutineScope(Dispatchers.IO).launch {
                            sessionStore.guardarRegistro(nombre, "")
                        }
                        navController.navigate(Routes.COMPRADOR) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    }
                )
            }
            composable(Routes.RECUPERAR) {
                RecuperarPasswordScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.COMPRADOR) {
                CompradorNavegacion(
                    onBack = { navController.navigate(Routes.LOGIN) },
                    nombreComprador = nombreComprador,
                    onRegister = { navController.navigate(Routes.REGISTRO) }
                )
            }
            composable(Routes.ADMIN) {
                AdminScreen(onBack = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.ADMIN) { inclusive = true } } })
            }
        }
    }
}
