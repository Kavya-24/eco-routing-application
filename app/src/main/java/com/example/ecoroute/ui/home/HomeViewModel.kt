package com.example.ecoroute.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecoroute.interfaces.RetrofitClient
import com.example.ecoroute.models.responses.NearbyStationsResponse
import com.example.ecoroute.utils.UiUtils
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Response


@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")
@Keep
class HomeViewModel : ViewModel() {


    val TAG = HomeViewModel::class.java.simpleName
    private val ecorouteService = RetrofitClient.ecorouteInterface()

    //For getting the nearby stations in the vicinity of 30 km
    var successful: MutableLiveData<Boolean> = MutableLiveData()
    var message: MutableLiveData<String> = MutableLiveData()
    private var mResponse: MutableLiveData<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>> = MutableLiveData()

    fun getStationsInVicinity(url: String, pb : ProgressBar, csl : ConstraintLayout): MutableLiveData<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>> {

        mResponse = vicinityStationsFunction(url,pb,csl)
        return mResponse
    }


    private fun vicinityStationsFunction(url: String, pb: ProgressBar, csl: ConstraintLayout): MutableLiveData<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>> {


        Log.e(TAG, "Stations in Vicinity URL: $url")

        ecorouteService.getStationsInVicinity(url)
            .enqueue(object : retrofit2.Callback<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>> {
                override fun onFailure(call: Call<ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>>, t: Throwable) {
                    successful.value = false
                    message.value = UiUtils().returnStateMessageForThrowable(t)
                    UiUtils().logThrowables(TAG, t)
                    pb.visibility = View.INVISIBLE
                    Snackbar.make(csl,  message.value.toString(), Snackbar.LENGTH_SHORT).show()

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