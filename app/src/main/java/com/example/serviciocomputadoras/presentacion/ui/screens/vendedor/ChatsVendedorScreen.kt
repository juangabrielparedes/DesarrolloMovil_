package com.example.serviciocomputadoras.presentacion.ui.screens.vendedor

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.serviciocomputadoras.presentacion.viewmodel.ChatUi
import com.example.serviciocomputadoras.presentacion.viewmodel.ChatsVendedorViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "ChatsVendedorUI"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatsVendedorScreen(
    ownerUid: String,
    navController: NavController,
    viewModel: ChatsVendedorViewModel = viewModel()
) {
    val chats by viewModel.chatsUi.collectAsState()
    val blue = Color(0xFF4654A3)

    LaunchedEffect(ownerUid) {
        Log.d(TAG, "ChatsVendedorScreen LaunchedEffect ownerUid='$ownerUid'")
        if (ownerUid.isNotBlank()) viewModel.startListening(ownerUid)
        else viewModel.stopListening()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopListening() }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(blue)
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chats",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }

                    Button(onClick = {
                        Log.d(TAG, "Botón Refrescar pulsado -> fetchOnce")
                        viewModel.fetchOnce(ownerUid)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                        Text("Refrescar", color = blue)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes conversaciones aún",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(chats) { chat ->
                        ChatListItem(chat = chat) {
                            val chatIdEncoded = Uri.encode(chat.chatId)
                            val clientUidEncoded = Uri.encode(chat.clientUid)
                            navController.navigate("chat_detail/$chatIdEncoded/$clientUidEncoded")
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatListItem(chat: ChatUi, onClick: () -> Unit) {
    val blue = Color(0xFF4654A3)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(blue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.clientName.firstOrNull()?.uppercase() ?: "C",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.clientName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chat.lastMessage,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Text(
                text = formatTimestamp(chat.updatedAtMillis),
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTimestamp(tsMillis: Long): String {
    if (tsMillis <= 0L) return ""
    val zdt = Instant.ofEpochMilli(tsMillis).atZone(ZoneId.systemDefault())
    val now = Instant.now().atZone(ZoneId.systemDefault())
    return if (zdt.toLocalDate() == now.toLocalDate()) {
        zdt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    } else {
        zdt.toLocalDate().toString()
    }
}
