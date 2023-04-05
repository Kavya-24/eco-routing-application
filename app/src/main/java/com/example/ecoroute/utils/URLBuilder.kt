package com.example.ecoroute.utils

import android.content.Context
import com.example.ecoroute.models.EVCar
import com.example.ecoroute.ui.user.EVCarStorage
import com.mapbox.geojson.Point
import com.squareup.moshi.Moshi

object URLBuilder {

    fun createStationsInVicinityQuery(pt: Point): String {
        return "https://ecoroute-server-ka.onrender.com/stationsInVicinity?lat=" + (pt.latitude()).toString() + "&lon=" + (pt.longitude()).toString() + "&radius=100000"
    }

    fun createEcoroutePathQuery(
        srcPoint: Point,
        dstPoint: Point,
        soc: Double,
        measure: String,
        context: Context
    ): String {

        val current_car_position = EVCarStorage.getCurrentCarPosition(context)
        val cars = EVCarStorage.getCars(context)
        val moshi = Moshi.Builder().build()
        val evCarAdapter = moshi.adapter(EVCar::class.java)
        val ev_car = evCarAdapter.toJson(cars[current_car_position])
        return "https://ecoroute-server-ka.onrender.com/ecoroutePath?lat1=" + (srcPoint.latitude()).toString() + "&lon1=" + (srcPoint.longitude()).toString() + "&lat2=" + (dstPoint.latitude()).toString() + "&lon2=" + (dstPoint.longitude()).toString() + "&soc=" + soc
            .toInt() + "&measure=" + measure + "&evcar=" + ev_car
    }


}