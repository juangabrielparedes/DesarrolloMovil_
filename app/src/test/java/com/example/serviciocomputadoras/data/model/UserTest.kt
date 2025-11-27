package com.example.serviciocomputadoras.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para el modelo User
 */
class UserTest {

    @Test
    fun `usuario se crea con valores por defecto`() {
        // Arrange & Act
        val user = User()

        // Assert
        assertEquals("", user.uid)
        assertEquals("", user.email)
        assertEquals("", user.nombre)
        assertEquals("Cliente", user.rol)
        assertEquals("activo", user.estado)
        assertTrue(user.permisos.isEmpty())
    }

    @Test
    fun `usuario se crea con valores personalizados`() {
        // Arrange & Act
        val user = User(
            uid = "abc123",
            email = "test@gmail.com",
            nombre = "Juan Pérez",
            rol = "Vendedor"
        )

        // Assert
        assertEquals("abc123", user.uid)
        assertEquals("test@gmail.com", user.email)
        assertEquals("Juan Pérez", user.nombre)
        assertEquals("Vendedor", user.rol)
    }

    @Test
    fun `usuario con rol Admin`() {
        // Arrange & Act
        val admin = User(
            uid = "admin001",
            email = "admin@empresa.com",
            nombre = "Administrador",
            rol = "Admin"
        )

        // Assert
        assertEquals("Admin", admin.rol)
    }

    @Test
    fun `usuario con rol Cliente por defecto`() {
        // Arrange & Act
        val cliente = User(
            uid = "cliente001",
            email = "cliente@gmail.com",
            nombre = "Cliente Test"
        )

        // Assert
        assertEquals("Cliente", cliente.rol)
    }

    @Test
    fun `usuario con permisos personalizados`() {
        // Arrange
        val permisos = listOf("ver_reportes", "editar_productos")

        // Act
        val user = User(
            uid = "user001",
            permisos = permisos
        )

        // Assert
        assertEquals(2, user.permisos.size)
        assertTrue(user.permisos.contains("ver_reportes"))
        assertTrue(user.permisos.contains("editar_productos"))
    }

    @Test
    fun `usuario con estado inactivo`() {
        // Arrange & Act
        val user = User(
            uid = "user001",
            estado = "inactivo"
        )

        // Assert
        assertEquals("inactivo", user.estado)
    }
}