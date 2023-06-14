package com.achugr.voyager.storage

import kotlinx.serialization.Serializable

@Serializable
data class Graph(
    val vertices: List<Vertex>,
    val edges: List<Edge>
)

@Serializable
data class Vertex(val id: String, val label: String, val name: String)

@Serializable
data class Edge(val id: String, val label: String, val start: String, val end: String)
