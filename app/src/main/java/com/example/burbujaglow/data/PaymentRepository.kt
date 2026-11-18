package com.example.burbujaglow.data

import com.example.burbujaglow.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

data class SolicitudPago(
    val numeroPedido: String,
    val descripcion: String,
    val valor: Int,
    val nombre: String,
    val correo: String
)

class PaymentRepository {
    val configurado: Boolean get() = BuildConfig.EPAYCO_SESSION_URL.isNotBlank() && BuildConfig.EPAYCO_PUBLIC_KEY.isNotBlank()

    suspend fun crearSesion(solicitud: SolicitudPago): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (!configurado) throw IllegalStateException("Configura EPAYCO_SESSION_URL y EPAYCO_PUBLIC_KEY")
            val body = JSONObject().apply {
                put("numeroPedido", solicitud.numeroPedido)
                put("descripcion", solicitud.descripcion)
                put("amount", solicitud.valor)
                put("currency", "COP")
                put("name", solicitud.nombre)
                put("email", solicitud.correo)
                put("publicKey", BuildConfig.EPAYCO_PUBLIC_KEY)
                put("custId", BuildConfig.EPAYCO_CUST_ID)
                put("test", BuildConfig.EPAYCO_TEST_MODE)
            }.toString()

            val conexion = URL(BuildConfig.EPAYCO_SESSION_URL).openConnection() as HttpURLConnection
            conexion.requestMethod = "POST"
            conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conexion.setRequestProperty("Accept", "application/json")
            conexion.doOutput = true
            val out: OutputStream = conexion.outputStream
            out.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val codigo = conexion.responseCode
            val respuesta = conexion.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            conexion.disconnect()
            if (codigo !in 200..299) throw IllegalStateException("ePayco respondió $codigo")
            val json = JSONObject(respuesta)
            val sessionId = json.optString("sessionId", json.optString("id_session", json.optString("session_id")))
            if (sessionId.isBlank()) throw IllegalStateException("ePayco no devolvió sessionId")
            sessionId
        }
    }

    fun htmlCheckout(sessionId: String): String = """
        <!DOCTYPE html><html><head><meta charset="utf-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <script src="https://checkout.epayco.co/checkout-v2.js"></script>
        <style>body{font-family:Arial,sans-serif;padding:18px;color:#1A1A1A}button{background:#8C7355;color:white;border:0;border-radius:24px;padding:14px 18px;font-weight:bold;width:100%}#estado{font-size:13px;margin-top:14px}</style>
        </head><body>
        <h3>Pago seguro ePayco</h3><button id="pagar" onclick="pagar()">PAGAR CON EPAYCO</button><p id="estado">Preparando checkout…</p>
        <script>
        function pagar(){
          var checkout = ePayco.checkout.configure({sessionId: '$sessionId', type: 'onpage', test: ${BuildConfig.EPAYCO_TEST_MODE}});
          checkout.setHooks({
            onCreated: function(){ document.getElementById('estado').textContent='Checkout listo.'; },
            onResponse: function(){ document.getElementById('estado').textContent='Pago respondido.'; AndroidPago.resultado('APROBADO'); },
            onErrors: function(){ document.getElementById('estado').textContent='Error en el pago.'; AndroidPago.resultado('ERROR'); },
            onClosed: function(){ document.getElementById('estado').textContent='Checkout cerrado.'; }
          });
          checkout.open();
        }
        pagar();
        </script></body></html>
    """.trimIndent()
}
