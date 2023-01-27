package com.example.ecoroute.ui.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecoroute.interfaces.RetrofitClient
import com.example.ecoroute.models.responses.NearbyStationsResponse
import com.example.ecoroute.utils.UiUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Response


@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")
class HomeViewModel : ViewModel() {


    val TAG = HomeViewModel::class.java.simpleName
    private val ecorouteService = RetrofitClient.ecorouteInterface()

    //For getting the nearby stations in the vicinity of 30 km
    var successful: MutableLiveData<Boolean> = MutableLiveData()
    var message: MutableLiveData<String> = MutableLiveData()
    private var mResponse: MutableLiveData<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>> = MutableLiveData()

    fun getStationsInVicinity(url: String): MutableLiveData<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>> {

        mResponse = vicinityStationsFunction(url)
        return mResponse
    }


    private fun vicinityStationsFunction(url: String): MutableLiveData<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>> {


        Log.e(TAG, "Stations in Vicinity URL: $url")

        ecorouteService.getStationsInVicinity(url)
            .enqueue(object : retrofit2.Callback<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>> {
                override fun onFailure(call: Call<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>>, t: Throwable) {
                    successful.value = false
                    message.value = UiUtils().returnStateMessageForThrowable(t)
                    UiUtils().logThrowables(TAG, t)

                }

                override fun onResponse(
                    call: Call<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>>,
                    response: Response<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>>
                ) {


                    if (response.isSuccessful && response.body() != null) {

                        successful.value = true
                        message.value = "Loaded stations"
                        mResponse.value = response.body()


                    } else {

                        successful.value = false
                        message.value = "Unable to load stations"
                    }


                }
            })


        return mResponse


    }
}