package com.example.ecoroute.utils

import com.mapbox.geojson.Point

object URLBuilder {

    fun createStationsInVicinityQuery(pt: Point): String {
        return "https://ecoroute-server-ka.onrender.com/stationsInVicinity?lat=" + (pt.latitude()).toString() + "&lon=" + (pt.longitude()).toString() + "&radius=100000"
    }
}