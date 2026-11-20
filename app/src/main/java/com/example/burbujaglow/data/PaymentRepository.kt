package com.example.burbujaglow.data

import com.example.burbujaglow.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Datos de facturación y entrega enviados al backend de pagos. */
data class ClientePago(
    val nombre: String,
    val email: String,
    val telefono: String,
    val direccion: String,
    val ciudad: String,
    val departamento: String,
    val codigoPostal: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)

/** Ítem mínimo enviado al backend para validar producto y cantidad antes de crear la sesión. */
data class ItemPago(
    val productoId: String,
    val cantidad: Int
)

/** Solicitud completa de checkout: pedido, comprador, ítems y cliente. */
data class SolicitudPago(
    val pedidoId: String,
    val compradorUid: String,
    val items: List<ItemPago>,
    val cliente: ClientePago
)

/** Estado devuelto por el backend para que la app decida si confirma o muestra error. */
data class EstadoPago(
    val numero: String,
    val estado: String,
    val estadoPago: String,
    val respuesta: String
)

/** Cliente HTTP del backend de pagos y generador del HTML del checkout ePayco. */
class PaymentRepository {
    val configurado: Boolean get() = BuildConfig.EPAYCO_SESSION_URL.isNotBlank()

/** Normaliza la URL configurada quitando sufijos y barras finales para armar endpoints. */
    private fun baseUrl(): String {
        val raw = BuildConfig.EPAYCO_SESSION_URL.trim().trimEnd('/')
        return if (raw.endsWith("/api/payments/session")) raw.removeSuffix("/api/payments/session") else raw
    }

/** Construye la URL absoluta de un endpoint del backend. */
    private fun endpoint(path: String): String = baseUrl() + path

/** Ejecuta la petición con timeouts y convierte respuestas no 2xx en errores legibles. */
    private fun llamada(method: String, url: String, body: String? = null): String {
        val conexion = URL(url).openConnection() as HttpURLConnection
        try {
            conexion.requestMethod = method
            conexion.connectTimeout = 15000
            conexion.readTimeout = 20000
            conexion.setRequestProperty("Accept", "application/json")
            if (body != null) {
                conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conexion.doOutput = true
                val out: OutputStream = conexion.outputStream
                out.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val codigo = conexion.responseCode
            val flujo = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
            val respuesta = flujo?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (codigo !in 200..299) throw IllegalStateException("Servidor de pagos respondió $codigo: ${respuesta.take(180)}")
            return respuesta
        } finally {
            conexion.disconnect()
        }
    }

/** Serializa la solicitud, llama a `/api/payments/session` y devuelve el `sessionId` de ePayco. */
    suspend fun crearSesion(solicitud: SolicitudPago): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (!configurado) throw IllegalStateException("Configura EPAYCO_SESSION_URL en local.properties")
            val items = JSONArray().apply {
                solicitud.items.forEach { item ->
                    put(JSONObject().apply {
                        put("productoId", item.productoId)
                        put("cantidad", item.cantidad)
                    })
                }
            }
            val cliente = JSONObject().apply {
                put("nombre", solicitud.cliente.nombre)
                put("email", solicitud.cliente.email)
                put("telefono", solicitud.cliente.telefono)
                put("direccion", solicitud.cliente.direccion)
                put("ciudad", solicitud.cliente.ciudad)
                put("departamento", solicitud.cliente.departamento)
                put("codigoPostal", solicitud.cliente.codigoPostal)
                put("latitud", solicitud.cliente.latitud)
                put("longitud", solicitud.cliente.longitud)
            }
            val body = JSONObject().apply {
                put("pedidoId", solicitud.pedidoId)
                put("compradorUid", solicitud.compradorUid)
                put("items", items)
                put("cliente", cliente)
            }.toString()
            val json = JSONObject(llamada("POST", endpoint("/api/payments/session"), body))
            val sessionId = json.optString("sessionId", json.optString("id_session", json.optString("session_id")))
            if (sessionId.isBlank()) throw IllegalStateException("El servidor no devolvió sessionId")
            sessionId
        }
    }

/** Consulta por `pedidoId` el estado del pago para el polling del checkout. */
    suspend fun verificar(pedidoId: String): Result<EstadoPago> = withContext(Dispatchers.IO) {
        runCatching {
            val id = URLEncoder.encode(pedidoId, "UTF-8")
            val json = JSONObject(llamada("GET", endpoint("/api/payments/verify?pedidoId=$id")))
            EstadoPago(
                numero = json.optString("numero", pedidoId),
                estado = json.optString("estado"),
                estadoPago = json.optString("estadoPago", "PENDIENTE"),
                respuesta = json.optString("respuesta")
            )
        }
    }

/** Genera el HTML del checkout onpage; el botón llama a `AndroidPago` con el resultado. */
    fun htmlCheckout(sessionId: String): String = """
        <!DOCTYPE html><html><head><meta charset="utf-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <script src="https://checkout.epayco.co/checkout-v2.js"></script>
        <style>body{font-family:Arial,sans-serif;padding:18px;color:#1A1A1A}button{background:#8C7355;color:white;border:0;border-radius:24px;padding:14px 18px;font-weight:bold;width:100%}#estado{font-size:13px;margin-top:14px}</style>
        </head><body>
        <button id="pagar" onclick="pagar()">PAGAR CON EPAYCO</button>
        <p id="estado">Toca el botón para abrir el checkout seguro.</p>
        <script>
        function pagar(){
          document.getElementById('pagar').disabled = true;
          document.getElementById('estado').textContent = 'Preparando checkout…';
          if (typeof ePayco === 'undefined' || !ePayco.checkout) {
            document.getElementById('estado').textContent = 'No se pudo cargar ePayco.';
            AndroidPago.resultado('ERROR');
            return;
          }
          var checkout = ePayco.checkout.configure({sessionId: '$sessionId', type: 'onpage', test: ${BuildConfig.EPAYCO_TEST_MODE}});
          checkout.setHooks({
            onCreated: function(){ document.getElementById('estado').textContent='Checkout listo.'; },
            onResponse: function(data){ document.getElementById('estado').textContent='Pago respondido. Verificando…'; AndroidPago.resultado('RESPONDIDO'); },
            onErrors: function(){ document.getElementById('estado').textContent='Error en el pago.'; AndroidPago.resultado('ERROR'); },
            onClosed: function(){ document.getElementById('estado').textContent='Checkout cerrado.'; AndroidPago.resultado('CERRADO'); }
          });
          checkout.open();
        }
        </script></body></html>
    """.trimIndent()
}
