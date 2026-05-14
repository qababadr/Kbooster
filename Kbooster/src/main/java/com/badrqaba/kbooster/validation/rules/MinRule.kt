package com.badrqaba.kbooster.validation.rules

import com.badrqaba.kbooster.validation.core.FieldError

class MinRule(
    override val field: String,
    private val min: Double,
    private val message: String = "Too small"
) : BaseRule<Any?>(field) {

    override fun validate(value: Any?): FieldError? {
        val number = (value as? Number)?.toDouble() ?: return null

        return if (number < min) {
            FieldError(message, "min")
        } else null
    }
}