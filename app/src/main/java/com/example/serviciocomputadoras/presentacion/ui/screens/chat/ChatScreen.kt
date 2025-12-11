package com.example.serviciocomputadoras.presentacion.ui.screens.chat

import android.util.Log
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.serviciocomputadoras.presentacion.viewmodel.ChatViewModel
import com.example.serviciocomputadoras.data.model.Message
import com.google.firebase.Timestamp
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
fun ChatScreen(
    currentUserUid: String,
    otherUserUid: String,
    businessId: String,

    navController: NavController? = null,
    businessName: String? = null,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val autoBusinessName by viewModel.businessName.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var text by remember { mutableStateOf("") }
    var chatId by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(true) }


    LaunchedEffect(businessId) {
        try {
            if (businessName.isNullOrBlank()) {
                viewModel.loadBusinessName(businessId)
            }
        } catch (e: Exception) {
            Log.e("ChatScreen", "Error cargando businessName automático", e)
        }
    }

    LaunchedEffect(currentUserUid, otherUserUid, businessId) {
        try {
            creating = true
            val chat = viewModel.getOrCreateChat(currentUserUid, businessId, otherUserUid)
            chatId = chat.chatId
            Log.d("DEBUG_CHAT", "ChatScreen: chatId creado='$chatId'")
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
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor)
    ) {


        Surface(
            color = TopBarBackground,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController?.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TopBarContentColor
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))


                val resolvedName = businessName ?: autoBusinessName
                BusinessAvatar(name = resolvedName, size = 40.dp)

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = resolvedName ?: "Chat",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TopBarContentColor,
                    modifier = Modifier.weight(1f)
                )


            }
        }


        if (creating && chatId == null && messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }


        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            state = listState
        ) {
            items(messages) { msg: Message ->
                MessageRow(msg, currentUserUid)
            }
        }


        Row(
            modifier = Modifier
                .padding(12.dp)
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
                            chatId?.let { cid ->
                                viewModel.sendMessage(cid, currentUserUid, otherUserUid, text)
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
}

@Composable
private fun BusinessAvatar(name: String?, size: Dp = 40.dp) {
    val initials = remember(name) {
        name?.trim()
            ?.split("\\s+".toRegex())
            ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
            ?.take(2)
            ?.joinToString("") ?: "B"
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
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun MessageRow(msg: Message, currentUserUid: String) {
    val isMine = msg.senderUid == currentUserUid

    val timeFormat = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    // Formatear Firebase Timestamp? -> Date
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
                    .clip(RoundedCornerShape(12.dp))
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
                        color = if (isMine) Color.White else Color.Black
                    )
                }
            }


            if (formattedTime.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedTime,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = Color.Gray
                )
            }
        }
    }
}
