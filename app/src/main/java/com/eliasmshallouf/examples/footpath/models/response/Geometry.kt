package com.eliasmshallouf.examples.footpath.models.response

import kotlinx.serialization.Serializable

@Serializable
data class Geometry(
    val coordinates: List<List<Double>>,
    val type: String
)
