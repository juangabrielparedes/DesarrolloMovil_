package com.example.serviciocomputadoras.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para el modelo Invoice e InvoiceItem
 */
class InvoiceTest {

    @Test
    fun `invoiceItem se crea con valores por defecto`() {
        // Arrange & Act
        val item = InvoiceItem()

        // Assert
        assertEquals("", item.desc)
        assertEquals(0L, item.price)
    }

    @Test
    fun `invoiceItem se crea con valores personalizados`() {
        // Arrange & Act
        val item = InvoiceItem(
            desc = "Mano de obra",
            price = 50000L
        )

        // Assert
        assertEquals("Mano de obra", item.desc)
        assertEquals(50000L, item.price)
    }

    @Test
    fun `invoice se crea con valores por defecto`() {
        // Arrange & Act
        val invoice = Invoice()

        // Assert
        assertEquals("", invoice.invoiceId)
        assertEquals("", invoice.repairOrderId)
        assertEquals("", invoice.clientUid)
        assertEquals("", invoice.clientName)
        assertEquals("", invoice.clientEmail)
        assertTrue(invoice.items.isEmpty())
        assertEquals(0L, invoice.total)
        assertEquals("pending", invoice.status)
        assertNull(invoice.checkoutUrl)
    }

    @Test
    fun `invoice se crea con valores personalizados`() {
        // Arrange
        val items = listOf(
            InvoiceItem("Pantalla LCD", 150000L),
            InvoiceItem("Mano de obra", 50000L)
        )

        // Act
        val invoice = Invoice(
            invoiceId = "inv001",
            repairOrderId = "order001",
            clientUid = "client123",
            clientName = "Juan Cliente",
            clientEmail = "juan@gmail.com",
            items = items,
            total = 200000L,
            status = "pending",
            checkoutUrl = "https://buy.stripe.com/test_abc123"
        )

        // Assert
        assertEquals("inv001", invoice.invoiceId)
        assertEquals("order001", invoice.repairOrderId)
        assertEquals("client123", invoice.clientUid)
        assertEquals("Juan Cliente", invoice.clientName)
        assertEquals("juan@gmail.com", invoice.clientEmail)
        assertEquals(2, invoice.items.size)
        assertEquals(200000L, invoice.total)
        assertEquals("pending", invoice.status)
        assertEquals("https://buy.stripe.com/test_abc123", invoice.checkoutUrl)
    }

    @Test
    fun `invoice con status paid`() {
        // Arrange & Act
        val invoice = Invoice(
            invoiceId = "inv002",
            status = "paid"
        )

        // Assert
        assertEquals("paid", invoice.status)
    }

    @Test
    fun `invoice calcula total correctamente desde items`() {
        // Arrange
        val items = listOf(
            InvoiceItem("Repuesto 1", 30000L),
            InvoiceItem("Repuesto 2", 20000L),
            InvoiceItem("Mano de obra", 50000L)
        )

        // Act
        val totalCalculado = items.sumOf { it.price }

        // Assert
        assertEquals(100000L, totalCalculado)
    }

    @Test
    fun `invoice copy cambia status a paid`() {
        // Arrange
        val original = Invoice(
            invoiceId = "inv003",
            clientName = "Cliente Test",
            total = 75000L,
            status = "pending"
        )

        // Act
        val updated = original.copy(status = "paid")

        // Assert
        assertEquals("inv003", updated.invoiceId)
        assertEquals("Cliente Test", updated.clientName)
        assertEquals(75000L, updated.total)
        assertEquals("paid", updated.status)
    }

    @Test
    fun `invoice con checkoutUrl de Stripe`() {
        // Arrange & Act
        val invoice = Invoice(
            invoiceId = "inv004",
            checkoutUrl = "https://buy.stripe.com/test_cNicN4dJeaCn3Pt9vOawo00"
        )

        // Assert
        assertNotNull(invoice.checkoutUrl)
        assertTrue(invoice.checkoutUrl!!.startsWith("https://buy.stripe.com"))
    }

    @Test
    fun `invoice sin checkoutUrl`() {
        // Arrange & Act
        val invoice = Invoice(invoiceId = "inv005")

        // Assert
        assertNull(invoice.checkoutUrl)
    }
}