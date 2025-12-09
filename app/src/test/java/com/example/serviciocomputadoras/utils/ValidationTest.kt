package com.example.serviciocomputadoras.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para validaciones y utilidades
 */
class ValidationTest {

    @Test
    fun `email válido retorna true`() {
        val email = "usuario@gmail.com"
        assertTrue(isValidEmail(email))
    }

    @Test
    fun `email sin arroba retorna false`() {
        val email = "usuariogmail.com"
        assertFalse(isValidEmail(email))
    }

    @Test
    fun `email sin dominio retorna false`() {
        val email = "usuario@"
        assertFalse(isValidEmail(email))
    }

    @Test
    fun `email vacío retorna false`() {
        val email = ""
        assertFalse(isValidEmail(email))
    }

    @Test
    fun `email con espacios retorna false`() {
        val email = "usuario @gmail.com"
        assertFalse(isValidEmail(email))
    }



    @Test
    fun `password con 6 caracteres es válido`() {
        val password = "123456"
        assertTrue(isValidPassword(password))
    }

    @Test
    fun `password con menos de 6 caracteres es inválido`() {
        val password = "12345"
        assertFalse(isValidPassword(password))
    }

    @Test
    fun `password vacío es inválido`() {
        val password = ""
        assertFalse(isValidPassword(password))
    }

    @Test
    fun `password con 10 caracteres es válido`() {
        val password = "1234567890"
        assertTrue(isValidPassword(password))
    }



    @Test
    fun `nombre con texto es válido`() {
        val nombre = "Juan Pérez"
        assertTrue(isValidName(nombre))
    }

    @Test
    fun `nombre vacío es inválido`() {
        val nombre = ""
        assertFalse(isValidName(nombre))
    }

    @Test
    fun `nombre solo espacios es inválido`() {
        val nombre = "   "
        assertFalse(isValidName(nombre))
    }



    @Test
    fun `teléfono con 10 dígitos es válido`() {
        val phone = "3001234567"
        assertTrue(isValidPhone(phone))
    }

    @Test
    fun `teléfono con menos de 10 dígitos es inválido`() {
        val phone = "300123456"
        assertFalse(isValidPhone(phone))
    }

    @Test
    fun `teléfono con letras es inválido`() {
        val phone = "300ABC4567"
        assertFalse(isValidPhone(phone))
    }


    @Test
    fun `precio positivo es válido`() {
        val price = 50000L
        assertTrue(isValidPrice(price))
    }

    @Test
    fun `precio cero es inválido`() {
        val price = 0L
        assertFalse(isValidPrice(price))
    }

    @Test
    fun `precio negativo es inválido`() {
        val price = -1000L
        assertFalse(isValidPrice(price))
    }


    @Test
    fun `calcular total de items`() {
        val items = listOf(30000L, 20000L, 50000L)
        val total = items.sum()
        assertEquals(100000L, total)
    }

    @Test
    fun `calcular total con lista vacía`() {
        val items = emptyList<Long>()
        val total = items.sum()
        assertEquals(0L, total)
    }

    @Test
    fun `calcular total con un item`() {
        val items = listOf(75000L)
        val total = items.sum()
        assertEquals(75000L, total)
    }


    @Test
    fun `formatear precio a string`() {
        val price = 50000L
        val formatted = formatPrice(price)
        assertTrue(formatted == "$50,000" || formatted == "$50.000")
    }
    @Test
    fun `formatear precio cero`() {
        val price = 0L
        val formatted = formatPrice(price)
        assertEquals("$0", formatted)
    }


    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
                email.contains("@") &&
                email.contains(".") &&
                !email.contains(" ")
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    private fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.trim().isNotEmpty()
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() }
    }

    private fun isValidPrice(price: Long): Boolean {
        return price > 0
    }

    private fun formatPrice(price: Long): String {
        return if (price == 0L) {
            "$0"
        } else {
            "$${String.format("%,d", price)}"
        }
    }
}