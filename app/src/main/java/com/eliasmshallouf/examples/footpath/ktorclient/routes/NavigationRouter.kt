package com.eliasmshallouf.examples.footpath.ktorclient.routes

import com.eliasmshallouf.examples.footpath.ktorclient.DirectionsApiClient
import com.eliasmshallouf.examples.footpath.models.response.Direction
import com.mapbox.geojson.Point
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

enum class NavigateBy(val type: String) {
    Walking("walking")
}

suspend fun DirectionsApiClient.navigate(
    start: Point,
    end: Point,
    type: NavigateBy = NavigateBy.Walking
) = client.get(
    "$BASE_URL/${type.type}/${start.longitude()},${start.latitude()};${end.longitude()},${end.latitude()}?"
) {
    parameter("geometries", "geojson")
    parameter("access_token", accessToken)
}.bodyAsText()