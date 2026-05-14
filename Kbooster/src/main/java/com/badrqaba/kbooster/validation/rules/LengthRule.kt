package com.badrqaba.kbooster.validation.rules

import com.badrqaba.kbooster.validation.core.FieldError

class LengthRule(
    override val field: String,
    private val min: Int = 0,
    private val max: Int = Int.MAX_VALUE,
    private val message: String = "Invalid length"
) : BaseRule<String>(field) {

    override fun validate(value: String?): FieldError? {
        val str = value ?: return null

        return if (str.length !in min..max) {
            FieldError(message, "length")
        } else null
    }
}