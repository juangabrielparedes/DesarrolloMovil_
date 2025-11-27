package com.example.serviciocomputadoras.presentacion.ui.screens.admin


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.serviciocomputadoras.presentacion.viewmodel.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.serviciocomputadoras.navigation.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.serviciocomputadoras.presentacion.viewmodel.AdminViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import com.example.serviciocomputadoras.data.model.User
import kotlinx.coroutines.delay

@Composable
fun MainScreenAdmin(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = getBottomNavItems("Admin")
    val adminViewModel: AdminViewModel = viewModel()

    Scaffold(
        containerColor = Color(0xFF4654A3),
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
                composable(BottomNavItem.DashboardAdmin.route) {
                    DashboardAdminContent(authViewModel, adminViewModel)
                }
                composable(BottomNavItem.UsuariosAdmin.route) {
                    UsuariosAdminContent(adminViewModel)
                }
                composable(BottomNavItem.GestionAdmin.route) {
                    GestionAdminContent(adminViewModel)
                }
                composable(BottomNavItem.PerfilAdmin.route) {
                    PerfilAdminContent(authViewModel, onLogout)
                }
            }
        }
    }
}


@Composable
fun DashboardAdminContent(
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel
) {
    val usuario = authViewModel.authState.collectAsState().value.user
    val adminState by adminViewModel.adminState.collectAsStateWithLifecycle()
    val stats = adminState.estadisticas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
                Text(
                    text = "⚙️",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dashboard Admin",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Administrador: ${usuario?.nombre ?: "null"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()  // ⭐ Agrega esto
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "📊 Estadísticas Generales",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Total usuarios
                EstadisticaItem(
                    icono = "👥",
                    titulo = "Total Usuarios",
                    valor = stats.totalUsuarios.toString(),
                    color = Color(0xFF4654A3)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Clientes
                EstadisticaItem(
                    icono = "🛒",
                    titulo = "Clientes",
                    valor = stats.totalClientes.toString(),
                    color = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Vendedores
                EstadisticaItem(
                    icono = "🏪",
                    titulo = "Vendedores",
                    valor = stats.totalVendedores.toString(),
                    color = Color(0xFFFF9800)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Admins
                EstadisticaItem(
                    icono = "👑",
                    titulo = "Administradores",
                    valor = stats.totalAdmins.toString(),
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun EstadisticaItem(
    icono: String,
    titulo: String,
    valor: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icono,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
        }
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
    }
}



@Composable
fun UsuariosAdminContent(adminViewModel: AdminViewModel) {
    val adminState by adminViewModel.adminState.collectAsStateWithLifecycle()
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Diálogo de cambio de rol
    if (showRoleDialog && selectedUser != null) {
        CambiarRolDialog(
            usuario = selectedUser!!,
            onDismiss = { showRoleDialog = false },
            onConfirm = { nuevoRol ->
                adminViewModel.cambiarRol(selectedUser!!.uid, nuevoRol)
                showRoleDialog = false
                selectedUser = null
            }
        )
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro de eliminar a ${selectedUser!!.nombre}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        adminViewModel.eliminarUsuario(selectedUser!!.uid)
                        showDeleteDialog = false
                        selectedUser = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Mostrar mensajes
    LaunchedEffect(adminState.successMessage, adminState.error) {
        if (adminState.successMessage != null || adminState.error != null) {
            delay(3000)
            adminViewModel.limpiarMensajes()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 Gestión de Usuarios",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                    IconButton(onClick = { adminViewModel.cargarUsuarios() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mensajes de éxito/error
                adminState.successMessage?.let {
                    Text(
                        text = "✅ $it",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                adminState.error?.let {
                    Text(
                        text = "❌ $it",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (adminState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                } else {
                    // Lista de usuarios
                    adminState.usuarios.forEach { usuario ->
                        UsuarioItem(
                            usuario = usuario,
                            onCambiarRol = {
                                selectedUser = usuario
                                showRoleDialog = true
                            },
                            onEliminar = {
                                selectedUser = usuario
                                showDeleteDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun UsuarioItem(
    usuario: User,
    onCambiarRol: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorRol = when (usuario.rol) {
        "Admin" -> Color.Red
        "Vendedor" -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorRol.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Text(
                        text = usuario.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Rol: ${usuario.rol}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorRol
                    )
                }

                Row {
                    IconButton(onClick = onCambiarRol) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Cambiar rol",
                            tint = Color(0xFF4654A3)
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CambiarRolDialog(
    usuario: User,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedRol by remember { mutableStateOf(usuario.rol) }
    val roles = listOf("Cliente", "Vendedor", "Admin")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Rol de ${usuario.nombre}") },
        text = {
            Column {
                Text(
                    text = "Selecciona el nuevo rol:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                roles.forEach { rol ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRol == rol,
                            onClick = { selectedRol = rol }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = rol)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRol) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4654A3)
                )
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// TAB 3: GESTIÓN GENERAL
@Composable
fun GestionAdminContent(adminViewModel: AdminViewModel) {
    val adminState by adminViewModel.adminState.collectAsStateWithLifecycle()

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
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Gestión",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Configuración General",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Text(
                    text = "Próximamente: Configuraciones avanzadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


@Composable
fun PerfilAdminContent(viewModel: AuthViewModel, onLogout: () -> Unit) {
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
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Perfil Administrador",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Text(
                    text = usuario?.nombre ?: "Admin",
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
                        containerColor = Color.Red
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