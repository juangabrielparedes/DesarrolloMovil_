package com.example.serviciocomputadoras.presentacion.ui.screens.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.serviciocomputadoras.presentacion.viewmodel.ChatViewModel
import com.example.serviciocomputadoras.data.model.Message
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    currentUserUid: String,
    otherUserUid: String,
    businessId: String,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var text by remember { mutableStateOf("") }
    var chatId by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(true) }


    LaunchedEffect(currentUserUid, otherUserUid, businessId) {
        try {
            creating = true

            val chat = viewModel.getOrCreateChat(currentUserUid, businessId, otherUserUid)
            chatId = chat.chatId
            Log.d("DEBUG_CHAT", "ChatScreen: chatId calculado/obtenido='$chatId'")

            viewModel.startListening(chatId!!)
        } catch (e: Exception) {
            Log.e("ChatScreen", "Error al crear/obtener chat", e)
        } finally {
            creating = false
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            chatId?.let { viewModel.stopListening() }
        }
    }


    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            // scroll to last index
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {


        if (creating && chatId == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            state = listState
        ) {
            items(messages) { msg: Message ->
                MessageRow(msg, currentUserUid)
            }
        }


        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe...") },
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        scope.launch {
                            val cid = chatId
                            if (cid == null) {
                                Log.e("ChatScreen", "Intento de enviar antes de crear chat")
                            } else {
                                viewModel.sendMessage(cid, currentUserUid, otherUserUid, text)
                                text = ""
                            }
                        }
                    }
                },
                enabled = !creating && text.isNotBlank()
            ) {
                Text("Enviar")
            }
        }
    }
}

@Composable
fun MessageRow(msg: Message, currentUserUid: String) {
    val isMine = msg.senderUid == currentUserUid

    Row(
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isMine)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(10.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(
                text = msg.text,
                color = if (isMine) Color.White else Color.Black
            )
        }
    }
}

