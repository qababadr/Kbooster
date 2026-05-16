package com.badrqaba.kbooster.core.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Email(
    val regex: String = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
    val message: String = "Invalid email"
)