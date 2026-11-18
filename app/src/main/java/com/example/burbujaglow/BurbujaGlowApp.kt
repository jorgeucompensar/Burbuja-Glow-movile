package com.example.burbujaglow

import android.app.Application
import com.example.burbujaglow.data.remote.FirestoreDataRepository

class BurbujaGlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Repositorios.datos.iniciarSincronizacion()
    }
}

object Repositorios {
    val datos: FirestoreDataRepository = FirestoreDataRepository()
}
