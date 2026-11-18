package com.example.burbujaglow.data.remote

import com.example.burbujaglow.BuildConfig
import com.example.burbujaglow.data.burbujaRepository
import com.example.burbujaglow.data.catalogoBurbujaGlow
import com.example.burbujaglow.data.model.ItemCarrito
import com.example.burbujaglow.data.model.PedidoAdmin
import com.example.burbujaglow.data.model.ProductoAdmin
import com.example.burbujaglow.data.model.ProductoComprador
import com.example.burbujaglow.data.model.UsuarioAdmin
import com.example.burbujaglow.data.productoDesdeAdmin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class FirestoreDataRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val habilitado: Boolean get() = BuildConfig.FIREBASE_CONFIGURED

    private val listeners = mutableListOf<ListenerRegistration>()

    fun iniciarSincronizacion() {
        if (!habilitado || listeners.isNotEmpty()) return
        listeners += db.collection("productos").addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            burbujaRepository.sincronizarCatalogo(snap.documents.mapNotNull { it.data?.aProducto() })
        }
        listeners += db.collection("pedidos").addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            burbujaRepository.reemplazarPedidos(snap.documents.mapNotNull { it.data?.aPedidoAdmin() })
        }
        listeners += db.collection("usuarios").addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            burbujaRepository.reemplazarUsuarios(snap.documents.mapNotNull { it.data?.aUsuarioAdmin() })
        }
    }

    fun detenerSincronizacion() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    suspend fun sembrarCatalogoDemo(): Result<Int> = runCatching {
        if (!habilitado) return Result.success(0)
        if (!db.collection("productos").limit(1).get().await().isEmpty) return Result.success(0)
        val batch = db.batch()
        catalogoBurbujaGlow().forEach { p ->
            val producto = p.copy(uid = p.uid.ifBlank { "p_${p.id}" })
            batch.set(db.collection("productos").document(producto.uid), producto.aMap())
        }
        batch.commit().await()
        catalogoBurbujaGlow().size
    }

    suspend fun guardarProducto(producto: ProductoComprador): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        val uid = producto.uid.ifBlank { "p_${producto.id}" }
        db.collection("productos").document(uid).set(producto.copy(uid = uid).aMap()).await()
    }

    suspend fun guardarProductoAdmin(producto: ProductoAdmin): Result<Unit> =
        guardarProducto(productoDesdeAdmin(producto).copy(uid = producto.uid.ifBlank { "p_${producto.id}" }))

    suspend fun eliminarProducto(producto: ProductoComprador): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        db.collection("productos").document(producto.uid.ifBlank { "p_${producto.id}" }).delete().await()
    }

    suspend fun eliminarProductoAdmin(producto: ProductoAdmin): Result<Unit> =
        eliminarProducto(productoDesdeAdmin(producto))

    suspend fun guardarUsuario(usuario: UsuarioAdmin): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        val uid = usuario.uid.ifBlank { "u_${usuario.id}" }
        db.collection("usuarios").document(uid).set(usuario.copy(uid = uid).aMap()).await()
    }

    suspend fun desactivarUsuario(usuario: UsuarioAdmin): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        val uid = usuario.uid.ifBlank { "u_${usuario.id}" }
        db.collection("usuarios").document(uid).update("estado", "Inactivo").await()
    }

    suspend fun obtenerPerfil(uid: String): Result<UsuarioAdmin?> = runCatching {
        if (!habilitado || uid.isBlank()) return Result.success(null)
        val snap = db.collection("usuarios").document(uid).get().await()
        snap.data?.aUsuarioAdmin()
    }

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

    suspend fun guardarPedido(pedido: PedidoAdmin): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        db.collection("pedidos").document(pedido.numero).set(pedido.aMap()).await()
    }

    suspend fun cambiarEstadoPedido(numero: String, estado: String): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        db.collection("pedidos").document(numero).update("estado", estado).await()
    }

    suspend fun actualizarEstadoPago(numero: String, estado: String): Result<Unit> = runCatching {
        if (!habilitado) return Result.success(Unit)
        db.collection("pedidos").document(numero).update("estadoPago", estado).await()
    }

    fun observarCarrito(uid: String, onCambio: (List<ItemCarrito>) -> Unit): ListenerRegistration? {
        if (!habilitado || uid.isBlank()) return null
        return db.collection("carritos").document(uid).collection("items")
            .addSnapshotListener { snap, _ ->
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

    suspend fun guardarItemCarrito(uid: String, item: ItemCarrito): Result<Unit> = runCatching {
        if (!habilitado || uid.isBlank()) return Result.success(Unit)
        val id = item.producto.uid.ifBlank { "p_${item.producto.id}" }
        db.collection("carritos").document(uid).collection("items").document(id)
            .set(mapOf("cantidad" to item.cantidad, "producto" to item.producto.aMap())).await()
    }

    suspend fun eliminarItemCarrito(uid: String, productoId: Int): Result<Unit> = runCatching {
        if (!habilitado || uid.isBlank()) return Result.success(Unit)
        db.collection("carritos").document(uid).collection("items").document("p_$productoId").delete().await()
    }

    suspend fun vaciarCarrito(uid: String): Result<Unit> = runCatching {
        if (!habilitado || uid.isBlank()) return Result.success(Unit)
        val docs = db.collection("carritos").document(uid).collection("items").get().await().documents
        val batch = db.batch()
        docs.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    private fun Any.asInt(): Int = (this as? Number)?.toInt() ?: 1
}
