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

        val state = AuthState()


        Assert.assertFalse(state.isLoading)
        Assert.assertFalse(state.isSuccess)
        Assert.assertNull(state.error)
        Assert.assertNull(state.user)
        Assert.assertNull(state.rol)
    }

    @Test
    fun `authState en estado loading`() {

        val state = AuthState(isLoading = true)


        Assert.assertTrue(state.isLoading)
        Assert.assertFalse(state.isSuccess)
        Assert.assertNull(state.error)
    }

    @Test
    fun `authState login exitoso`() {

        val user = User(
            uid = "user123",
            email = "test@gmail.com",
            nombre = "Test User",
            rol = "Cliente"
        )


        val state = AuthState(
            isSuccess = true,
            user = user,
            rol = "Cliente"
        )


        Assert.assertTrue(state.isSuccess)
        Assert.assertNotNull(state.user)
        Assert.assertEquals("user123", state.user?.uid)
        Assert.assertEquals("Cliente", state.rol)
        Assert.assertNull(state.error)
    }

    @Test
    fun `authState login fallido`() {

        val state = AuthState(
            isLoading = false,
            isSuccess = false,
            error = "Credenciales inválidas"
        )


        Assert.assertFalse(state.isLoading)
        Assert.assertFalse(state.isSuccess)
        Assert.assertEquals("Credenciales inválidas", state.error)
        Assert.assertNull(state.user)
    }

    @Test
    fun `authState con usuario vendedor`() {

        val vendedor = User(
            uid = "vendor001",
            email = "vendedor@gmail.com",
            nombre = "Vendedor Test",
            rol = "Vendedor"
        )


        val state = AuthState(
            isSuccess = true,
            user = vendedor,
            rol = "Vendedor"
        )


        Assert.assertEquals("Vendedor", state.rol)
        Assert.assertEquals("Vendedor", state.user?.rol)
    }

    @Test
    fun `authState con usuario admin`() {

        val admin = User(
            uid = "admin001",
            email = "admin@empresa.com",
            nombre = "Admin",
            rol = "Admin"
        )


        val state = AuthState(
            isSuccess = true,
            user = admin,
            rol = "Admin"
        )


        Assert.assertEquals("Admin", state.rol)
    }

    @Test
    fun `authState copy mantiene usuario al resetear flags`() {

        val user = User(uid = "user123", nombre = "Test")
        val original = AuthState(
            isLoading = false,
            isSuccess = true,
            user = user,
            rol = "Cliente"
        )


        val reset = original.copy(
            isLoading = false,
            isSuccess = false,
            error = null
        )


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


        val logout = AuthState()


        Assert.assertFalse(logout.isSuccess)
        Assert.assertNull(logout.user)
        Assert.assertNull(logout.rol)
    }
}