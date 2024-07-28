package com.eliasmshallouf.examples.footpath.models.response

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val weight: Double,
    val duration: Double,
    val distance: Double,
    val geometry: Geometry
)
