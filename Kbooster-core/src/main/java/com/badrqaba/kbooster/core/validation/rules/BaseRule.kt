package com.badrqaba.kbooster.core.validation.rules

abstract class BaseRule<T>(
    override val field: String
) : ValidationRule<T>