package com.badrqaba.kbooster.network

import kotlinx.serialization.Serializable


// Generic API response with optional extra data
@Serializable
data class ApiResponse<Data, Extra>(
    val data: Data? = null,
    val message: String? = null,
    val errors: Map<String, List<String>>? = null,
    val extra: Extra? = null
)

// Marker class for "Nothing" (like Swift's empty Extra)
object Nothing

// Typealias equivalent for Response<T> = ApiResponse<T, Nothing>
typealias Response<T> = ApiResponse<T, Nothing>