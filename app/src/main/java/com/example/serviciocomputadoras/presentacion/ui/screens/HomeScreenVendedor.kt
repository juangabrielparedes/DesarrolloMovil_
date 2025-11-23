package com.example.serviciocomputadoras.presentacion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.serviciocomputadoras.presentacion.viewmodel.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.serviciocomputadoras.navigation.*
import androidx.compose.runtime.collectAsState
import androidx.annotation.DrawableRes
import com.example.serviciocomputadoras.R
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.delay




@Composable
fun MainScreenVendedor(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = getBottomNavItems("Vendedor")

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
                composable(BottomNavItem.TiendaVendedor.route) {
                    TiendaVendedorContent(authViewModel)
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

// Contenido de cada Tab VENDEDOR
@Composable
fun TiendaVendedorContent(viewModel: AuthViewModel) {
    val usuario = viewModel.authState.collectAsState().value.user

    Box(Modifier.fillMaxSize()) {

        // CONTENIDO PRINCIPAL
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
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Tienda",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF4654A3)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Mi Tienda",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )
                    Text(
                        text = "Vendedor: ${usuario?.nombre}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Gestiona tu negocio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 210.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text(
                    text = "Añadir\nProducto",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Buscar\nProducto",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(15.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                LargeAddButton(
                    onClick = { /* acción */ }
                    /*modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)*/
                )

                LargeSearchButton(
                    onClick = { }
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
            //CarouselProducts
            Row(

                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                CarouselProducts()
            }

        }
    }

}

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
                modifier = Modifier.padding(24.dp),
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
                modifier = Modifier.padding(24.dp),
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

@Composable
fun LargeAddButton(onClick: () -> Unit,  modifier: Modifier = Modifier) {
    LargeFloatingActionButton(
        onClick = { onClick() },
        modifier = modifier
    ) {
        Icon(Icons.Filled.Add, "Large floating action button")
    }
}

@Composable
fun LargeSearchButton(onClick: () -> Unit,  modifier: Modifier = Modifier) {
    LargeFloatingActionButton(
        onClick = { onClick() },
        modifier = modifier
    ) {
        Icon(Icons.Filled.Search, "Large floating action button")
    }
}

@Composable
fun CarouselProducts() {
    data class CarouselItem(
        val id: Int,
        @DrawableRes val imageResId: Int,
        val contentDescription: String
    )

    val items = listOf(
        CarouselItem(0, R.drawable.avatar, "cupcake"),
        CarouselItem(1, R.drawable.avatar, "donut"),
        CarouselItem(2, R.drawable.avatar, "eclair"),
        CarouselItem(3, R.drawable.avatar, "froyo"),
        CarouselItem(4, R.drawable.avatar, "gingerbread"),
    )

    val listState = rememberLazyListState()
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            index = (index + 1) % items.size
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 32.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .height(140.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Image(
                    painter = painterResource(item.imageResId),
                    contentDescription = item.contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
