package com.badrqaba.kbooster.validation.rules

import com.badrqaba.kbooster.validation.core.FieldError

class NotBlankRule(
    override val field: String,
    private val message: String = "Required"
) : BaseRule<Any?>(field) {

    override fun validate(value: Any?): FieldError? {
        val str = value as? String
        return if (str.isNullOrBlank()) {
            FieldError(message, "not_blank")
        } else null
    }
}