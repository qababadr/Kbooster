package com.badrqaba.kbooster.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import java.lang.reflect.Type

/**
 * Reads the response body as text and converts it to Raw Response.
 * Throws [ApiException] if the HTTP status is not success.
 */
suspend inline fun <reified T> HttpResponse.toRawApiResponse(
    gson: Gson
): T {
    // Read body as text
    val bodyText = this.body<String>()

    // Deserialize to ApiResponse<T, Extra>
    val type: Type = object : TypeToken<T>() {}.type
    val apiResponse = gson.fromJson<T>(bodyText, type)

    // Throw ApiException for non-successful status
    if (!status.isSuccess()) {
        throw ApiException(
            message = "Request failed with status ${status.value}",
            errors = null,
            statusCode = status.value
        )
    }

    return apiResponse
}

/**
 * Reads the response body as text and converts it to [ApiResponse].
 * Throws [ApiException] if the HTTP status is not success.
 */
suspend inline fun <reified T, reified Extra> HttpResponse.toApiResponse(
    gson: Gson
): ApiResponse<T, Extra> {
    // Read body as text
    val bodyText = this.body<String>()

    // Deserialize to ApiResponse<T, Extra>
    val type: Type = object : TypeToken<ApiResponse<T, Extra>>() {}.type
    val apiResponse = gson.fromJson<ApiResponse<T, Extra>>(bodyText, type)

    // Throw ApiException for non-successful status
    if (!status.isSuccess()) {
        throw ApiException(
            message = apiResponse.message ?: "Request failed with status ${status.value}",
            errors = apiResponse.errors,
            statusCode = status.value
        )
    }

    return apiResponse
}