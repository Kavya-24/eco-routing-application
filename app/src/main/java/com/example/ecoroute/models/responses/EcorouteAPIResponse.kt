package com.example.ecoroute.models.responses
import kotlinx.serialization.Serializable
import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep

class EcorouteAPIResponse : ArrayList<EcorouteAPIResponse.EcorouteAPIResponseItem>(){

    @Serializable
    data class EcorouteAPIResponseItem(
        @field:Json(name = "latitude")
        val latitude: String,
        @field:Json(name = "longitude")
        val longitude: String,
        @field:Json(name = "name")
        val name: String,
        @field:Json(name = "port")
        val port: String,
        @field:Json(name = "soc")
        val soc: String,
        @field:Json(name = "timestamp")
        val timestamp: String
    )
}