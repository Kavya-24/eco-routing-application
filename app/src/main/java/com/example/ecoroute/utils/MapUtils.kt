package com.example.ecoroute.utils

import android.content.res.Resources
import android.util.Log
import androidx.annotation.Keep
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
@Keep
object MapUtils {


    private val pixelDensity = Resources.getSystem().displayMetrics.density

    val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity, 40.0 * pixelDensity, 120.0 * pixelDensity, 40.0 * pixelDensity
        )
    }
    val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity, 380.0 * pixelDensity, 110.0 * pixelDensity, 20.0 * pixelDensity
        )
    }
    val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity, 40.0 * pixelDensity, 150.0 * pixelDensity, 40.0 * pixelDensity
        )
    }
    val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity, 380.0 * pixelDensity, 110.0 * pixelDensity, 40.0 * pixelDensity
        )
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