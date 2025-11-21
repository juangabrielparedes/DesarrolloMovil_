package com.example.serviciocomputadoras.presentacion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.serviciocomputadoras.presentacion.viewmodel.AuthViewModel

@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Has iniciado sesión correctamente",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            }
        ) {
            Text("Cerrar Sesión")
        }
    }
}