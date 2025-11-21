package com.example.serviciocomputadoras.presentacion.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.serviciocomputadoras.presentacion.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            showSuccessDialog = true
            viewModel.resetState()
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onNavigateBack()
            },
            title = { Text("Correo enviado") },
            text = { Text("Se ha enviado un correo de recuperación a tu dirección. Revisa tu bandeja de entrada.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onNavigateBack()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Recuperar Contraseña",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email"
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (authState.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = authState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.resetPassword(email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading && email.isNotBlank()
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Enviar correo de recuperación")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Volver al inicio de sesión")
        }
    }
}