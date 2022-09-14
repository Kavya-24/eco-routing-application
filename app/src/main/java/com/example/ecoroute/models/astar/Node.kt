package com.example.ecoroute.models.astar

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point

data class Node(
    val node_point: Point?,
    val g_n: Double,
    val h_n: Double,
    val soc : Int,
    var features : List<Feature>?
){

}

