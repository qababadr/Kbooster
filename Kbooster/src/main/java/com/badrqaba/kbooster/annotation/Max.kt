package com.badrqaba.kbooster.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Max(val value: Double, val message: String = "Too large")