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

// Persistencia básica de sesión/registro con DataStore Preferences
class SessionStore(private val context: Context) {
    companion object {
        private val NOMBRE_COMPRADOR = stringPreferencesKey("nombre_comprador")
        private val CORREO_REGISTRO = stringPreferencesKey("correo_registro")
    }

    val nombreComprador: Flow<String> = context.dataStore.data.map { it[NOMBRE_COMPRADOR] ?: "" }
    val correoRegistro: Flow<String> = context.dataStore.data.map { it[CORREO_REGISTRO] ?: "" }

    suspend fun guardarRegistro(nombre: String, correo: String) {
        context.dataStore.edit { prefs ->
            prefs[NOMBRE_COMPRADOR] = nombre
            prefs[CORREO_REGISTRO] = correo
        }
    }
}
