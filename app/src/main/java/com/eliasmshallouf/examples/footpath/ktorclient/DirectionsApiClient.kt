package com.eliasmshallouf.examples.footpath.ktorclient

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object DirectionsApiClient {
    const val BASE_URL = "https://api.mapbox.com/directions/v5/mapbox"
    var accessToken = ""

    @OptIn(ExperimentalSerializationApi::class)
    var client = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.ALL
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 5000L
            connectTimeoutMillis = 5000L
            socketTimeoutMillis = 5000L
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    explicitNulls = false
                }
            )
            jackson {
                configure(SerializationFeature.INDENT_OUTPUT, true)
                setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                })
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            }
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.XHttpMethodOverride, "GET")
            accept(ContentType.Application.Json)
        }
    }

    fun accessToken(token: String) {
        this.accessToken = token
    }
}