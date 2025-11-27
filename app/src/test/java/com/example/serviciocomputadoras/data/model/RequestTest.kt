package com.example.serviciocomputadoras.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para el modelo Request
 */
class RequestTest {

    @Test
    fun `request se crea con valores por defecto`() {
        // Arrange & Act
        val request = Request()

        // Assert
        assertEquals("", request.id)
        assertEquals("", request.businessId)
        assertEquals("", request.clientUid)
        assertNull(request.clientName)
        assertNull(request.clientEmail)
        assertEquals("", request.description)
        assertNull(request.preferredDate)
        assertEquals("pending", request.status)
    }

    @Test
    fun `request se crea con valores personalizados`() {
        // Arrange & Act
        val request = Request(
            id = "req001",
            businessId = "pcfix_001",
            clientUid = "client123",
            clientName = "Juan Cliente",
            clientEmail = "juan@gmail.com",
            description = "Mi laptop no enciende",
            preferredDate = "2025-12-15",
            status = "pending"
        )

        // Assert
        assertEquals("req001", request.id)
        assertEquals("pcfix_001", request.businessId)
        assertEquals("client123", request.clientUid)
        assertEquals("Juan Cliente", request.clientName)
        assertEquals("juan@gmail.com", request.clientEmail)
        assertEquals("Mi laptop no enciende", request.description)
        assertEquals("2025-12-15", request.preferredDate)
        assertEquals("pending", request.status)
    }

    @Test
    fun `request con status accepted`() {
        // Arrange & Act
        val request = Request(
            id = "req002",
            status = "accepted"
        )

        // Assert
        assertEquals("accepted", request.status)
    }

    @Test
    fun `request con status rejected`() {
        // Arrange & Act
        val request = Request(
            id = "req003",
            status = "rejected"
        )

        // Assert
        assertEquals("rejected", request.status)
    }

    @Test
    fun `request con status completed`() {
        // Arrange & Act
        val request = Request(
            id = "req004",
            status = "completed"
        )

        // Assert
        assertEquals("completed", request.status)
    }

    @Test
    fun `request copy cambia solo status`() {
        // Arrange
        val original = Request(
            id = "req005",
            businessId = "biz001",
            clientUid = "client001",
            description = "Problema original",
            status = "pending"
        )

        // Act
        val updated = original.copy(status = "accepted")

        // Assert
        assertEquals("req005", updated.id)
        assertEquals("biz001", updated.businessId)
        assertEquals("client001", updated.clientUid)
        assertEquals("Problema original", updated.description)
        assertEquals("accepted", updated.status)
    }
}