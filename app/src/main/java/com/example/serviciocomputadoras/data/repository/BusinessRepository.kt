package com.example.serviciocomputadoras.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.serviciocomputadoras.data.model.Business
import com.example.serviciocomputadoras.data.model.Request
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class BusinessRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val businessCollection = firestore.collection("businesses")
    private val requestCollection = firestore.collection("requests")
    private val usersCollection = firestore.collection("usuarios")


    suspend fun getAllBusinesses(): List<Business> {
        return try {
            val snapshot = businessCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Business::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun getBusinessById(id: String): Business? {
        return try {
            val doc = businessCollection.document(id).get().await()
            doc.toObject(Business::class.java)
        } catch (e: Exception) {
            null
        }
    }


    suspend fun getRequestsByUser(): List<Request> {
        val user = auth.currentUser ?: return emptyList()
        return try {
            val snapshot = requestCollection
                .whereEqualTo("clientUid", user.uid)
                .orderBy("createdAt")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Request::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun createRequest(
        businessId: String,
        description: String,
        preferredDate: String? = null
    ): Result<Boolean> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no logueado"))

        return try {

            val userDoc = usersCollection.document(user.uid).get().await()
            val name = userDoc.getString("nombre") ?: "Usuario"

            val data = hashMapOf(
                "businessId" to businessId,
                "clientUid" to user.uid,
                "clientName" to name,
                "clientEmail" to user.email,
                "description" to description,
                "preferredDate" to preferredDate,
                "createdAt" to Timestamp.now(),
                "status" to "pending"
            )

            requestCollection.add(data).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun searchBusinesses(query: String): List<Business> {
        return try {
            val allBusinesses = getAllBusinesses()
            allBusinesses.filter { it.name.contains(query, ignoreCase = true) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}