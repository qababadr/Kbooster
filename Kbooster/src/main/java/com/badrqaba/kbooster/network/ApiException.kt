package com.badrqaba.kbooster.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText

data class ApiException(
    override val message: String,
    val errors: Map<String, List<String>>? = null,
    val statusCode: Int? = null,
    val causeException: Throwable? = null
) : Exception(message, causeException)

// Extension to convert ResponseException to ApiException using Gson
suspend fun ResponseException.toApiError(): ApiException {
    val status = response.status.value
    val bodyText = try { response.bodyAsText() } catch (_: Exception) { "" }

    val gson = Gson()
    var messageFromBody: String? = null
    var errorsFromBody: Map<String, List<String>>? = null

    runCatching {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = gson.fromJson(bodyText, mapType)

        // Always try to get "message"
        messageFromBody = map["message"]?.toString()

        // Try to get "errors" only if present
        val errorsRaw = map["errors"]
        if (errorsRaw is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            errorsFromBody = (errorsRaw as Map<String, Any>).mapValues { entry ->
                when (val value = entry.value) {
                    is List<*> -> value.filterIsInstance<String>()
                    is String -> listOf(value)
                    else -> emptyList()
                }
            }
        }
    }

    return ApiException(
        message = messageFromBody ?: "Request failed with status $status",
        errors = errorsFromBody,
        statusCode = status,
        causeException = this
    )
}