package com.example.ecoroute.ui.route

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecoroute.interfaces.RetrofitClient
import com.example.ecoroute.models.responses.EcorouteResponse
import com.example.ecoroute.utils.UiUtils
import retrofit2.Call
import retrofit2.Response

@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")
class RouteViewModel : ViewModel() {

    val TAG = RouteViewModel::class.java.simpleName
    private val ecorouteService = RetrofitClient.ecorouteInterface()

    var successful: MutableLiveData<Boolean> = MutableLiveData()
    var message: MutableLiveData<String> = MutableLiveData()
    private var mResponse: MutableLiveData<ArrayList<EcorouteResponse.EcorouteResponseItem>> =
        MutableLiveData()

    fun getOptimalPath(url: String): MutableLiveData<ArrayList<EcorouteResponse.EcorouteResponseItem>> {

        mResponse = optimalPath(url)
        return mResponse
    }


    private fun optimalPath(url: String): MutableLiveData<ArrayList<EcorouteResponse.EcorouteResponseItem>> {


        Log.e(TAG, "PathURL:  $url")

        ecorouteService.getEcoroutePath(url)
            .enqueue(object :
                retrofit2.Callback<ArrayList<EcorouteResponse.EcorouteResponseItem>> {
                override fun onFailure(
                    call: Call<ArrayList<EcorouteResponse.EcorouteResponseItem>>,
                    t: Throwable
                ) {
                    successful.value = false
                    message.value = UiUtils().returnStateMessageForThrowable(t)
                    UiUtils().logThrowables(TAG, t)

                }

                override fun onResponse(
                    call: Call<ArrayList<EcorouteResponse.EcorouteResponseItem>>,
                    response: Response<ArrayList<EcorouteResponse.EcorouteResponseItem>>
                ) {

                    Log.e(TAG, "Response: $response")
                    if (response.isSuccessful && response.body() != null) {

                        successful.value = true
                        message.value = "Loaded path"
                        mResponse.value = response.body()


                    } else {

                        successful.value = false
                        message.value = "Unable to find path"
                    }


                }
            })


        return mResponse


    }
}