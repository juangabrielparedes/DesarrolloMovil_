package com.example.serviciocomputadoras.data.repository

import android.util.Log
import com.example.serviciocomputadoras.data.model.Invoice
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class InvoicesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val TAG = "InvoicesRepo"
    private val invoicesCol = firestore.collection("invoices")

    /**
     * Escucha en tiempo real las facturas del cliente.
     *
     * Nota: NO utilizamos orderBy("createdAt") en la query porque provoca que Firestore
     * requiera un índice compuesto en combinación con whereEqualTo("clientUid", ...).
     * En su lugar, traemos los documentos con whereEqualTo(...) y ordenamos en cliente
     * por createdAt (si existe).
     *
     * Además: si addSnapshotListener devuelve un error indicando que se requiere índice,
     * hacemos un fallback con un get() one-shot (sin orderBy) para que el usuario
     * reciba resultados aunque no exista el índice.
     */
    fun listenInvoicesForClient(
        clientUid: String,
        listener: (List<Invoice>) -> Unit
    ): ListenerRegistration {
        Log.d(TAG, "listenInvoicesForClient clientUid=$clientUid")

        // Query SIN orderBy para evitar necesidad de índice compuesto en Firestore
        val q = invoicesCol.whereEqualTo("clientUid", clientUid)

        return q.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(TAG, "listen error: ${err.message}", err)

                // Si el error sugiere que falta un índice, hacemos un fallback: fetch one-shot
                val msg = err.message ?: ""
                if (msg.contains("requires an index", ignoreCase = true) ||
                    msg.contains("index", ignoreCase = true)
                ) {
                    Log.d(
                        TAG,
                        "listenInvoicesForClient: índice requerido. Ejecutando fallback get() one-shot sin orderBy."
                    )
                    // Ejecutar petición one-shot sin orderBy (asincrónico)
                    invoicesCol.whereEqualTo("clientUid", clientUid)
                        .get()
                        .addOnSuccessListener { snap2 ->
                            handleSnapshotAndNotify(snap2, clientUid, listener)
                        }
                        .addOnFailureListener { ex ->
                            Log.w(TAG, "fallback get() failed: ${ex.message}", ex)
                            listener(emptyList())
                        }
                    return@addSnapshotListener
                }

                // Si no es un tema de índice, devolvemos lista vacía (y ya está logueado)
                listener(emptyList())
                return@addSnapshotListener
            }

            // Si llegamos aquí, snap no es null y no hubo error
            handleSnapshotAndNotify(snap, clientUid, listener)
        }
    }

    // Manejo centralizado del snapshot -> map -> listener
    private fun handleSnapshotAndNotify(
        snap: QuerySnapshot?,
        clientUid: String,
        listener: (List<Invoice>) -> Unit
    ) {
        val docs = snap?.documents ?: emptyList()
        Log.d(TAG, "snapshot docs count=${docs.size} for clientUid=$clientUid")
        docs.forEach { d ->
            Log.d(TAG, "doc id=${d.id} data=${d.data}")
        }

        val list = docs.mapNotNull { doc ->
            try {
                // usar toObject y luego copiar invoiceId desde doc.id
                val inv = doc.toObject(Invoice::class.java)?.copy(invoiceId = doc.id)
                inv
            } catch (e: Exception) {
                Log.w(TAG, "toObject failed for ${doc.id}: ${e.message}")
                null
            }
        }
            // ordenar en cliente por createdAt (descendente: más recientes primero)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }

        Log.d(TAG, "mapped invoices count=${list.size}")
        listener(list)
    }

    // one-shot fetch (sin orderBy para no requerir índice)
    suspend fun fetchInvoicesOnce(clientUid: String): List<Invoice> {
        try {
            val q = invoicesCol.whereEqualTo("clientUid", clientUid)
            val snap = q.get().await()
            val list = snap.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Invoice::class.java)?.copy(invoiceId = doc.id)
                } catch (e: Exception) {
                    Log.w(TAG, "fetchInvoicesOnce: toObject failed for ${doc.id}: ${e.message}")
                    null
                }
            }.sortedByDescending { it.createdAt?.seconds ?: 0L }
            Log.d(TAG, "fetchInvoicesOnce returned ${list.size} invoices for clientUid=$clientUid")
            return list
        } catch (e: Exception) {
            Log.w(TAG, "fetchInvoicesOnce failed: ${e.message}", e)
            return emptyList()
        }
    }

    // get invoice by id (sin cambios)
    suspend fun getInvoice(invoiceId: String): Invoice? {
        return try {
            val snap = invoicesCol.document(invoiceId).get().await()
            if (snap.exists()) {
                snap.toObject(Invoice::class.java)?.copy(invoiceId = snap.id)
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "getInvoice failed: ${e.message}", e)
            null
        }
    }

    // update invoice doc (sin cambios)
    suspend fun updateInvoice(invoiceId: String, updates: Map<String, Any?>) {
        invoicesCol.document(invoiceId).update(updates).await()
    }

    suspend fun markAsPaid(invoiceId: String): Boolean {
        return try {
            invoicesCol.document(invoiceId)
                .update("status", "paid")
                .await()
            Log.d(TAG, "Invoice $invoiceId marcada como paid")
            true
        } catch (e: Exception) {
            Log.e(TAG, "markAsPaid failed: ${e.message}", e)
            false
        }
    }
}
