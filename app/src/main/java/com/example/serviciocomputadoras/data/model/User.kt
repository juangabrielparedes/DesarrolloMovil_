package com.example.serviciocomputadoras.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val rol: String = "Cliente", // Cliente, Vendedor, Admin
    val permisos: List<String> = emptyList(),
    val estado: String = "activo",
    val fechaCreacion: Long = System.currentTimeMillis()
)