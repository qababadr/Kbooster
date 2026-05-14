package com.badrqaba.kbooster.network

import com.google.gson.Gson
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.cio.CIO

/**
 * A builder class for creating instances of [ApiService].
 * It allows configuration of base URL, timeouts, logging, and custom HTTP engines or Gson instances.
 */
class ApiServiceBuilder {

    private var baseUrl: String = ""
    private var timeout: Long = 30_000 // in milliseconds
    private var withLogger: Boolean = false
    private var engine: HttpClientEngine? = null
    private var gson: Gson = Gson()

    /**
     * Sets the base URL for the [ApiService].
     *
     * @param url The base URL string.
     * @return This builder instance.
     */
    fun baseUrl(url: String): ApiServiceBuilder{
        this.baseUrl = url
        return this
    }

    /**
     * Sets the timeout for network requests.
     *
     * @param timeoutMillis The timeout in milliseconds.
     * @return This builder instance.
     */
    fun timeout(timeoutMillis: Long): ApiServiceBuilder {
        timeout = timeoutMillis
        return this
    }

    /**
     * Enables logging for the [ApiService].
     *
     * @return This builder instance.
     */
    fun withLogger(): ApiServiceBuilder {
        withLogger = true
        return this
    }

    /**
     * Sets a custom [HttpClientEngine] for the [ApiService].
     * If not set, [Android] engine will be used by default.
     *
     * @param engine The custom HTTP client engine.
     * @return This builder instance.
     */
    fun engine(engine: HttpClientEngine): ApiServiceBuilder {
        this.engine = engine
        return this
    }

    /**
     * Sets a custom [Gson] instance for JSON serialization/deserialization.
     *
     * @param gson The custom Gson instance.
     * @return This builder instance.
     */
    fun gson(gson: Gson): ApiServiceBuilder {
        this.gson = gson
        return this
    }

    /**
     * Builds and returns a new [ApiService] instance with the configured parameters.
     *
     * @return A configured [ApiService] instance.
     */
    fun build(): ApiService {
        val clientEngine = engine ?: CIO.create()

        return ApiService(
            engine = clientEngine,
            baseUrl = baseUrl,
            gson = gson,
            withLogger = withLogger,
            timeout = timeout
        )
    }
}