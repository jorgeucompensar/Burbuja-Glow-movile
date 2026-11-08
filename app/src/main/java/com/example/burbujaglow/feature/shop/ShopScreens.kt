package com.example.burbujaglow.feature.shop

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.burbujaglow.core.components.*
import com.example.burbujaglow.data.model.ProductoComprador
import com.example.burbujaglow.data.model.totalesCarrito

// Contenedor del comprador: Scaffold + Bottom Navigation + diálogos.
// Cada ruta delega en su pantalla (Home/Catalogo/Detalle/Carrito/Checkout).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompradorNavegacion(
    onBack: () -> Unit,
    nombreComprador: String = "",
    onRegister: () -> Unit = {},
    viewModel: ShopViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ruta = state.ruta
    val carrito = state.carrito
    val totales = totalesCarrito(carrito)
    val totalItems = carrito.sumOf { it.cantidad }
    val total = totales.total

    // Efectos UI (Toast) emitidos por el ViewModel, fuera de la composición
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { efecto ->
            if (efecto is ShopUiEffect.Toast) Toast.makeText(context, efecto.mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    fun addToCart(p: ProductoComprador, qty: Int = 1) {
        val error = viewModel.addToCart(p, qty)
        if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "${p.nombre} añadido al carrito", Toast.LENGTH_SHORT).show()
    }

    if (state.confirmarVaciar) {
        AlertDialog(
            onDismissRequest = { viewModel.onConfirmarVaciarChange(false) },
            title = { Text("Vaciar carrito", fontFamily = Serif, fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres vaciar el carrito? Esta acción eliminará todos los productos.") },
            confirmButton = {
                TextButton(onClick = { viewModel.vaciarCarrito(); viewModel.onConfirmarVaciarChange(false); Toast.makeText(context, "Carrito vaciado", Toast.LENGTH_SHORT).show() }) {
                    Text("Sí, vaciar", color = Rose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.onConfirmarVaciarChange(false) }) { Text("Cancelar") } },
            shape = RoundedCornerShape(20.dp)
        )
    }
    if (state.confirmarCompra) {
        AlertDialog(
            onDismissRequest = { viewModel.onConfirmarCompraChange(false) },
            title = { Text("Confirmar pedido", fontFamily = Serif, fontWeight = FontWeight.Bold) },
            text = { Text("Vas a pagar ${fmtCOP(total)} con ${state.metodoPagoSel}.\nCliente: ${state.fullName}\nEnvío a: ${state.address}, ${state.city}, ${state.department}\n¿Deseas confirmar la orden?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmarCompraChange(false); viewModel.procesarCompra(); Toast.makeText(context, "¡Pedido ${state.numeroPedido} confirmado!", Toast.LENGTH_LONG).show() }) { Text("Confirmar", color = Gold, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { viewModel.onConfirmarCompraChange(false) }) { Text("Cancelar") } },
            shape = RoundedCornerShape(20.dp)
        )
    }
    if (state.mostrarExito) {
        AlertDialog(
            onDismissRequest = { viewModel.onMostrarExitoChange(false); viewModel.navegar(CompradorRoute.Inicio) },
            icon = { Box(Modifier.size(48.dp).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, contentDescription = "Pedido confirmado", tint = Gold, modifier = Modifier.size(26.dp)) } },
            title = { Text("¡Pedido confirmado!", fontFamily = Serif, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.numeroPedido, fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gold)
                    Spacer(Modifier.height(8.dp))
                    Text("Cliente: ${state.fullName}\nCorreo: ${state.email}\nEnvío a: ${state.address}, ${state.city}\nMétodo: ${state.metodoPagoSel}\nTotal: ${fmtCOP(total)}", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 17.sp)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.onMostrarExitoChange(false); viewModel.navegar(CompradorRoute.Inicio) }, shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Charcoal)) {
                    Text("SEGUIR COMPRANDO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        containerColor = Ivory,
        bottomBar = {
            CompradorBottomBar(
                rutaActual = ruta,
                totalItems = totalItems,
                onInicio = { viewModel.navegar(CompradorRoute.Inicio) },
                onTienda = { viewModel.navegar(CompradorRoute.Catalogo) },
                onCarrito = { viewModel.navegar(CompradorRoute.Carrito) },
                onSesion = onBack
            )
        }
    ) { innerPadding ->
    Column(Modifier.fillMaxSize().background(Ivory).padding(innerPadding)) {
        Box(Modifier.fillMaxWidth().height(4.dp).background(Gold))
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                when (ruta) {
                    CompradorRoute.Inicio -> onBack()
                    CompradorRoute.Catalogo -> viewModel.navegar(CompradorRoute.Inicio)
                    is CompradorRoute.Detalle -> viewModel.navegar(CompradorRoute.Catalogo)
                    CompradorRoute.Carrito -> viewModel.navegar(CompradorRoute.Catalogo)
                    CompradorRoute.Pago -> viewModel.navegar(CompradorRoute.Carrito)
                }
            }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Gold) }
            Column(Modifier.weight(1f)) {
                Text("Burbuja Glow", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Gold, letterSpacing = 2.sp)
                Text(
                    when (ruta) {
                        CompradorRoute.Inicio -> "Inicio"
                        is CompradorRoute.Detalle -> "Detalle de Producto"
                        CompradorRoute.Carrito -> "Carrito de Compras ($totalItems)"
                        CompradorRoute.Pago -> "Checkout"
                        else -> "Todos los productos"
                    },
                    fontFamily = Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Charcoal
                )
            }
            Box(Modifier.clip(CircleShape).background(Charcoal).clickable { viewModel.navegar(CompradorRoute.Carrito) }.padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ShoppingBag, contentDescription = "Ver carrito", tint = Color.White, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("$totalItems", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        HorizontalDivider(color = LineSoft)

        when (val r = ruta) {
            CompradorRoute.Inicio -> HomeScreen(viewModel)
            CompradorRoute.Catalogo -> CatalogoScreen(viewModel, nombreComprador) { p -> addToCart(p) }
            is CompradorRoute.Detalle -> DetalleProductoScreen(r.producto, viewModel) { p, qty -> addToCart(p, qty) }
            CompradorRoute.Carrito -> CarritoScreen(viewModel, nombreComprador, onRegister)
            CompradorRoute.Pago -> CheckoutScreen(viewModel)
        }
    }
    }
}
