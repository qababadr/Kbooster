package com.badrqaba.kbooster.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Min(val value: Double, val message: String = "Too small")