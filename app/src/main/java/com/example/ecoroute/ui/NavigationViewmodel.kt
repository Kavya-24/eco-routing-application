package com.example.ecoroute.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecoroute.interfaces.RetrofitClient
import com.example.ecoroute.models.responses.GeoCodedQueryResponse
import com.example.ecoroute.utils.MapUtils
import com.example.ecoroute.utils.UiUtils
import com.mapbox.api.isochrone.IsochroneCriteria
import com.mapbox.api.isochrone.MapboxIsochrone
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")
class NavigationViewModel : ViewModel() {

    val TAG = "ASTAR VIEWMODEL"

    private val mapboxInterfaceService = RetrofitClient.navigationAPICalls()


    val successfulGeocode: MutableLiveData<Boolean> = MutableLiveData()
    var messageGeocode: MutableLiveData<String> = MutableLiveData()
    var geocodeQueryResponse: MutableLiveData<GeoCodedQueryResponse> =
        MutableLiveData()

    fun getGeocodeQuery(url: String): MutableLiveData<GeoCodedQueryResponse> {
        geocodeQueryResponse = getGeocodeQueryLocal(url)
        return geocodeQueryResponse
    }

    private fun getGeocodeQueryLocal(url: String): MutableLiveData<GeoCodedQueryResponse> {

        Log.e(TAG, "Geocode URL $url")

        mapboxInterfaceService.getGeocodedQuery(mapbox_url = url)
            .enqueue(object : retrofit2.Callback<GeoCodedQueryResponse> {
                override fun onResponse(
                    call: Call<GeoCodedQueryResponse>,
                    response: Response<GeoCodedQueryResponse>
                ) {

                    if (response.body() != null) {
                        Log.e(TAG, "Geocode query response message = ${response.message()}")
                        geocodeQueryResponse.value = response.body()
                        successfulGeocode.value = true
                        messageGeocode.value = response.message()
                    } else {
                        successfulGeocode.value = false
                        messageGeocode.value = response.message()
                    }

                }

                override fun onFailure(call: Call<GeoCodedQueryResponse>, t: Throwable) {
                    successfulGeocode.value = false
                    messageGeocode.value = UiUtils().returnStateMessageForThrowable(t)

                    UiUtils().logThrowables(TAG, t)

                }
            })

        return geocodeQueryResponse
    }


    val successfulMapboxIsochrone: MutableLiveData<Boolean> = MutableLiveData()
    var messageMapboxIsochrone: MutableLiveData<String> = MutableLiveData()
    var isochroneMapboxFeature: MutableLiveData<List<Feature>> = MutableLiveData()

    fun mapboxIsochrone(
        originPoint: Point,
        usePolygon: Boolean,
        ACCESS_TOKEN: String,
        SOC: Int
    ): MutableLiveData<List<Feature>> {
        isochroneMapboxFeature =
            createIsochroneAroundPoint(originPoint, usePolygon, ACCESS_TOKEN, SOC)
        return isochroneMapboxFeature
    }

    private fun createIsochroneAroundPoint(
        originPoint: Point, usePolygon: Boolean, ACCESS_TOKEN: String, SOC: Int
    ): MutableLiveData<List<Feature>> {


        val mapboxIsochroneRequest = MapboxIsochrone.builder()
            .accessToken(ACCESS_TOKEN)
            .profile(IsochroneCriteria.PROFILE_DRIVING)
            .addContoursMinutes(SOC)
            .polygons(usePolygon)
            .addContoursColors("4286f4")
            .generalize(2f)
            .denoise(.4f)
            .coordinates(
                Point.fromLngLat(
                    originPoint.longitude(),
                    originPoint.latitude()
                )
            )
            .build()

        mapboxIsochroneRequest.enqueueCall(object : Callback<FeatureCollection?> {

            override fun onResponse(
                call: Call<FeatureCollection?>,
                response: Response<FeatureCollection?>
            ) {


                if (response.body() != null && response.body()!!.features() != null) {


                    isochroneMapboxFeature.value = response.body()!!.features()!!
                    successfulMapboxIsochrone.value = true
                    messageMapboxIsochrone.value = response.message()

                    Log.e(
                        TAG, "Isochrone non-null body response message = ${response.message()}"
                    )

                    Log.e(
                        TAG,
                        "BBOX Generated = ${
                            MapUtils.generateBBOXFromFeatures(
                                response.body()!!.features()!!
                            )
                        }"
                    )

                } else {
                    successfulMapboxIsochrone.value = false
                    messageMapboxIsochrone.value = response.message()
                    Log.e(
                        TAG, "Isochrone null body response = ${response.message()}"
                    )
                }
            }

            override fun onFailure(call: Call<FeatureCollection?>, throwable: Throwable) {
                successfulMapboxIsochrone.value = false
                messageMapboxIsochrone.value = UiUtils().returnStateMessageForThrowable(throwable)
                UiUtils().logThrowables(TAG, throwable)

            }
        })

        return isochroneMapboxFeature
    }


}