package com.example.serviciocomputadoras.presentacion.ui.screens.vendedor

import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

// Importar pantallas relacionadas con chats y órdenes
import com.example.serviciocomputadoras.presentacion.ui.screens.chat.ChatScreenVendedor
import com.example.serviciocomputadoras.presentacion.ui.screens.vendedor.ChatsVendedorScreen
import com.example.serviciocomputadoras.presentacion.ui.screens.vendedor.OrdersVendedorScreen

private const val TAG = "MainScreenVendedor"

@Composable
fun MainScreenVendedor(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = getBottomNavItems("Vendedor")

    // 🔥 TOMAMOS UID DIRECTAMENTE DEL FIREBASEAUTH
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val firebaseUid = firebaseUser?.uid ?: ""

    // Esto sigue sirviendo para nombre y email
    val usuario = authViewModel.authState.collectAsState().value.user

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Log para saber siempre si FirebaseAuth ya tiene UID
    LaunchedEffect(firebaseUid) {
        Log.d(TAG, "FirebaseAuth UID = '${firebaseUid}'  (null si no logueado)")
    }

    LaunchedEffect(usuario) {
        Log.d(
            TAG,
            "AuthViewModel usuario: uid='${usuario?.uid}', nombre='${usuario?.nombre}', email='${usuario?.email}'"
        )
    }

    Scaffold(
        containerColor = Color(0xFF4654A3),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {

                            Log.d(TAG, "BottomNavItem clicked -> ${item.title}")

                            // Evitar navegar a Chats u Órdenes si NO hay usuario en Firebase
                            if ((item == BottomNavItem.ChatsVendedor || item == BottomNavItem.OrdenesVendedor) && firebaseUid.isBlank()) {
                                scope.launch {
                                    Log.d(TAG, "No se puede abrir ${item.title}: FirebaseAuth UID está vacío")
                                    snackbarHostState.showSnackbar("Cargando usuario...")
                                }
                                return@NavigationBarItem
                            }

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

                composable(BottomNavItem.TiendaVendedor.route) {
                    OwnerPanelScreen(ownerUid = firebaseUid)
                }

                composable(BottomNavItem.ProductosVendedor.route) {
                    ProductosVendedorContent()
                }

                composable(BottomNavItem.VentasVendedor.route) {
                    VentasVendedorContent()
                }

                // Chats (lista)
                composable(BottomNavItem.ChatsVendedor.route) {
                    Log.d(TAG, "Entrando a ChatsVendedorScreen con ownerUid='$firebaseUid'")
                    ChatsVendedorScreen(
                        ownerUid = firebaseUid,
                        navController = navController
                    )
                }

                // Órdenes (lista ordenada por fecha próxima)
                composable(BottomNavItem.OrdenesVendedor.route) {
                    Log.d(TAG, "Entrando a OrdersVendedorScreen con ownerUid='$firebaseUid'")
                    OrdersVendedorScreen(ownerUid = firebaseUid)
                }

                composable(BottomNavItem.PerfilVendedor.route) {
                    PerfilVendedorContent(authViewModel, onLogout)
                }

                // Detalle chat: navegar a la pantalla de chat con navController (para poder volver)
                composable("chat_detail/{chatId}/{clientUid}") { back ->
                    val chatId = Uri.decode(back.arguments?.getString("chatId") ?: "")
                    val clientUid = Uri.decode(back.arguments?.getString("clientUid") ?: "")

                    Log.d(TAG, "Navegando a ChatScreenVendedor -> chatId='$chatId' clientUid='$clientUid' ownerUid='$firebaseUid'")

                    ChatScreenVendedor(
                        chatId = chatId,
                        clientUid = clientUid,
                        ownerUid = firebaseUid,
                        navController = navController
                    )
                }
            }
        }
    }
}


// ========================
//  CONTENIDO VISUAL (se mantienen igual que antes)
// ========================

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}
