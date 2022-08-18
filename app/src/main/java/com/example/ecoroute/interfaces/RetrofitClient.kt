package com.example.ecoroute.interfaces

import android.content.Context
import androidx.annotation.Keep
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Keep
object RetrofitClient {
    val url = "https://api.mapbox.com/"
    val TAG = RetrofitClient::class.java.simpleName


    fun makeMapboxAPICalls(): directionInterface {

        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(directionInterface::class.java)

    }

}