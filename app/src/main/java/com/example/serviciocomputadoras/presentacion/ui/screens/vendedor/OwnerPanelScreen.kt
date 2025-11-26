package com.example.serviciocomputadoras.presentacion.ui.screens.vendedor


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.serviciocomputadoras.data.model.Request
import com.example.serviciocomputadoras.presentacion.viewmodel.RequestViewModel
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPanelScreen(
    ownerUid: String,
    requestViewModel: RequestViewModel = viewModel()
) {
    val state by requestViewModel.uiState.collectAsStateWithLifecycle()
    val firestore = FirebaseFirestore.getInstance()

    // Nombres de negocios (para mostrar en la UI)
    val businessNames = remember { mutableStateMapOf<String, String>() }

    // Filtro de estado
    var selectedFilter by remember { mutableStateOf("all") }
    val filtros = listOf("all", "pending", "accepted", "rejected")
    val filtroLabels = mapOf(
        "all" to "Todas",
        "pending" to "Pendientes",
        "accepted" to "Aceptadas",
        "rejected" to "Rechazadas"
    )

    var expanded by remember { mutableStateOf(false) }

    // Cargar solicitudes del vendedor
    LaunchedEffect(ownerUid) {
        requestViewModel.loadRequestsForOwner(ownerUid)
    }

    // Cargar nombres de negocios
    LaunchedEffect(state.requests) {
        val ids = state.requests.map { it.businessId }.distinct()
        ids.forEach { id ->
            try {
                val doc = firestore.collection("businesses").document(id).get().await()
                businessNames[id] = doc.getString("name") ?: id
            } catch (_: Exception) {
                businessNames[id] = id
            }
        }
    }

    // Filtrar solicitudes
    val filteredRequests = remember(state.requests, selectedFilter) {
        when (selectedFilter) {
            "pending" -> state.requests.filter { it.status.equals("pending", true) }
            "accepted" -> state.requests.filter { it.status.equals("accepted", true) }
            "rejected" -> state.requests.filter { it.status.equals("rejected", true) }
            else -> state.requests
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4654A3))  // Tu fondo azul
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Solicitudes de tus servicios",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dropdown para filtros
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                filtroLabels[selectedFilter] ?: "Filtro",
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            filtros.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(filtroLabels[f] ?: f) },
                                    onClick = {
                                        selectedFilter = f
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contenido
                    when {
                        state.loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        filteredRequests.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay solicitudes para mostrar",
                                    color = Color.Gray
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 480.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredRequests) { request ->
                                    RequestItem(
                                        request = request,
                                        businessName = businessNames[request.businessId] ?: "Cargando...",
                                        onAccept = {
                                            requestViewModel.updateRequestStatus(
                                                request.id,
                                                "accepted"
                                            )
                                        },
                                        onReject = {
                                            requestViewModel.updateRequestStatus(
                                                request.id,
                                                "rejected"
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    state.error?.let {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Error: $it", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItem(
    request: Request,
    businessName: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Cliente: ${request.clientName ?: request.clientEmail}",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black
            )
            Spacer(Modifier.height(4.dp))

            Text(
                "Servicio: $businessName",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))

            Text(
                "Descripción: ${request.description}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )

            request.preferredDate?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Fecha preferida: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(8.dp))

            // Badge de estado
            Surface(
                color = when (request.status.lowercase()) {
                    "pending" -> Color(0xFFFFA726)
                    "accepted" -> Color(0xFF66BB6A)
                    "rejected" -> Color(0xFFEF5350)
                    else -> Color.Gray
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (request.status.lowercase()) {
                        "pending" -> "Pendiente"
                        "accepted" -> "Aceptada"
                        "rejected" -> "Rechazada"
                        else -> request.status
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Botones de acción (solo si está pendiente)
            if (request.status.equals("pending", true)) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("✓ Aceptar")
                    }
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF5350)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("✗ Rechazar")
                    }
                }
            }
        }
    }
}