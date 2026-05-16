package com.badrqaba.kbooster.core.validation.rules

import com.badrqaba.kbooster.core.validation.core.FieldError

class RequiredRule(
    field: String,
    private val message: String = "Required"
) : BaseRule<Any?>(field) {

    override fun validate(value: Any?): FieldError? {
        return if (value == null || value.toString().isBlank()) {
            FieldError(message, "required")
        } else null
    }
}