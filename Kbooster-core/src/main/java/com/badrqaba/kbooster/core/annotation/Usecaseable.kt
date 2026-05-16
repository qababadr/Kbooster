package com.badrqaba.kbooster.core.annotation

import com.badrqaba.kbooster.core.model.DIScope
import com.badrqaba.kbooster.core.model.Visibility

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Usecaseable(
    val prefixWithRepo: Boolean = true,
    val generateWrapper: Boolean = true,
    val visibility: Visibility = Visibility.PUBLIC,
    val generateDI: Boolean = false,
    val diScope: DIScope = DIScope.SINGLETON
)
