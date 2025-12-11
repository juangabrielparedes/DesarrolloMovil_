package com.example.serviciocomputadoras.presentacion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviciocomputadoras.data.model.Chat
import com.example.serviciocomputadoras.data.model.Message
import com.example.serviciocomputadoras.data.repository.ChatRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repo: ChatRepository = ChatRepository()) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages


    private val _businessName = MutableStateFlow<String?>(null)
    val businessName: StateFlow<String?> = _businessName


    private val _clientName = MutableStateFlow<String?>(null)
    val clientName: StateFlow<String?> = _clientName

    private var registration: ListenerRegistration? = null

    suspend fun getOrCreateChat(clientUid: String, businessId: String, ownerUid: String): Chat {
        return repo.createOrGetChat(clientUid, businessId, ownerUid)
    }

    fun createOrGetChat(clientUid: String, businessId: String, ownerUid: String) = viewModelScope.launch {
        try {
            repo.createOrGetChat(clientUid, businessId, ownerUid)
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error creando/obteniendo chat", e)
        }
    }

    fun startListening(chatId: String) {
        Log.d("ChatViewModel", "startListening -> chatId='$chatId'")
        registration?.remove()
        registration = repo.listenMessages(chatId) { list ->
            Log.d("ChatViewModel", "listenMessages callback -> ${list.size} mensajes (chatId='$chatId')")
            list.forEach { m ->
                Log.d("ChatViewModel", "MSG id=${m.messageId} chatId=${m.chatId} sender=${m.senderUid} text='${m.text}'")
            }
            _messages.value = list
        }
    }

    fun stopListening() {
        Log.d("ChatViewModel", "stopListening")
        registration?.remove()
        registration = null
    }

    fun sendMessage(chatId: String, senderUid: String, receiverUid: String, text: String) = viewModelScope.launch {
        try {
            Log.d("ChatViewModel", "sendMessage -> chatId=$chatId sender=$senderUid receiver=$receiverUid text='$text'")
            repo.sendMessage(chatId, senderUid, receiverUid, text)
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error enviando mensaje", e)
        }
    }


    fun loadBusinessName(businessId: String) = viewModelScope.launch {
        try {
            val name = try {
                repo.getBusinessName(businessId)
            } catch (e: NoSuchMethodError) {
                Log.w("ChatViewModel", "ChatRepository no implementa getBusinessName(businessId). ${e.message}")
                null
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error al obtener businessName desde repo", e)
                null
            }
            _businessName.value = name
            Log.d("ChatViewModel", "loadBusinessName -> businessId=$businessId name=$name")
        } catch (e: Exception) {
            Log.e("ChatViewModel", "loadBusinessName fallo", e)
            _businessName.value = null
        }
    }


    fun loadClientName(clientUid: String) = viewModelScope.launch {
        try {
            val name = try {
                repo.getUserDisplayName(clientUid)
            } catch (e: NoSuchMethodError) {
                Log.w("ChatViewModel", "ChatRepository no implementa getUserDisplayName(uid). ${e.message}")
                null
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error al obtener clientName desde repo", e)
                null
            }
            _clientName.value = name
            Log.d("ChatViewModel", "loadClientName -> clientUid=$clientUid name=$name")
        } catch (e: Exception) {
            Log.e("ChatViewModel", "loadClientName fallo", e)
            _clientName.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        registration?.remove()
    }
}
