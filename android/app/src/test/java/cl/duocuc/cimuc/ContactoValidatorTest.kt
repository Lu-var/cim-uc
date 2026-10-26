package cl.duocuc.cimuc

import cl.duocuc.cimuc.validation.ContactoValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactoValidatorTest {

    private val validator = ContactoValidator()

    // Regla de correo (#8): formato @ y dominio reconocible
    @Test
    fun correoValido_esAceptado() {
        assertTrue(validator.validarCorreo("ana.morales@duocuc.cl").isValid)
        assertTrue(validator.validarCorreo("contacto@ejemplo.com").isValid)
        assertTrue(validator.validarCorreo("  raul+test@sub.dominio.mx  ").isValid)
    }

    @Test
    fun correoSinFormato_correcto_esRechazado() {
        assertFalse(validator.validarCorreo("sin-arroba.com").isValid)
        assertFalse(validator.validarCorreo("arriba@").isValid)
        assertFalse(validator.validarCorreo("@dominio.cl").isValid)
        assertFalse(validator.validarCorreo("dos@@dominio.cl").isValid)
        assertFalse(validator.validarCorreo("nombre@dominio").isValid) // sin TLD
        assertFalse(validator.validarCorreo("nombre@dominio.c").isValid) // TLD de 1 letra
    }

    @Test
    fun correoVacio_esRechazado() {
        val result = validator.validarCorreo("   ")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun nombreVacio_oMuyCorto_esRechazado() {
        assertFalse(validator.validarNombre("").isValid)
        assertFalse(validator.validarNombre("   ").isValid)
        assertFalse(validator.validarNombre("An").isValid)
        assertTrue(validator.validarNombre("Ana Morales").isValid)
    }

    @Test
    fun mensajeVacio_oMuyCorto_esRechazado() {
        assertFalse(validator.validarMensaje("").isValid)
        assertFalse(validator.validarMensaje("Hola").isValid)
        assertTrue(validator.validarMensaje("Me interesa una cotización técnica").isValid)
    }

    @Test
    fun formularioCompleto_retornaErroresPorCampo() {
        val resultado = validator.validarFormulario(nombre = "", correo = "malo", mensaje = "")

        assertFalse(resultado.esValido)
        assertNotNull(resultado.errorNombre)
        assertNotNull(resultado.errorCorreo)
        assertNotNull(resultado.errorMensaje)
    }

    @Test
    fun formularioValido_noTieneErrores() {
        val resultado = validator.validarFormulario(
            nombre = "Ana Morales",
            correo = "ana@duocuc.cl",
            mensaje = "Me interesa información sobre proyectos de madera"
        )

        assertTrue(resultado.esValido)
        assertNull(resultado.errorNombre)
        assertNull(resultado.errorCorreo)
        assertNull(resultado.errorMensaje)
        assertEquals(null, resultado.errorNombre)
    }
}
