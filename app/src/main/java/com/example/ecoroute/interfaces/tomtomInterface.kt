package com.example.ecoroute.interfaces

import androidx.annotation.Keep
import com.example.ecoroute.models.responses.EVStationResponse
import com.example.ecoroute.models.responses.GeoCodedQueryResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url
@Keep
interface tomtomInterface {

    @GET()
    fun getEVStations(
        @Url tom_tom_url: String
    ): Call<EVStationResponse>

}