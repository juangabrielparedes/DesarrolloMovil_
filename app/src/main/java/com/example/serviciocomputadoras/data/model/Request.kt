package com.example.serviciocomputadoras.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Request(
    @DocumentId
    val id: String = "",
    val businessId: String = "",
    val clientUid: String = "",
    val clientName: String? = null,
    val clientEmail: String? = null,
    val description: String = "",
    val preferredDate: String? = null,
    val createdAt: Timestamp? = null,
    val status: String = "pending"
)