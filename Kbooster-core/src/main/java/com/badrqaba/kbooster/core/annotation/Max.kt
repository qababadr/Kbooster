package com.badrqaba.kbooster.core.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Max(val value: Double, val message: String = "Too large")