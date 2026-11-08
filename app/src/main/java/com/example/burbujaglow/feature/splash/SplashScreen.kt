package com.example.burbujaglow.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burbujaglow.core.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()
    private val _terminado = MutableStateFlow(false)
    val terminado: StateFlow<Boolean> = _terminado.asStateFlow()

    init {
        viewModelScope.launch {
            repeat(10) { _progreso.value += 0.1f; delay(180) }
            delay(350)
            _terminado.value = true
        }
    }
}

// ── SPLASH SCREEN (logo Burbuja Glow + temática animalista) ────────────────
@Composable
fun SplashScreen(onFinished: () -> Unit, viewModel: SplashViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val progreso by viewModel.progreso.collectAsState()
    val terminado by viewModel.terminado.collectAsState()
    LaunchedEffect(terminado) { if (terminado) onFinished() }
    Column(Modifier.fillMaxSize().background(Ivory)) {
        GoldenTopBar()
        Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(130.dp).clip(CircleShape).background(Color.White).shadow(4.dp, CircleShape), contentAlignment = Alignment.Center) {
                LogoBurbujaGlow(Modifier.size(122.dp).clip(CircleShape))
            }
            Spacer(Modifier.height(18.dp))
            Text("BURBUJA GLOW", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 4.sp)
            Spacer(Modifier.height(8.dp))
            Text("Belleza sin crueldad", fontFamily = Serif, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Charcoal, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text("Cosmética y cuidado capilar", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
            Spacer(Modifier.height(30.dp))
            CircularProgressIndicator(progress = { progreso }, modifier = Modifier.width(120.dp).height(5.dp).clip(RoundedCornerShape(50.dp)), color = Gold, trackColor = LineSoft, strokeWidth = 5.dp)
            Spacer(Modifier.height(14.dp))
            Text("Cargando tu experiencia Burbuja Glow…", fontSize = 11.sp, color = Color.Gray)
            Spacer(Modifier.weight(1f))
            Text("BURBUJA GLOW", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp)
        }
    }
}
