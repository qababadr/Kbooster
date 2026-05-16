package com.badrqaba.kbooster.core.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Required(val message: String = "Required")