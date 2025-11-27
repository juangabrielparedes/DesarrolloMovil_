package com.example.serviciocomputadoras.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Request(
    @DocumentId
    val id: String = "",
    val businessId: String = "",  // id del negocio solicitado
    val clientUid: String = "",   // uid del cliente que solicita
    val clientName: String? = null,
    val clientEmail: String? = null,
    val description: String = "",
    val preferredDate: String? = null,  // fecha preferida (opcional)
    val createdAt: Timestamp? = null,
    val status: String = "pending"  // pending, accepted, rejected, completed
)