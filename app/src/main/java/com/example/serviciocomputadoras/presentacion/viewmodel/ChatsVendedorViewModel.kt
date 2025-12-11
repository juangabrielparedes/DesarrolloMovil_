package com.example.serviciocomputadoras.presentacion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviciocomputadoras.data.model.Chat
import com.example.serviciocomputadoras.data.repository.ChatRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "ChatsVendedorVM"

data class ChatUi(
    val chatId: String,
    val clientUid: String,
    val clientName: String,
    val lastMessage: String,
    val updatedAtMillis: Long
)

class ChatsVendedorViewModel(private val repo: ChatRepository = ChatRepository()): ViewModel() {

    private val _chatsUi = MutableStateFlow<List<ChatUi>>(emptyList())
    val chatsUi: StateFlow<List<ChatUi>> = _chatsUi


    private val _debugLog = MutableStateFlow<String>("")
    val debugLog: StateFlow<String> = _debugLog

    private var registration: ListenerRegistration? = null

    fun startListening(ownerUid: String) {
        Log.d(TAG, "startListening called for ownerUid='$ownerUid'")
        _debugLog.value = "startListening called for ownerUid='$ownerUid'"

        registration?.remove()
        registration = repo.listenChatsForOwner(ownerUid) { list ->
            Log.d(TAG, "listenChatsForOwner callback -> recibidos ${list.size} chats")
            _debugLog.value = "Recibidos ${list.size} chats (snapshot). Resolviendo nombres..."
            // resolver nombres en coroutine
            viewModelScope.launch {
                try {
                    val uiList = mutableListOf<ChatUi>()
                    for (chat in list) {

                        val rawName = try {
                            repo.getUserDisplayName(chat.clientUid)
                        } catch (e: Exception) {
                            Log.w(TAG, "getUserDisplayName fallo para ${chat.clientUid}: ${e.message}")
                            null
                        }


                        val name = if (!rawName.isNullOrBlank()) {
                            rawName
                        } else {

                            val short = if (chat.clientUid.length >= 6) chat.clientUid.take(6) else chat.clientUid
                            "Cliente ($short)"
                        }

                        val tsMillis = chat.updatedAt?.toDate()?.time ?: 0L
                        uiList.add(ChatUi(chat.chatId, chat.clientUid, name, chat.lastMessage, tsMillis))

                        Log.d(TAG, "mapped chatId=${chat.chatId} clientUid=${chat.clientUid} -> name=$name (rawName=${rawName ?: "null"})")
                    }
                    _chatsUi.value = uiList
                    _debugLog.value = "Chats mapeados: ${uiList.size}"
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapeando chats", e)
                    _debugLog.value = "Error mapeando chats: ${e.message}"
                }
            }
        }
    }

    fun stopListening() {
        Log.d(TAG, "stopListening")
        _debugLog.value = "Stop listening"
        registration?.remove()
        registration = null
    }

    /**
     * fetchOne-shot (útil para debug / refresh manual desde UI)
     */
    fun fetchOnce(ownerUid: String) {
        Log.d(TAG, "fetchOnce called for ownerUid='$ownerUid'")
        _debugLog.value = "fetchOnce called for ownerUid='$ownerUid'"
        viewModelScope.launch {
            try {
                val list = repo.fetchChatsForOwnerOnce(ownerUid)
                Log.d(TAG, "fetchOnce got ${list.size} chats")
                _debugLog.value = "fetchOnce got ${list.size} chats"
                val uiList = list.map { c ->
                    val rawName = try {
                        repo.getUserDisplayName(c.clientUid)
                    } catch (e: Exception) {
                        Log.w(TAG, "getUserDisplayName fallo en fetchOnce para ${c.clientUid}: ${e.message}")
                        null
                    }
                    val name = if (!rawName.isNullOrBlank()) {
                        rawName
                    } else {
                        val short = if (c.clientUid.length >= 6) c.clientUid.take(6) else c.clientUid
                        "Cliente ($short)"
                    }
                    ChatUi(c.chatId, c.clientUid, name, c.lastMessage, c.updatedAt?.toDate()?.time ?: 0L)
                }
                _chatsUi.value = uiList
            } catch (e: Exception) {
                Log.e(TAG, "fetchOnce error: ${e.message}", e)
                _debugLog.value = "fetchOnce error: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        registration?.remove()
    }
}
