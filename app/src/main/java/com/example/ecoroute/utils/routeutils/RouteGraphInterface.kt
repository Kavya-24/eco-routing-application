package com.example.ecoroute.utils.routeutils

import com.example.ecoroute.models.RouteGraph

data class Edge(
    val from: RouteGraph.RouteGraphNode,
    val to: RouteGraph.RouteGraphNode,
    val edgeWeight: Double
)

interface Graph<T, W> {

    fun getAllVertices(): Set<RouteGraph.RouteGraphNode>
    fun adjacentVertices(vertex: RouteGraph.RouteGraphNode): Set<RouteGraph.RouteGraphNode>
    fun getDistance(from: RouteGraph.RouteGraphNode, to: RouteGraph.RouteGraphNode): Double


}


class GraphImpl(val directed: Boolean = false, val defaultWeight: Double) :
    Graph<RouteGraph.RouteGraphNode, Double> {

    private val edges = mutableSetOf<Edge>()
    private val vertices = mutableSetOf<RouteGraph.RouteGraphNode>()

    fun addVertex(node: RouteGraph.RouteGraphNode): GraphImpl {
        vertices += node
        return this
    }

    fun addArc(
        _pair: Pair<RouteGraph.RouteGraphNode, RouteGraph.RouteGraphNode>,
        _edgeWeight: Double? = null
    ): GraphImpl {
        val (from, to) = _pair
        addVertex(from)
        addVertex(to)

        addRelation(from, to, _edgeWeight ?: defaultWeight)
        if (!directed) {
            addRelation(to, from, _edgeWeight ?: defaultWeight)
        }

        return this
    }

    override fun getAllVertices(): Set<RouteGraph.RouteGraphNode> {
        return vertices.toSet()
    }

    private fun addRelation(
        from: RouteGraph.RouteGraphNode,
        to: RouteGraph.RouteGraphNode,
        _edgeWeight: Double? = null
    ) {
        edges.add(Edge(from, to, _edgeWeight ?: defaultWeight))
    }

    override fun adjacentVertices(vertex: RouteGraph.RouteGraphNode): Set<RouteGraph.RouteGraphNode> {
        return edges.filter { it.from == vertex }.map { it.to }.toSet()
    }

    override fun getDistance(
        from: RouteGraph.RouteGraphNode,
        to: RouteGraph.RouteGraphNode
    ): Double {

        return edges
            .filter { it.from == from && it.to == to }
            .map { it.edgeWeight }
            .first()
    }

    override fun toString(): String {
        return vertices.joinToString("\n") {
            val adjacentNodes = adjacentVertices(it)
            val connection = if (directed) "->" else "--"
            "$it $connection $adjacentNodes"
        }
    }


}