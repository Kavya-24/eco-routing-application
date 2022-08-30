package com.example.ecoroute.models.responses


import com.squareup.moshi.Json

data class GeoCodedQueryResponse(
    @Json(name = "attribution")
    val attribution: String, // NOTICE: © 2022 Mapbox and its suppliers. All rights reserved. Use of this data is subject to the Mapbox Terms of Service (https://www.mapbox.com/about/maps/). This response and the information it contains may not be retained. POI(s) provided by Foursquare.
    @Json(name = "features")
    val features: List<Feature>,
    @Json(name = "query")
    val query: List<String>,
    @Json(name = "type")
    val type: String // FeatureCollection
) {
    data class Feature(
        @Json(name = "center")
        val center: List<Double>,
        @Json(name = "context")
        val context: List<Context>,
        @Json(name = "geometry")
        val geometry: Geometry,
        @Json(name = "id")
        val id: String, // poi.68719488767
        @Json(name = "place_name")
        val placeName: String, // International House, 2299 Piedmont Ave, Berkeley, California 94704, United States
        @Json(name = "place_type")
        val placeType: List<String>,
        @Json(name = "properties")
        val properties: Properties,
        @Json(name = "relevance")
        val relevance: Int, // 1
        @Json(name = "text")
        val text: String, // International House
        @Json(name = "type")
        val type: String // Feature
    ) {
        data class Context(
            @Json(name = "id")
            val id: String, // neighborhood.668708076
            @Json(name = "short_code")
            val shortCode: String, // US-CA
            @Json(name = "text")
            val text: String, // University of California
            @Json(name = "wikidata")
            val wikidata: String // Q484678
        )

        data class Geometry(
            @Json(name = "coordinates")
            val coordinates: List<Double>,
            @Json(name = "type")
            val type: String // Point
        )

        data class Properties(
            @Json(name = "address")
            val address: String, // 2299 Piedmont Ave
            @Json(name = "category")
            val category: String, // college cafe, cafe, coffee, tea
            @Json(name = "foursquare")
            val foursquare: String, // 4b5ce96df964a5207f4a29e3
            @Json(name = "landmark")
            val landmark: Boolean, // true
            @Json(name = "maki")
            val maki: String // cafe
        )
    }
}