package com.example.ecoroute.utils

import com.mapbox.geojson.Point

object URLBuilder {

    fun createStationsInVicinityQuery(pt: Point): String {
        return "https://ecoroute-server-ka.onrender.com/stationsInVicinity?lat=" + (pt.latitude()).toString() + "&lon=" + (pt.longitude()).toString() + "&radius=100000"
    }

    fun createEcoroutePathQuery(
        srcPoint: Point,
        dstPoint: Point,
        soc: Double,
        measure: String
    ): String {
        return "https://ecoroute-server-ka.onrender.com/ecoroutePath?lat1=" + (srcPoint.latitude()).toString() + "&lon1=" + (srcPoint.longitude()).toString()+ "&lat2=" + (dstPoint.latitude()).toString() + "&lon2=" + (dstPoint.longitude()).toString()+ "&soc="+soc
            .toInt() + "&measure=" + measure
    }


}