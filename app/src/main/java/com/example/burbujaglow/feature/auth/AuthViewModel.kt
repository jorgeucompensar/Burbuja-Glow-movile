package com.example.burbujaglow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burbujaglow.BuildConfig
import com.example.burbujaglow.data.AuthRepository
import com.example.burbujaglow.data.SesionUsuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estado de los formularios de login, registro, recuperación y sesión biométrica. */
data class AuthUiState(
    val usuario: String = "",
    val password: String = "",
    val nombreComprador: String = "",
    val registroNombre: String = "",
    val registroApellido: String = "",
    val registroCorreo: String = "",
    val registroPassword: String = "",
    val registroRol: String = "COMPRADOR",
    val registroCodigo: String = "",
    val correoRecuperar: String = "",
    val biometricoEscaneando: Boolean = false,
    val biometricoReconocido: Boolean = false,
    val cargando: Boolean = false,
    val sesion: SesionUsuario? = null
)

/** Efectos puntuales de autenticación, como mensajes Toast. */
sealed interface AuthUiEffect {
    data class Toast(val mensaje: String) : AuthUiEffect
}

/** Resultado del login: demo por rol, sesión remota o error con mensaje. */
sealed interface LoginResult {
    object Admin : LoginResult
    object Vendedor : LoginResult
    object Comprador : LoginResult
    data class Remoto(val sesion: SesionUsuario) : LoginResult
    data class Error(val mensaje: String) : LoginResult
}

/** Gestiona autenticación remota, modo demo, registro, recuperación y biometría. */
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AuthUiEffect>(extraBufferCapacity = 16)
    val uiEffect: SharedFlow<AuthUiEffect> = _uiEffect.asSharedFlow()

/** Emite un mensaje corto para la UI. */
    fun toast(mensaje: String) { _uiEffect.tryEmit(AuthUiEffect.Toast(mensaje)) }

    /** Resuelve el login demo por alias usando credenciales configuradas localmente, nunca versionadas. */
    fun intentarLogin(): LoginResult {
        val s = _uiState.value
        val u = s.usuario.trim().lowercase()
        return when {
            u == "admin" && s.password == BuildConfig.DEMO_ADMIN_PASSWORD -> LoginResult.Admin
            u == "admin" -> LoginResult.Error("Contraseña incorrecta para el administrador")
            u == "vendedor" && s.password == BuildConfig.DEMO_VENDOR_PASSWORD -> LoginResult.Vendedor
            u == "vendedor" -> LoginResult.Error("Contraseña incorrecta para el vendedor")
            u.isNotBlank() -> LoginResult.Comprador
            u.isBlank() -> LoginResult.Error("Ingresa tu usuario o regístrate como comprador")
            else -> LoginResult.Error("Verifica tu usuario y contraseña")
        }
    }

/** Inicia sesión en Firebase; si no está configurado, delega en el modo demo. */
    fun loginRemoto(onResult: (LoginResult) -> Unit) {
        if (!authRepository.habilitado) {
            onResult(intentarLogin())
            return
        }
        val s = _uiState.value
        if (s.usuario.isBlank() || s.password.isBlank()) {
            onResult(LoginResult.Error("Ingresa usuario y contraseña"))
            return
        }
        _uiState.value = s.copy(cargando = true)
        viewModelScope.launch {
            val respuesta = authRepository.login(s.usuario, s.password)
            val sesion = respuesta.getOrNull()
            val resultado = respuesta.fold(
                onSuccess = { mapSesion(it) },
                onFailure = { LoginResult.Error(it.message ?: "No se pudo iniciar sesión") }
            )
            _uiState.value = _uiState.value.copy(cargando = false, sesion = sesion)
            if (resultado is LoginResult.Error) toast(resultado.mensaje)
            onResult(resultado)
        }
    }

