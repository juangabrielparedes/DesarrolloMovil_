package com.example.serviciocomputadoras.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Items para CLIENTE
    object HomeCliente : BottomNavItem("home_cliente_tab", "Inicio", Icons.Default.Home)
    object ExplorarCliente : BottomNavItem("explorar_cliente", "Explorar", Icons.Default.Search)
    object CarritoCliente : BottomNavItem("carrito_cliente", "Carrito", Icons.Default.ShoppingCart)
    object PerfilCliente : BottomNavItem("perfil_cliente_tab", "Perfil", Icons.Default.Person)

    // Items para VENDEDOR
    object TiendaVendedor : BottomNavItem("tienda_vendedor", "Tienda", Icons.Default.ShoppingCart)
    object ProductosVendedor : BottomNavItem("productos_vendedor", "Productos", Icons.Default.List)
    object VentasVendedor : BottomNavItem("ventas_vendedor", "Ventas", Icons.Default.AttachMoney)
    object PerfilVendedor : BottomNavItem("perfil_vendedor_tab", "Perfil", Icons.Default.Person)

    // Items para ADMIN
    object DashboardAdmin : BottomNavItem("dashboard_admin", "Dashboard", Icons.Default.Dashboard)
    object UsuariosAdmin : BottomNavItem("usuarios_admin", "Usuarios", Icons.Default.People)
    object GestionAdmin : BottomNavItem("gestion_admin", "Gestión", Icons.Default.Settings)
    object PerfilAdmin : BottomNavItem("perfil_admin_tab", "Perfil", Icons.Default.Person)
}

fun getBottomNavItems(rol: String): List<BottomNavItem> {
    return when (rol) {
        "Cliente" -> listOf(
            BottomNavItem.HomeCliente,
            BottomNavItem.ExplorarCliente,
            BottomNavItem.CarritoCliente,
            BottomNavItem.PerfilCliente
        )
        "Vendedor" -> listOf(
            BottomNavItem.TiendaVendedor,
            BottomNavItem.ProductosVendedor,
            BottomNavItem.VentasVendedor,
            BottomNavItem.PerfilVendedor
        )
        "Admin" -> listOf(
            BottomNavItem.DashboardAdmin,
            BottomNavItem.UsuariosAdmin,
            BottomNavItem.GestionAdmin,
            BottomNavItem.PerfilAdmin
        )
        else -> listOf(
            BottomNavItem.HomeCliente,
            BottomNavItem.PerfilCliente
        )
    }
}