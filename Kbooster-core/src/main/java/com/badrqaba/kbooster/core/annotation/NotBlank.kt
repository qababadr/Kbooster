package com.badrqaba.kbooster.core.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class NotBlank(
    val message: String = "Cannot be blank"
)