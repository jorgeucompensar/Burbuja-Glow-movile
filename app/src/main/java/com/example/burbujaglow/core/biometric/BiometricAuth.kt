package com.example.burbujaglow.core.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuth {
    fun disponible(activity: FragmentActivity): Boolean =
        BiometricManager.from(activity).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

    fun autenticar(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!disponible(activity)) {
            onError("Este dispositivo no tiene biometría configurada")
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onError("No se reconoció la biometría")
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Burbuja Glow")
            .setSubtitle("Verifica tu huella o rostro para ingresar")
            .setNegativeButtonText("Cancelar")
            .build()
        prompt.authenticate(info)
    }
}
