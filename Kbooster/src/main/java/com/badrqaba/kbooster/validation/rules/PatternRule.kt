package com.badrqaba.kbooster.validation.rules

import com.badrqaba.kbooster.validation.core.FieldError

class PatternRule(
    field: String,
    private val regex: Regex,
    private val message: String = "Invalid format"
) : BaseRule<String>(field) {

    override fun validate(value: String?): FieldError? {
        return if (value == null || !regex.matches(value)) {
            FieldError(message, "pattern")
        } else null
    }
}