package com.example.serviciocomputadoras.presentacion.ui.screens.vendedor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.serviciocomputadoras.presentacion.viewmodel.AuthViewModel
import com.example.serviciocomputadoras.navigation.*
import androidx.compose.runtime.collectAsState

@Composable
fun MainScreenVendedor(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = getBottomNavItems("Vendedor")
    val usuario = authViewModel.authState.collectAsState().value.user  // Para obtener UID


    LaunchedEffect(usuario) {
        android.util.Log.d("DEBUG", "=== VENDEDOR LOGUEADO ===")
        android.util.Log.d("DEBUG", "UID: ${usuario?.uid}")
        android.util.Log.d("DEBUG", "Email: ${usuario?.email}")
        android.util.Log.d("DEBUG", "Nombre: ${usuario?.nombre}")
    }
    Scaffold(
        containerColor = Color(0xFF4654A3),
        topBar = {
            Text(
                text = "UID: ${usuario?.uid ?: "null"}",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF4654A3))
        ) {
            NavHost(
                navController = navController,
                startDestination = items.first().route
            ) {
                //Ahora TiendaVendedor muestra OwnerPanelScreen
                composable(BottomNavItem.TiendaVendedor.route) {
                    OwnerPanelScreen(ownerUid = usuario?.uid ?: "")
                }
                composable(BottomNavItem.ProductosVendedor.route) {
                    ProductosVendedorContent()
                }
                composable(BottomNavItem.VentasVendedor.route) {
                    VentasVendedorContent()
                }
                composable(BottomNavItem.PerfilVendedor.route) {
                    PerfilVendedorContent(authViewModel, onLogout)
                }
            }
        }
    }
}

// Mantén el resto de funciones igual...
@Composable
fun ProductosVendedorContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Productos",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4654A3)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mis Servicios",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Text(
                    text = "Administra tus servicios ofrecidos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun VentasVendedorContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Ventas",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4654A3)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mis Ventas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Text(
                    text = "Historial de servicios prestados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PerfilVendedorContent(viewModel: AuthViewModel, onLogout: () -> Unit) {
    val usuario = viewModel.authState.collectAsState().value.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4654A3)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mi Perfil",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Text(
                    text = usuario?.nombre ?: "Vendedor",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = usuario?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}