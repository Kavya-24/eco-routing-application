package com.example.ecoroute.ui.route

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.ecoroute.R
import com.example.ecoroute.models.responses.EcorouteResponse
import com.example.ecoroute.ui.VisualPathActivity
import com.example.ecoroute.ui.user.EVCarStorage
import com.example.ecoroute.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
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
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.*
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
//import com.mapbox.navigation.ui.maps.camera.view.MapboxRecenterButton
import com.mapbox.navigation.ui.maps.camera.view.MapboxRouteOverviewButton
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.toNavigationRouteLines
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.*
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import com.mapbox.navigation.ui.voice.view.MapboxSoundButton
import com.mapbox.search.OfflineSearchEngineSettings
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_route.*
import java.util.*


@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")
@ExperimentalPreviewMapboxNavigationAPI
class RouteFragment : Fragment() {

    private val TAG = RouteFragment::class.java.simpleName
    private lateinit var root: View
    private val uiUtilInstance = UiUtils()
    private val viewModel: RouteViewModel by viewModels()
    private lateinit var pb: ProgressBar
    private val ctx = ApplicationUtils.getContext()


    private lateinit var routeMapView: MapView
    private lateinit var routemapboxMap: MapboxMap
    private lateinit var routemapboxNavigation: MapboxNavigation
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    private lateinit var annotationApi: AnnotationPlugin
    private lateinit var pointAnnotationManager: PointAnnotationManager


