package com.badrqaba.kbooster.validation.core

import kotlinx.coroutines.flow.MutableStateFlow

class FormState<T>(
    initial: T,
    private val validator: Validator<T>
) {

    private val _state = MutableStateFlow(initial)
    val state = _state

    private val _errors =
        MutableStateFlow<Map<String, List<FieldError>>>(emptyMap())
    val errors = _errors

    private val touched = mutableSetOf<String>()

    fun update(transform: T.() -> T) {
        val newState = _state.value.transform()
        _state.value = newState

        validateInternal()
    }

    fun updateField(
        field: String,
        transform: T.() -> T
    ) {
        touched.add(field)

        val newState = _state.value.transform()
        _state.value = newState

        val result = validator.validate(newState)

        _errors.value = result.errors.filterKeys {
            touched.contains(it)
        }
    }

    fun validate(): ValidationResult {
        val result = validator.validate(_state.value)
        _errors.value = result.errors
        return result
    }

    private fun validateInternal() {
        val result = validator.validate(_state.value)
        _errors.value = result.errors
    }
}