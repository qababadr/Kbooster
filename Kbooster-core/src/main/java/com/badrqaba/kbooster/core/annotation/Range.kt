package com.badrqaba.kbooster.core.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Range(
    val min: Double,
    val max: Double,
    val message: String = "Out of range"
)