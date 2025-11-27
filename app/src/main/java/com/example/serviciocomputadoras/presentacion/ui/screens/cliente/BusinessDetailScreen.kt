package com.example.serviciocomputadoras.presentacion.ui.screens.cliente


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.serviciocomputadoras.presentacion.viewmodel.BusinessViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    businessId: String,
    viewModel: BusinessViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val requestStatus by viewModel.requestStatus.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    val business = businesses.find { it.id == businessId }

    var showForm by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var preferredDate by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    val clientEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    // Obtener nombre del usuario
    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val userDoc = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .await()
            clientName = userDoc.getString("nombre") ?: ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4654A3))  // Tu fondo azul
    ) {
        Column(
            Modifier
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
                if (business == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@Card
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // Botón atrás
                    TextButton(onClick = onBack) {
                        Text("← Atrás", color = Color(0xFF4654A3))
                    }

                    Spacer(Modifier.height(8.dp))

                    // Nombre del negocio
                    Text(
                        business.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(8.dp))

                    // Descripción
                    Text(business.description, color = Color.Gray)

                    Spacer(Modifier.height(12.dp))

                    // Mensaje de estado
                    requestStatus?.let {
                        Text(
                            text = it,
                            color = if (it.contains("éxito")) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Formulario de solicitud
                    if (showForm) {
                        Text("Tu nombre:", color = Color.Black)
                        Text(clientName, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))

                        Text("Tu correo:", color = Color.Black)
                        Text(clientEmail, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))

                        Text("Fecha preferida (YYYY-MM-DD):", color = Color.Black)
                        OutlinedTextField(
                            value = preferredDate,
                            onValueChange = { preferredDate = it },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4654A3),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        Text("Describe tu problema:", color = Color.Black)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4654A3),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (description.isBlank()) return@Button
                                if (preferredDate.isNotBlank()) {
                                    try {
                                        val selectedDate = LocalDate.parse(preferredDate)
                                        if (selectedDate.isBefore(LocalDate.now())) return@Button
                                    } catch (_: Exception) {
                                        return@Button
                                    }
                                }

                                scope.launch {
                                    viewModel.createRequest(
                                        businessId = business.id,
                                        description = description,
                                        preferredDate = preferredDate.ifBlank { null }
                                    )
                                    description = ""
                                    preferredDate = ""
                                    showForm = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4654A3)
                            )
                        ) {
                            Text(if (loading) "Enviando..." else "Enviar solicitud")
                        }

                    } else {
                        Button(
                            onClick = { showForm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4654A3)
                            )
                        ) {
                            Text("Solicitar servicio")
                        }
                    }
                }
            }
        }
    }
}