package com.example.burbujaglow.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

/** Coordenadas geográficas devueltas por el proveedor de ubicación. */
data class Ubicacion(val latitud: Double, val longitud: Double)

/** Obtiene la ubicación actual comprobando permisos antes de llamar al proveedor. */
class LocationRepository(private val context: Context) {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    /** Indica si el usuario concedió permiso de ubicación fina o aproximada. */
    fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Devuelve la posición actual o un error si no hay permiso o el proveedor no responde. */
    @SuppressLint("MissingPermission")
    suspend fun ubicacionActual(): Result<Ubicacion> = runCatching {
        if (!tienePermiso()) throw SecurityException("Permiso de ubicación no concedido")
        val location = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            ?: throw IllegalStateException("No se pudo obtener la ubicación")
        Ubicacion(location.latitude, location.longitude)
    }
}
