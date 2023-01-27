package com.example.ecoroute.interfaces

import androidx.annotation.Keep
import com.example.ecoroute.models.responses.NearbyStationsResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Keep
object RetrofitClient {
    val url = "https://api.mapbox.com/"
    val TAG = RetrofitClient::class.java.simpleName


    fun navigationAPICalls(): navigationInterface {
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(navigationInterface::class.java)
    }


    val tomtomUrl = "https://api.tomtom.com/"
    fun navigateTOMTOMApis(): tomtomInterface {
        return Retrofit.Builder()
            .baseUrl(tomtomUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(tomtomInterface::class.java)
    }

    val ecorouteServerURL = "https://ecoroute-server-ka.onrender.com/"
    fun ecorouteInterface(): EcorouteInterface {

        class NearbyStationsClientAdapter {
            @ToJson
            fun arrayListToJson(list: ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>): List<NearbyStationsResponse.NearbyStationsResponseItem> = list

            @FromJson
            fun arrayListFromJson(list: List<NearbyStationsResponse.NearbyStationsResponseItem>): ArrayList<NearbyStationsResponse.NearbyStationsResponseItem> = ArrayList(list)
        }

        val moshi = Moshi.Builder()
            .add(NearbyStationsClientAdapter())
            .build()

        return Retrofit.Builder()
            .baseUrl(ecorouteServerURL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(EcorouteInterface::class.java)
    }





}