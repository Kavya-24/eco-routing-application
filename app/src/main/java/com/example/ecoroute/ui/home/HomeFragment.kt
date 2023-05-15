package com.example.ecoroute.ui.home


import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.ecoroute.R
import com.example.ecoroute.models.responses.NearbyStationsResponse
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.URLBuilder
import com.example.ecoroute.utils.UiUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import kotlinx.android.synthetic.main.fragment_home.*


@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")

class HomeFragment : Fragment() {


    private val TAG = HomeFragment::class.java.simpleName
    private lateinit var root: View
    private val uiUtilInstance = UiUtils()
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var pb: ProgressBar
    private val ctx = ApplicationUtils.getContext()
    private var station_response : ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>? = null
    private lateinit var ms : SwitchMaterial

    private lateinit var homeMapView: MapView
    private lateinit var homemapboxMap: MapboxMap
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    private lateinit var annotationApi: AnnotationPlugin
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private val navigationLocationProvider = NavigationLocationProvider()


    private var currentLocation: Location? = null
    private lateinit var locationManager: LocationManager


    private var FINDING_STATION = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(com.example.ecoroute.R.layout.fragment_home, container, false)
        pb = root.findViewById(R.id.pbHome)
        homeMapView = root.findViewById(R.id.mapViewHome)
        homemapboxMap = homeMapView.getMapboxMap()
//        ms = root.findViewById(R.id.material_switch)

        annotationApi = homeMapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()

        root.findViewById<ImageView>(R.id.info_home).setOnClickListener {
            Snackbar.make(
                csl_home,
                "Long press anywhere to get all the stations in the nearby vicinity",
                Snackbar.LENGTH_SHORT
            ).show()
        }
//        ms.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                markStations(station_response, true)
//            } else {
//                markStations(station_response, false)
//            }
//        }
        onMapReady()



        return root
    }

    private fun onMapReady() {

        homemapboxMap.loadStyle(style(styleUri = Style.TRAFFIC_DAY) {


        }, object : Style.OnStyleLoaded {
            override fun onStyleLoaded(style: Style) {

                homeMapView.gestures.addOnMapLongClickListener {
                    if (!FINDING_STATION) {

                        loadStations(it)

                    }

                    true
                }

            }
        }

        )


        homeMapView.location.apply {
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    requireContext(), com.example.ecoroute.R.drawable.mapbox_user_puck_icon
                )
            )
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }


        viewportDataSource = MapboxNavigationViewportDataSource(homemapboxMap)
        navigationCamera = NavigationCamera(
            homemapboxMap, homeMapView.camera, viewportDataSource
        )

        homeMapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )


    }

    private fun loadStations(it: Point) {

        val url = URLBuilder.createStationsInVicinityQuery(it)
        val csl = root.findViewById<ConstraintLayout>(R.id.csl_home)
        FINDING_STATION = true
        pb.visibility = View.VISIBLE
        clearObservers()
        viewModel.getStationsInVicinity(url,pb,csl).observe(viewLifecycleOwner, Observer { mResponse ->

            if (viewModel.successful.value != null) {

                station_response = mResponse
                pb.visibility = View.INVISIBLE
                FINDING_STATION = false
                uiUtilInstance.showToast(ctx, viewModel.message.value.toString())
                Log.e(TAG, mResponse.toString())
                if (viewModel.successful.value == true) {
                    markStations(mResponse, true)
                }

            } else {
                pb.visibility = View.VISIBLE
            }
        })
    }

    private fun markStations(mResponse: ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>?, show : Boolean) {

        if(mResponse == null){
            return
        }

        Log.e(TAG, "Marking ${mResponse.size} stations")
        pointAnnotationManager.deleteAll()
        for (stations in mResponse) {
            attachMarkers(stations.position.lat, stations.position.lon, stations.poi.name, show)
        }

    }

    private fun attachMarkers(_latitude: Double, _longitude: Double, name: String, show: Boolean) {


        uiUtilInstance.bitmapFromDrawableRes(
            requireContext(), R.drawable.ic_baseline_location_on_24
        )?.let {

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions().withPoint(
                Point.fromLngLat(
                    _longitude, _latitude
                )
            ).withIconImage(it).withTextAnchor(TextAnchor.TOP)
            if(show){

                pointAnnotationOptions.textField = name
                pointAnnotationOptions.textSize= 8.0

            }
            pointAnnotationManager.create(pointAnnotationOptions)


        }


    }


    override fun onStart() {
        super.onStart()
        homeMapView?.onStart()
        FINDING_STATION = false

    }

    override fun onStop() {
        super.onStop()
        homeMapView?.onStop()
        FINDING_STATION = false
    }

    override fun onLowMemory() {
        super.onLowMemory()
        homeMapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeMapView?.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearObservers()
        homeMapView?.onDestroy()
    }

    private fun clearObservers() {
        viewModel.successful.removeObservers(this)
        viewModel.successful.value = null
        viewModel.message.removeObservers(this)
        viewModel.message.value = null
    }

}