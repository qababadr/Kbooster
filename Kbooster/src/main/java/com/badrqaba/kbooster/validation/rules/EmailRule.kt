package com.badrqaba.kbooster.validation.rules

import com.badrqaba.kbooster.validation.core.FieldError

class EmailRule(
    field: String,
    private val regex: Regex,
    private val message: String = "Invalid email"
) : BaseRule<String>(field) {

    override fun validate(value: String?): FieldError? {
        return if (value == null || !regex.matches(value)) {
            FieldError(message, "email")
        } else null
    }
}