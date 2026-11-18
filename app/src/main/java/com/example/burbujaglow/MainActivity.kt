package com.example.burbujaglow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.burbujaglow.core.components.Ivory
import com.example.burbujaglow.navigation.AppNavHost
import com.example.burbujaglow.ui.theme.BurbujaGlowTheme

// Punto de entrada único (MVVM modular): la UI vive en feature/*,
// la navegación en navigation/, los datos en data/ y componentes en core/.
// Roles: Login → Administrador / Vendedor / Comprador (catálogo, carrito, pago).
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BurbujaGlowTheme(darkTheme = false, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize(), color = Ivory) {
                    AppNavHost()
                }
            }
        }
    }
}
