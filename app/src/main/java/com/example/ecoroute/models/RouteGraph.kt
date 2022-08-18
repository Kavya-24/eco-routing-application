package com.example.ecoroute.models

data class RouteGraph(

    val source_node: RouteGraphNode,                     //The initial source node
    val destination_node: RouteGraphNode,                //The initial destination node
    val graph_nodes: List<RouteGraphNode>,               //The set of all the nodes
    val graph_edges: List<RouteGraphEdge?>                //The set of the roads
) {

    data class RouteGraphNode(
        val node_longitude: Double,
        val node_latitude: Double,
        val node_height: Double,
        val node_description: String,
        val node_weight: Double,
        val node_time: Double,
    ) {
        override fun equals(other: Any?): Boolean {
            return super.equals(other)
        }

        override fun hashCode(): Int {
            var result = node_longitude.hashCode()
            result = 31 * result + node_latitude.hashCode()
            result = 31 * result + node_height.hashCode()
            result = 31 * result + node_description.hashCode()
            result = 31 * result + node_weight.hashCode()
            result = 31 * result + node_time.hashCode()
            return result
        }

    }

    data class RouteGraphEdge(
        val edge_start_node: RouteGraphNode,
        val edge_end_node: RouteGraphNode,
        val edge_description: String,
        val edge_average_height: Double,
        val edge_weight: Double,
        val edge_time: Double
    )


}
