package com.example.ecoroute.models.responses


import androidx.annotation.Keep
import com.squareup.moshi.Json
@Keep
data class DirectionRouteResponse(
    @Json(name = "code")
    val code: String, // Ok
    @Json(name = "routes")
    val routes: List<Route>,
    @Json(name = "uuid")
    val uuid: String, // 14QsXJWTR65g7bN8SR9hIMeHR3VQhQgrBvVuRgsTrZ5IKyTaoVgExw==
    @Json(name = "waypoints")
    val waypoints: List<Waypoint>,
    @Json(name = "message")
    val message: String,
) {
    data class Route(
        @Json(name = "country_crossed")
        val countryCrossed: Boolean, // false
        @Json(name = "distance")
        val distance: Double, // 6437.363
        @Json(name = "duration")
        val duration: Double, // 964.055
        @Json(name = "geometry")
        val geometry: Geometry,
        @Json(name = "legs")
        val legs: List<Leg>,
        @Json(name = "weight")
        val weight: Double, // 893.084
        @Json(name = "weight_name")
        val weightName: String // auto
    ) {
        data class Geometry(
            @Json(name = "coordinates")
            val coordinates: List<List<Double>>,
            @Json(name = "type")
            val type: String // LineString
        )

        data class Leg(
            @Json(name = "admins")
            val admins: List<Admin>,
            @Json(name = "distance")
            val distance: Double, // 6437.363
            @Json(name = "duration")
            val duration: Double, // 964.055
            @Json(name = "steps")
            val steps: List<Any>,
            @Json(name = "summary")
            val summary: String, // MDR, SH2
            @Json(name = "via_waypoints")
            val viaWaypoints: List<Any>,
            @Json(name = "weight")
            val weight: Double // 893.084
        ) {
            data class Admin(
                @Json(name = "iso_3166_1")
                val iso31661: String, // IN
                @Json(name = "iso_3166_1_alpha3")
                val iso31661Alpha3: String // IND
            )
        }
    }

    data class Waypoint(
        @Json(name = "distance")
        val distance: Double, // 525.457
        @Json(name = "location")
        val location: List<Double>,
        @Json(name = "name")
        val name: String // MDR
    )
}