package com.example.burbujaglow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "burbuja_glow")

// Persistencia básica de sesión/registro con DataStore Preferences
class SessionStore(private val context: Context) {
    companion object {
        private val NOMBRE_COMPRADOR = stringPreferencesKey("nombre_comprador")
        private val CORREO_REGISTRO = stringPreferencesKey("correo_registro")
        private val ROL = stringPreferencesKey("rol")
        private val UID = stringPreferencesKey("uid")
        private val USUARIO = stringPreferencesKey("usuario")
        private val BIOMETRIA = booleanPreferencesKey("biometria_activa")
    }

    val nombreComprador: Flow<String> = context.dataStore.data.map { it[NOMBRE_COMPRADOR] ?: "" }
    val correoRegistro: Flow<String> = context.dataStore.data.map { it[CORREO_REGISTRO] ?: "" }
    val rol: Flow<String> = context.dataStore.data.map { it[ROL] ?: "" }
    val uid: Flow<String> = context.dataStore.data.map { it[UID] ?: "" }
    val usuario: Flow<String> = context.dataStore.data.map { it[USUARIO] ?: "" }
    val biometriaActiva: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRIA] ?: false }

    suspend fun guardarRegistro(nombre: String, correo: String) {
        context.dataStore.edit { prefs ->
            prefs[NOMBRE_COMPRADOR] = nombre
            prefs[CORREO_REGISTRO] = correo
        }
    }

    suspend fun guardarSesion(nombre: String, correo: String, rol: String, uid: String, usuario: String) {
        context.dataStore.edit { prefs ->
            prefs[NOMBRE_COMPRADOR] = nombre
            prefs[CORREO_REGISTRO] = correo
            prefs[ROL] = rol
            prefs[UID] = uid
            prefs[USUARIO] = usuario
        }
    }

    suspend fun activarBiometria(activa: Boolean) {
        context.dataStore.edit { it[BIOMETRIA] = activa }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { prefs ->
            prefs[ROL] = ""
            prefs[UID] = ""
            prefs[USUARIO] = ""
        }
    }
}
