package com.adm.url_parser.commons.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

sealed class ParserRequestTypes {
    data object Get : ParserRequestTypes()
    data class Post(val body: Any?) : ParserRequestTypes()
}

sealed class UrlParserNetworkResponse<T>(
    val data: T? = null,
    val error: String? = null
) {
    class Success<T>(data: T?) : UrlParserNetworkResponse<T>(data = data)
    class Failure<T>(error: String) : UrlParserNetworkResponse<T>(error = error)
    class Loading<T>() : UrlParserNetworkResponse<T>()
    class Idle<T>() : UrlParserNetworkResponse<T>()
}


object UrlParserNetworkClient {
    private const val TIMEOUT = 60_000L
    val jsonHelper = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun getJsonHelperr() = jsonHelper
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(jsonHelper)
        }
        install(HttpTimeout) {
            connectTimeoutMillis = TIMEOUT
            socketTimeoutMillis = TIMEOUT
            requestTimeoutMillis = TIMEOUT
        }
    }

    suspend fun ParserRequestTypes.getHttpBuilder(
        url: String, callback: (HttpRequestBuilder) -> Unit
    ): HttpResponse {
        return when (this) {
            is ParserRequestTypes.Get -> {
                client.get(url) {
                    callback.invoke(this)
                }
            }

            is ParserRequestTypes.Post -> {
                client.post(url) {
                    callback.invoke(this)
                    setBody(body)
                }
            }
        }
    }

    suspend inline fun <reified T> makeNetworkRequest(
        url: String,
        requestType: ParserRequestTypes,
        headers: Map<String, String>? = null,
    ): UrlParserNetworkResponse<T> {
        Log.d("makeNetworkRequest", "makeNetworkRequest: $url")
        return withContext(Dispatchers.IO) {
            try {
                val response: String = requestType.getHttpBuilder(url) {
                    Log.d("makeNetworkRequest", "request builder=${url}")

                    if (requestType is ParserRequestTypes.Post) {
                        Log.d("makeNetworkRequest", "Body = ${requestType.body}")
                        it.header("Content-Type", "application/json")
                        it.setBody(requestType.body)
                    }
                    headers?.let { headers ->
                        headers.forEach { (key, value) ->
                            it.header(key, value)
                        }
                    }
                    Log.d("cvv", "${it.body}")
                }.body()
                Log.d("makeNetworkRequest", "Network Response:$response")


                val newResponse: T = jsonHelper.decodeFromString(response)
                (UrlParserNetworkResponse.Success(newResponse))

            } catch (e: ClientRequestException) {
                Log.d("makeNetworkRequest", "Network ClientRequestException:${e.message}")

                (UrlParserNetworkResponse.Failure(e.message))
            } catch (e: ServerResponseException) {
                Log.d("makeNetworkRequest", "Network ServerResponseException:${e.message}")

                (UrlParserNetworkResponse.Failure(e.message))
            } catch (e: Exception) {
                Log.d("makeNetworkRequest", "Network Exception:${e.message}")
                (UrlParserNetworkResponse.Failure(e.message ?: "Unknown error"))
            }
        }
    }

    suspend fun makeNetworkRequestString(
        url: String,
        requestType: ParserRequestTypes,
        headers: Map<String, String>? = null,
        formData: Map<String, String>? = null,
    ): UrlParserNetworkResponse<String> {
        Log.d("makeNetworkRequest", "makeNetworkRequest: $url")
        return withContext(Dispatchers.IO) {
            try {
                val response: String = requestType.getHttpBuilder(url) {
                    Log.d("makeNetworkRequest", "request builder=${url}")

                    if (requestType is ParserRequestTypes.Post && formData == null) {
                        it.header("Content-Type", "application/json")
                        it.setBody(requestType.body)
                    }
                    headers?.let { headers ->
                        headers.forEach { (key, value) ->
                            it.header(key, value)
                        }
                    }
                    val params = Parameters.build {
                        formData?.forEach { data ->
                            append(data.key, data.value)
                        }
                    }
                    it.setBody(
                        FormDataContent(
                            params
                        )
                    )
                    Log.d("cvv", "${it.body}")
                }.body()
                Log.d("makeNetworkRequest", "Network Response:$response")
                (UrlParserNetworkResponse.Success(response))

            } catch (e: ClientRequestException) {
                Log.d("makeNetworkRequest", "Network ClientRequestException:${e.message}")

                (UrlParserNetworkResponse.Failure(e.message))
            } catch (e: ServerResponseException) {
                Log.d("makeNetworkRequest", "Network ServerResponseException:${e.message}")

                (UrlParserNetworkResponse.Failure(e.message))
            } catch (e: Exception) {
                Log.d("makeNetworkRequest", "Network Exception:${e.message}")
                (UrlParserNetworkResponse.Failure(e.message ?: "Unknown error"))
            }
        }
    }

    suspend inline fun <reified T> makeNetworkRequestXXForm(
        url: String,
        headers: Map<String, String>? = null,
        formData: Map<String, String>? = null,
    ): UrlParserNetworkResponse<T> {
        Log.d("makeNetworkRequest", "makeNetworkRequest: $url")
        return withContext(Dispatchers.IO) {
            try {
                val response: String = ParserRequestTypes.Post(null).getHttpBuilder(url) {
                    Log.d("makeNetworkRequest", "request builder=${url}")
                    val params = Parameters.build {
                        formData?.forEach { data ->
                            append(data.key, data.value)
                        }
                    }
                    it.setBody(
                        FormDataContent(
                            params
                        )
                    )
                    headers?.let { headers ->
                        headers.forEach { (key, value) ->
                            it.header(key, value)
                        }
                    }
                    Log.d("cvv", "${it.body}")
                }.body()
                Log.d("makeNetworkRequest", "Network Response:$response")


                val newResponse: T = jsonHelper.decodeFromString(response)
                (UrlParserNetworkResponse.Success(newResponse))

            } catch (e: ClientRequestException) {
                Log.d("makeNetworkRequest", "Network ClientRequestException:${e.message}")

                (UrlParserNetworkResponse.Failure(e.message))
            } catch (e: ServerResponseException) {
                Log.d("makeNetworkRequest", "Network ServerResponseException:${e.message}")

                (UrlParserNetworkResponse.Failure(e.message))
            } catch (e: Exception) {
                Log.d("makeNetworkRequest", "Network Exception:${e.message}")
                (UrlParserNetworkResponse.Failure(e.message ?: "Unknown error"))
            }
        }
    }

    suspend inline fun makeNetworkRequestStringXXForm(
        url: String,
        headers: Map<String, String>? = null,
        formData: Map<String, String>? = null,
    ): UrlParserNetworkResponse<String> {
        Log.d("makeNetworkRequest", "makeNetworkRequest: $url")
        return withContext(Dispatchers.IO) {
            try {
                val response: String = ParserRequestTypes.Post(null).getHttpBuilder(url) {
                    val params = Parameters.build {
                        formData?.forEach { data ->
                            append(data.key, data.value)
                        }
                    }
                    it.setBody(
                        FormDataContent(
                            params
                        )
                    )
                    headers?.let { headers ->
                        headers.forEach { (key, value) ->
                            it.header(key, value)
                        }
                    }
                    Log.d("cvv", "${it.body}")
                }.body()
                Log.d("makeNetworkRequest", "Network Response:$response")
                (UrlParserNetworkResponse.Success(response))

            } catch (e: ClientRequestException) {
                Log.d("makeNetworkRequest", "Network ClientRequestException:${e.message}")

                (UrlParserNetworkResponse.Failure(e.message))
            } catch (e: ServerResponseException) {
                Log.d("makeNetworkRequest", "Network ServerResponseException:${e.message}")

                (UrlParserNetworkResponse.Failure(e.message))
            } catch (e: Exception) {
                Log.d("makeNetworkRequest", "Network Exception:${e.message}")
                (UrlParserNetworkResponse.Failure(e.message ?: "Unknown error"))
            }
        }
    }

}