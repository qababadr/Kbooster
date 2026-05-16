package com.badrqaba.kbooster.core.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Pattern(
    val value: String,
    val message: String = "Invalid format"
)