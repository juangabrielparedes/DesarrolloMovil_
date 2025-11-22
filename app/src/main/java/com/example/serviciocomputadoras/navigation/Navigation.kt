package com.example.serviciocomputadoras.navigation


import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.serviciocomputadoras.presentacion.ui.screens.*
import com.example.serviciocomputadoras.presentacion.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object HomeCliente : Screen("home_cliente")
    object HomeVendedor : Screen("home_vendedor")
    object HomeAdmin : Screen("home_admin")
}

@Composable
fun NavigationGraph(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()

    val startDestination = if (authViewModel.isUserLoggedIn()) {
        when (authViewModel.getRolActual()) {
            "Admin" -> Screen.HomeAdmin.route
            "Vendedor" -> Screen.HomeVendedor.route
            else -> Screen.HomeCliente.route
        }
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLoginSuccess = {
                    val rol = authViewModel.getRolActual()
                    val destino = when (rol) {
                        "Admin" -> Screen.HomeAdmin.route
                        "Vendedor" -> Screen.HomeVendedor.route
                        else -> Screen.HomeCliente.route
                    }
                    navController.navigate(destino) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.HomeCliente.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ⭐ PANTALLA PRINCIPAL CLIENTE (con Bottom Navigation)
        composable(Screen.HomeCliente.route) {
            MainScreenCliente(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ⭐ PANTALLA PRINCIPAL VENDEDOR (con Bottom Navigation)
        composable(Screen.HomeVendedor.route) {
            MainScreenVendedor(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ⭐ PANTALLA PRINCIPAL ADMIN (con Bottom Navigation)
        composable(Screen.HomeAdmin.route) {
            MainScreenAdmin(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}