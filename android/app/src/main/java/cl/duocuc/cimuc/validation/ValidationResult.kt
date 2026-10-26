package cl.duocuc.cimuc.validation

// Resultado de una regla de validación, sin dependencias de UI (issue #12)
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        fun success() = ValidationResult(isValid = true)
        fun error(message: String) = ValidationResult(isValid = false, errorMessage = message)
    }
}
