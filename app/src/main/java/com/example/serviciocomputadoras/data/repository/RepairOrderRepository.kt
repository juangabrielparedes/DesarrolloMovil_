package com.example.serviciocomputadoras.data.repository

import android.util.Log
import com.example.serviciocomputadoras.data.model.Invoice
import com.example.serviciocomputadoras.data.model.InvoiceItem
import com.example.serviciocomputadoras.data.model.PartItem
import com.example.serviciocomputadoras.data.model.RepairOrder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class RepairOrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val chatRepo: com.example.serviciocomputadoras.data.repository.ChatRepository = com.example.serviciocomputadoras.data.repository.ChatRepository()
) {
    private val TAG = "RepairOrderRepo"

    private val ordersCol = firestore.collection("repairOrders")
    private val invoicesCol = firestore.collection("invoices")
    private val usersCol = firestore.collection("users")
    private val usuariosCol = firestore.collection("usuarios")
    private val businessesCol = firestore.collection("businesses")

    // Método anterior mantenido (por compatibilidad)
    suspend fun createOrderAndInvoice(order: RepairOrder): Pair<String, String>? {

        return createOrderAndInvoiceWithSchedule(order, 0L)
    }


    suspend fun createOrderAndInvoiceWithSchedule(order: RepairOrder, scheduledMillis: Long = 0L): Pair<String, String>? {
        try {
            Log.d(TAG, "createOrderAndInvoiceWithSchedule: creando order provisional")
            val orderId = ordersCol.document().id

            val partsTotal = order.parts.sumOf { it.price }
            val total = partsTotal + order.laborCost

            // convertir scheduledMillis a Timestamp? si aplica
            val scheduledTimestamp: Timestamp? = if (scheduledMillis > 0L) {
                Timestamp(Date(scheduledMillis))
            } else {
                null
            }

            val orderToSave = order.copy(
                orderId = orderId,
                partsTotal = partsTotal,
                totalCost = total,
                createdAt = Timestamp.now(),
                scheduledDate = scheduledTimestamp
            )


            ordersCol.document(orderId).set(orderToSave).await()
            Log.d(TAG, "order guardada id=$orderId scheduled=$scheduledTimestamp")


            val invoiceId = invoicesCol.document().id
            val items = mutableListOf<InvoiceItem>()
            orderToSave.parts.forEach { p ->
                items.add(InvoiceItem(desc = p.name, price = p.price))
            }
            if (orderToSave.laborCost > 0L) {
                items.add(InvoiceItem(desc = "Mano de obra", price = orderToSave.laborCost))
            }

            val invoice = Invoice(
                invoiceId = invoiceId,
                repairOrderId = orderId,
                clientUid = orderToSave.clientUid,
                clientName = orderToSave.clientName,
                clientEmail = orderToSave.clientEmail,
                items = items,
                total = orderToSave.totalCost,
                status = "pending",
                //checkoutUrl = "https://buy.stripe.com/test_00wfZggVq4dZ4Tx4buawo01",
                createdAt = Timestamp.now()
            )

            invoicesCol.document(invoiceId).set(invoice).await()
            Log.d(TAG, "invoice guardada id=$invoiceId total=${invoice.total}")


            try {
                val chatId = chatRepo.chatIdFor(orderToSave.clientUid, orderToSave.businessId)

                // <-- MENSAJE AMIGABLE: cambiado aquí
                val shortMsg = "Se creó una factura a tu cuenta. Revísala, por favor."
                // -----------------------------------------------

                chatRepo.sendMessage(chatId, orderToSave.ownerId, orderToSave.clientUid, shortMsg)
                Log.d(TAG, "mensaje enviado al chat $chatId")
            } catch (e: Exception) {
                Log.w(TAG, "fallo al enviar mensaje en chat: ${e.message}")
            }

            return Pair(orderId, invoiceId)
        } catch (e: Exception) {
            Log.e(TAG, "createOrderAndInvoiceWithSchedule error: ${e.message}", e)
            return null
        }
    }

    /**
     * getUserInfo (ya robusto) - da nombre/email por uid (intenta varias estrategias)
     */
    suspend fun getUserInfo(uid: String): Pair<String?, String?>? {
        val TAG2 = "$TAG.getUserInfo"
        try {
            Log.d(TAG2, "Resolviendo getUserInfo para uid=$uid")

            try {
                val snap = usersCol.document(uid).get().await()
                if (snap.exists()) {
                    val nombre = snap.getString("nombre")
                    val email = snap.getString("email")
                    if (!nombre.isNullOrBlank() || !email.isNullOrBlank()) return Pair(nombre, email)
                }
            } catch (_: Exception) {}

            try {
                val snap2 = usuariosCol.document(uid).get().await()
                if (snap2.exists()) {
                    val nombre2 = snap2.getString("nombre")
                    val email2 = snap2.getString("email")
                    if (!nombre2.isNullOrBlank() || !email2.isNullOrBlank()) return Pair(nombre2, email2)
                }
            } catch (_: Exception) {}

            try {
                val q = usersCol.whereEqualTo("uid", uid).get().await()
                if (!q.isEmpty) {
                    val d = q.documents.first()
                    val nombreQ = d.getString("nombre")
                    val emailQ = d.getString("email")
                    if (!nombreQ.isNullOrBlank() || !emailQ.isNullOrBlank()) return Pair(nombreQ, emailQ)
                }
            } catch (_: Exception) {}

            try {
                val q2 = usuariosCol.whereEqualTo("uid", uid).get().await()
                if (!q2.isEmpty) {
                    val d = q2.documents.first()
                    val nombreQ2 = d.getString("nombre")
                    val emailQ2 = d.getString("email")
                    if (!nombreQ2.isNullOrBlank() || !emailQ2.isNullOrBlank()) return Pair(nombreQ2, emailQ2)
                }
            } catch (_: Exception) {}

            return null
        } catch (e: Exception) {
            Log.e(TAG2, "getUserInfo fallo general para $uid: ${e.message}", e)
            return null
        }
    }


    suspend fun getBusinessOwner(businessId: String): String? {
        return try {
            val snap = businessesCol.document(businessId).get().await()
            if (!snap.exists()) return null
            snap.getString("ownerId")
        } catch (e: Exception) {
            Log.w(TAG, "getBusinessOwner error for $businessId: ${e.message}")
            null
        }
    }
}

