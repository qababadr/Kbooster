package com.badrqaba.kbooster.model

import com.google.devtools.ksp.symbol.Visibility

data class UsecaseableConfig(
    val prefixWithRepo: Boolean,
    val generateWrapper: Boolean,
    val visibility: Visibility,
    val generateDI: Boolean,
    val diScope: DIScope
)