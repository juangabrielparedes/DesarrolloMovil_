package com.example.serviciocomputadoras.data.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: FirebaseUser?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido al iniciar sesión")
        }
    }

    suspend fun register(email: String, password: String, nombre: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return AuthResult.Error("Error al obtener UID")

            //  NUEVO: Crear documento en Firestore
            userRepository.crearUsuario(uid, email, nombre)

            AuthResult.Success(result.user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido al registrar")
        }
    }

    suspend fun resetPassword(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(null)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error al enviar correo de recuperación")
        }
    }

    fun logout() {
        auth.signOut()
    }
}