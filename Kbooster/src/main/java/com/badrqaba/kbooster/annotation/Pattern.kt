package com.badrqaba.kbooster.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Pattern(
    val value: String,
    val message: String = "Invalid format"
)