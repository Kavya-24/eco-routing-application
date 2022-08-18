package com.example.ecoroute.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecoroute.R
import com.example.ecoroute.interfaces.RetrofitClient
import com.example.ecoroute.models.responses.DirectionRouteResponse
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.UiUtils
import retrofit2.Call
import retrofit2.Response

class NavigationViewModel : ViewModel() {

    val TAG = NavigationViewModel::class.java.simpleName

    private val ctx = ApplicationUtils.getContext()
    private val mapboxInterfaceService = RetrofitClient.makeMapboxAPICalls()

    //For getting the details of the supply stock
    val successfulDirections: MutableLiveData<Boolean> = MutableLiveData()
    var messageDirections: MutableLiveData<String> = MutableLiveData()
    private var directionRouteResponse: MutableLiveData<DirectionRouteResponse> = MutableLiveData()

    fun getDirections(
        points: String
    ): MutableLiveData<DirectionRouteResponse> {
        directionRouteResponse = getDirectionRoutes(points)
        return directionRouteResponse
    }

    private fun getDirectionRoutes(
        points: String
    ): MutableLiveData<DirectionRouteResponse> {

        val directions_url = ctx.resources.getString(
            R.string.mapbox_directions_route,
            "driving",
            points,
            "geojson",
            ctx.resources.getString(R.string.mapbox_access_token)
        )

        Log.e(TAG, "Directions url = $directions_url")

        mapboxInterfaceService.getDirectionsRoutes(mapbox_url = directions_url)
            .enqueue(object : retrofit2.Callback<DirectionRouteResponse> {
                override fun onResponse(
                    call: Call<DirectionRouteResponse>,
                    response: Response<DirectionRouteResponse>
                ) {

                    if (response.body() != null) {
                        Log.e(TAG, "Response not null and body =  ${response.body().toString()}")

                        if (response.body()!!.code == "Ok") {

                            successfulDirections.value = true
                            messageDirections.value = "OK"
                            directionRouteResponse.value = response.body()!!

                        } else {

                            successfulDirections.value = false
                            messageDirections.value = response.body()!!.code

                        }
                    } else {
                        successfulDirections.value = false
                        messageDirections.value = ctx.getString(R.string.direction_null)
                    }

                }

                override fun onFailure(call: Call<DirectionRouteResponse>, t: Throwable) {
                    successfulDirections.value = false
                    messageDirections.value = UiUtils().returnStateMessageForThrowable(t)

                    UiUtils().logThrowables(TAG, t)


                }
            })


        return directionRouteResponse
    }

}