    private val navigationLocationProvider = NavigationLocationProvider()

    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {

            val enhancedLocation = locationMatcherResult.enhancedLocation
            Log.e(TAG, "LogStatement-Newoc: $enhancedLocation")
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
        }
    }


    //UI elements
    private lateinit var routeQueryCSL: ConstraintLayout
    private lateinit var mtbRouteQuery: MaterialButton

    //Path Varibles
    private var sourceSearchPoint: Point? = null
    private var destinationSearchPoint: Point? = null
    private var initialSOC: Double? = null


    private var FINDING_PATH = false
    private var NAVIGATION_IN_PROGRESS = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(com.example.ecoroute.R.layout.fragment_route, container, false)
        pb = root.findViewById(R.id.pbRoute)

        routeQueryCSL = root.findViewById(R.id.csl_route_dialog)
        mtbRouteQuery = root.findViewById(R.id.mtbOpenDialog)

        routeMapView = root.findViewById(R.id.mapViewRoute)
        routemapboxMap = routeMapView.getMapboxMap()

        annotationApi = routeMapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()

        root.findViewById<ImageView>(R.id.info_route).setOnClickListener {
            Snackbar.make(
                csl_route,
                "Drop pins for source and destination or use query box",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        root.findViewById<ImageView>(R.id.iv_go_query).setOnClickListener {
            generatePath()
        }

        onMapReady()
        mtbRouteQuery.setOnClickListener {
            openDialog()
        }





        return root
    }

    private fun generatePath() {


        if (t_src == null || t_dst == null) {
            uiUtilInstance.showToast(ctx, "Enter source and destination")
        } else if (FINDING_PATH) {
            uiUtilInstance.showToast(ctx, "Request already in progress")
        } else if (EVCarStorage.getCars(requireContext()).isEmpty()) {
            uiUtilInstance.showToast(ctx, "No car found and selected. Add a car. ")
        } else {

            //Default config: Normal charging, Energy objetive, 100 SOC
            return ecoroute(
                URLBuilder.createEcoroutePathQuery(
                    t_src!!,
                    t_dst!!,
                    100.0,
                    "energy",
                    requireContext()
                )
            )


        }

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
        clearDialog()
        initialSOC = 100.0


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
            } else if (FINDING_PATH) {
                uiUtilInstance.showToast(ctx, "Request already in progress")
            } else if (EVCarStorage.getCars(requireContext()).isEmpty()) {
                uiUtilInstance.showToast(ctx, "No car found and selected. Add a car. ")
            } else {

                closeDialog()
                if (root.findViewById<RadioButton>(R.id.radio_petrol).isChecked) {

                    ecoroute(
                        URLBuilder.createEcoroutePathQuery(
                            sourceSearchPoint!!,
                            destinationSearchPoint!!,
                            initialSOC!!,
                            "petrol",
                            requireContext()
                        )
                    )

                } else if (root.findViewById<RadioButton>(R.id.radio_energy).isChecked) {
                    ecoroute(
                        URLBuilder.createEcoroutePathQuery(
                            sourceSearchPoint!!,
                            destinationSearchPoint!!,
                            initialSOC!!,
                            "energy",
                            requireContext()
                        )
                    )
                } else if (root.findViewById<RadioButton>(R.id.radio_time).isChecked) {

                    ecoroute(
                        URLBuilder.createEcoroutePathQuery(
                            sourceSearchPoint!!,
                            destinationSearchPoint!!,
                            initialSOC!!,
                            "time",
                            requireContext()
                        )
                    )

                } else {
                    ecoroute(
                        URLBuilder.createEcoroutePathQuery(
                            sourceSearchPoint!!,
                            destinationSearchPoint!!,
                            initialSOC!!,
                            "energy",
                            requireContext()
                        )
                    )

                }


            }

        }
    }

    private fun ecoroute(url: String) {

        FINDING_PATH = true
        clearObservers()
        try {
            viewModel.getOptimalPath(url).observe(viewLifecycleOwner, Observer { mResponse ->

                if (viewModel.successful.value != null) {
                    pb.visibility = View.INVISIBLE
                    FINDING_PATH = false
                    uiUtilInstance.showToast(ctx, viewModel.message.value.toString())
                    findRoute(mResponse)

                } else {
                    pb.visibility = View.VISIBLE
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Unable to find path ${e.cause} and ${e.message}")
            uiUtilInstance.showToast(ctx, "Unable to find path")
            FINDING_PATH = false

        }
    }

    private fun findRoute(mResponse: ArrayList<EcorouteResponse.EcorouteResponseItem>) {


        val path_list = mutableListOf<Point>()
        var path_string = ""
        for (item in mResponse) {
            path_list.add(Point.fromLngLat(item.lon.toDouble(), item.lat.toDouble()))
            path_string += item.lon.toDouble().toString() + ","
            path_string += item.lat.toDouble().toString() + ";"
        }

        val intent = Intent(requireContext(), VisualPathActivity::class.java)
        intent.putExtra("coordinate", path_string)
        startActivity(intent)

//
//        routemapboxNavigation.requestRoutes(RouteOptions.builder().applyDefaultNavigationOptions()
//            .applyLanguageAndVoiceUnitOptions(requireContext()).coordinatesList(path_list.toList())
//            .layersList(MapUtils.getDirectionLayers(path_list)).build(),
//            object : RouterCallback {
//                override fun onRoutesReady(
//                    routes: List<DirectionsRoute>, routerOrigin: RouterOrigin
//                ) {
//
//                    setRouteAndStartNavigation(routes, routerOrigin)
//
//                }
//
//                override fun onFailure(
//                    reasons: List<RouterFailure>, routeOptions: RouteOptions
//                ) {
//                }
//
//                override fun onCanceled(
//                    routeOptions: RouteOptions, routerOrigin: RouterOrigin
//                ) {
//                }
//            })

    }


    private fun clearDialog() {
        root.findViewById<TextInputEditText>(R.id.et_src).text?.clear()
        root.findViewById<TextInputEditText>(R.id.et_dst).text?.clear()
    }

    private fun closeDialog() {

        clearDialog()
        routeQueryCSL.visibility = View.INVISIBLE
        mtbRouteQuery.visibility = View.VISIBLE

    }


    @SuppressLint("MissingPermission")


    /**
     * Touch Query Module
     */
    private var t_src_started = false
    private var t_dst_started = false
    private var t_src: Point? = null
    private var t_dst: Point? = null

    private fun onMapReady() {

        routemapboxMap.loadStyle(style(styleUri = Style.TRAFFIC_DAY) {


        }, object : Style.OnStyleLoaded {
            override fun onStyleLoaded(style: Style) {

                routeMapView.gestures.addOnMapLongClickListener { it ->

                    if (!t_src_started) {
                        t_src_started = true
                        t_src = it
                        attachMarkers()
                    } else {
                        t_dst_started = true
                        t_dst = it
                        attachMarkers()
                    }
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

        routemapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(requireContext())
                    .accessToken(getString(R.string.mapbox_access_token))
                    //.locationEngine(replayLocationEngine)
                    .build()
            )
        }



        viewportDataSource = MapboxNavigationViewportDataSource(routemapboxMap)
        navigationCamera = NavigationCamera(
            routemapboxMap, routeMapView.camera, viewportDataSource
        )

        routeMapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )


        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = MapUtils.landscapeOverviewPadding
            viewportDataSource.followingPadding = MapUtils.landscapeFollowingPadding
        } else {
            viewportDataSource.overviewPadding = MapUtils.overviewPadding
            viewportDataSource.followingPadding = MapUtils.followingPadding
        }




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
                    s: CharSequence, start: Int, count: Int, after: Int
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
                    s: CharSequence, start: Int, count: Int, after: Int
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
                    searchResult: SearchResult, responseInfo: ResponseInfo
                ) {
                    root.findViewById<TextInputEditText>(R.id.et_src)
                        .setText(searchResult.name.toString(), TextView.BufferType.EDITABLE)
                    sourceSearchPoint = searchResult.coordinate

                    root.findViewById<SearchResultsView>(R.id.sv_src).visibility = View.GONE
                }

                override fun onPopulateQueryClicked(
                    suggestion: SearchSuggestion, responseInfo: ResponseInfo
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
                    results: List<SearchResult>, responseInfo: ResponseInfo
                ) {
                }

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo
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
                    searchResult: SearchResult, responseInfo: ResponseInfo
                ) {
                    root.findViewById<TextInputEditText>(R.id.et_dst)
                        .setText(searchResult.name.toString(), TextView.BufferType.EDITABLE)
                    destinationSearchPoint = searchResult.coordinate

                    root.findViewById<SearchResultsView>(R.id.sv_dst).visibility = View.GONE
                }

                override fun onPopulateQueryClicked(
                    suggestion: SearchSuggestion, responseInfo: ResponseInfo
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
                    results: List<SearchResult>, responseInfo: ResponseInfo
                ) {
                }

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo
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
        t_src_started = false
        t_dst_started = false
        t_src = null
        t_dst = null


    }

    override fun onStop() {
        super.onStop()
        routeMapView?.onStop()
        t_src_started = false
        t_dst_started = false
        t_src = null
        t_dst = null


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

    private fun clearObservers() {
        viewModel.successful.removeObservers(this)
        viewModel.successful.value = null
        viewModel.message.removeObservers(this)
        viewModel.message.value = null
    }


    private fun attachMarkers() {
        pointAnnotationManager.deleteAll()
        if (t_src != null) {
            mark(t_src!!.latitude(), t_src!!.longitude(), R.drawable.source_location)
        }
        if (t_dst != null) {
            mark(t_dst!!.latitude(), t_dst!!.longitude(), R.drawable.destination_locatiom)
        }
    }

    private fun mark(_latitude: Double, _longitude: Double, dr: Int) {

        uiUtilInstance.bitmapFromDrawableRes(
            requireContext(), dr
        )?.let {

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions().withPoint(
                Point.fromLngLat(
                    _longitude, _latitude
                )
            ).withIconImage(it).withTextAnchor(TextAnchor.TOP)

            pointAnnotationManager.create(pointAnnotationOptions)


        }


    }


}
