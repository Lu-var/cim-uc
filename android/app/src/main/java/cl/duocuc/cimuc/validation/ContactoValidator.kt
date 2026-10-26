package cl.duocuc.cimuc.validation

// Validador puro del formulario de contacto: funciones sin dependencias de
// Android Views, Compose ni contexto (V2 / issue #12, regla de correo #8)
class ContactoValidator {

    fun validarNombre(nombre: String): ValidationResult {
        return if (nombre.trim().isBlank()) {
            ValidationResult.error("El nombre es obligatorio")
        } else if (nombre.trim().length < 3) {
            ValidationResult.error("El nombre debe tener al menos 3 caracteres")
        } else {
            ValidationResult.success()
        }
    }

    fun validarCorreo(correo: String): ValidationResult {
        val limpio = correo.trim()
        return when {
            limpio.isBlank() ->
                ValidationResult.error("El correo es obligatorio")
            !EMAIL_REGEX.matches(limpio) ->
                ValidationResult.error("Ingresa un correo válido, por ejemplo nombre@dominio.cl")
            else ->
                ValidationResult.success()
        }
    }

    fun validarMensaje(mensaje: String): ValidationResult {
        return when {
            mensaje.isBlank() ->
                ValidationResult.error("El mensaje es obligatorio")
            mensaje.trim().length < 10 ->
                ValidationResult.error("El mensaje debe tener al menos 10 caracteres")
            else ->
                ValidationResult.success()
        }
    }

    // Valida el formulario completo y retorna el resultado por campo
    fun validarFormulario(
        nombre: String,
        correo: String,
        mensaje: String
    ): FormularioValidationResult {
        val nombreResult = validarNombre(nombre)
        val correoResult = validarCorreo(correo)
        val mensajeResult = validarMensaje(mensaje)
        val esValido = nombreResult.isValid && correoResult.isValid && mensajeResult.isValid
        return FormularioValidationResult(
            esValido = esValido,
            errorNombre = nombreResult.errorMessage,
            errorCorreo = correoResult.errorMessage,
            errorMensaje = mensajeResult.errorMessage
        )
    }

    // Formato mínimo: algo@algo.dominio con TLD de al menos 2 letras (#8)
    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    }
}

data class FormularioValidationResult(
    val esValido: Boolean,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorMensaje: String? = null
)
