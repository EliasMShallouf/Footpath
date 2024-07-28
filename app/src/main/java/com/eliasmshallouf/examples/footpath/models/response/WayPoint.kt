package com.eliasmshallouf.examples.footpath.models.response

import kotlinx.serialization.Serializable

@Serializable
data class WayPoint(
    val distance: Double,
    val name: String,
    val location: List<Double>
)
