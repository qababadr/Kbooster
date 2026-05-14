package com.badrqaba.kbooster.validation.core

import com.badrqaba.kbooster.validation.rules.ValidationRule

class FieldRule<T>(
    override val field: String,
    val extractor: (T) -> Any?,
    val validator: (Any?) -> FieldError?
) : ValidationRule<T> {
    override fun validate(value: T?): FieldError? {
        if (value == null) return null
        return validator(extractor(value))
    }
}
