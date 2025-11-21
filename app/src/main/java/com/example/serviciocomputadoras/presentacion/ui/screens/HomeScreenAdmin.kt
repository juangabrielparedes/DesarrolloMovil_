package com.example.serviciocomputadoras.presentacion.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.serviciocomputadoras.presentacion.viewmodel.AuthViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun HomeScreenAdmin(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val usuario = viewModel.authState.collectAsState().value.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Admin",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Bienvenido, ${usuario?.nombre}!",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Rol: ${usuario?.rol}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.error
        )

        Text(
            text = "Administra la aplicación completa",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
    }
}