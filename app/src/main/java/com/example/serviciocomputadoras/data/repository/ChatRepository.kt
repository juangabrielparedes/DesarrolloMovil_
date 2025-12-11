package com.example.serviciocomputadoras.data.repository

import android.util.Log
import com.example.serviciocomputadoras.data.model.Chat
import com.example.serviciocomputadoras.data.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val TAG = "ChatRepository"

    private val chats = firestore.collection("chats")
    private val messages = firestore.collection("messages")
    private val users = firestore.collection("users")
    private val usuarios = firestore.collection("usuarios")

    private val businesses = firestore.collection("businesses")

    fun chatIdFor(clientUid: String, businessId: String) = "${clientUid}_$businessId"

    suspend fun createOrGetChat(clientUid: String, businessId: String, ownerUid: String): Chat {
        val id = chatIdFor(clientUid, businessId)
        val docRef = chats.document(id)
        val snapshot = docRef.get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(Chat::class.java)!!.copy(chatId = id)
        } else {
            val chat = Chat(
                chatId = id,
                businessId = businessId,
                clientUid = clientUid,
                ownerUid = ownerUid,
                lastMessage = "",
                updatedAt = Timestamp.now()
            )
            docRef.set(chat).await()
            chat
        }
    }

    suspend fun sendMessage(chatId: String, senderUid: String, receiverUid: String, text: String) {
        val messageId = messages.document().id
        val message = Message(
            messageId = messageId,
            chatId = chatId,
            senderUid = senderUid,
            receiverUid = receiverUid,
            text = text,
            timestamp = Timestamp.now(),
            type = "text"
        )


        messages.document(messageId).set(message).await()
        Log.d(TAG, "sendMessage: message saved id=$messageId chatId=$chatId")


        try {
            chats.document(chatId).update(mapOf(
                "lastMessage" to text,
                "updatedAt" to Timestamp.now()
            )).await()
            Log.d(TAG, "sendMessage: chat updated chatId=$chatId")
        } catch (e: Exception) {
            Log.w(TAG, "sendMessage: update chat failed, intentando crear chat doc. error=${e.message}")
            try {
                val fallbackChat = Chat(
                    chatId = chatId,
                    businessId = "",
                    clientUid = "",
                    ownerUid = "",
                    lastMessage = text,
                    updatedAt = Timestamp.now()
                )
                chats.document(chatId).set(fallbackChat).await()
                Log.d(TAG, "sendMessage: chat doc creado defensivamente chatId=$chatId")
            } catch (e2: Exception) {
                Log.e(TAG, "sendMessage: fallo creando chat defensivo", e2)
            }
        }
    }

    fun listenMessages(chatId: String, listener: (List<Message>) -> Unit): ListenerRegistration {
        Log.d(TAG, "listenMessages: subscribing to chatId='$chatId' (snapshot listener)")
        val query = messages
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "listenMessages snapshot error for chatId=$chatId: ${error.message}", error)
                listener(emptyList()); return@addSnapshotListener
            }

            val docs = snapshot?.documents ?: emptyList()
            Log.d(TAG, "listenMessages snapshot docs=${docs.size} for chatId=$chatId")

            val list = docs.mapNotNull { doc ->
                try {
                    doc.toObject(Message::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "listenMessages: toObject failed for ${doc.id}", e)
                    null
                }
            }

            Log.d(TAG, "listenMessages: mapped messages=${list.size} for chatId=$chatId")
            listener(list)
        }

        return registration
    }

    fun listenChatsForOwner(ownerUid: String, listener: (List<Chat>) -> Unit): ListenerRegistration {
        val TAG2 = "$TAG.listenChatsForOwner"
        Log.d(TAG2, "Iniciando listenChatsForOwner ownerUid=$ownerUid")

        val query = chats
            .whereEqualTo("ownerUid", ownerUid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG2, "snapshotListener ERROR ownerUid=$ownerUid -> ${error.message}", error)

                chats.whereEqualTo("ownerUid", ownerUid).get()
                    .addOnSuccessListener { snap ->
                        Log.d(TAG2, "FALLBACK GET success docs=${snap.documents.size} for ownerUid=$ownerUid")
                        val listFallback = snap.documents.mapNotNull { d ->
                            try {
                                val c = d.toObject(Chat::class.java)
                                c?.copy(chatId = d.id)
                            } catch (ex: Exception) {
                                Log.e(TAG2, "FALLBACK mapping failed for ${d.id}", ex)
                                null
                            }
                        }
                        listener(listFallback)
                    }
                    .addOnFailureListener { getErr ->
                        Log.e(TAG2, "FALLBACK GET failed for ownerUid=$ownerUid: ${getErr.message}", getErr)
                        listener(emptyList())
                    }
                return@addSnapshotListener
            }

            val docs = snapshot?.documents ?: emptyList()
            Log.d(TAG2, "snapshotListener docs=${docs.size} for ownerUid=$ownerUid")
            val list = docs.mapNotNull { doc ->
                try {
                    val c = doc.toObject(Chat::class.java)
                    c?.copy(chatId = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG2, "toObject failed for ${doc.id}", e)
                    null
                }
            }

            list.forEach { c ->
                Log.d(TAG2, "chat -> id=${c.chatId} clientUid=${c.clientUid} lastMessage='${c.lastMessage}' updatedAt=${c.updatedAt}")
            }

            listener(list)
        }

        return registration
    }


    suspend fun fetchChatsForOwnerOnce(ownerUid: String): List<Chat> {
        val TAG2 = "$TAG.fetchOnce"
        return try {
            Log.d(TAG2, "fetchChatsForOwnerOnce llamado ownerUid=$ownerUid")
            val snap = chats
                .whereEqualTo("ownerUid", ownerUid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            Log.d(TAG2, "fetchChatsForOwnerOnce result size=${snap.documents.size}")
            snap.documents.mapNotNull { d ->
                try {
                    val c = d.toObject(Chat::class.java)
                    c?.copy(chatId = d.id)
                } catch (e: Exception) {
                    Log.e(TAG2, "fetchChatsForOwnerOnce mapping failed for ${d.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG2, "fetchChatsForOwnerOnce ERROR ownerUid=$ownerUid: ${e.message}", e)
            emptyList()
        }
    }


    suspend fun getUserDisplayName(uid: String): String? {
        val TAG2 = "$TAG.getUserDisplayName"
        try {
            Log.d(TAG2, "Resolviendo nombre para uid=$uid")


            try {
                val snap = users.document(uid).get().await()
                if (snap.exists()) {
                    Log.d(TAG2, "users doc existe para uid=$uid; data keys=${snap.data?.keys}")
                    val nombre = snap.getString("nombre")
                    if (!nombre.isNullOrBlank()) {
                        Log.d(TAG2, "Resolved from users/${uid} -> nombre='$nombre'")
                        return nombre
                    }

                    val fallbackFields = listOf("displayName", "name", "email", "username")
                    for (k in fallbackFields) {
                        val v = snap.getString(k)
                        if (!v.isNullOrBlank()) {
                            Log.d(TAG2, "Resolved from users/${uid} -> field=$k value='$v'")
                            return v
                        }
                    }
                    Log.d(TAG2, "users/${uid} existe pero no tiene campos útiles")
                } else {
                    Log.d(TAG2, "users/${uid} NO existe")
                }
            } catch (e: Exception) {
                Log.w(TAG2, "Error leyendo users/${uid}: ${e.message}")
            }


            try {
                val snap2 = usuarios.document(uid).get().await()
                if (snap2.exists()) {
                    Log.d(TAG2, "usuarios doc existe para uid=$uid; data keys=${snap2.data?.keys}")
                    val nombre2 = snap2.getString("nombre")
                    if (!nombre2.isNullOrBlank()) {
                        Log.d(TAG2, "Resolved from usuarios/${uid} -> nombre='$nombre2'")
                        return nombre2
                    }
                    val fallbackFields = listOf("displayName", "name", "email", "username")
                    for (k in fallbackFields) {
                        val v = snap2.getString(k)
                        if (!v.isNullOrBlank()) {
                            Log.d(TAG2, "Resolved from usuarios/${uid} -> field=$k value='$v'")
                            return v
                        }
                    }
                    Log.d(TAG2, "usuarios/${uid} existe pero no tiene campos útiles")
                } else {
                    Log.d(TAG2, "usuarios/${uid} NO existe")
                }
            } catch (e: Exception) {
                Log.w(TAG2, "Error leyendo usuarios/${uid}: ${e.message}")
            }


            try {
                val q = users.whereEqualTo("uid", uid).get().await()
                if (!q.isEmpty) {
                    val d = q.documents.first()
                    Log.d(TAG2, "Query users where uid=$uid devolvió docId=${d.id} keys=${d.data?.keys}")
                    val nombreQ = d.getString("nombre")
                    if (!nombreQ.isNullOrBlank()) {
                        Log.d(TAG2, "Resolved from users query doc ${d.id} -> nombre='$nombreQ'")
                        return nombreQ
                    }
                    val fallbackFields = listOf("displayName", "name", "email", "username")
                    for (k in fallbackFields) {
                        val v = d.getString(k)
                        if (!v.isNullOrBlank()) {
                            Log.d(TAG2, "Resolved from users query doc ${d.id} -> field=$k value='$v'")
                            return v
                        }
                    }
                    Log.d(TAG2, "users query doc ${d.id} no tiene campos útiles")
                } else {
                    Log.d(TAG2, "users query where uid=$uid no devolvió docs")
                }
            } catch (e: Exception) {
                Log.w(TAG2, "Error query users where uid=$uid: ${e.message}")
            }


            try {
                val q2 = usuarios.whereEqualTo("uid", uid).get().await()
                if (!q2.isEmpty) {
                    val d = q2.documents.first()
                    Log.d(TAG2, "Query usuarios where uid=$uid devolvió docId=${d.id} keys=${d.data?.keys}")
                    val nombreQ = d.getString("nombre")
                    if (!nombreQ.isNullOrBlank()) {
                        Log.d(TAG2, "Resolved from usuarios query doc ${d.id} -> nombre='$nombreQ'")
                        return nombreQ
                    }
                    val fallbackFields = listOf("displayName", "name", "email", "username")
                    for (k in fallbackFields) {
                        val v = d.getString(k)
                        if (!v.isNullOrBlank()) {
                            Log.d(TAG2, "Resolved from usuarios query doc ${d.id} -> field=$k value='$v'")
                            return v
                        }
                    }
                    Log.d(TAG2, "usuarios query doc ${d.id} no tiene campos útiles")
                } else {
                    Log.d(TAG2, "usuarios query where uid=$uid no devolvió docs")
                }
            } catch (e: Exception) {
                Log.w(TAG2, "Error query usuarios where uid=$uid: ${e.message}")
            }

            Log.d(TAG2, "No se pudo resolver nombre para uid=$uid")
            return null
        } catch (e: Exception) {
            Log.e(TAG2, "getUserDisplayName fallo general para $uid: ${e.message}", e)
            return null
        }
    }


    suspend fun getBusinessName(businessId: String): String? {
        val TAG2 = "$TAG.getBusinessName"
        try {
            Log.d(TAG2, "Resolviendo businessName para businessId=$businessId")

            // 0) intentar businesses/{businessId} - preferido si existe una colección 'businesses'
            try {
                val bSnap = businesses.document(businessId).get().await()
                if (bSnap.exists()) {
                    Log.d(TAG2, "businesses doc existe para businessId=$businessId; keys=${bSnap.data?.keys}")
                    val bName = bSnap.getString("name")
                    if (!bName.isNullOrBlank()) {
                        Log.d(TAG2, "Resolved businessName from businesses/${businessId} -> name='$bName'")
                        return bName
                    }
                    // fallback to ownerId -> try resolve owner display name
                    val ownerId = bSnap.getString("ownerId")
                    if (!ownerId.isNullOrBlank()) {
                        Log.d(TAG2, "businesses/${businessId} tiene ownerId=$ownerId, intentando resolver displayName del owner")
                        val ownerResolved = getUserDisplayName(ownerId)
                        if (!ownerResolved.isNullOrBlank()) {
                            Log.d(TAG2, "Resolved businessName from businesses/${businessId} ownerId -> '$ownerResolved'")
                            return ownerResolved
                        }
                    }
                    Log.d(TAG2, "businesses/${businessId} existe pero no tiene campos útiles para nombre")
                } else {
                    Log.d(TAG2, "businesses/${businessId} NO existe")
                }
            } catch (e: Exception) {
                Log.w(TAG2, "Error leyendo businesses/${businessId}: ${e.message}")
            }


            try {
                val snap = chats
                    .whereEqualTo("businessId", businessId)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                if (!snap.isEmpty) {
                    val doc = snap.documents.first()
                    Log.d(TAG2, "Found chat docId=${doc.id} for businessId=$businessId; keys=${doc.data?.keys}")
                    val ownerUid = doc.getString("ownerUid")
                    if (!ownerUid.isNullOrBlank()) {
                        Log.d(TAG2, "Attempting resolve owner displayName for ownerUid=$ownerUid")
                        val resolved = getUserDisplayName(ownerUid)
                        if (!resolved.isNullOrBlank()) {
                            Log.d(TAG2, "Resolved businessName from ownerUid=$ownerUid -> '$resolved'")
                            return resolved
                        } else {
                            Log.d(TAG2, "No displayName resolved from ownerUid=$ownerUid")
                        }
                    } else {
                        Log.d(TAG2, "chat doc ${doc.id} para businessId=$businessId no tiene ownerUid")
                    }
                } else {
                    Log.d(TAG2, "No chat doc found with businessId=$businessId")
                }
            } catch (e: Exception) {
                Log.w(TAG2, "Error buscando chat por businessId=$businessId: ${e.message}")
            }


            try {
                val snap2 = users.document(businessId).get().await()
                if (snap2.exists()) {
                    Log.d(TAG2, "users doc existe para businessId=$businessId; keys=${snap2.data?.keys}")
                    val nombre = snap2.getString("nombre")
                    if (!nombre.isNullOrBlank()) {
                        Log.d(TAG2, "Resolved businessName from users/${businessId} -> nombre='$nombre'")
                        return nombre
                    }
                    val fallbackFields = listOf("displayName", "name", "email", "username")
                    for (k in fallbackFields) {
                        val v = snap2.getString(k)
                        if (!v.isNullOrBlank()) {
                            Log.d(TAG2, "Resolved businessName from users/${businessId} -> field=$k value='$v'")
                            return v
                        }
                    }
                    Log.d(TAG2, "users/${businessId} existe pero no tiene campos útiles")
                } else {
                    Log.d(TAG2, "users/${businessId} NO existe")
                }
            } catch (e: Exception) {
                Log.w(TAG2, "Error leyendo users/${businessId}: ${e.message}")
            }


            try {
                val snap3 = usuarios.document(businessId).get().await()
                if (snap3.exists()) {
                    Log.d(TAG2, "usuarios doc existe para businessId=$businessId; keys=${snap3.data?.keys}")
                    val nombre2 = snap3.getString("nombre")
                    if (!nombre2.isNullOrBlank()) {
                        Log.d(TAG2, "Resolved businessName from usuarios/${businessId} -> nombre='$nombre2'")
                        return nombre2
                    }
                    val fallbackFields = listOf("displayName", "name", "email", "username")
                    for (k in fallbackFields) {
                        val v = snap3.getString(k)
                        if (!v.isNullOrBlank()) {
                            Log.d(TAG2, "Resolved businessName from usuarios/${businessId} -> field=$k value='$v'")
                            return v
                        }
                    }
                    Log.d(TAG2, "usuarios/${businessId} existe pero no tiene campos útiles")
                } else {
                    Log.d(TAG2, "usuarios/${businessId} NO existe")
                }
            } catch (e: Exception) {
                Log.w(TAG2, "Error leyendo usuarios/${businessId}: ${e.message}")
            }

            Log.d(TAG2, "No se pudo resolver businessName para businessId=$businessId")
            return null
        } catch (e: Exception) {
            Log.e(TAG2, "getBusinessName fallo general para businessId=$businessId: ${e.message}", e)
            return null
        }
    }
}
