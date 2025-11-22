package com.example.serviciocomputadoras.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviciocomputadoras.data.repository.AuthRepository
import com.example.serviciocomputadoras.data.repository.AuthResult
import com.example.serviciocomputadoras.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    val uid = result.user?.uid
                    if (uid != null) {
                        //  NUEVO: Obtener rol de Firestore
                        val usuario = userRepository.obtenerUsuario(uid)
                        _authState.value = AuthState(
                            isSuccess = true,
                            user = usuario,
                            rol = usuario?.rol
                        )
                    } else {
                        _authState.value = AuthState(error = "Error al obtener usuario")
                    }
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState(error = result.message)
                }
            }
        }
    }

    fun register(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            when (val result = authRepository.register(email, password, nombre)) {
                is AuthResult.Success -> {
                    val uid = result.user?.uid
                    if (uid != null) {
                        //  NUEVO: Obtener usuario recién creado
                        val usuario = userRepository.obtenerUsuario(uid)
                        _authState.value = AuthState(
                            isSuccess = true,
                            user = usuario,
                            rol = usuario?.rol
                        )
                    } else {
                        _authState.value = AuthState(error = "Error al crear usuario")
                    }
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState(error = result.message)
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            when (val result = authRepository.resetPassword(email)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState(isSuccess = true)
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState(error = result.message)
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthState()
    }

    fun resetState() {
        _authState.value = AuthState()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.getCurrentUser() != null
    }

    fun getRolActual(): String? {
        return _authState.value.rol
    }
}