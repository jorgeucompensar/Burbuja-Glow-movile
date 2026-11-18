package com.example.burbujaglow.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

data class Ubicacion(val latitud: Double, val longitud: Double)

class LocationRepository(private val context: Context) {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    suspend fun ubicacionActual(): Result<Ubicacion> = runCatching {
        val permisoConcedido =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!permisoConcedido) throw SecurityException("Permiso de ubicación no concedido")
        val location = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            ?: throw IllegalStateException("No se pudo obtener la ubicación")
        Ubicacion(location.latitude, location.longitude)
    }
}
