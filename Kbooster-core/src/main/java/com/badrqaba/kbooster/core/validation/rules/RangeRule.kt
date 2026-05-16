package com.badrqaba.kbooster.core.validation.rules

import com.badrqaba.kbooster.core.validation.core.FieldError

class RangeRule(
    override val field: String,
    private val min: Double,
    private val max: Double,
    private val message: String = "Out of range"
) : BaseRule<Any?>(field) {

    override fun validate(value: Any?): FieldError? {
        val number = (value as? Number)?.toDouble() ?: return null

        return if (number !in min..max) {
            FieldError(message, "range")
        } else null
    }
}