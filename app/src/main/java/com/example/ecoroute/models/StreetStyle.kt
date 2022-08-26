package com.example.ecoroute.models


import com.squareup.moshi.Json

data class StreetStyle(
    @Json(name = "layers")
    val layers: List<Layer>,
    @Json(name = "name")
    val name: String, // Mapbox Streets tileset v8
    @Json(name = "sources")
    val sources: Sources,
    @Json(name = "version")
    val version: Int // 8
) {
    data class Layer(
        @Json(name = "id")
        val id: String, // admin
        @Json(name = "paint")
        val paint: Paint,
        @Json(name = "source")
        val source: String, // mapbox-streets
        @Json(name = "source-layer")
        val sourceLayer: String, // admin
        @Json(name = "type")
        val type: String // line
    ) {
        data class Paint(
            @Json(name = "circle-color")
            val circleColor: String?, // rgba(238,78,139, 0.4)
            @Json(name = "circle-radius")
            val circleRadius: Int?, // 3
            @Json(name = "circle-stroke-color")
            val circleStrokeColor: String?, // rgba(238,78,139, 1)
            @Json(name = "circle-stroke-width")
            val circleStrokeWidth: Int?, // 1
            @Json(name = "fill-color")
            val fillColor: String?, // rgba(66,100,251, 0.3)
            @Json(name = "fill-outline-color")
            val fillOutlineColor: String?, // rgba(66,100,251, 1)
            @Json(name = "line-color")
            val lineColor: String? // #ffffff
        )
    }

    data class Sources(
        @Json(name = "mapbox-streets")
        val mapboxStreets: MapboxStreets
    ) {
        data class MapboxStreets(
            @Json(name = "type")
            val type: String, // vector
            @Json(name = "url")
            val url: String // mapbox://mapbox.mapbox-streets-v8
        )
    }
}