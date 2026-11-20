package com.example.burbujaglow

import android.app.Application
import com.example.burbujaglow.data.remote.FirestoreDataRepository

/** Punto de entrada de la app: arranca la sincronización Firebase al crear el proceso. */
class BurbujaGlowApp : Application() {
    /** Inicia los listeners remotos una sola vez durante el ciclo de vida de la aplicación. */
    override fun onCreate() {
        super.onCreate()
        Repositorios.datos.iniciarSincronizacion()
    }
}

/** Service locator que expone instancias únicas de autenticación y datos remotos. */
object Repositorios {
    val auth: com.example.burbujaglow.data.AuthRepository = com.example.burbujaglow.data.AuthRepository()
    val datos: FirestoreDataRepository = FirestoreDataRepository()
}
