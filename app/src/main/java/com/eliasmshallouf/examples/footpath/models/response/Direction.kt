package com.eliasmshallouf.examples.footpath.models.response

import kotlinx.serialization.Serializable

@Serializable
data class Direction(
    val routes: List<Route>,
    val code: String,
    val uuid: String
)
