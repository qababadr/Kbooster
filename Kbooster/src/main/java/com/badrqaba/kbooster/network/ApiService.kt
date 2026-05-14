package com.badrqaba.kbooster.network

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.google.gson.Strictness
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.gson.gson
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import java.io.File

/**
 * A generic API service for making network requests using Ktor.
 * It provides methods for common HTTP operations like GET, POST, PUT, PATCH, and DELETE,
 * along with specialized methods for file download and upload.
 *
 * @property engine The [HttpClientEngine] to be used by the [HttpClient].
 * @property gson The [Gson] instance for JSON serialization and deserialization.
 * @property baseUrl The base URL for all API requests. If provided, it's prepended to endpoints.
 * @property withLogger Whether to enable logging for the [HttpClient].
 */
class ApiService constructor(
    engine: HttpClientEngine,
    val gson: Gson = Gson(),
    private val baseUrl: String? = "",
    private val withLogger: Boolean = true,
    private val timeout: Long = 30_000
) {

    /**
     * The underlying Ktor [HttpClient] instance.
     */
    val client = HttpClient(engine) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
                serializeNulls()
                setStrictness(Strictness.LENIENT)
                setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            }
        }

        if (withLogger) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("ApiService: $message")
                        Log.i("ApiService", message)
                    }
                }
                level = LogLevel.ALL
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = timeout   // 30 seconds
            connectTimeoutMillis = timeout   // 30 seconds
            socketTimeoutMillis = timeout    // 30 seconds
        }

        defaultRequest {
            url(baseUrl)
            header("Accept", "application/json")
        }
    }

    companion object {

        private val staticClient by lazy { HttpClient() }

        /**
         * Downloads a file from the given [url] and saves it to the app's external
         * downloads directory. Using app-specific directory avoids "Permission Denied"
         * on Android 10+ (Scoped Storage).
         *
         * @param context Android context.
         * @param url The direct URL of the file to download.
         * @param fileName The name of the file to be saved.
         * @param folderName Optional subfolder inside the app's downloads directory.
         * @param shouldOverwrite Whether an existing file should be replaced.
         * @param headers Optional HTTP headers.
         * @param onProgress Callback with progress percentage (0.0–100.0).
         * @param onSuccess Callback with resulting [File].
         * @param onFailure Callback with error message.
         */
        suspend fun download(
            context: Context,
            url: String,
            fileName: String,
            folderName: String? = null,
            shouldOverwrite: Boolean = false,
            headers: Map<String, String> = emptyMap(),
            onProgress: (Double) -> Unit,
            onSuccess: (File) -> Unit,
            onFailure: (String?) -> Unit,
        ) {
            val finalFolderName = folderName ?: context.packageName.substringAfterLast(".")

            // Using getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) avoids Permission Denied
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (directory == null) {
                onFailure("Could not access external storage")
                return
            }

            val folder = File(directory, finalFolderName)
            if (!folder.exists()) folder.mkdirs()

            val file = File(folder, fileName)

            if (file.exists()) {
                if (shouldOverwrite) file.delete()
                else {
                    onSuccess(file)
                    return
                }
            }

            try {
                val response: HttpResponse = staticClient.get(url) {
                    headers.forEach { (k, v) -> header(k, v) }
                }

                if (!response.status.isSuccess()) {
                    onFailure("Download failed with status ${response.status.value}")
                    return
                }

                val contentLength = response.contentLength() ?: -1L
                val channel = response.bodyAsChannel()

                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesCopied = 0L
                    val ctx = currentCoroutineContext()

                    while (!channel.isClosedForRead && ctx.isActive) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead == -1) break

                        output.write(buffer, 0, bytesRead)
                        bytesCopied += bytesRead

                        if (contentLength > 0) {
                            onProgress(bytesCopied.toDouble() / contentLength * 100)
                        }
                    }
                }

                onSuccess(file)

            } catch (e: Exception) {
                onFailure(e.message)
            }
        }
    }


    /**
     * Performs a GET request and returns a wrapped [ApiResponse].
     *
     * @param T The type of the data expected in the response.
     * @param Extra The type of any extra metadata expected in the response.
     * @param endPoint The API endpoint.
     * @param baseUrl An optional base URL to override the default one.
     * @param headers Optional HTTP headers for the request.
     * @return An [ApiResponse] containing the parsed data or error.
     * @throws ApiException if the request fails or parsing error occurs.
     */
    suspend inline fun <reified T, reified Extra> get(
        endPoint: String,
        baseUrl: String? = "",
        headers: Map<String, String> = emptyMap()
    ): ApiResponse<T, Extra> {
        try {

            val response = client.get(buildUrl(baseUrl, endPoint)) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }

            return response.toApiResponse(gson)

        } catch (e: ResponseException) {
            throw e.toApiError()
        }
    }

    /**
     * Performs a POST request with a body and returns a wrapped [ApiResponse].
     * Supports both JSON and Form URL-encoded content types.
     *
     * @param T The type of the data expected in the response.
     * @param Extra The type of any extra metadata expected in the response.
     * @param Body The type of the request body.
     * @param endPoint The API endpoint.
     * @param body The request body object.
     * @param baseUrl An optional base URL to override the default one.
     * @param headers Optional HTTP headers for the request.
     * @return An [ApiResponse] containing the parsed data or error.
     * @throws ApiException if the request fails or parsing error occurs.
     */
    suspend inline fun <reified T, reified Extra, reified Body> post(
        endPoint: String,
        body: Body,
        baseUrl: String? = "",
        headers: Map<String, String>? = emptyMap()
    ): ApiResponse<T, Extra> {
        try {

            val response = client.post(buildUrl(baseUrl, endPoint)) {
                val contentType =
                    headers?.get("Content-Type") ?: ContentType.Application.Json.toString()
                contentType(ContentType.parse(contentType))

                headers?.forEach { (key, value) ->
                    header(key, value)
                }

                if (contentType == ContentType.Application.FormUrlEncoded.toString()) {
                    val params = Parameters.build {
                        (body as Map<*, *>).forEach { (key, value) ->
                            append(key.toString(), value.toString())
                        }
                    }

                    setBody(FormDataContent(params))
                } else {
                    setBody(body)
                }
            }

            return response.toApiResponse(gson)

        } catch (e: ResponseException) {
            throw e.toApiError()
        }
    }

    /**
     * Performs a GET request and returns the raw parsed object of type [T].
     *
     * @param T The type of the object to parse the response into.
     * @param endPoint The API endpoint.
     * @param baseUrl An optional base URL to override the default one.
     * @param headers Optional HTTP headers for the request.
     * @return The parsed object of type [T].
     * @throws ApiException if the request fails or parsing error occurs.
     */
    suspend inline fun <reified T> rawGet(
        endPoint: String,
        baseUrl: String? = "",
        headers: Map<String, String> = emptyMap()
    ): T {
        try {

            val response = client.get(buildUrl(baseUrl, endPoint)) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }

            return response.toRawApiResponse(gson)

        } catch (e: ResponseException) {
            throw e.toApiError()
        }
    }

    /**
     * Performs a POST request and returns the raw parsed object of type [T].
     * Supports both JSON and Form URL-encoded content types.
     *
     * @param T The type of the object to parse the response into.
     * @param Body The type of the request body.
     * @param endPoint The API endpoint.
     * @param body The request body object.
     * @param baseUrl An optional base URL to override the default one.
     * @param headers Optional HTTP headers for the request.
     * @return The parsed object of type [T].
     * @throws ApiException if the request fails or parsing error occurs.
     */
    suspend inline fun <reified T, reified Body> rawPost(
        endPoint: String,
        body: Body,
        baseUrl: String? = "",
        headers: Map<String, String>? = emptyMap()
    ): T {
        try {

            val response = client.post(buildUrl(baseUrl, endPoint)) {
                val contentType =
                    headers?.get("Content-Type") ?: ContentType.Application.Json.toString()
                contentType(ContentType.parse(contentType))

                headers?.forEach { (key, value) ->
                    header(key, value)
                }

                if (contentType == ContentType.Application.FormUrlEncoded.toString()) {
                    val params = Parameters.build {
                        (body as Map<*, *>).forEach { (key, value) ->
                            append(key.toString(), value.toString())
                        }
                    }

                    setBody(FormDataContent(params))
                } else {
                    setBody(body)
                }
            }

            return response.toRawApiResponse(gson)

        } catch (e: ResponseException) {
            throw e.toApiError()
        }
    }

    /**
     * Performs a PUT request with a body and returns a wrapped [ApiResponse].
     *
     * @param T The type of the data expected in the response.
     * @param Extra The type of any extra metadata expected in the response.
     * @param Body The type of the request body.
     * @param endPoint The API endpoint.
     * @param body The request body object.
     * @param baseUrl An optional base URL to override the default one.
     * @param headers Optional HTTP headers for the request.
     * @return An [ApiResponse] containing the parsed data or error.
     * @throws ApiException if the request fails or parsing error occurs.
     */
    suspend inline fun <reified T, reified Extra, reified Body> put(
        endPoint: String,
        body: Body,
        baseUrl: String? = "",
        headers: Map<String, String>? = emptyMap()
    ): ApiResponse<T, Extra> {
        try {

            val response = client.put(buildUrl(baseUrl, endPoint)) {
                val contentType =
                    headers?.get("Content-Type") ?: ContentType.Application.Json.toString()
                contentType(ContentType.parse(contentType))

                headers?.forEach { (key, value) ->
                    header(key, value)
                }

                if (contentType == ContentType.Application.FormUrlEncoded.toString()) {
                    val params = Parameters.build {
                        (body as Map<*, *>).forEach { (key, value) ->
                            append(key.toString(), value.toString())
                        }
                    }

                    setBody(FormDataContent(params))
                } else {
                    setBody(body)
                }
            }

            return response.toApiResponse(gson)

        } catch (e: ResponseException) {
            throw e.toApiError()
        }
    }

    /**
     * Performs a PATCH request with a body and returns a wrapped [ApiResponse].
     *
     * @param T The type of the data expected in the response.
     * @param Extra The type of any extra metadata expected in the response.
     * @param Body The type of the request body.
     * @param endPoint The API endpoint.
     * @param body The request body object.
     * @param baseUrl An optional base URL to override the default one.
     * @param headers Optional HTTP headers for the request.
     * @return An [ApiResponse] containing the parsed data or error.
     * @throws ApiException if the request fails or parsing error occurs.
     */
    suspend inline fun <reified T, reified Extra, reified Body> patch(
        endPoint: String,
        body: Body,
        baseUrl: String? = "",
        headers: Map<String, String>? = emptyMap()
    ): ApiResponse<T, Extra> {
        try {

            val response = client.patch(buildUrl(baseUrl, endPoint)) {
                val contentType =
                    headers?.get("Content-Type") ?: ContentType.Application.Json.toString()
                contentType(ContentType.parse(contentType))

                headers?.forEach { (key, value) ->
                    header(key, value)
                }

                if (contentType == ContentType.Application.FormUrlEncoded.toString()) {
                    val params = Parameters.build {
                        (body as Map<*, *>).forEach { (key, value) ->
                            append(key.toString(), value.toString())
                        }
                    }

                    setBody(FormDataContent(params))
                } else {
                    setBody(body)
                }
            }

            return response.toApiResponse(gson)

        } catch (e: ResponseException) {
            throw e.toApiError()
        }
    }

    /**
     * Performs a DELETE request and returns a wrapped [ApiResponse].
     *
     * @param T The type of the data expected in the response.
     * @param Extra The type of any extra metadata expected in the response.
     * @param endPoint The API endpoint.
     * @param baseUrl An optional base URL to override the default one.
     * @param headers Optional HTTP headers for the request.
     * @return An [ApiResponse] containing the parsed data or error.
     * @throws ApiException if the request fails or parsing error occurs.
     */
    suspend inline fun <reified T, reified Extra> delete(
        endPoint: String,
        baseUrl: String? = "",
        headers: Map<String, String> = emptyMap()
    ): ApiResponse<T, Extra> {
        try {

            val response = client.delete(buildUrl(baseUrl, endPoint)) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }

            return response.toApiResponse(gson)

        } catch (e: ResponseException) {
            throw e.toApiError()
        }
    }

    /**
     * Downloads a file from the given [url] into [destFile].
     * - If [overwrite] is false and file exists, skips download.
     * - [progress] lambda provides percentage 0..100.
     */
    suspend fun download(
        url: String,
        destFile: File,
        headers: Map<String, String> = emptyMap(),
        overwrite: Boolean = false,
        progress: ((Double) -> Unit)? = null
    ): File {
        if (!overwrite && destFile.exists()) return destFile
        destFile.parentFile?.mkdirs()

        val response: HttpResponse = client.get(url) {
            headers.forEach { (k, v) -> header(k, v) }
        }

        if (!response.status.isSuccess()) {
            throw ApiException("Download failed with status: ${response.status.value}")
        }

        val contentLength = response.contentLength() ?: -1L
        val channel = response.bodyAsChannel()

        destFile.outputStream().use { output ->
            val buffer = ByteArray(8192)
            var bytesCopied = 0L
            val ctx = currentCoroutineContext()  // get coroutine context

            while (!channel.isClosedForRead && ctx.isActive) {
                val bytesRead = channel.readAvailable(buffer)
                if (bytesRead == -1) break
                output.write(buffer, 0, bytesRead)
                bytesCopied += bytesRead

                if (contentLength > 0) {
                    progress?.invoke(bytesCopied.toDouble() / contentLength * 100)
                }
            }
        }

        return destFile
    }

    /**
     * Uploads a file using multipart form data and returns a wrapped [ApiResponse].
     *
     * @param T The type of the data expected in the response.
     * @param Extra The type of any extra metadata expected in the response.
     * @param Body The type of additional JSON data to be sent with the file.
     * @param client The [HttpClient] to use for the upload (consider using the internal client).
     * @param url The full URL for the upload endpoint.
     * @param baseUrl An optional base URL to override the default one.
     * @param file The [File] to upload.
     * @param filename The name of the file being uploaded.
     * @param contentType The MIME type of the file.
     * @param additionalData Optional metadata to be sent alongside the file.
     * @param mediaField The field name for the file in the multipart form.
     * @param headers Optional HTTP headers for the request.
     * @param gson [Gson] instance for serializing [additionalData].
     * @return An [ApiResponse] containing the parsed data or error.
     * @throws ApiException if the upload fails or parsing error occurs.
     */
    suspend inline fun <reified T, reified Extra, reified Body> upload(
        endPoint: String,
        file: File,
        filename: String,
        contentType: String,
        baseUrl: String? = "",
        additionalData: Body? = null,
        mediaField: String = "file",
        headers: Map<String, String> = emptyMap(),
        gson: Gson = Gson()
    ): ApiResponse<T, Extra> {
        try {
            val response = client.post(buildUrl(baseUrl, endPoint)) {
                contentType(ContentType.MultiPart.FormData)
                headers.forEach { (k, v) -> header(k, v) }

                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(mediaField, file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                            })

                            additionalData?.let {
                                val jsonObject = gson.toJsonTree(it).asJsonObject
                                for ((key, value) in jsonObject.entrySet()) {
                                    append(key, value.toString())
                                }
                            }
                        }
                    ))
            }

            return response.toApiResponse(gson)

        } catch (e: ResponseException) {
            throw e.toApiError()
        }
    }

    /**
     * Helper function to construct a full URL from a base URL and an endpoint.
     */
    fun buildUrl(base: String? = "", endPoint: String): String {
        return if (baseUrl == null) base + endPoint else baseUrl + endPoint
    }
}