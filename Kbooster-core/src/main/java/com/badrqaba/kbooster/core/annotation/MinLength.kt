package com.badrqaba.kbooster.core.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class MinLength(
    val value: Int,
    val message: String = "Too short"
)