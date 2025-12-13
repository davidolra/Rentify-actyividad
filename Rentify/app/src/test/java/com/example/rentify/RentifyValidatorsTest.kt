package com.example.rentify

import com.example.rentify.domain.validation.*
import org.junit.Assert.*
import org.junit.Test

class RentifyValidatorsTest {

    // ==================== TEST EMAIL ====================
    @Test
    fun email_valido_retorna_null() {
        assertNull(validateEmail("usuario@duocuc.cl"))
    }

    @Test
    fun email_invalido_retorna_error() {
        assertEquals("Formato de email inválido", validateEmail("correo-sin-arroba"))
    }

    // ==================== TEST RUT ====================
    @Test
    fun rut_valido_retorna_null() {
        // 18.123.456-0 es un RUT matemáticamente válido
        assertNull(validateRut("12.345.678-5"))
    }

    @Test
    fun rut_sin_puntos_valido() {
        assertNull(validateRut("123456785"))
    }

    @Test
    fun rut_invalido_dv_malo() {
        // El guion K no corresponde a esta numeración
        assertEquals("RUT inválido (dígito verificador incorrecto)", validateRut("18.123.456-K"))
    }

    // ==================== TEST TELÉFONO ====================
    @Test
    fun telefono_chileno_9_digitos_ok() {
        assertNull(validatePhoneChileno("912345678"))
    }

    @Test
    fun telefono_largo_invalido() {
        assertEquals("Debe tener 9 dígitos (ej: 912345678)", validatePhoneChileno("12345678"))
    }

    // ==================== TEST PASSWORD ====================
    @Test
    fun password_fuerte_ok() {
        // Tiene Mayus, Minus, Numero, Simbolo y largo 8+
        assertNull(validateStrongPassword("Hola1234."))
    }

    @Test
    fun password_sin_mayuscula_error() {
        assertEquals("Debe incluir una mayúscula", validateStrongPassword("hola1234."))
    }

    @Test
    fun password_sin_numero_error() {
        assertEquals("Debe incluir un número", validateStrongPassword("Hola....."))
    }

    // ==================== TEST PRECIO ====================
    @Test
    fun precio_valido_ok() {
        assertNull(validatePrecio("500000"))
    }

    @Test
    fun precio_negativo_error() {
        assertEquals("El precio debe ser mayor a 0", validatePrecio("-500"))
    }

    @Test
    fun precio_letras_error() {
        assertEquals("Precio inválido", validatePrecio("cinco mil"))
    }
}