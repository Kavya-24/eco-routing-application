package com.example.ecoroute.ui.route

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ecoroute.R
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.LocationPermissionHelper
import com.example.ecoroute.utils.UiUtils
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import java.lang.ref.WeakReference

class RouteFragment : Fragment() {

    private val TAG = RouteFragment::class.java.simpleName
    private lateinit var root: View
    private val uiUtilInstance = UiUtils()
    private val viewModel: RouteViewModel by viewModels()
    private lateinit var pb: ProgressBar
    private val ctx = ApplicationUtils.getContext()


    private lateinit var routeMapView: MapView
    private lateinit var routemapboxMap: MapboxMap
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    private lateinit var annotationApi: AnnotationPlugin
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private val navigationLocationProvider = NavigationLocationProvider()
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private var currentLocation: Location? = null
    private lateinit var locationManager: LocationManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(com.example.ecoroute.R.layout.fragment_route, container, false)
        pb = root.findViewById(R.id.pbRoute)
        routeMapView = root.findViewById(R.id.mapViewRoute)
        routemapboxMap = routeMapView.getMapboxMap()

        annotationApi = routeMapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()



        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }



        return root
    }


    private fun onMapReady() {

        routemapboxMap.loadStyle(
            style(styleUri = Style.TRAFFIC_DAY) {


            }, object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {

                    routeMapView.gestures.addOnMapLongClickListener {


                        true
                    }

                }
            }

        )


        routeMapView.location.apply {
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.mapbox_user_puck_icon
                )
            )
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }


        viewportDataSource = MapboxNavigationViewportDataSource(routemapboxMap)
        navigationCamera = NavigationCamera(
            routemapboxMap,
            routeMapView.camera,
            viewportDataSource
        )

        routeMapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )


    }


    override fun onStart() {
        super.onStart()
        routeMapView?.onStart()

    }

    override fun onStop() {
        super.onStop()
        routeMapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        routeMapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        routeMapView?.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()

        routeMapView?.onDestroy()
    }

}
