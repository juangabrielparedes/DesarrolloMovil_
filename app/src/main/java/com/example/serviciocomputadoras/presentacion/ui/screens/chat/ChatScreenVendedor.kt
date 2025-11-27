package com.example.serviciocomputadoras.presentacion.ui.screens.chat

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.serviciocomputadoras.data.model.Message
import com.example.serviciocomputadoras.presentacion.viewmodel.ChatViewModel
import com.example.serviciocomputadoras.presentacion.viewmodel.RepairOrderViewModel
import com.example.serviciocomputadoras.presentacion.ui.screens.vendedor.RepairOrderFormScreen
import kotlinx.coroutines.launch

@Composable
fun ChatScreenVendedor(
    chatId: String,
    clientUid: String,
    ownerUid: String,
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var text by remember { mutableStateOf("") }
    var creating by remember { mutableStateOf(true) }

    var sheetVisible by remember { mutableStateOf(false) }
    var sheetExpanded by remember { mutableStateOf(false) }

    val orderViewModel: RepairOrderViewModel = viewModel()

    LaunchedEffect(chatId) {
        if (chatId.isNotBlank()) {
            creating = true
            try {
                viewModel.startListening(chatId)
            } catch (e: Exception) {
                Log.e("ChatScreenVendedor", "Error al iniciar escucha: ${e.message}")
            } finally {
                creating = false
            }
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // == TOP BAR BLANCO ==
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Chat",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = {
                        scope.launch {
                            sheetVisible = !sheetVisible
                            if (!sheetVisible) sheetExpanded = false
                        }
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = Color.Black)
                    }
                }
            }

            if (creating && messages.isEmpty()) {
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
                    MessageRowVendedor(msg, ownerUid)
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
                    placeholder = { Text("Escribe...") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            scope.launch {
                                val cid = chatId
                                if (cid.isNotBlank()) {
                                    viewModel.sendMessage(cid, ownerUid, clientUid, text)
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

        AnimatedVisibility(
            visible = sheetVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val sheetHeight = if (sheetExpanded) 600.dp else 220.dp

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeight),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Formulario orden",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(onClick = { sheetExpanded = !sheetExpanded }) {
                            Text(if (sheetExpanded) "Reducir" else "Expandir")
                        }

                        TextButton(onClick = {
                            sheetVisible = false
                            sheetExpanded = false
                        }) {
                            Text("Cerrar")
                        }
                    }

                    Divider()

                    val businessIdFromChat = remember(chatId) {
                        val idx = chatId.indexOf('_')
                        if (idx > 0 && chatId.startsWith(clientUid)) chatId.substring(idx + 1) else ""
                    }

                    RepairOrderFormScreen(
                        clientUid = clientUid,
                        ownerUid = ownerUid,
                        businessId = businessIdFromChat,
                        onClose = {
                            sheetVisible = false
                        },
                        onCreated = { orderId, invoiceId ->
                            Log.d("ChatScreenVendedor", "Order creada: $orderId invoice=$invoiceId")
                            sheetVisible = false
                            sheetExpanded = false
                        },
                        viewModel = orderViewModel
                    )
                }
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
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
