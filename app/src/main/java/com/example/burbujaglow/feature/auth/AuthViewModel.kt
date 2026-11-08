package com.example.burbujaglow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val usuario: String = "",
    val password: String = "",
    val nombreComprador: String = "",
    val registroNombre: String = "",
    val registroApellido: String = "",
    val registroCorreo: String = "",
    val correoRecuperar: String = "",
    val biometricoEscaneando: Boolean = false,
    val biometricoReconocido: Boolean = false
)

sealed interface AuthUiEffect {
    data class Toast(val mensaje: String) : AuthUiEffect
}

sealed interface LoginResult {
    object Admin : LoginResult
    object Comprador : LoginResult
    data class Error(val mensaje: String) : LoginResult
}

// ViewModel de autenticación: concentra credenciales y registro (antes en Composables)
class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AuthUiEffect>(extraBufferCapacity = 16)
    val uiEffect: SharedFlow<AuthUiEffect> = _uiEffect.asSharedFlow()

    fun toast(mensaje: String) { _uiEffect.tryEmit(AuthUiEffect.Toast(mensaje)) }

    // Escaneo biométrico simulado fuera de la composición (antes: LaunchedEffect en el diálogo)
    fun iniciarEscaneoBiometrico() {
        if (_uiState.value.biometricoEscaneando || _uiState.value.biometricoReconocido) return
        _uiState.value = _uiState.value.copy(biometricoEscaneando = true)
        viewModelScope.launch {
            delay(1400)
            _uiState.value = _uiState.value.copy(biometricoEscaneando = false, biometricoReconocido = true)
        }
    }

    fun resetBiometrico() { _uiState.value = _uiState.value.copy(biometricoEscaneando = false, biometricoReconocido = false) }

    // Administrador SOLO con usuario "admin" y contraseña "admin12345"
    fun intentarLogin(): LoginResult {
        val s = _uiState.value
        val u = s.usuario.trim().lowercase()
        return when {
            u == "admin" && s.password == "admin12345" -> LoginResult.Admin
            u == "admin" -> LoginResult.Error("Contraseña incorrecta para el administrador")
            u.isNotBlank() -> LoginResult.Comprador
            else -> LoginResult.Error("Ingresa tu usuario o regístrate como comprador")
        }
    }

    fun onUsuarioChange(v: String) { _uiState.value = _uiState.value.copy(usuario = v) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v) }
    fun onRegistroNombreChange(v: String) { _uiState.value = _uiState.value.copy(registroNombre = v) }
    fun onRegistroApellidoChange(v: String) { _uiState.value = _uiState.value.copy(registroApellido = v) }
    fun onRegistroCorreoChange(v: String) { _uiState.value = _uiState.value.copy(registroCorreo = v) }
    fun onCorreoRecuperarChange(v: String) { _uiState.value = _uiState.value.copy(correoRecuperar = v) }
    fun onNombreCompradorChange(v: String) { _uiState.value = _uiState.value.copy(nombreComprador = v) }

    fun correoValido(correo: String): Boolean = correo.contains("@") && correo.contains(".") && correo.length > 5
}
