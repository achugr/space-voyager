package com.achugr.voyager.service

import com.achugr.voyager.SpaceTokenInfo
import com.achugr.voyager.entity.ClientAware
import com.achugr.voyager.storage.Edge
import com.achugr.voyager.storage.Graph
import com.achugr.voyager.storage.Vertex
import com.achugr.voyager.storage.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GetGraphService(private val spaceTokenInfo: SpaceTokenInfo) {
    private val log: Logger = LoggerFactory.getLogger("GetGraphService.kt")

    fun getGraph(): Graph {
        val vertices = hashSetOf<Vertex>()
        val edges = hashSetOf<Edge>()
        val clientId = spaceTokenInfo.spaceAppInstance.clientId
        transaction { session ->
            session.query(
                "Match (n {clientId:'$clientId'})" +
                        "-[r {clientId:'$clientId'}]" +
                        "-(m {clientId:'$clientId'})\n" +
                        "Return n,r,m", mapOf<String, Any>(), true
            )
                .flatMap { entry -> listOf(entry["n"], entry["m"], entry["r"]) }
                .filter { graphEntity ->
                    val entityClientId = (graphEntity as ClientAware).clientId
                    val accessLegal = entityClientId == clientId
                    if (!accessLegal) {
                        log.warn("Filtering graph entity of another customer. Context: ${clientId}, entity: $entityClientId")
                    }
                    accessLegal
                }
                .map { graphEntity ->
                    try {
                        when (graphEntity) {
                            is com.achugr.voyager.entity.Vertex -> vertices.add(mapVertex(graphEntity))
                            is com.achugr.voyager.entity.Edge -> edges.add(parseEdge(graphEntity))
                            else -> {
                                log.warn("Unknown entity: $graphEntity")
                            }
                        }
                    } catch (e: Exception) {
                        log.error(e.message, e)
                    }
                }
        }
        return Graph(vertices.toList(), edges.toList())
    }

    private fun mapVertex(vertex: com.achugr.voyager.entity.Vertex): Vertex {
        return Vertex(vertex.id, vertex.label, vertex.name)
    }

    private fun parseEdge(edge: com.achugr.voyager.entity.Edge): Edge {
        return Edge(edge.id, edge.relation, edge.startId, edge.endId)
    }
}