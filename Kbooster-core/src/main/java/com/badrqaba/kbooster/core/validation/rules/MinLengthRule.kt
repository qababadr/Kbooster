package com.badrqaba.kbooster.core.validation.rules

import com.badrqaba.kbooster.core.validation.core.FieldError

class MinLengthRule(
    field: String,
    private val min: Int,
    private val message: String = "Too short"
) : BaseRule<Any>(field) {

    override fun validate(value: Any?): FieldError? {
        val str = value as? String
        return if (str == null || str.length < min) {
            FieldError(message, "min_length")
        } else null
    }
}