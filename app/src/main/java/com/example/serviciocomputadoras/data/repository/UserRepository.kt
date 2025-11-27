package com.example.serviciocomputadoras.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.serviciocomputadoras.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun crearUsuario(uid: String, email: String, nombre: String): AuthResult {
        return try {
            val usuario = User(
                uid = uid,
                email = email,
                nombre = nombre,
                rol = "Cliente" // Por defecto nuevo usuario es Cliente
            )

            db.collection("usuarios")
                .document(uid)
                .set(usuario)
                .await()

            AuthResult.Success(null)
        } catch (e: Exception) {
            AuthResult.Error("Error al crear usuario en BD: ${e.message}")
        }
    }

    suspend fun obtenerUsuario(uid: String): User? {
        return try {
            val documento = db.collection("usuarios")
                .document(uid)
                .get()
                .await()

            documento.toObject(User::class.java)?.copy(uid = uid)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun actualizarRol(uid: String, nuevoRol: String): AuthResult {
        return try {
            db.collection("usuarios")
                .document(uid)
                .update("rol", nuevoRol)
                .await()

            AuthResult.Success(null)
        } catch (e: Exception) {
            AuthResult.Error("Error al actualizar rol: ${e.message}")
        }
    }

    suspend fun obtenerTodosLosUsuarios(): List<User> {
        return try {
            val resultado = db.collection("usuarios")
                .get()
                .await()

            resultado.toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}