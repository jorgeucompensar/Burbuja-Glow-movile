package com.example.burbujaglow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "burbuja_glow")

/** Persiste sesión y datos de registro con DataStore Preferences para restaurarlos al reiniciar. */
class SessionStore(private val context: Context) {
    companion object {
        private val NOMBRE_COMPRADOR = stringPreferencesKey("nombre_comprador")
        private val CORREO_REGISTRO = stringPreferencesKey("correo_registro")
        private val ROL = stringPreferencesKey("rol")
        private val UID = stringPreferencesKey("uid")
        private val USUARIO = stringPreferencesKey("usuario")
    }

    val nombreComprador: Flow<String> = context.dataStore.data.map { it[NOMBRE_COMPRADOR] ?: "" }
    val correoRegistro: Flow<String> = context.dataStore.data.map { it[CORREO_REGISTRO] ?: "" }
    val rol: Flow<String> = context.dataStore.data.map { it[ROL] ?: "" }
    val uid: Flow<String> = context.dataStore.data.map { it[UID] ?: "" }
    val usuario: Flow<String> = context.dataStore.data.map { it[USUARIO] ?: "" }
    /** Guarda nombre, correo, rol, uid y alias de la sesión activa. */
    suspend fun guardarSesion(nombre: String, correo: String, rol: String, uid: String, usuario: String) {
        context.dataStore.edit { prefs ->
            prefs[NOMBRE_COMPRADOR] = nombre
            prefs[CORREO_REGISTRO] = correo
            prefs[ROL] = rol
            prefs[UID] = uid
            prefs[USUARIO] = usuario
        }
    }

    /** Limpia rol, uid y alias; mantiene nombre/correo para comodidad en el formulario. */
    suspend fun cerrarSesion() {
        context.dataStore.edit { prefs ->
            prefs[ROL] = ""
            prefs[UID] = ""
            prefs[USUARIO] = ""
        }
    }
}
