package com.example.burbujaglow.data

import com.example.burbujaglow.BuildConfig
import com.example.burbujaglow.data.model.UsuarioAdmin
import com.example.burbujaglow.data.remote.aMap
import com.example.burbujaglow.data.remote.aUsuarioAdmin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Datos de la sesión autenticada que la UI necesita para decidir navegación y permisos. */
data class SesionUsuario(
    val uid: String,
    val correo: String,
    val nombre: String,
    val rol: String
)

/** Repositorio de autenticación Firebase: login, registro, recuperación y lectura del perfil. */
class AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val habilitado: Boolean get() = BuildConfig.FIREBASE_CONFIGURED
    val currentUser: FirebaseUser? get() = if (habilitado) auth.currentUser else null

    /** Inicia sesión por correo o alias y devuelve la sesión con el rol desde Firestore. */
    suspend fun login(usuario: String, password: String): Result<SesionUsuario> = runCatching {
        if (!habilitado) return Result.failure(IllegalStateException("Firebase no configurado"))
        val correo = resolverCorreo(usuario)
        auth.signInWithEmailAndPassword(correo, password).await()
        sesionActual().getOrThrow()
    }

    /** Inicia sesión con una credencial biométrica ya liberada por el sistema. */
    suspend fun loginConCredential(credential: AuthCredential): Result<SesionUsuario> = runCatching {
        if (!habilitado) return Result.failure(IllegalStateException("Firebase no configurado"))
        auth.signInWithCredential(credential).await()
        sesionActual().getOrThrow()
    }

    /** Crea la cuenta Firebase y su perfil; el registro público nunca asigna rol ADMIN sin código. */
    suspend fun registrar(
        nombre: String,
        apellido: String,
        correo: String,
        password: String,
        rol: String,
        codigoAdmin: String
    ): Result<SesionUsuario> = runCatching {
        if (!habilitado) return Result.failure(IllegalStateException("Firebase no configurado"))
        val rolFinal = rol.uppercase()
        if (rolFinal == "ADMIN" && codigoAdmin != BuildConfig.ADMIN_INVITE_CODE) {
            throw IllegalArgumentException("Código de administrador incorrecto")
        }
        val rolGuardado = when (rolFinal) {
            "ADMIN" -> "ADMIN"
            "VENDEDOR" -> "VENDEDOR"
            else -> "COMPRADOR"
        }
        val pass = password.ifBlank { "Burbuja123*" }
        val resultado = auth.createUserWithEmailAndPassword(correo.trim(), pass).await()
        val uid = resultado.user?.uid ?: throw IllegalStateException("No se pudo crear la cuenta")
        val perfil = UsuarioAdmin(
            id = uid.hashCode(),
            nombre = "${nombre.trim()} ${apellido.trim()}".trim(),
            correo = correo.trim(),
            telefono = "",
            rol = rolGuardado,
            estado = "Activo",
            uid = uid,
            usuarioAlias = correo.trim().substringBefore("@")
        )
        db.collection("usuarios").document(uid).set(perfil.aMap()).await()
        SesionUsuario(uid, correo.trim(), perfil.nombre, perfil.rol)
    }

    /** Solicita el correo de restablecimiento de contraseña de Firebase. */
    suspend fun recuperar(correo: String): Result<Unit> = runCatching {
        if (!habilitado) return Result.failure(IllegalStateException("Firebase no configurado"))
        auth.sendPasswordResetEmail(correo.trim()).await()
    }

    /** Recupera la sesión activa y completa nombre/rol con el documento `usuarios/{uid}`. */
    suspend fun sesionActual(): Result<SesionUsuario> = runCatching {
        val user = currentUser ?: throw IllegalStateException("No hay sesión activa")
        val snap = db.collection("usuarios").document(user.uid).get().await()
        val perfil = snap.data?.aUsuarioAdmin()
        SesionUsuario(
            uid = user.uid,
            correo = user.email.orEmpty(),
            nombre = perfil?.nombre ?: user.displayName.orEmpty(),
            rol = (perfil?.rol ?: "COMPRADOR").uppercase()
        )
    }

    /** Cierra la sesión de Firebase cuando el repositorio está habilitado. */
    fun logout() {
        if (habilitado) auth.signOut()
    }

    /** Traduce los alias demo a correos configurables; cualquier otro texto se trata como correo directo. */
    private fun resolverCorreo(usuario: String): String {
        val entrada = usuario.trim()
        if (entrada.contains("@")) return entrada
        return when (entrada.lowercase()) {
            "admin" -> BuildConfig.DEMO_ADMIN_EMAIL
            "vendedor" -> BuildConfig.DEMO_VENDOR_EMAIL
            else -> "${entrada.lowercase()}@burbujaglow.com"
        }
    }
}
