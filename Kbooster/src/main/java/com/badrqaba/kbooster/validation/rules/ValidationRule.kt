package com.badrqaba.kbooster.validation.rules

import com.badrqaba.kbooster.validation.core.FieldError

interface ValidationRule<T> {
    val field: String
    fun validate(value: T?): FieldError?
}