package com.example.ecoroute.utils.routeutils

import com.example.ecoroute.models.RouteGraph
import com.example.ecoroute.utils.InitOptions
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.LegStep
import com.mapbox.geojson.Point


object RouteGraphUtil {

    fun getRouteGraphInstance(
        routes: List<DirectionsRoute>,
        src: RouteGraph.RouteGraphNode,
        dst: RouteGraph.RouteGraphNode
    ): RouteGraph? {

        var routeGraph: RouteGraph? = null

        if (routes.isEmpty()) {
            return routeGraph
        }
        //Extract the legs from the primary route
        val routeLegs = routes.first().legs()


        if (routeLegs.isNullOrEmpty()) {
            return routeGraph
        }


        val graphNodes: MutableList<RouteGraph.RouteGraphNode> = mutableListOf()
        val graphEdges: MutableList<RouteGraph.RouteGraphEdge?> = mutableListOf()

        //Add source
        graphNodes.add(src)
        //Previous Node iterator
        var idx = 0;
        var c = "Adding Edges:\n"

        for (j in 0 until routeLegs.size) {

            val routeSteps = routeLegs[j].steps()

            if (routeSteps != null) {


                for (i in 0 until routeSteps.size) {


                    val previousNode = graphNodes[idx]



                    if (i == routeSteps.size - 1) {
                        graphNodes.add(dst)
                    } else {
                        graphNodes.add(
                            convertManeuversToNode(
                                routeSteps.elementAt(i).maneuver().location()
                            )
                        )
                    }

                    val nextNode = graphNodes[idx + 1]

                    graphEdges.add(
                        convertStepsToRoad(
                            routeSteps.elementAt(i),
                            previousNode,
                            nextNode
                        )
                    )

                    idx++

                }
            }
        }


        //Populate routeGraph
        routeGraph =
            RouteGraph(graphNodes[0], graphNodes[graphNodes.size - 1], graphNodes, graphEdges)



        return routeGraph
    }

    private fun convertStepsToRoad(
        step: LegStep,
        previousNode: RouteGraph.RouteGraphNode,
        nextNode: RouteGraph.RouteGraphNode
    ): RouteGraph.RouteGraphEdge {


        return RouteGraph.RouteGraphEdge(
            edge_start_node = previousNode,
            edge_end_node = nextNode,
            edge_description = if (!step.name().isNullOrEmpty()) step.name()!! else "STANDARD_EDGE",
            edge_time = step.duration().toDouble(),
            edge_average_height = if (!step.maneuver().location()
                    .altitude().isNaN()
            ) step.maneuver().location().altitude() else 200.0,
            edge_weight = -step.weight().toDouble()
        )


    }


    //There is a 70% probability that this will not be a special node
    private fun convertManeuversToNode(_coordinates: Point): RouteGraph.RouteGraphNode {

        val special = (1..10).random()

        //Not a special node
        return if (special <= 8) {
            RouteGraph.RouteGraphNode(
                _coordinates.longitude(),
                _coordinates.latitude(),
                if (!_coordinates.altitude().isNaN()) _coordinates.altitude() else 200.0,
                "STANDARD",
                -1.0,
                1.0
            )
        }
        //Special Node. Make it charge stop.
        else {
            val specialType = InitOptions.getRandomNodeWeightsForMapping()
            RouteGraph.RouteGraphNode(
                _coordinates.longitude(),
                _coordinates.latitude(),
                if (!_coordinates.altitude().isNaN()) _coordinates.altitude() else 200.0,
                "EV",
                (1000..5000).random().toDouble(),
                (3600..7200).random().toDouble()
            )
        }


    }


    fun convertTerminalsToNode(
        _coordinates: com.mapbox.geojson.Point,
        _description: String
    ): RouteGraph.RouteGraphNode {
        val specialType = InitOptions.getRandomNodeWeightsForMapping()
        return RouteGraph.RouteGraphNode(
            _coordinates.longitude(),
            _coordinates.latitude(),
            if (!_coordinates.altitude().isNaN()) _coordinates.altitude() else 200.0,
            _description,
            -specialType.value.first,
            specialType.value.second
        )
    }


    //TODO: https://docs.mapbox.com/api/maps/tilequery/ use this API for finding the elevations of in-between geometries

}