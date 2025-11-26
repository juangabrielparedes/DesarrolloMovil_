package com.example.serviciocomputadoras.presentacion.ui.screens.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.serviciocomputadoras.data.model.Message
import com.example.serviciocomputadoras.presentacion.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreenVendedor(
    chatId: String,
    clientUid: String,
    ownerUid: String,
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    var text by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Top bar + content layout
    Column(modifier = Modifier.fillMaxSize()) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    Log.d("ChatScreenVendedor", "Back pressed - navigating up to chats")
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chat",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }


        LaunchedEffect(chatId) {
            if (chatId.isNotBlank()) {
                viewModel.startListening(chatId)
                // Si quieres marcar como leído, agrega función en repo y llámala aquí.
            }
        }

        DisposableEffect(Unit) {
            onDispose { viewModel.stopListening() }
        }


        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }


        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            state = listState
        ) {
            items(messages) { msg: Message ->
                MessageRowVendedor(msg = msg, currentUserUid = ownerUid)
            }
        }


        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (text.isNotBlank()) {
                    scope.launch {
                        try {
                            // enviar con receiver = clientUid
                            viewModel.sendMessage(chatId, ownerUid, clientUid, text)
                            text = ""
                        } catch (e: Exception) {
                            Log.e("ChatScreenVendedor", "Error enviando mensaje: ${e.message}")
                        }
                    }
                }
            }) {
                Text("Enviar")
            }
        }
    }
}


@Composable
fun MessageRowVendedor(msg: Message, currentUserUid: String) {
    val isMine = msg.senderUid == currentUserUid

    Row(
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Surface(
            tonalElevation = if (isMine) 4.dp else 0.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

