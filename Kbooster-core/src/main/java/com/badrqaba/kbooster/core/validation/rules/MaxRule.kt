package com.badrqaba.kbooster.core.validation.rules

import com.badrqaba.kbooster.core.validation.core.FieldError


class MaxRule(
    override val field: String,
    private val max: Double,
    private val message: String = "Too large"
) : BaseRule<Any?>(field) {

    override fun validate(value: Any?): FieldError? {
        val number = (value as? Number)?.toDouble() ?: return null

        return if (number > max) {
            FieldError(message, "max")
        } else null
    }
}