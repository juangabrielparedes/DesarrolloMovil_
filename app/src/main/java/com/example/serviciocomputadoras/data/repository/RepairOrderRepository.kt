package com.example.serviciocomputadoras.data.repository

import com.example.serviciocomputadoras.data.model.Invoice
import com.example.serviciocomputadoras.data.model.InvoiceItem
import com.example.serviciocomputadoras.data.model.RepairOrder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class RepairOrderRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val orders = firestore.collection("repair_orders")
    private val invoices = firestore.collection("invoices")
    private val usuarios = firestore.collection("usuarios")

    suspend fun createRepairOrder(order: RepairOrder): String {
        val id = orders.document().id
        val toSave = order.copy(orderId = id, createdAt = Timestamp.now())
        orders.document(id).set(toSave).await()
        return id
    }

    suspend fun generateInvoiceFromOrder(orderId: String): String {
        val snap = orders.document(orderId).get().await()
        val order = snap.toObject(RepairOrder::class.java) ?: throw Exception("Order no encontrada")
        val items = mutableListOf<InvoiceItem>()
        if (order.laborCost > 0) items.add(InvoiceItem("Mano de obra", order.laborCost))
        order.parts.forEach { items.add(InvoiceItem("Repuesto: ${it.name}", it.price)) }
        val total = items.sumOf { it.price }
        val invoiceId = invoices.document().id
        val invoice = Invoice(
            invoiceId = invoiceId,
            repairOrderId = orderId,
            clientUid = order.clientUid,
            clientName = order.clientName,
            clientEmail = order.clientEmail,
            items = items,
            total = total,
            status = "pending",
            checkoutUrl = null,
            createdAt = Timestamp.now()
        )
        invoices.document(invoiceId).set(invoice).await()
        orders.document(orderId).update(mapOf("status" to "pending_payment")).await()
        return invoiceId
    }

    suspend fun attachCheckoutUrlToInvoice(invoiceId: String, url: String) {
        invoices.document(invoiceId).update(mapOf("checkoutUrl" to url)).await()
    }

    // helper: si quieres obtener uid por email (opcional)
    suspend fun findUserUidByEmail(email: String): String? {
        val q = usuarios.whereEqualTo("email", email).get().await()
        return if (q.documents.isNotEmpty()) q.documents[0].id else null
    }
}
