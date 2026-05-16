package com.badrqaba.kbooster.core.validation.rules

import com.badrqaba.kbooster.core.validation.core.FieldError

interface ValidationRule<T> {
    val field: String
    fun validate(value: T?): FieldError?
}