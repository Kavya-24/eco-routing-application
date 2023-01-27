package com.example.ecoroute.interfaces

import com.example.ecoroute.models.responses.NearbyStationsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface EcorouteInterface {


    @GET()
    fun getStationsInVicinity(
        @Url stations_in_vicinity: String
    ): Call<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>>


}