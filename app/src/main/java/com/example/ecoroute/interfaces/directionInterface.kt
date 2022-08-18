package com.example.ecoroute.interfaces

import androidx.annotation.Keep
import com.example.ecoroute.models.responses.DirectionRouteResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

@Keep
interface directionInterface {

    @Headers("Content-Type:application/json")
    @GET()
    fun getDirectionsRoutes(
        @Url mapbox_url: String
    ): Call<DirectionRouteResponse>

}