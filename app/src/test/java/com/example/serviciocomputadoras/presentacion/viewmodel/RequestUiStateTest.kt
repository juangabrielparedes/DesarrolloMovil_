package com.example.serviciocomputadoras.presentacion.viewmodel

import com.example.serviciocomputadoras.data.model.Request
import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para RequestUiState
 */
class RequestUiStateTest {

    @Test
    fun `requestUiState se crea con valores por defecto`() {
        // Arrange & Act
        val state = RequestUiState()

        // Assert
        assertTrue(state.requests.isEmpty())
        assertFalse(state.loading)
        assertNull(state.error)
    }

    @Test
    fun `requestUiState en estado loading`() {
        // Arrange & Act
        val state = RequestUiState(loading = true)

        // Assert
        assertTrue(state.loading)
        assertTrue(state.requests.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `requestUiState con lista de requests`() {
        // Arrange
        val requests = listOf(
            Request(id = "req1", description = "Problema 1", status = "pending"),
            Request(id = "req2", description = "Problema 2", status = "accepted"),
            Request(id = "req3", description = "Problema 3", status = "rejected")
        )

        // Act
        val state = RequestUiState(requests = requests, loading = false)

        // Assert
        assertEquals(3, state.requests.size)
        assertFalse(state.loading)
    }

    @Test
    fun `requestUiState con error`() {
        // Arrange & Act
        val state = RequestUiState(
            loading = false,
            error = "Error de conexión"
        )

        // Assert
        assertFalse(state.loading)
        assertEquals("Error de conexión", state.error)
        assertTrue(state.requests.isEmpty())
    }

    @Test
    fun `requestUiState filtrar requests pendientes`() {
        // Arrange
        val requests = listOf(
            Request(id = "req1", status = "pending"),
            Request(id = "req2", status = "accepted"),
            Request(id = "req3", status = "pending"),
            Request(id = "req4", status = "rejected")
        )
        val state = RequestUiState(requests = requests)

        // Act
        val pendientes = state.requests.filter { it.status == "pending" }

        // Assert
        assertEquals(2, pendientes.size)
    }

    @Test
    fun `requestUiState filtrar requests aceptadas`() {
        // Arrange
        val requests = listOf(
            Request(id = "req1", status = "pending"),
            Request(id = "req2", status = "accepted"),
            Request(id = "req3", status = "accepted")
        )
        val state = RequestUiState(requests = requests)

        // Act
        val aceptadas = state.requests.filter { it.status == "accepted" }

        // Assert
        assertEquals(2, aceptadas.size)
    }

    @Test
    fun `requestUiState filtrar requests rechazadas`() {
        // Arrange
        val requests = listOf(
            Request(id = "req1", status = "rejected"),
            Request(id = "req2", status = "pending"),
            Request(id = "req3", status = "rejected")
        )
        val state = RequestUiState(requests = requests)

        // Act
        val rechazadas = state.requests.filter { it.status == "rejected" }

        // Assert
        assertEquals(2, rechazadas.size)
    }

    @Test
    fun `requestUiState actualizar status de request`() {
        // Arrange
        val requests = listOf(
            Request(id = "req1", status = "pending"),
            Request(id = "req2", status = "pending")
        )
        val state = RequestUiState(requests = requests)

        // Act - Simula actualización de status
        val updatedRequests = state.requests.map { req ->
            if (req.id == "req1") req.copy(status = "accepted") else req
        }
        val newState = state.copy(requests = updatedRequests)

        // Assert
        assertEquals("accepted", newState.requests.find { it.id == "req1" }?.status)
        assertEquals("pending", newState.requests.find { it.id == "req2" }?.status)
    }

    @Test
    fun `requestUiState copy limpia error`() {
        // Arrange
        val state = RequestUiState(
            loading = false,
            error = "Hubo un error"
        )

        // Act
        val cleared = state.copy(error = null)

        // Assert
        assertNull(cleared.error)
    }
}