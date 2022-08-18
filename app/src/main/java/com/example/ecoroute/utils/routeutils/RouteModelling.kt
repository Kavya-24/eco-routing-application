package com.example.ecoroute.utils.routeutils

import com.example.ecoroute.models.RouteGraph

object RouteModelling {

    var c = ""

    fun modelGraph(
        routeGraph: RouteGraph,
        source_node: RouteGraph.RouteGraphNode,
        destination_node: RouteGraph.RouteGraphNode,
        initial_charge: Double
    ): Pair<Pair<List<RouteGraph.RouteGraphNode>, Double>, String> {

        val graph = getGraphImplementation(routeGraph)
        return shortestPath(graph, source_node, destination_node, initial_charge) to c

    }

    private fun getGraphImplementation(routeGraph: RouteGraph): GraphImpl {


        val graph =
            GraphImpl(directed = false, defaultWeight = -1.0)

        for (i in 0 until routeGraph.graph_edges.size) {
            graph.addArc(
                routeGraph.graph_edges[i]!!.edge_start_node to routeGraph.graph_edges[i]!!.edge_end_node,
                routeGraph.graph_edges[i]!!.edge_weight
            )

        }

        return graph
    }

    private fun shortestPath(
        graph: Graph<RouteGraph.RouteGraphNode, Double>,
        source_node: RouteGraph.RouteGraphNode,
        destination_node: RouteGraph.RouteGraphNode,
        initial_charge: Double
    ): Pair<List<RouteGraph.RouteGraphNode>, Double> {
        return dijkstra(graph, source_node, destination_node, initial_charge)[destination_node]
            ?: (emptyList<RouteGraph.RouteGraphNode>() to Double.POSITIVE_INFINITY)
    }

    //Dijkstra From source, returns the shortest path to every node with the remaining weight charge value
    //This modified version of Dijkstra returns the maximum remaining charge and possible path till
    private fun dijkstra(
        graph: Graph<RouteGraph.RouteGraphNode, Double>,
        source_node: RouteGraph.RouteGraphNode,
        destination_node: RouteGraph.RouteGraphNode,
        initial_charge: Double
    ): Map<RouteGraph.RouteGraphNode, Pair<List<RouteGraph.RouteGraphNode>, Double>> {
        val unvisitedSet = graph.getAllVertices().toMutableSet()


        val distances =
            graph.getAllVertices().map { it to Double.POSITIVE_INFINITY }.toMap().toMutableMap()
        val paths = mutableMapOf<RouteGraph.RouteGraphNode, List<RouteGraph.RouteGraphNode>>()
        distances[source_node] = initial_charge + source_node.node_weight
        var current = source_node

        c += "\nInitial weight at the start = ${distances[current]}"

        while (unvisitedSet.isNotEmpty() && unvisitedSet.contains(destination_node)) {

            c += "\n\nFor current node  ${current.node_description} with distance = ${distances[current]} "

            graph.adjacentVertices(current).forEach { adjacent ->
                val distance = graph.getDistance(current, adjacent).toDouble()

                c += "\n\tDistance to adjacent  ${adjacent.node_description}  is $distance with self-distance = ${distances[adjacent]}"


                /** From current, we can not reach current because the fuel finishes
                 * Continue on the next adjacent level
                 * */
                if (distances[current]!! + distance + adjacent.node_weight < 0.0) {
                    return@forEach
                }

                if (distances[adjacent] == Double.POSITIVE_INFINITY || (distances[adjacent] != Double.POSITIVE_INFINITY && distances[current]!! + distance + adjacent.node_weight > distances[adjacent]!!)) {
                    distances[adjacent] = distances[current]!! + distance + adjacent.node_weight
                    c += "\n\t\tDistance updated for ${adjacent.node_description}. New distance from source = ${distances[adjacent]}"
                    paths[adjacent] =
                        paths.getOrDefault(current, listOf(current)) + listOf(adjacent)
                }
            }

            unvisitedSet.remove(current)

            /**If we have reached the destination node or no other path exists and we are stuck at infinity*/
            if (current == destination_node || unvisitedSet.all { distances[it]!!.isInfinite() }) {
                break
            }

            if (unvisitedSet.isNotEmpty()) {

                current = unvisitedSet.minByOrNull { distances[it]!! }!!
            }

        }

        return paths.mapValues { entry -> entry.value to distances[entry.key]!! }
    }

}


