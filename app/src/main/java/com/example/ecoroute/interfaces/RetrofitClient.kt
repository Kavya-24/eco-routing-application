package com.example.ecoroute.interfaces

import androidx.annotation.Keep
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
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


}