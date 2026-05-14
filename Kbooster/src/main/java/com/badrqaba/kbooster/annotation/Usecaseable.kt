package com.badrqaba.kbooster.annotation

import com.badrqaba.kbooster.model.DIScope
import com.badrqaba.kbooster.model.Visibility

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Usecaseable(
    val prefixWithRepo: Boolean = true,
    val generateWrapper: Boolean = true,
    val visibility: Visibility = Visibility.PUBLIC,
    val generateDI: Boolean = false,
    val diScope: DIScope = DIScope.SINGLETON
)
