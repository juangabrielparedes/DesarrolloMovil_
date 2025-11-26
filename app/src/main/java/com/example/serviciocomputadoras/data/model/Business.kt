package com.example.serviciocomputadoras.data.model
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Business(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    val address: String = "",
    val phone: String = "",
    val services: List<String> = emptyList(),
    val priceStarting: Long = 0L,
    val ownerId: String = "",  // UID del vendedor/dueño del negocio
    val createdAt: Timestamp? = null
)