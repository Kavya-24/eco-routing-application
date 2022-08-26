package com.example.ecoroute.ui

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecoroute.R
import com.example.ecoroute.interfaces.RetrofitClient
import com.example.ecoroute.models.responses.IsochronePolygonResponse
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.UiUtils
import retrofit2.Call
import retrofit2.Response

class NavigationViewModel : ViewModel() {

    val TAG = NavigationViewModel::class.java.simpleName

    private val ctx = ApplicationUtils.getContext()
    private val mapboxInterfaceService = RetrofitClient.navigationAPICalls()


    val successfulIsochrone: MutableLiveData<Boolean> = MutableLiveData()
    var messageIsochrone: MutableLiveData<String> = MutableLiveData()
    private var isochronePolygonResponse: MutableLiveData<IsochronePolygonResponse> =
        MutableLiveData()

    fun getIsochrone(url: String): MutableLiveData<IsochronePolygonResponse> {
        isochronePolygonResponse = getIsochronePolygon(url)
        return isochronePolygonResponse
    }

    private fun getIsochronePolygon(url: String): MutableLiveData<IsochronePolygonResponse> {

        Log.e(TAG, "Isochrone URL $url")

        mapboxInterfaceService.getIsochronePolygon(mapbox_url = url)
            .enqueue(object : retrofit2.Callback<IsochronePolygonResponse> {
                override fun onResponse(
                    call: Call<IsochronePolygonResponse>,
                    response: Response<IsochronePolygonResponse>
                ) {

                    if (response.body() != null) {
                        isochronePolygonResponse.value = response.body()
                        successfulIsochrone.value = true
                        messageIsochrone.value = response.message()
                    } else {
                        successfulIsochrone.value = false
                        messageIsochrone.value = ctx.getString(R.string.isochrone_null)
                    }

                }

                override fun onFailure(call: Call<IsochronePolygonResponse>, t: Throwable) {
                    successfulIsochrone.value = false
                    messageIsochrone.value = UiUtils().returnStateMessageForThrowable(t)

                    UiUtils().logThrowables(TAG, t)

                }
            })

        return isochronePolygonResponse
    }

}