package com.example.ecoroute.ui.route

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ecoroute.R
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.LocationPermissionHelper
import com.example.ecoroute.utils.UiUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.mapbox.geojson.Point
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
import com.mapbox.search.OfflineSearchEngineSettings
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import java.lang.ref.WeakReference


@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")
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


    //UI elements
    private lateinit var routeQueryCSL: ConstraintLayout
    private lateinit var mtbRouteQuery: MaterialButton

    //Path Varibles
    private var sourceSearchPoint: Point? = null
    private var destinationSearchPoint: Point? = null
    private var initialSOC: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(com.example.ecoroute.R.layout.fragment_route, container, false)
        pb = root.findViewById(R.id.pbRoute)

        routeQueryCSL = root.findViewById(R.id.csl_route_dialog)
        mtbRouteQuery = root.findViewById(R.id.mtbOpenDialog)

        routeMapView = root.findViewById(R.id.mapViewRoute)
        routemapboxMap = routeMapView.getMapboxMap()

        annotationApi = routeMapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()



        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }

        mtbRouteQuery.setOnClickListener {
            openDialog()
        }





        return root
    }

    private fun openDialog() {
        routeQueryCSL.visibility = View.VISIBLE
        mtbRouteQuery.visibility = View.GONE

        setupDialog()
    }

    private fun setupDialog() {

        //Search Points
        sourceSearchPoint = null
        destinationSearchPoint = null
        initialSOC = null


        //SOC metre
        root.findViewById<Slider>(R.id.ecoroute_slider)
            .addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {

                }

                override fun onStopTrackingTouch(slider: Slider) {
                    initialSOC = slider.value.toDouble()
                    root.findViewById<TextView>(R.id.ecoroute_tvCharging).text =
                        resources.getString(R.string.charging) + " " + slider.value.toInt()
                            .toString() + "%"
                }

            })


        //Action buttons
        root.findViewById<MaterialButton>(R.id.mtb_route_cancel).setOnClickListener {
            closeDialog()
        }

        root.findViewById<MaterialButton>(R.id.mtb_navigate).setOnClickListener {

            if (sourceSearchPoint == null || destinationSearchPoint == null) {
                uiUtilInstance.showToast(ctx, "Enter source and destination")
                closeDialog()
            } else {

                closeDialog()
                if (root.findViewById<RadioButton>(R.id.radio_petrol).isChecked) {

                    petrolPath()

                } else if (root.findViewById<RadioButton>(R.id.radio_energy).isChecked) {
                    energyPath()
                } else if (root.findViewById<RadioButton>(R.id.radio_time).isChecked) {

                    timePath()

                } else {

                    petrolPath()
                }


            }

        }
    }

    private fun energyPath() {

    }

    private fun timePath() {

    }

    private fun petrolPath() {

    }


    private fun clearDialog() {

    }

    private fun closeDialog() {

        clearDialog()
        routeQueryCSL.visibility = View.INVISIBLE
        mtbRouteQuery.visibility = View.VISIBLE
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

        setUpSearchResultView()
        addSearchResultViewListeners()
        addQueryListeners()

    }

    private fun addQueryListeners() {


        root.findViewById<TextInputEditText>(R.id.et_src)
            .addTextChangedListener(object : TextWatcher {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
                    if (!s.toString().isEmpty()) {
                        root.findViewById<SearchResultsView>(R.id.sv_src).search(s.toString())
                    }

                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    root.findViewById<SearchResultsView>(R.id.sv_src).visibility = View.VISIBLE
                }

                override fun afterTextChanged(e: Editable) {


                }
            })

        root.findViewById<TextInputEditText>(R.id.et_dst)
            .addTextChangedListener(object : TextWatcher {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
                    if (!s.toString().isEmpty()) {
                        root.findViewById<SearchResultsView>(R.id.sv_dst).search(s.toString())
                    }


                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    root.findViewById<SearchResultsView>(R.id.sv_dst).visibility = View.VISIBLE
                }

                override fun afterTextChanged(e: Editable) {


                }
            })
    }

    private fun addSearchResultViewListeners() {


        root.findViewById<SearchResultsView>(R.id.sv_src)
            .addSearchListener(object : SearchResultsView.SearchListener {


                override fun onHistoryItemClicked(historyRecord: HistoryRecord) {

                    root.findViewById<TextInputEditText>(R.id.et_src)
                        .setText(historyRecord.name.toString(), TextView.BufferType.EDITABLE)
                    sourceSearchPoint = historyRecord.coordinate
                    root.findViewById<SearchResultsView>(R.id.sv_src).visibility = View.GONE
                }

                override fun onSearchResult(
                    searchResult: SearchResult,
                    responseInfo: ResponseInfo
                ) {
                    root.findViewById<TextInputEditText>(R.id.et_src)
                        .setText(searchResult.name.toString(), TextView.BufferType.EDITABLE)
                    sourceSearchPoint = searchResult.coordinate

                    root.findViewById<SearchResultsView>(R.id.sv_src).visibility = View.GONE
                }

                override fun onPopulateQueryClicked(
                    suggestion: SearchSuggestion,
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

                override fun onFeedbackClicked(responseInfo: ResponseInfo) {
                }

                override fun onOfflineSearchResults(
                    results: List<SearchResult>,
                    responseInfo: ResponseInfo
                ) {
                }

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo
                ) {
                }
            })

        root.findViewById<SearchResultsView>(R.id.sv_dst)
            .addSearchListener(object : SearchResultsView.SearchListener {


                override fun onHistoryItemClicked(historyRecord: HistoryRecord) {

                    root.findViewById<TextInputEditText>(R.id.et_dst)
                        .setText(historyRecord.name.toString(), TextView.BufferType.EDITABLE)
                    destinationSearchPoint = historyRecord.coordinate
                    root.findViewById<SearchResultsView>(R.id.sv_dst).visibility = View.GONE
                }

                override fun onSearchResult(
                    searchResult: SearchResult,
                    responseInfo: ResponseInfo
                ) {
                    root.findViewById<TextInputEditText>(R.id.et_dst)
                        .setText(searchResult.name.toString(), TextView.BufferType.EDITABLE)
                    destinationSearchPoint = searchResult.coordinate

                    root.findViewById<SearchResultsView>(R.id.sv_dst).visibility = View.GONE
                }

                override fun onPopulateQueryClicked(
                    suggestion: SearchSuggestion,
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

                override fun onFeedbackClicked(responseInfo: ResponseInfo) {
                }

                override fun onOfflineSearchResults(
                    results: List<SearchResult>,
                    responseInfo: ResponseInfo
                ) {
                }

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo
                ) {
                }
            })


    }

    private fun setUpSearchResultView() {
        val accessToken = getString(R.string.mapbox_access_token)

        root.findViewById<SearchResultsView>(R.id.sv_src).initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL),
                searchEngineSettings = SearchEngineSettings(accessToken),
                offlineSearchEngineSettings = OfflineSearchEngineSettings(accessToken)
            )
        )
        root.findViewById<SearchResultsView>(R.id.sv_dst).initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL),
                searchEngineSettings = SearchEngineSettings(accessToken),
                offlineSearchEngineSettings = OfflineSearchEngineSettings(accessToken)
            )
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
