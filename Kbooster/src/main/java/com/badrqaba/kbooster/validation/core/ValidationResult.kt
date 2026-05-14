package com.badrqaba.kbooster.validation.core

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, List<FieldError>>
) {

    fun errorsFor(field: String): List<FieldError> =
        errors[field].orEmpty()

    fun firstError(field: String): FieldError? =
        errors[field]?.firstOrNull()

    companion object {
        val Success = ValidationResult(
            isValid = true,
            errors = emptyMap()
        )
    }
}