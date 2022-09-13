package com.example.ecoroute.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ecoroute.R
import com.example.ecoroute.utils.LocationPermissionHelper
//import com.example.ecoroute.utils.LocationPermissionHelper
import com.example.ecoroute.utils.UiUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import java.lang.ref.WeakReference


class HomeFragment : Fragment(), OnMapClickListener  {


    private val TAG = HomeFragment::class.java.simpleName


    private lateinit var root: View
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: SearchRequestTask


    private var startLocationAdapter: ArrayAdapter<String>? = null
    private var endLocationAdapter: ArrayAdapter<String>? = null

    private val searchCallbackSource = object : SearchSelectionCallback {

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {


            val str = mutableListOf<String>()
            for (y in suggestions.indices) {
                str.add(suggestions[y].name)

            }
            startLocationAdapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    str
                )


            et_start_location.setAdapter(startLocationAdapter)


        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {

        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {

        }

        override fun onError(e: Exception) {

        }
    }
    private val searchCallbackDestination = object : SearchSelectionCallback {

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {

            val str = mutableListOf<String>()
            for (y in suggestions.indices) {
                str.add(suggestions[y].name)

            }
            endLocationAdapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    str
                )


            et_end_location.setAdapter(endLocationAdapter)

        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {

        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {

        }

        override fun onError(e: Exception) {

        }
    }
    private val reverseSearchCallback = object : SearchCallback {
        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
        }

        override fun onError(e: Exception) {
        }
    }


    private var originLocation: Point? = null

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())

    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        originLocation = it
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }


    private lateinit var mapView: MapView

    private lateinit var fab: FloatingActionButton


    //Search Query View variables
    private lateinit var _actvCar: AutoCompleteTextView
    private lateinit var _chargeSlider: Slider
    private lateinit var _startSearch: MaterialButton

    private lateinit var et_start_location: AutoCompleteTextView
    private lateinit var et_end_location: AutoCompleteTextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_home, container, false)

        mapView = root.findViewById(R.id.mapView)

        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }


        fab = root.findViewById(R.id.fab_route)


        fab.setOnClickListener {
            if (originLocation != null) {
                createSearchQueryFramework()
            } else {
                locationPermissionHelper.checkPermissions { onMapReady() }
            }

        }



        return root
    }

    private fun createSearchQueryFramework() {

        //Open the Toolbar of home and add functionalities
        createDialog()
    }

    private fun createDialog() {
        val d = AlertDialog.Builder(requireContext())
        val v = layoutInflater.inflate(R.layout.search_query_view, null)
        d.setView(v)

        _actvCar = v.findViewById(R.id.actv_selectCar)
        _chargeSlider = v.findViewById(R.id.sliderEvCharge)
        _startSearch = v.findViewById(R.id.mtbStartSearch)


        et_start_location = v.findViewById(R.id.query_ui_start_location)
        et_end_location = v.findViewById(R.id.query_ui_end_location)

        UiUtils().setupGenericAdapters(listOf("Honda", "Tesla"), _actvCar, requireContext())


        setupSearchBar()



        _startSearch.setOnClickListener {

        }

        d.create()
        d.show()
    }


    private fun setupSearchBar() {

        initializeAdapters()
        addQueryChangeListeners()



        if (!requireContext().isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_LOCATION
            )
        }

    }


    val startItemClick =
        AdapterView.OnItemClickListener { adapterView, view, i, l ->


            et_start_location.setText(
                adapterView.getItemAtPosition(i).toString(),
                TextView.BufferType.EDITABLE
            )


        }

    val endItemClick =
        AdapterView.OnItemClickListener { adapterView, view, i, l ->
            et_end_location.setText(
                adapterView.getItemAtPosition(i).toString(),
                TextView.BufferType.EDITABLE
            )

        }

    private fun initializeAdapters() {

        startLocationAdapter = UiUtils().setupLocationAdapters(et_start_location, requireContext())
        endLocationAdapter = UiUtils().setupLocationAdapters(et_end_location, requireContext())

        et_start_location.onItemClickListener = startItemClick
        et_end_location.onItemClickListener = endItemClick
    }


    private fun addQueryChangeListeners() {
        et_start_location.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {


                if (s.toString().isNotEmpty()) {

                    searchRequestTask = searchEngine.search(
                        s.toString(),
                        SearchOptions(
                            proximity = originLocation, fuzzyMatch = true,
                            limit = 5
                        ),
                        searchCallbackSource
                    )

                }


            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(e: Editable) { /* not implemented */
            }
        })


        et_end_location.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {


                if (s.toString().isNotEmpty()) {

                    searchRequestTask = searchEngine.search(
                        s.toString(),
                        SearchOptions(
                            fuzzyMatch = true,
                            limit = 5
                        ),
                        searchCallbackDestination
                    )
                }


            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(e: Editable) { /* not implemented */
            }
        })

    }


    private companion object {

        private const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()

        }

        mapView.getMapboxMap().addOnMapClickListener(this)



        searchEngine = MapboxSearchSdk.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings(resources.getString(R.string.mapbox_access_token))
        )

    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        val x = mapView.location

        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.mapbox_user_icon_shadow,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
//

    }

    private fun onCameraTrackingDismissed() {

        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
        searchRequestTask.cancel()
        mapView.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onMapClick(point: Point): Boolean {

        return true
    }


}
