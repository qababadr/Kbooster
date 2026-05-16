package com.badrqaba.kbooster.core.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class EqualsTo(
    val field: String,
    val message: String = "Does not match"
)