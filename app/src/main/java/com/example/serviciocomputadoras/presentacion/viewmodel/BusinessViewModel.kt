package com.example.serviciocomputadoras.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviciocomputadoras.data.repository.BusinessRepository
import com.example.serviciocomputadoras.data.model.Business
import com.example.serviciocomputadoras.data.model.Request
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BusinessViewModel(
    private val repository: BusinessRepository = BusinessRepository()
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _businesses = MutableStateFlow<List<Business>>(emptyList())
    val businesses: StateFlow<List<Business>> = _businesses

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _requestStatus = MutableStateFlow<String?>(null)
    val requestStatus: StateFlow<String?> = _requestStatus

    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests

    init {
        loadAll()
        loadMyRequests()
    }

    // Cargar todos los negocios
    fun loadAll() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _businesses.value = repository.getAllBusinesses()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
            _loading.value = false
        }
    }

    // Buscar negocios por nombre
    fun search(query: String) {
        if (query.isBlank()) {
            loadAll()
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                val list = repository.searchBusinesses(query)
                _businesses.value = list
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
            _loading.value = false
        }
    }

    // Cargar solicitudes del usuario actual
    fun loadMyRequests() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _requests.value = repository.getRequestsByUser()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
            _loading.value = false
        }
    }

    // Crear una solicitud de servicio
    fun createRequest(
        businessId: String,
        description: String,
        preferredDate: String? = null
    ) {
        if (description.isBlank()) {
            _requestStatus.value = "La descripción no puede estar vacía."
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _requestStatus.value = null

            val result = repository.createRequest(businessId, description, preferredDate)
            _loading.value = false

            result.fold(
                onSuccess = {
                    _requestStatus.value = "Solicitud enviada con éxito."
                    loadMyRequests()
                },
                onFailure = { e ->
                    _requestStatus.value = "Error: ${e.message ?: "Error desconocido"}"
                }
            )
        }
    }

    // Limpiar mensajes de estado
    fun clearRequestStatus() {
        _requestStatus.value = null
    }
}