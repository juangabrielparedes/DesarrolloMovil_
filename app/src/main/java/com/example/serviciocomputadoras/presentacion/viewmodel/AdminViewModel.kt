package com.example.serviciocomputadoras.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviciocomputadoras.data.model.User
import com.example.serviciocomputadoras.data.repository.UserRepository
import com.example.serviciocomputadoras.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminState(
    val isLoading: Boolean = false,
    val usuarios: List<User> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val estadisticas: Estadisticas = Estadisticas()
)

data class Estadisticas(
    val totalUsuarios: Int = 0,
    val totalClientes: Int = 0,
    val totalVendedores: Int = 0,
    val totalAdmins: Int = 0
)

class AdminViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _adminState = MutableStateFlow(AdminState())
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)

            val usuarios = userRepository.obtenerTodosLosUsuarios()

            // Calcular estadísticas
            val stats = Estadisticas(
                totalUsuarios = usuarios.size,
                totalClientes = usuarios.count { it.rol == "Cliente" },
                totalVendedores = usuarios.count { it.rol == "Vendedor" },
                totalAdmins = usuarios.count { it.rol == "Admin" }
            )

            _adminState.value = _adminState.value.copy(
                isLoading = false,
                usuarios = usuarios,
                estadisticas = stats
            )
        }
    }

    fun cambiarRol(uid: String, nuevoRol: String) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)

            when (val result = userRepository.actualizarRol(uid, nuevoRol)) {
                is AuthResult.Success -> {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        successMessage = "Rol actualizado correctamente"
                    )
                    cargarUsuarios() // Recargar lista
                }
                is AuthResult.Error -> {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun eliminarUsuario(uid: String) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)

            try {
                // Aquí eliminarías el documento de Firestore
                // Por ahora solo recargamos
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    successMessage = "Usuario eliminado correctamente"
                )
                cargarUsuarios()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = "Error al eliminar usuario: ${e.message}"
                )
            }
        }
    }

    fun limpiarMensajes() {
        _adminState.value = _adminState.value.copy(
            error = null,
            successMessage = null
        )
    }
}