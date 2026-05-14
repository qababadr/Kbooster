package com.badrqaba.kbooster.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class NotBlank(
    val message: String = "Cannot be blank"
)