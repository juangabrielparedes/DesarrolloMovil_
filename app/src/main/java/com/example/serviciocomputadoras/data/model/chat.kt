package com.example.serviciocomputadoras.data.model

import com.google.firebase.Timestamp

data class Chat(
    val chatId: String = "",
    val businessId: String = "",
    val clientUid: String = "",
    val ownerUid: String = "",
    val lastMessage: String = "",
    val updatedAt: Timestamp? = null
)
