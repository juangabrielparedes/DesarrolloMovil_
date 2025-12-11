package com.example.serviciocomputadoras.presentacion.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviciocomputadoras.data.repository.RequestRepository
import com.example.serviciocomputadoras.data.model.Request
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RequestUiState(
    val requests: List<Request> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class RequestViewModel(
    private val repository: RequestRepository = RequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestUiState())
    val uiState: StateFlow<RequestUiState> = _uiState.asStateFlow()

    // Cargar solicitudes para un vendedor/dueño
    fun loadRequestsForOwner(ownerUid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            try {
                val requests = repository.getRequestsForOwner(ownerUid)
                _uiState.value = _uiState.value.copy(
                    requests = requests,
                    loading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
                )
            }
        }
    }

    // Actualizar estado de una solicitud (aceptar/rechazar)
    fun updateRequestStatus(requestId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                val result = repository.updateRequestStatus(requestId, newStatus)

                result.fold(
                    onSuccess = {
                        // Actualizar localmente
                        val updatedRequests = _uiState.value.requests.map { req ->
                            if (req.id == requestId) req.copy(status = newStatus) else req
                        }
                        _uiState.value = _uiState.value.copy(requests = updatedRequests)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // Limpiar errores
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}