package com.example.ecoroute.models.responses


import com.squareup.moshi.Json

class EcorouteResponse : ArrayList<EcorouteResponse.EcorouteResponseItem>(){
    data class EcorouteResponseItem(
        @Json(name = "lat")
        val lat: String, // 26.9124
        @Json(name = "lon")
        val lon: String // 75.7873
    )
}