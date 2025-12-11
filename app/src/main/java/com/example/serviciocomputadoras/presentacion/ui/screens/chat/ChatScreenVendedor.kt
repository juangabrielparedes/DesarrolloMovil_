package com.example.serviciocomputadoras.presentacion.ui.screens.chat

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.serviciocomputadoras.data.model.Message
import com.example.serviciocomputadoras.presentacion.viewmodel.ChatViewModel
import com.example.serviciocomputadoras.presentacion.viewmodel.RepairOrderViewModel
import com.example.serviciocomputadoras.presentacion.ui.screens.vendedor.RepairOrderFormScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


private val AppBackgroundColor = Color(0xFFF5F6F8)
private val MyMessageColor = Color(0xFFAAAAAA)
private val OtherMessageColor = Color.White
private val BorderColor = Color(0x22000000)
private val SendButtonColor = Color(0xFF4654A3)
private val InputFieldBackground = Color.White
private val TopBarBackground = Color(0xFF4654A3)
private val TopBarContentColor = Color.White

@Composable
fun ChatScreenVendedor(
    chatId: String,
    clientUid: String,
    ownerUid: String,
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val clientName by viewModel.clientName.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var text by remember { mutableStateOf("") }
    var creating by remember { mutableStateOf(true) }

    var sheetVisible by remember { mutableStateOf(false) }
    var sheetExpanded by remember { mutableStateOf(false) }

    val orderViewModel: RepairOrderViewModel = viewModel()


    LaunchedEffect(clientUid) {
        try {
            if (clientUid.isNotBlank()) {
                viewModel.loadClientName(clientUid)
            }
        } catch (e: Exception) {
            Log.e("ChatScreenVendedor", "Error cargando clientName automático", e)
        }
    }

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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(AppBackgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TopBarBackground)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TopBarContentColor)
                    }

                    Spacer(modifier = Modifier.width(8.dp))


                    UserAvatar(name = clientName, size = 40.dp)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = clientName ?: "Cliente",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TopBarContentColor,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = {
                        scope.launch {
                            sheetVisible = !sheetVisible
                            if (!sheetVisible) sheetExpanded = false
                        }
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = TopBarContentColor)
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
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                state = listState
            ) {
                items(messages) { msg: Message ->
                    MessageRowVendedor(msg, ownerUid)
                }
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    placeholder = { Text("Escribe...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = SendButtonColor,
                        focusedContainerColor = InputFieldBackground,
                        unfocusedContainerColor = InputFieldBackground
                    )
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
                    enabled = !creating && text.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SendButtonColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(48.dp)
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
private fun UserAvatar(name: String?, size: Dp = 40.dp) {
    val initials = remember(name) {
        name?.trim()
            ?.split("\\s+".toRegex())
            ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
            ?.take(2)
            ?.joinToString("") ?: "C"
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MyMessageColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun MessageRowVendedor(msg: Message, currentUserUid: String) {
    val isMine = msg.senderUid == currentUserUid

    val timeFormat = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }


    val formattedTime = try {
        msg.timestamp?.toDate()?.let { date -> timeFormat.format(date) } ?: ""
    } catch (_: Exception) {
        ""
    }

    Row(
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {

        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {

            Surface(
                color = if (isMine) MyMessageColor else OtherMessageColor,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .then(
                        if (!isMine) Modifier.border(
                            width = 1.dp,
                            color = BorderColor,
                            shape = RoundedCornerShape(12.dp)
                        ) else Modifier
                    )
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = msg.text,
                        color = if (isMine) Color.White else Color.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }


            if (formattedTime.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = Color.Gray
                )
            }
        }
    }
}

