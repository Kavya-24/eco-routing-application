package com.example.ecoroute.utils

import android.content.res.Resources
import android.location.Location
import android.util.Log
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.EdgeInsets
import com.mapbox.turf.TurfMeasurement
import java.lang.Double.max
import java.lang.Double.min

object MapUtils {

    val MAXIMUM_CHARGE = 60
    val MAXIMUM_THRESHOLD = 10000
    val MAXIMUM_NODES = 3
    val MAXIMUM_FOUND = 20
    fun convertChargeToSOC(initialSOC: Double): Int {
        return (initialSOC * 0.6).toInt()
    }

    fun getCenter(o1: Point, o2: Point): Point {

        return Point.fromLngLat(
            (o1.longitude() + o2.longitude()) / 2,
            (o1.latitude() + o2.latitude()) / 2
        )
    }

    fun pointInAdmissibleCircle(c1: Point, p: Point, r: Double): Boolean {
        val distanceToCenter = TurfMeasurement.distance(c1, p)
        Log.e("ASTAR", "Len distance between c, point = $distanceToCenter")
        return distanceToCenter<= r
    }

    private val pixelDensity = Resources.getSystem().displayMetrics.density

    val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    fun returnMapboxAcceptedLngLat(point: Point): String {
        return point.longitude().toString() + "," + point.latitude().toString()
    }


    fun generateBBOXFromFeatures(features: List<Feature>): String {

        var minLat: Double = 180.0
        var minLong: Double = 180.0
        var maxLat: Double = -180.0
        var maxLong: Double = -180.0

        val polygon = features.get(0).geometry() as Polygon
        var lng = 0.0
        var lat = 0.0

        for (e in 0 until polygon.coordinates().get(0).size) {
            lng = polygon.coordinates().get(0)[e].longitude()
            lat = polygon.coordinates().get(0)[e].latitude()
            minLat = min(minLat, lat)
            maxLat = max(maxLat, lat)
            minLong = min(minLong, lng)
            maxLong = max(maxLong, lng)
        }

        return "$minLong,$minLat,$maxLong,$maxLat"
    }

    data class bbox_data(
        val bbox_west: Double,
        val bbox_east: Double,
        val bbox_south: Double,
        val bbox_north: Double
    )


    fun getDirectionBearings(
        originLocation: Location,
        isochroneCenters: MutableList<Point>
    ): MutableList<Bearing?> {


        val bearings = mutableListOf<Bearing?>()

        for (e in 0..isochroneCenters.size - 2) {
            bearings.add(
                Bearing.builder()
                    .angle(originLocation.bearing.toDouble())
                    .degrees(45.0)
                    .build()
            )
        }

        bearings.add(null)

        return bearings
    }

    fun getDirectionLayers(isochroneCenters: MutableList<Point>): MutableList<Int> {
        val zLevels = mutableListOf<Int>()
        isochroneCenters.forEach { it ->
            zLevels.add(it.altitude().toInt())
        }
        Log.e("ASTAR", "ZLevels to bearing: ${zLevels.toString()}")
        return zLevels
    }


}