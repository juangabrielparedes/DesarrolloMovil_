package com.example.serviciocomputadoras.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.serviciocomputadoras.data.model.Request
import kotlinx.coroutines.tasks.await

class RequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val requestsRef = firestore.collection("requests")
    private val usersRef = firestore.collection("usuarios")  // Tu colección de usuarios

    // crear una solicitud
    suspend fun createRequest(
        businessId: String,
        description: String,
        preferredDate: String?
    ): Result<String> {
        return try {
            val current = auth.currentUser
            val uid = current?.uid ?: return Result.failure(Exception("No user logged in"))
            val email = current.email

            // obtener nombre del usuario
            val name = usersRef.document(uid).get().await().getString("nombre")

            val doc = requestsRef.document()
            val request = Request(
                id = doc.id,
                businessId = businessId,
                clientUid = uid,
                clientName = name,
                clientEmail = email,
                description = description,
                preferredDate = preferredDate,
                createdAt = Timestamp.now(),
                status = "pending"
            )
            doc.set(request).await()
            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // obtener solicitudes de un negocio específico
    suspend fun getRequestsForBusiness(businessId: String): List<Request> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("businessId", businessId)
                .orderBy("createdAt")
                .get()
                .await()
            snapshot.documents.mapNotNull {
                it.toObject(Request::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // obtener solicitudes para un vendedor/dueño (basado en sus negocios)
    suspend fun getRequestsForOwner(ownerUid: String): List<Request> {
        return try {
            // Obtener negocios del dueño
            val businessesSnap = firestore.collection("businesses")
                .whereEqualTo("ownerId", ownerUid)
                .get()
                .await()

            val bizIds = businessesSnap.documents.mapNotNull { it.id }
            if (bizIds.isEmpty()) return emptyList()

            // Obtener solicitudes de esos negocios
            val result = mutableListOf<Request>()
            val chunks = bizIds.chunked(10)  // Firestore limita whereIn a 10 elementos

            for (chunk in chunks) {
                val snap = requestsRef
                    .whereIn("businessId", chunk)
                    .orderBy("createdAt")
                    .get()
                    .await()
                result += snap.documents.mapNotNull { doc ->
                    doc.toObject(Request::class.java)?.copy(id = doc.id)
                }
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    // actualizar estado de una solicitud
    suspend fun updateRequestStatus(requestId: String, newStatus: String): Result<Unit> {
        return try {
            requestsRef.document(requestId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}