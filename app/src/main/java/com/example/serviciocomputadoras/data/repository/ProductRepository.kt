package com.example.serviciocomputadoras.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.serviciocomputadoras.data.model.Product
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun createProduct(pid: String, name: String, price: Long, urlImage: String, unit: Int): AuthResult{
        return try {
            val product = Product(
                pid = pid,
                name = name,
                price = price,
                urlImage = urlImage,
                unit = unit
            )
            db.collection("Productos")
                .document(pid)
                .set(product)
                .await()

            AuthResult.Success(null)
        } catch(e: Exception){
            AuthResult.Error("Error al crear el producto en BD: ${e.message}")
        }
    }

}