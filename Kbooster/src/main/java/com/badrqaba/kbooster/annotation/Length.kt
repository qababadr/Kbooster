package com.badrqaba.kbooster.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Length(
    val min: Int = 0,
    val max: Int = Int.MAX_VALUE,
    val message: String = "Invalid length"
)