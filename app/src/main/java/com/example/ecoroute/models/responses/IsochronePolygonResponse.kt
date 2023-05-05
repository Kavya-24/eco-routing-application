package com.example.ecoroute.models.responses


import androidx.annotation.Keep
import com.squareup.moshi.Json
@Keep
data class IsochronePolygonResponse(
    @Json(name = "features")
    val features: List<Feature>,
    @Json(name = "type")
    val type: String // FeatureCollection
) {
    data class Feature(
        @Json(name = "geometry")
        val geometry: Geometry,
        @Json(name = "properties")
        val properties: Properties,
        @Json(name = "type")
        val type: String // Feature
    ) {
        data class Geometry(
            @Json(name = "coordinates")
            val coordinates: List<List<List<Double>>>,
            @Json(name = "type")
            val type: String // Polygon
        )

        data class Properties(
            @Json(name = "color")
            val color: String, // #4286f4
            @Json(name = "contour")
            val contour: Int, // 15
            @Json(name = "fill")
            val fill: String, // #4286f4
            @Json(name = "fillColor")
            val fillColor: String, // #4286f4
            @Json(name = "fillOpacity")
            val fillOpacity: Double, // 0.33
            @Json(name = "fill-opacity")
            val _fillOpacity: Double, // 0.33
            @Json(name = "metric")
            val metric: String, // time
            @Json(name = "opacity")
            val opacity: Double // 0.33
        )
    }
}