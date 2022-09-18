package com.example.ecoroute.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecoroute.R
import com.example.ecoroute.interfaces.RetrofitClient
import com.example.ecoroute.models.responses.GeoCodedQueryResponse
import com.example.ecoroute.models.responses.IsochronePolygonResponse
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.UiUtils
import com.mapbox.api.isochrone.IsochroneCriteria
import com.mapbox.api.isochrone.MapboxIsochrone
import com.mapbox.api.tilequery.MapboxTilequery
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NavigationViewModel : ViewModel() {

    val TAG = NavigationViewModel::class.java.simpleName

    private val ctx = ApplicationUtils.getContext()
    private val mapboxInterfaceService = RetrofitClient.navigationAPICalls()


    val successfulIsochrone: MutableLiveData<Boolean> = MutableLiveData()
    var messageIsochrone: MutableLiveData<String> = MutableLiveData()
    var isochronePolygonResponse: MutableLiveData<IsochronePolygonResponse> =
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
                call: Call<FeatureCollection?>?,
                response: Response<FeatureCollection?>
            ) {


                if (response.body() != null && response.body()!!.features() != null) {
                    Log.e(
                        "Navigation", "Isochrone response message = ${response.message()}"
                    )
                    isochroneMapboxFeature.value = response.body()!!.features()!!
                    successfulMapboxIsochrone.value = true
                    messageMapboxIsochrone.value = response.message()
                } else {
                    successfulMapboxIsochrone.value = false
                    messageMapboxIsochrone.value = response.message()
                }
            }

            override fun onFailure(call: Call<FeatureCollection?>?, throwable: Throwable) {
                successfulMapboxIsochrone.value = false
                messageMapboxIsochrone.value = UiUtils().returnStateMessageForThrowable(throwable)
                UiUtils().logThrowables(TAG, throwable)

            }
        })

        return isochroneMapboxFeature
    }


    val successfulMapboxTileQuery: MutableLiveData<Boolean> = MutableLiveData()
    var messageMapboxTileQuery: MutableLiveData<String> = MutableLiveData()
    var tileQueryMapboxFeature: MutableLiveData<List<Feature>> = MutableLiveData()

    fun mapboxTileQuery(
        originPoint: Point,
        ACCESS_TOKEN: String,

    ): MutableLiveData<List<Feature>> {
        tileQueryMapboxFeature =
            createTileQueryAroundPoint(originPoint,ACCESS_TOKEN)
        return tileQueryMapboxFeature
    }

    private fun createTileQueryAroundPoint(
        p: Point, ACCESS_TOKEN: String
    ): MutableLiveData<List<Feature>> {


        val elevationQuery = MapboxTilequery.builder()
            .accessToken(ACCESS_TOKEN)
            .tilesetIds("mapbox.mapbox-terrain-v2")
            .query(p)
            .geometry("polygon")
            .layers("contour")
            .build()
        elevationQuery.enqueueCall(object : Callback<FeatureCollection?> {

            override fun onResponse(
                call: Call<FeatureCollection?>?,
                response: Response<FeatureCollection?>
            ) {


                if (response.body() != null && response.body()!!.features() != null) {
                    Log.e(
                        "ASTARN", "Elevation response message = ${response.message()} "
                    )
                    tileQueryMapboxFeature.value = response.body()!!.features()!!
                    successfulMapboxTileQuery.value = true
                    messageMapboxTileQuery.value = response.message()
                } else {
                    successfulMapboxTileQuery.value = false
                    messageMapboxTileQuery.value = response.message()
                }
            }

            override fun onFailure(call: Call<FeatureCollection?>?, throwable: Throwable) {
                successfulMapboxTileQuery.value = false
                messageMapboxTileQuery.value = UiUtils().returnStateMessageForThrowable(throwable)
                UiUtils().logThrowables(TAG, throwable)

            }
        })

        return tileQueryMapboxFeature
    }


}