package com.badrqaba.kbooster.core.model

data class UsecaseableConfig(
    val prefixWithRepo: Boolean,
    val generateWrapper: Boolean,
    val visibility: Visibility,
    val generateDI: Boolean,
    val diScope: DIScope
)