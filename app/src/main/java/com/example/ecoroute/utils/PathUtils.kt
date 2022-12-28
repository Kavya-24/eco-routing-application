package com.example.ecoroute.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.ecoroute.utils.MapUtils.ALPHA
import com.example.ecoroute.utils.MapUtils.buildStepPointsFromGeometry
import com.example.ecoroute.utils.MapUtils.mapToManeuverType
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.tilequery.MapboxTilequery
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.vision.ar.core.models.ManeuverType
import com.mapbox.vision.ar.core.models.RoutePoint
import com.mapbox.vision.mobile.core.models.position.GeoCoordinate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.sqrt

@SuppressLint("LogNotTimber")
class PathUtils {


    var maneuverStatistics = 0.0
    var elevationStatistics = 0.0
    val maneuverRoutePoints = mutableListOf<RoutePoint>()
    val points = mutableListOf<Point>()
    val ELEVATION_STATUS = MutableLiveData<Boolean>()

    fun modelIntermediateRoute(
        routes: List<DirectionsRoute>,
        context: Context,
        ACCESS_TOKEN: String
    ) {

        routes.first().getRoutePoints()

        getElevationStatistics(context, ACCESS_TOKEN, 0)


    }


    fun DirectionsRoute.getRoutePoints() {


        legs()?.forEach { leg ->
            leg.steps()?.forEach { step ->

                val maneuverPoint = RoutePoint(
                    GeoCoordinate(
                        latitude = step.maneuver().location().latitude(),
                        longitude = step.maneuver().location().longitude(),
                    ),
                    maneuverType = step.maneuver().type().mapToManeuverType(),
                )
                maneuverRoutePoints.add(maneuverPoint)

                points.add(
                    Point.fromLngLat(
                        step.maneuver().location().longitude(),
                        step.maneuver().location().latitude()
                    )
                )

                step.geometry()?.buildStepPointsFromGeometry()?.map { geometryStep ->
                    RoutePoint(
                        GeoCoordinate(
                            latitude = geometryStep.latitude(),
                            longitude = geometryStep.longitude()
                        )
                    )
                }?.let { stepPoints ->
                    maneuverRoutePoints.addAll(stepPoints)
                }



                //ALPHA = -10. If Maneuver is bad, cost has to increase and hence negative effect, else positive effect to decrease cost.
                //If the distance(in meters) on which this effect is observed, th greater the effect
                maneuverStatistics += ALPHA * maneuverPoint.maneuverType.mapToCostFactors() * (sqrt(step.distance()))

            }
        }


    }


    private fun ManeuverType.mapToCostFactors(): Double = when (this) {
        ManeuverType.Turn -> -0.1
        ManeuverType.Depart -> -0.2
        ManeuverType.Arrive -> -0.2
        ManeuverType.Merge -> -0.1
        ManeuverType.OnRamp -> -0.2
        ManeuverType.OffRamp -> -0.2
        ManeuverType.Fork -> -0.1
        ManeuverType.Roundabout -> -0.2
        ManeuverType.RoundaboutExit -> -0.2
        ManeuverType.EndOfRoad -> -0.3
        ManeuverType.NewName -> 0.2
        ManeuverType.Continue -> 0.2
        ManeuverType.Rotary -> -0.1
        ManeuverType.RoundaboutTurn -> -0.1
        ManeuverType.Notification -> -0.1
        else -> 0.1
    }

    fun getElevationStatistics(
        context: Context,
        ACCESS_TOKEN: String,
        idx: Int

    ) {

        if (idx >= points.size) {
            ELEVATION_STATUS.value = true
            return
        }

        val elevationQuery = MapboxTilequery.builder()
            .accessToken(ACCESS_TOKEN)
            .tilesetIds("mapbox.mapbox-terrain-v2")
            .query(
                points[idx]
            )
            .geometry("polygon")
            .layers("contour")
            .build()


        elevationQuery.enqueueCall(object : Callback<FeatureCollection?> {

            override fun onResponse(
                call: Call<FeatureCollection?>,
                response: Response<FeatureCollection?>
            ) {


                if (response.body() != null && response.body()!!.features() != null) {

                    elevationStatistics += processElevations(response.body()!!.features()!!)
                    getElevationStatistics(context, ACCESS_TOKEN, idx + 1)

                } else {
                    Log.e("ASTAR", "Response = NULL")
                }
            }

            override fun onFailure(call: Call<FeatureCollection?>, throwable: Throwable) {

                Log.e(
                    "ASTAR",
                    "${throwable.message} and error = ${throwable.cause} Failed for idx = $idx and type = ${points[idx]}"
                )
                ELEVATION_STATUS.value = true
            }
        })

    }

    private fun processElevations(features: List<Feature>): Double {

        if (features.isEmpty()) {
            return 0.0
        }

        var f = 0.0
        for (x in features) {
            f += x.getStringProperty("ele").toInt()
        }


        return f / features.size

    }

}

