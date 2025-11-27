package com.example.serviciocomputadoras.presentacion.ui.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.serviciocomputadoras.presentacion.viewmodel.BusinessViewModel
import com.example.serviciocomputadoras.data.model.Business
import android.util.Log


@Composable
fun BusinessListScreen(
    viewModel: BusinessViewModel,
    currentUid: String? = null,
    onOpenBusiness: (Business) -> Unit,
    onOpenChat: ((Business, String) -> Unit)? = null
) {
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val list by viewModel.businesses.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4654A3))  // Tu fondo azul
            .padding(16.dp)
    ) {
        // Card para búsqueda
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Buscar Servicios",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar por nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4654A3),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color(0xFF4654A3)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { viewModel.search(query) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4654A3)
                        )
                    ) {
                        Text("Buscar")
                    }
                    Button(
                        onClick = {
                            query = ""
                            viewModel.loadAll()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text("Refrescar")
                    }
                }
            }
        }

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
            return@Column
        }

        if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(list) { business ->
                BusinessItem(
                    business = business,
                    onClick = { onOpenBusiness(business) },
                    onOpenChat = {
                        // Si el callback onOpenChat llegó desde MainScreenCliente, lo llamamos con los datos.
                        // Si currentUid es null o vacío, se registra en log y no se crash.
                        val uidToUse = currentUid ?: ""
                        Log.d("DEBUG_CHAT", "BusinessItem click -> currentUid='$uidToUse', ownerId='${business.ownerId}', businessId='${business.id}'")
                        onOpenChat?.invoke(business, uidToUse)
                    },
                    showChatButton = onOpenChat != null // Si viene callback, mostramos botón
                )
            }
        }
    }
}

@Composable
fun BusinessItem(business: Business, onClick: () -> Unit, onOpenChat: (() -> Unit)? = null, showChatButton: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = business.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = business.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Desde: \$${business.priceStarting}",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF4654A3)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onClick() }) { Text("Ver detalle") }

                if (showChatButton) {
                    Button(
                        onClick = { onOpenChat?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4654A3))
                    ) { Text("Abrir chat") }
                }
            }
        }
    }
}

