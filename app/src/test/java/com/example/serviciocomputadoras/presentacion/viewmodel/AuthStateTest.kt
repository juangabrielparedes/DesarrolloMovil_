package com.example.serviciocomputadoras.presentacion.viewmodel

import com.example.serviciocomputadoras.data.model.User
import org.junit.Assert
import org.junit.Test

/**
 * Pruebas unitarias para AuthState
 */
class AuthStateTest {

    @Test
    fun `authState se crea con valores por defecto`() {
        // Arrange & Act
        val state = AuthState()

        // Assert
        Assert.assertFalse(state.isLoading)
        Assert.assertFalse(state.isSuccess)
        Assert.assertNull(state.error)
        Assert.assertNull(state.user)
        Assert.assertNull(state.rol)
    }

    @Test
    fun `authState en estado loading`() {
        // Arrange & Act
        val state = AuthState(isLoading = true)

        // Assert
        Assert.assertTrue(state.isLoading)
        Assert.assertFalse(state.isSuccess)
        Assert.assertNull(state.error)
    }

    @Test
    fun `authState login exitoso`() {
        // Arrange
        val user = User(
            uid = "user123",
            email = "test@gmail.com",
            nombre = "Test User",
            rol = "Cliente"
        )

        // Act
        val state = AuthState(
            isSuccess = true,
            user = user,
            rol = "Cliente"
        )

        // Assert
        Assert.assertTrue(state.isSuccess)
        Assert.assertNotNull(state.user)
        Assert.assertEquals("user123", state.user?.uid)
        Assert.assertEquals("Cliente", state.rol)
        Assert.assertNull(state.error)
    }

    @Test
    fun `authState login fallido`() {
        // Arrange & Act
        val state = AuthState(
            isLoading = false,
            isSuccess = false,
            error = "Credenciales inválidas"
        )

        // Assert
        Assert.assertFalse(state.isLoading)
        Assert.assertFalse(state.isSuccess)
        Assert.assertEquals("Credenciales inválidas", state.error)
        Assert.assertNull(state.user)
    }

    @Test
    fun `authState con usuario vendedor`() {
        // Arrange
        val vendedor = User(
            uid = "vendor001",
            email = "vendedor@gmail.com",
            nombre = "Vendedor Test",
            rol = "Vendedor"
        )

        // Act
        val state = AuthState(
            isSuccess = true,
            user = vendedor,
            rol = "Vendedor"
        )

        // Assert
        Assert.assertEquals("Vendedor", state.rol)
        Assert.assertEquals("Vendedor", state.user?.rol)
    }

    @Test
    fun `authState con usuario admin`() {
        // Arrange
        val admin = User(
            uid = "admin001",
            email = "admin@empresa.com",
            nombre = "Admin",
            rol = "Admin"
        )

        // Act
        val state = AuthState(
            isSuccess = true,
            user = admin,
            rol = "Admin"
        )

        // Assert
        Assert.assertEquals("Admin", state.rol)
    }

    @Test
    fun `authState copy mantiene usuario al resetear flags`() {
        // Arrange
        val user = User(uid = "user123", nombre = "Test")
        val original = AuthState(
            isLoading = false,
            isSuccess = true,
            user = user,
            rol = "Cliente"
        )

        // Act - Simula resetState() corregido
        val reset = original.copy(
            isLoading = false,
            isSuccess = false,
            error = null
            // user y rol se mantienen
        )

        // Assert
        Assert.assertFalse(reset.isLoading)
        Assert.assertFalse(reset.isSuccess)
        Assert.assertNull(reset.error)
        Assert.assertNotNull(reset.user) // Usuario se mantiene
        Assert.assertEquals("user123", reset.user?.uid)
        Assert.assertEquals("Cliente", reset.rol)
    }

    @Test
    fun `authState copy borra todo al logout`() {
        // Arrange
        val user = User(uid = "user123", nombre = "Test")
        val original = AuthState(
            isSuccess = true,
            user = user,
            rol = "Cliente"
        )

        // Act - Simula logout completo
        val logout = AuthState() // Estado vacío

        // Assert
        Assert.assertFalse(logout.isSuccess)
        Assert.assertNull(logout.user)
        Assert.assertNull(logout.rol)
    }
}