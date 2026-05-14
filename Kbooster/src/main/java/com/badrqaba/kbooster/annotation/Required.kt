package com.badrqaba.kbooster.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Required(val message: String = "Required")