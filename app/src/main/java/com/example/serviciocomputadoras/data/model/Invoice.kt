package com.example.serviciocomputadoras.data.model

import com.google.firebase.Timestamp

data class InvoiceItem(val desc: String = "", val price: Long = 0L)

data class Invoice(
    val invoiceId: String = "",
    val repairOrderId: String = "",
    val clientUid: String = "",
    val clientName: String = "",
    val clientEmail: String = "",
    val items: List<InvoiceItem> = emptyList(),
    val total: Long = 0L,
    val status: String = "pending",
    val checkoutUrl: String? = null,
    val createdAt: Timestamp? = null
)
