package com.example.burbujaglow.data.remote

import android.util.Log
import com.example.burbujaglow.BuildConfig
import com.example.burbujaglow.data.burbujaRepository
import com.example.burbujaglow.data.catalogoBurbujaGlow
import com.example.burbujaglow.data.model.ItemCarrito
import com.example.burbujaglow.data.model.ItemPedido
import com.example.burbujaglow.data.model.PedidoAdmin
import com.example.burbujaglow.data.model.ProductoAdmin
import com.example.burbujaglow.data.model.ProductoComprador
import com.example.burbujaglow.data.model.UsuarioAdmin
import com.example.burbujaglow.data.productoDesdeAdmin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreDataRepository"

/** Repositorio remoto que sincroniza Firestore con el repositorio en memoria según el rol del usuario. */
class FirestoreDataRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val habilitado: Boolean get() = BuildConfig.FIREBASE_CONFIGURED

    private var iniciado = false
    private var productoListener: ListenerRegistration? = null
    private var pedidoListener: ListenerRegistration? = null
    private var usuarioListener: ListenerRegistration? = null
    private var rolListener: ListenerRegistration? = null
    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var syncedUid = ""
    private var syncedRol = ""

    /** Inicia listeners de catálogo y rol; los datos protegidos se activan cuando hay sesión autenticada. */
    fun iniciarSincronizacion() {
        if (!habilitado || iniciado) return
        iniciado = true
        productoListener = db.collection("productos").addSnapshotListener { snap, error ->
            if (error != null) {
                Log.e(TAG, "Error al escuchar productos", error)
                return@addSnapshotListener
            }
            if (snap == null) return@addSnapshotListener
            val remotos = snap.documents.mapNotNull { it.data?.aProducto() }
            if (remotos.isNotEmpty() || burbujaRepository.productos.isEmpty()) {
                burbujaRepository.sincronizarCatalogo(remotos)
            }
        }
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            rolListener?.remove()
            rolListener = null
            if (uid != null) {
                rolListener = db.collection("usuarios").document(uid).addSnapshotListener { snap, error ->
                    if (error != null) {
                        Log.e(TAG, "Error al leer rol del usuario", error)
                        return@addSnapshotListener
                    }
                    val rol = snap?.getString("rol")?.uppercase().orEmpty()
                    iniciarSincronizacionProtegida(uid, rol)
                }
            }
        }
        authListener = listener
        FirebaseAuth.getInstance().addAuthStateListener(listener)
    }

    /** Abre pedidos/usuarios según permisos: ADMIN ve todo, VENDEDOR solo sus pedidos y COMPRADOR nada protegido. */
    private fun iniciarSincronizacionProtegida(uid: String, rol: String) {
        if (syncedUid == uid && syncedRol == rol) return
        detenerSincronizacionProtegida()
        syncedUid = uid
        syncedRol = rol
        when (rol) {
            "ADMIN" -> {
                pedidoListener = db.collection("pedidos").addSnapshotListener { snap, error ->
                    if (error != null) {
                        Log.e(TAG, "Error al escuchar pedidos", error)
                        return@addSnapshotListener
                    }
                    if (snap == null) return@addSnapshotListener
                    burbujaRepository.reemplazarPedidos(snap.documents.mapNotNull { it.data?.aPedidoAdmin() })
                }
                usuarioListener = db.collection("usuarios").addSnapshotListener { snap, error ->
                    if (error != null) {
                        Log.e(TAG, "Error al escuchar usuarios", error)
                        return@addSnapshotListener
                    }
                    if (snap == null) return@addSnapshotListener
                    burbujaRepository.reemplazarUsuarios(snap.documents.mapNotNull { it.data?.aUsuarioAdmin() })
                }
            }
            "VENDEDOR" -> {
                pedidoListener = db.collection("pedidos").whereArrayContains("vendedorIds", uid)
                    .addSnapshotListener { snap, error ->
                        if (error != null) {
                            Log.e(TAG, "Error al escuchar pedidos del vendedor", error)
                            return@addSnapshotListener
                        }
                        if (snap == null) return@addSnapshotListener
                        burbujaRepository.reemplazarPedidos(snap.documents.mapNotNull { it.data?.aPedidoAdmin() })
                    }
                burbujaRepository.reemplazarUsuarios(emptyList())
            }
            else -> {
                burbujaRepository.reemplazarPedidos(emptyList())
                burbujaRepository.reemplazarUsuarios(emptyList())
            }
        }
    }

    /** Cierra listeners protegidos y limpia pedidos/usuarios al cambiar de sesión o de rol. */
    private fun detenerSincronizacionProtegida() {
        pedidoListener?.remove()
        usuarioListener?.remove()
        pedidoListener = null
        usuarioListener = null
        syncedUid = ""
        syncedRol = ""
        burbujaRepository.reemplazarPedidos(emptyList())
        burbujaRepository.reemplazarUsuarios(emptyList())
    }

    /** Libera todos los listeners y reinicia el estado de sincronización. */
    fun detenerSincronizacion() {
        authListener?.let { FirebaseAuth.getInstance().removeAuthStateListener(it) }
        authListener = null
        rolListener?.remove()
        rolListener = null
        detenerSincronizacionProtegida()
        productoListener?.remove()
        productoListener = null
        iniciado = false
    }

    /** Siembra el catálogo demo en un batch solo cuando la colección `productos` está vacía. */
    suspend fun sembrarCatalogoDemo(vendedorUid: String = ""): Result<Int> = runCatching {
        if (!habilitado) error("Firebase no configurado")
        if (!db.collection("productos").limit(1).get().await().isEmpty) return Result.success(0)
        val catalogo = catalogoBurbujaGlow()
        val batch = db.batch()
        catalogo.forEach { p ->
            val producto = p.copy(
                uid = p.uid.ifBlank { "p_${p.id}" },
                vendedorId = p.vendedorId.ifBlank { vendedorUid },
                imagen = 0
            )
            batch.set(db.collection("productos").document(producto.uid), producto.aMap())
        }
        batch.commit().await()
        catalogo.size
    }

    /** Crea o reemplaza un producto en Firestore usando su `uid` como identificador de documento. */
    suspend fun guardarProducto(producto: ProductoComprador): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        val uid = producto.uid.ifBlank { "p_${producto.id}" }
        val productoRemoto = producto.copy(
            uid = uid,
            imagen = 0
        )
        db.collection("productos").document(uid).set(productoRemoto.aMap()).await()
    }

    /** Adapta el producto del panel administrativo al modelo comprador antes de guardarlo. */
    suspend fun guardarProductoAdmin(producto: ProductoAdmin): Result<Unit> =
        guardarProducto(productoDesdeAdmin(producto).copy(uid = producto.uid.ifBlank { "p_${producto.id}" }))

    /** Elimina el documento de producto correspondiente al `uid` indicado. */
    suspend fun eliminarProducto(producto: ProductoComprador): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        db.collection("productos").document(producto.uid.ifBlank { "p_${producto.id}" }).delete().await()
    }

    /** Elimina un producto del panel admin convirtiendo primero su modelo al del comprador. */
    suspend fun eliminarProductoAdmin(producto: ProductoAdmin): Result<Unit> =
        eliminarProducto(productoDesdeAdmin(producto))

    /** Crea o actualiza el perfil de un usuario en `usuarios/{uid}`. */
    suspend fun guardarUsuario(usuario: UsuarioAdmin): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        val uid = usuario.uid.ifBlank { "u_${usuario.id}" }
        db.collection("usuarios").document(uid).set(usuario.copy(uid = uid).aMap()).await()
    }

    /** Marca un usuario como Inactivo sin alterar su rol ni sus demás datos. */
    suspend fun desactivarUsuario(usuario: UsuarioAdmin): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        val uid = usuario.uid.ifBlank { "u_${usuario.id}" }
        db.collection("usuarios").document(uid).update("estado", "Inactivo").await()
    }

    /** Obtiene el perfil remoto del usuario; devuelve `null` si no hay documento. */
    suspend fun obtenerPerfil(uid: String): Result<UsuarioAdmin?> = runCatching {
        if (!habilitado || uid.isBlank()) return Result.success(null)
        val snap = db.collection("usuarios").document(uid).get().await()
        snap.data?.aUsuarioAdmin()
    }

    /** Actualiza en una sola escritura los campos editables del perfil del vendedor. */
    suspend fun actualizarPerfilVendedor(
        uid: String,
        nombre: String,
        correo: String,
        telefono: String,
        ciudad: String
    ): Result<Unit> = runCatching {
        if (!habilitado || uid.isBlank()) return Result.success(Unit)
        db.collection("usuarios").document(uid).update(
            mapOf(
                "nombre" to nombre,
                "correo" to correo,
                "telefono" to telefono,
                "direccion" to ciudad,
                "ciudad" to ciudad,
                "latitud" to 0.0,
                "longitud" to 0.0
            )
        ).await()
    }

    /** Guarda dirección, ciudad y coordenadas del usuario para la entrega. */
    suspend fun actualizarUbicacion(
        uid: String,
        direccion: String,
        ciudad: String,
        latitud: Double,
        longitud: Double
    ): Result<Unit> = runCatching {
        if (!habilitado || uid.isBlank()) return Result.success(Unit)
        db.collection("usuarios").document(uid).update(
            mapOf(
                "direccion" to direccion,
                "ciudad" to ciudad,
                "latitud" to latitud,
                "longitud" to longitud
            )
        ).await()
    }

    /** Crea el pedido y opcionalmente descuenta stock en un batch, validando stock antes de escribir. */
    suspend fun crearPedido(
        pedido: PedidoAdmin,
        items: List<ItemPedido>,
        descontarStock: Boolean
    ): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        val snaps = coroutineScope {
            items.map { item ->
                async {
                    db.collection("productos").document(item.productoId.ifBlank { "p_${item.nombre}" }).get().await()
                }
            }.awaitAll()
        }
        val actualizaciones = items.zip(snaps).map { (item, snap) ->
            val uid = item.productoId.ifBlank { "p_${item.nombre}" }
            val stock = (snap.get("stock") as? Number)?.toInt() ?: 0
            if (stock < item.cantidad) error("Stock insuficiente para ${item.nombre.ifBlank { uid }}")
            val vendidos = (snap.get("vendidos") as? Number)?.toInt() ?: 0
            Triple(uid, stock - item.cantidad, vendidos + item.cantidad)
        }
        db.runBatch { batch ->
            batch.set(db.collection("pedidos").document(pedido.numero), pedido.aMap())
            if (descontarStock) {
                actualizaciones.forEach { (uid, stock, vendidos) ->
                    batch.update(
                        db.collection("productos").document(uid),
                        mapOf("stock" to stock, "vendidos" to vendidos)
                    )
                }
            }
        }.await()
    }

    /** Cambia el estado de un pedido existente desde el panel administrativo. */
    suspend fun cambiarEstadoPedido(numero: String, estado: String): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        db.collection("pedidos").document(numero).update("estado", estado).await()
    }

    /** Escucha `carritos/{uid}/items` y convierte cada documento en `ItemCarrito` para la UI. */
    fun observarCarrito(uid: String, onCambio: (List<ItemCarrito>) -> Unit): ListenerRegistration? {
        if (!habilitado || uid.isBlank()) return null
        return db.collection("carritos").document(uid).collection("items")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e(TAG, "Error al escuchar carrito", error)
                    return@addSnapshotListener
                }
                if (snap == null) return@addSnapshotListener
                onCambio(snap.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val cantidad = (data["cantidad"] as? Number)?.asInt() ?: 1
                    @Suppress("UNCHECKED_CAST")
                    val producto = (data["producto"] as? Map<String, Any?>)?.aProducto() ?: return@mapNotNull null
                    ItemCarrito(producto, cantidad)
                })
            }
    }

    /** Elimina todos los ítems del carrito remoto en un batch. */
    suspend fun vaciarCarrito(uid: String): Result<Unit> = runCatching {
        if (!habilitado || uid.isBlank()) return Result.success(Unit)
        val docs = db.collection("carritos").document(uid).collection("items").get().await().documents
        val batch = db.batch()
        docs.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    private fun Any.asInt(): Int = (this as? Number)?.toInt() ?: 1
}
