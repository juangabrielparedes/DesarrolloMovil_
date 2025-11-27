package com.example.serviciocomputadoras.presentacion.ui.screens.cliente

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.serviciocomputadoras.presentacion.viewmodel.AuthViewModel
import com.example.serviciocomputadoras.presentacion.viewmodel.BusinessViewModel
import com.example.serviciocomputadoras.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.runtime.collectAsState
import com.example.serviciocomputadoras.presentacion.ui.screens.chat.ChatScreen
import com.example.serviciocomputadoras.data.model.Business
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreenCliente(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val businessViewModel: BusinessViewModel = viewModel()
    val items = getBottomNavItems("Cliente")


    val authState by authViewModel.authState.collectAsState()
    Log.d("DEBUG_AUTH", "authState = $authState")


    val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
    val currentUid = firebaseUid ?: authState.user?.uid ?: ""

    Scaffold(
        containerColor = Color(0xFF4654A3),
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
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            NavHost(navController = navController, startDestination = items.first().route) {


                composable(BottomNavItem.HomeCliente.route) {
                    HomeClienteContent(authViewModel)
                }


                composable(BottomNavItem.ExplorarCliente.route) {
                    BusinessListScreen(
                        viewModel = businessViewModel,
                        currentUid = currentUid,
                        onOpenBusiness = { business ->
                            navController.navigate("business_detail/${business.id}")
                        },
                        onOpenChat = { business, currentUidLocal ->
                            val ownerId = business.ownerId
                            val businessId = business.id

                            if (currentUidLocal.isNotBlank() && ownerId.isNotBlank() && businessId.isNotBlank()) {
                                navController.navigate("chat/$currentUidLocal/$ownerId/$businessId")
                            } else {
                                Log.e("DEBUG_CHAT", "No se puede abrir chat, datos incompletos")
                            }
                        }
                    )
                }


                composable(BottomNavItem.CarritoCliente.route) {
                    CarritoClienteContent()
                }


                composable(BottomNavItem.FacturasCliente.route) {
                    InvoicesClienteScreen(currentUid = currentUid, onOpenInvoice = { invoiceId ->
                        // podrías navegar a detalle si quieres
                        navController.navigate("invoice_detail/$invoiceId")
                    })
                }


                composable(BottomNavItem.PerfilCliente.route) {
                    PerfilClienteContent(authViewModel, onLogout)
                }


                composable("business_detail/{businessId}") { backStackEntry ->
                    val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
                    BusinessDetailScreen(
                        businessId = businessId,
                        viewModel = businessViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }


                composable("chat/{currentUserUid}/{ownerId}/{businessId}") { backStackEntry ->
                    val currentUserUid = backStackEntry.arguments?.getString("currentUserUid") ?: ""
                    val ownerId = backStackEntry.arguments?.getString("ownerId") ?: ""
                    val businessId = backStackEntry.arguments?.getString("businessId") ?: ""

                    if (currentUserUid.isNotBlank() && ownerId.isNotBlank() && businessId.isNotBlank()) {
                        ChatScreen(
                            currentUserUid = currentUserUid,
                            otherUserUid = ownerId,
                            businessId = businessId
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: datos del chat incompletos")
                        }
                    }
                }

                // (Opcional) Ruta de detalle de invoice — puedes implementarla si quieres
                composable("invoice_detail/{invoiceId}") { back ->
                    val invoiceId = back.arguments?.getString("invoiceId") ?: ""
                    InvoiceDetailScreen(invoiceId = invoiceId, currentUid = currentUid)
                }
            }
        }
    }
}


@Composable
fun HomeClienteContent(viewModel: AuthViewModel) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4654A3)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¡Bienvenido, ${usuario?.nombre}!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Text(
                    text = "Rol: Cliente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Explora servicios de computadoras",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun CarritoClienteContent() {
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
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4654A3)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mis Solicitudes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Text(
                    text = "Servicios solicitados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PerfilClienteContent(viewModel: AuthViewModel, onLogout: () -> Unit) {
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
                    text = usuario?.nombre ?: "Usuario",
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
