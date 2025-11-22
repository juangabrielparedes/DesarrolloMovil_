package com.example.serviciocomputadoras.presentacion.viewmodel

import com.example.serviciocomputadoras.data.model.User

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: User? = null,  //  NUEVO: usuario con rol
    val rol: String? = null)