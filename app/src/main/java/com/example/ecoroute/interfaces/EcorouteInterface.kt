package com.example.ecoroute.interfaces

import androidx.annotation.Keep
import com.example.ecoroute.models.responses.EcorouteAPIResponse
import com.example.ecoroute.models.responses.EcorouteResponse
import com.example.ecoroute.models.responses.NearbyStationsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

@Keep
interface EcorouteInterface {


    @GET()
    fun getStationsInVicinity(
        @Url stations_in_vicinity: String
    ): Call<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>>


    @GET()
    fun getEcoroutePath(
        @Url ecoroute_path : String
    ) : Call<ArrayList<EcorouteAPIResponse.EcorouteAPIResponseItem>>

}