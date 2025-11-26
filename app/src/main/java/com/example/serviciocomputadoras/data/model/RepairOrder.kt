package com.example.serviciocomputadoras.data.model

import com.google.firebase.Timestamp

data class RepairOrder(
    val orderId: String = "",
    val businessId: String = "",
    val ownerId: String = "",
    val clientUid: String = "",
    val clientName: String = "",
    val clientEmail: String = "",
    val deviceType: String = "",
    val problemReported: String = "",
    val diagnosis: String = "",
    val laborCost: Long = 0L,
    val parts: List<PartItem> = emptyList(),
    val partsTotal: Long = 0L,
    val totalCost: Long = 0L,
    val status: String = "pending_approval",
    val createdAt: Timestamp? = null
)
