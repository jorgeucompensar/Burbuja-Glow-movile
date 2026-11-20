package com.example.burbujaglow.feature.shop

import android.Manifest
import android.annotation.SuppressLint
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.burbujaglow.core.components.*
import com.example.burbujaglow.data.LocationRepository
import com.example.burbujaglow.data.model.totalesCarrito
import kotlinx.coroutines.launch

/** Puente JS→Kotlin que recibe el estado del checkout ePayco dentro del WebView. */
private class PagoBridge(private val onResultado: (String) -> Unit) {
    /** Método invocado por el script del checkout; reenvía el estado al ViewModel. */
    @JavascriptInterface
    fun resultado(estado: String) {
        onResultado(estado)
    }
}

/** Formulario de checkout y diálogo WebView con el pago seguro de ePayco. */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(viewModel: ShopViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationRepository = remember { LocationRepository(context) }
    val permisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (!concedido) Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val carrito = state.carrito
    val totales = totalesCarrito(carrito)
    val metodos = listOf(
        Triple("ePayco", "Tarjeta, PSE, Nequi y más · checkout seguro ePayco", Icons.Filled.Payment),
        Triple("Contraentrega", "Pagas en efectivo al recibir", Icons.Filled.LocalShipping)
    )

    // Cuando hay sesión de pago se muestra el WebView; al cerrarse se destruye para liberar memoria.
    if (state.sesionPago.isNotBlank()) {
        Dialog(onDismissRequest = viewModel::cerrarPago) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
                Column(Modifier.padding(12.dp)) {
                    Text("Pago ePayco", fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Charcoal)
                    Spacer(Modifier.height(8.dp))
                    var webView by remember { mutableStateOf<WebView?>(null) }
                    DisposableEffect(webView) {
                        onDispose { webView?.destroy() }
                    }
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().height(460.dp),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webView = this
                                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                settings.setSupportMultipleWindows(true)
                                webViewClient = object : WebViewClient() {
                                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                        Log.e("BurbujaPago", "WebView error ${error?.errorCode}: ${error?.description} ${request?.url}")
                                    }
                                }
                                webChromeClient = object : WebChromeClient() {
                                    override fun onCreateWindow(
                                        view: WebView?,
                                        isDialog: Boolean,
                                        isUserGesture: Boolean,
                                        resultMsg: Message?
                                    ): Boolean {
                                        val popup = WebView(ctx)
                                        popup.settings.javaScriptEnabled = true
                                        popup.settings.domStorageEnabled = true
                                        popup.webViewClient = WebViewClient()
                                        popup.webChromeClient = object : WebChromeClient() {
                                            override fun onCloseWindow(window: WebView?) {
                                                (popup.parent as? android.view.ViewGroup)?.removeView(popup)
                                                popup.destroy()
                                            }
                                        }
                                        view?.addView(popup)
                                        resultMsg?.let { msg ->
                                            (msg.obj as? WebView.WebViewTransport)?.webView = popup
                                            msg.sendToTarget()
                                        }
                                        return true
                                    }
                                }
                                addJavascriptInterface(
                                    PagoBridge { resultado ->
                                        post {
                                            viewModel.pagoResultado(resultado)
                                            viewModel.cerrarPago()
                                        }
                                    },
                                    "AndroidPago"
                                )
                                loadDataWithBaseURL("https://checkout.epayco.co", viewModel.htmlPago(state.sesionPago), "text/html", "UTF-8", null)
                            }
                        }
                    )
                    TextButton(onClick = viewModel::cerrarPago, modifier = Modifier.fillMaxWidth()) {
                        Text("Cerrar", color = Rose, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("Checkout", fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Charcoal)
        Spacer(Modifier.height(4.dp))
        Text("Compra segura · Envíos a toda Colombia · Cambios sin costo", fontSize = 11.sp, color = Color.Gray)
        Spacer(Modifier.height(14.dp))
        BoutiqueCard(elevation = 2) {
            Text("1 · CONTACTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))
            StyledField("Nombre completo", state.fullName, viewModel::onFullNameChange, "Ej: María Fernanda López", Icons.Filled.Person)
            if (state.erroresCheckout.contains("fullName")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Teléfono", state.phone, viewModel::onPhoneChange, "Ej: 300 123 4567", Icons.Filled.LocalShipping)
            if (state.erroresCheckout.contains("phone")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Correo electrónico", state.email, viewModel::onEmailChange, "tu@email.com", Icons.Filled.Mail)
            if (state.erroresCheckout.contains("email")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
        }
        Spacer(Modifier.height(12.dp))
        BoutiqueCard(elevation = 2) {
            Text("2 · ENVÍO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))
            StyledField("Dirección", state.address, viewModel::onAddressChange, "Calle 12 # 34-56", Icons.Filled.LocalShipping)
            if (state.erroresCheckout.contains("address")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Ciudad", state.city, viewModel::onCityChange, "Ej: Bogotá", Icons.Filled.Storefront)
            if (state.erroresCheckout.contains("city")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Departamento", state.department, viewModel::onDepartmentChange, "Ej: Cundinamarca", Icons.Filled.Storefront)
            if (state.erroresCheckout.contains("department")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            StyledField("Código postal", state.postal, viewModel::onPostalChange, "Ej: 110111", Icons.Filled.LocalShipping)
            if (state.erroresCheckout.contains("postal")) { Spacer(Modifier.height(4.dp)); Text("Revisa este campo", fontSize = 10.sp, color = Danger) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    if (locationRepository.tienePermiso()) {
                        scope.launch {
                            locationRepository.ubicacionActual().fold(
                                onSuccess = { ubicacion ->
                                    viewModel.onUbicacionChange(ubicacion.latitud, ubicacion.longitud)
                                    Toast.makeText(context, "Ubicación adjuntada al pedido", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { Toast.makeText(context, it.message ?: "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    } else {
                        permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (state.latitud != 0.0) "Ubicación adjuntada ✓" else "Adjuntar mi ubicación", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        BoutiqueCard(elevation = 2) {
            Text("3 · PAGO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))
            metodos.forEach { (nombre, desc, icono) ->
                val sel = state.metodoPagoSel == nombre
                Box(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp).shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp))
                        .background(if (sel) GoldLight else Color.White).clickable { viewModel.onMetodoPagoChange(nombre) }.padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icono, contentDescription = nombre, tint = if (sel) Gold else Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text(nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal); Text(desc, fontSize = 10.sp, color = Color.Gray) }
                        if (sel) Box(Modifier.size(22.dp).clip(CircleShape).background(Charcoal), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, contentDescription = "Seleccionado", tint = Color.White, modifier = Modifier.size(13.dp)) }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        BoutiqueCard(elevation = 2) {
            Text("RESUMEN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
            carrito.forEach { item -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${item.producto.nombre} ×${item.cantidad}", fontSize = 11.sp, color = InkSoft); Text(fmtCOP(item.producto.precioValor * item.cantidad), fontSize = 11.sp, color = Charcoal) }; Spacer(Modifier.height(3.dp)) }
            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = LineSoft)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Subtotal", fontSize = 12.sp, color = Color.Gray); Text(fmtCOP(totales.subtotal), fontSize = 12.sp, color = Charcoal) }
            if (totales.descuento > 0) { Spacer(Modifier.height(3.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Ahorro por ofertas", fontSize = 12.sp, color = Success); Text(fmtCOP(totales.descuento), fontSize = 12.sp, color = Success) } }
            Spacer(Modifier.height(3.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Envío", fontSize = 12.sp, color = Color.Gray); Text(if (totales.envio == 0) "Gratis" else fmtCOP(totales.envio), fontSize = 12.sp, color = Charcoal) }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal); Text(fmtCOP(totales.total), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gold) }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (!viewModel.validarCheckout()) {
                    Toast.makeText(context, "Revisa los campos marcados", Toast.LENGTH_SHORT).show()
                } else if (state.metodoPagoSel == "ePayco") {
                    viewModel.prepararPago(
                        onListo = {},
                        onError = { error -> Toast.makeText(context, error, Toast.LENGTH_LONG).show() }
                    )
                } else {
                    viewModel.onConfirmarCompraChange(true)
                }
            },
            enabled = carrito.isNotEmpty() && !state.pagando && !state.verificandoPago,
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Charcoal)
        ) { Text(
            if (state.verificandoPago) "VERIFICANDO PAGO…" else if (state.pagando) "PREPARANDO PAGO…" else "CONFIRMAR PEDIDO",
            fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp
        ) }
    }
}
