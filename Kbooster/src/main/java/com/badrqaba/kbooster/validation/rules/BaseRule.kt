package com.badrqaba.kbooster.validation.rules

abstract class BaseRule<T>(
    override val field: String
) : ValidationRule<T>