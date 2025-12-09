package com.example.serviciocomputadoras.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para el modelo Business
 */
class BusinessTest {

    @Test
    fun `business se crea con valores por defecto`() {

        val business = Business()

        // Assert
        assertEquals("", business.id)
        assertEquals("", business.name)
        assertEquals("", business.description)
        assertEquals(0.0, business.rating, 0.01)
        assertEquals("", business.address)
        assertEquals("", business.phone)
        assertTrue(business.services.isEmpty())
        assertEquals(0L, business.priceStarting)
        assertEquals("", business.ownerId)
    }

    @Test
    fun `business se crea con valores personalizados`() {

        val servicios = listOf("Formateo", "Mantenimiento", "Reparación")


        val business = Business(
            id = "pcfix_001",
            name = "PC World",
            description = "Servicio de computadoras",
            rating = 4.5,
            address = "Calle 123",
            phone = "3001234567",
            services = servicios,
            priceStarting = 30000L,
            ownerId = "vendor001"
        )


        assertEquals("pcfix_001", business.id)
        assertEquals("PC World", business.name)
        assertEquals("Servicio de computadoras", business.description)
        assertEquals(4.5, business.rating, 0.01)
        assertEquals("Calle 123", business.address)
        assertEquals("3001234567", business.phone)
        assertEquals(3, business.services.size)
        assertEquals(30000L, business.priceStarting)
        assertEquals("vendor001", business.ownerId)
    }

    @Test
    fun `business contiene servicio específico`() {

        val servicios = listOf("Formateo", "Mantenimiento", "Reparación")
        val business = Business(services = servicios)


        assertTrue(business.services.contains("Formateo"))
        assertTrue(business.services.contains("Mantenimiento"))
        assertTrue(business.services.contains("Reparación"))
        assertFalse(business.services.contains("Instalación"))
    }

    @Test
    fun `business con rating máximo`() {

        val business = Business(rating = 5.0)


        assertEquals(5.0, business.rating, 0.01)
    }

    @Test
    fun `business con rating mínimo`() {

        val business = Business(rating = 0.0)


        assertEquals(0.0, business.rating, 0.01)
    }

    @Test
    fun `business copy actualiza ownerId`() {

        val original = Business(
            id = "biz001",
            name = "Mi Negocio",
            ownerId = "owner001"
        )


        val updated = original.copy(ownerId = "owner002")


        assertEquals("biz001", updated.id)
        assertEquals("Mi Negocio", updated.name)
        assertEquals("owner002", updated.ownerId)
    }
}