/** Registra un usuario remoto y devuelve nombre, rol, uid y correo para la navegación. */
    fun registrarRemoto(onResult: (String, String, String, String) -> Unit) {
        val s = _uiState.value
        if (!authRepository.habilitado) {
            onResult(s.registroNombre.trim(), "COMPRADOR", "", "")
            return
        }
        if (s.registroNombre.isBlank() || s.registroApellido.isBlank() || !correoValido(s.registroCorreo)) {
            onResult("", "ERROR", "", "")
            return
        }
        _uiState.value = s.copy(cargando = true)
        viewModelScope.launch {
            val resultado = authRepository.registrar(
                s.registroNombre, s.registroApellido, s.registroCorreo, s.registroPassword,
                s.registroRol, s.registroCodigo
            )
            resultado.fold(
                onSuccess = { sesion ->
                    _uiState.value = _uiState.value.copy(cargando = false, sesion = sesion)
                    onResult(sesion.nombre, sesion.rol.uppercase(), sesion.uid, sesion.correo)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(cargando = false)
                    toast(it.message ?: "No se pudo registrar")
                    onResult("", "ERROR", "", "")
                }
            )
        }
    }

/** Solicita el correo de recuperación; valida formato antes de llamar a Firebase. */
    fun recuperarRemoto(onResult: (Boolean) -> Unit) {
        if (!authRepository.habilitado) {
            onResult(true)
            return
        }
        val correo = _uiState.value.correoRecuperar
        if (!correoValido(correo)) {
            onResult(false)
            return
        }
        _uiState.value = _uiState.value.copy(cargando = true)
        viewModelScope.launch {
            val resultado = authRepository.recuperar(correo)
            _uiState.value = _uiState.value.copy(cargando = false)
            resultado.fold(
                onSuccess = { onResult(true) },
                onFailure = {
                    toast(it.message ?: "No se pudo enviar el correo")
                    onResult(false)
                }
            )
        }
    }

/** Restaura la sesión activa y entrega el rol para decidir el destino inicial. */
    fun sesionActual(onResult: (String?) -> Unit) {
        if (!authRepository.habilitado) {
            onResult(null)
            return
        }
        viewModelScope.launch {
            authRepository.sesionActual().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(sesion = it, usuario = it.correo)
                    onResult(it.rol.uppercase())
                },
                onFailure = { onResult(null) }
            )
        }
    }

/** Reutiliza la sesión Firebase ya autenticada tras la validación biométrica del sistema. */
    fun loginBiometrico(onResult: (LoginResult) -> Unit) {
        if (!authRepository.habilitado) {
            onResult(LoginResult.Error("Configura Firebase para usar biometría"))
            return
        }
        viewModelScope.launch {
            val resultado = authRepository.sesionActual().fold(
                onSuccess = { mapSesion(it) },
                onFailure = { LoginResult.Error("Inicia sesión con contraseña una vez para activar la biometría") }
            )
            if (resultado is LoginResult.Error) toast(resultado.mensaje)
            onResult(resultado)
        }
    }

    fun onUsuarioChange(v: String) { _uiState.value = _uiState.value.copy(usuario = v) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v) }
    fun onRegistroNombreChange(v: String) { _uiState.value = _uiState.value.copy(registroNombre = v) }
    fun onRegistroApellidoChange(v: String) { _uiState.value = _uiState.value.copy(registroApellido = v) }
    fun onRegistroCorreoChange(v: String) { _uiState.value = _uiState.value.copy(registroCorreo = v) }
    fun onRegistroPasswordChange(v: String) { _uiState.value = _uiState.value.copy(registroPassword = v) }
    fun onRegistroRolChange(v: String) { _uiState.value = _uiState.value.copy(registroRol = v) }
    fun onRegistroCodigoChange(v: String) { _uiState.value = _uiState.value.copy(registroCodigo = v) }
    fun onCorreoRecuperarChange(v: String) { _uiState.value = _uiState.value.copy(correoRecuperar = v) }
    fun onNombreCompradorChange(v: String) { _uiState.value = _uiState.value.copy(nombreComprador = v) }

/** Validación mínima de correo usada por registro y recuperación. */
    fun correoValido(correo: String): Boolean = correo.contains("@") && correo.contains(".") && correo.length > 5

    /** Convierte una sesión Firebase en resultado remoto para la UI. */
    private fun mapSesion(sesion: SesionUsuario): LoginResult = LoginResult.Remoto(sesion)
}
