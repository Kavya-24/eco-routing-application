package com.example.ecoroute.ui.route
import com.example.ecoroute.models.responses.EcorouteResponse
import android.annotation.SuppressLint
import androidx.cardview.widget.CardView
import com.mapbox.api.directions.v5.models.DirectionsRoute
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.ecoroute.R
import com.example.ecoroute.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.bindgen.Expected
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
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.options.NavigationOptions
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
import com.mapbox.navigation.ui.maps.camera.view.MapboxRecenterButton
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
import java.lang.ref.WeakReference
import java.util.*
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.route.*


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
    private lateinit var routemapboxNavigation: MapboxNavigation
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    private lateinit var annotationApi: AnnotationPlugin
    private lateinit var pointAnnotationManager: PointAnnotationManager


    private val navigationLocationProvider = NavigationLocationProvider()
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
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
    private var currentLocation: Location? = null
    private lateinit var locationManager: LocationManager
    private val locationCallback =
        object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                Log.e(TAG, "Succesfully initiated location callback")
            }

            override fun onFailure(exception: Exception) {
                uiUtilInstance.logExceptions(TAG, exception)
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

    //Mapbox Map Variables
    private var usePolygon = true
    private val BUTTON_ANIMATION_DURATION = 1500L



    //API points and inits
    private lateinit var maneuverApi: MapboxManeuverApi
    private lateinit var tripProgressApi: MapboxTripProgressApi
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()
    private lateinit var routeArrowView: MapboxRouteArrowView

    /**Observers and callbacks */
    //Voice commands
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                root.findViewById<MapboxSoundButton>(R.id.soundButtonRoute).muteAndExtend(
                    BUTTON_ANIMATION_DURATION
                )
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                root.findViewById<MapboxSoundButton>(R.id.soundButtonRoute).unmuteAndExtend(
                    BUTTON_ANIMATION_DURATION
                )
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }
    private lateinit var speechApi: MapboxSpeechApi
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }
    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            speechApi.clean(value)
        }

    //Route
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->

        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = routemapboxMap.getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    requireContext(),
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                root.findViewById<MapboxManeuverView>(R.id.maneuverViewRoute).visibility =
                    View.VISIBLE
                root.findViewById<MapboxManeuverView>(R.id.maneuverViewRoute).renderManeuvers(
                    maneuvers
                )
            }
        )

        root.findViewById<MapboxTripProgressView>(R.id.tripProgressViewRoute).render(
            tripProgressApi.getTripProgress(routeProgress)
        )
    }
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.toDirectionsRoutes().isNotEmpty()) {

            val routeLines = routeUpdateResult.navigationRoutes.toDirectionsRoutes()
                .map { RouteLine(it, null) }

            routeLineApi.setNavigationRouteLines(
                routeLines
                    .toNavigationRouteLines()
            ) { value ->
                routemapboxMap.getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

            viewportDataSource.onRouteChanged(
                routeUpdateResult.navigationRoutes.toDirectionsRoutes().first()
                    .toNavigationRoute()
            )
            viewportDataSource.evaluate()
        } else {
            val style = routemapboxMap.getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }


    //Replayer
    private val mapboxReplayer = MapboxReplayer()
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)


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
            } else {

                closeDialog()
                if (root.findViewById<RadioButton>(R.id.radio_petrol).isChecked) {

                    ecoroute(
                        URLBuilder.createEcoroutePathQuery(
                            sourceSearchPoint!!,
                            destinationSearchPoint!!,
                            initialSOC!!,
                            "petrol"
                        )
                    )

                } else if (root.findViewById<RadioButton>(R.id.radio_energy).isChecked) {
                    ecoroute(
                        URLBuilder.createEcoroutePathQuery(
                            sourceSearchPoint!!,
                            destinationSearchPoint!!,
                            initialSOC!!,
                            "energy"
                        )
                    )
                } else if (root.findViewById<RadioButton>(R.id.radio_time).isChecked) {

                    ecoroute(
                        URLBuilder.createEcoroutePathQuery(
                            sourceSearchPoint!!,
                            destinationSearchPoint!!,
                            initialSOC!!,
                            "time"
                        )
                    )

                } else {
                    ecoroute(
                        URLBuilder.createEcoroutePathQuery(
                            sourceSearchPoint!!,
                            destinationSearchPoint!!,
                            initialSOC!!,
                            "energy"
                        )
                    )

                }


            }

        }
    }

    private fun ecoroute(url: String) {

        FINDING_PATH = true
        clearObservers()
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
    }

    private fun findRoute(mResponse : ArrayList<EcorouteResponse.EcorouteResponseItem>){

        val path_list = mutableListOf<Point>()
        for(item in mResponse){
            path_list.add(Point.fromLngLat(item.lon.toDouble(), item.lat.toDouble()))
        }

        routemapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(requireContext())
                .coordinatesList(path_list.toList())
                .layersList(MapUtils.getDirectionLayers(path_list))
                .build(),
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {

                    setRouteAndStartNavigation(routes, routerOrigin)

                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                }

                override fun onCanceled(
                    routeOptions: RouteOptions,
                    routerOrigin: RouterOrigin
                ) {
                }
            }
        )

    }

    private fun setRouteAndStartNavigation(
        routes: List<DirectionsRoute>,
        routerOrigin: RouterOrigin
    ) {

        NAVIGATION_IN_PROGRESS = true
        routemapboxNavigation.setNavigationRoutes(routes.toNavigationRoutes(routerOrigin))

        val _cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(sourceSearchPoint!!.longitude(), sourceSearchPoint!!.latitude()))
            .pitch(45.0)
            .zoom(15.5)
            .bearing(-17.6)
            .build()

        routemapboxMap.setCamera(_cameraOptions)

        //startSimulation(routes.first())

        /** show UI elements*/
        root.findViewById<MapboxSoundButton>(R.id.soundButtonRoute).visibility = View.VISIBLE
        root.findViewById<MapboxRouteOverviewButton>(R.id.routeOverviewRoute).visibility =
            View.VISIBLE
        root.findViewById<CardView>(R.id.tripProgressCardRoute).visibility = View.VISIBLE

        /** move the camera to overview when new route is available */
        navigationCamera.requestNavigationCameraToOverview()


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


//        routeMapView.location.apply {
//            this.locationPuck = LocationPuck2D(
//                bearingImage = ContextCompat.getDrawable(
//                    requireContext(),
//                    R.drawable.mapbox_user_puck_icon
//                )
//            )
//            setLocationProvider(navigationLocationProvider)
//            enabled = true
//        }

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
            routemapboxMap,
            routeMapView.camera,
            viewportDataSource
        )

        routeMapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )

        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->


            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> root.findViewById<MapboxRecenterButton>(R.id.recenterRoute).visibility =
                    View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE ->

                    root.findViewById<MapboxRecenterButton>(R.id.recenterRoute).visibility =
                        View.VISIBLE
            }
        }

        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = MapUtils.landscapeOverviewPadding
            viewportDataSource.followingPadding = MapUtils.landscapeFollowingPadding
        } else {
            viewportDataSource.overviewPadding = MapUtils.overviewPadding
            viewportDataSource.followingPadding = MapUtils.followingPadding
        }

        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = routemapboxNavigation.navigationOptions.distanceFormatterOptions

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(requireContext())
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(requireContext())
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(requireContext(), TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            requireContext(),
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            requireContext(),
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )

        /** initialize route line, the withRouteLineBelowLayerId is specified to place
        the route line below road labels layer on the map
        the value of this option will depend on the style that you are using
        and under which layer the route line should be placed on the map layers stack */

        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(requireContext())
            .withRouteLineBelowLayerId("road-label")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(requireContext()).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)



        setUpSearchResultView()
        addSearchResultViewListeners()
        addQueryListeners()

        root.findViewById<ImageView>(R.id.stopRoute).setOnClickListener {
            clearRouteAndStopNavigation()
        }
        root.findViewById<MapboxRecenterButton>(R.id.recenterRoute).setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            root.findViewById<MapboxRouteOverviewButton>(R.id.routeOverviewRoute).showTextAndExtend(
                BUTTON_ANIMATION_DURATION
            )
        }
        root.findViewById<MapboxRouteOverviewButton>(R.id.routeOverviewRoute).setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            root.findViewById<MapboxRecenterButton>(R.id.recenterRoute).showTextAndExtend(
                BUTTON_ANIMATION_DURATION
            )
        }
        root.findViewById<MapboxSoundButton>(R.id.soundButtonRoute).setOnClickListener {
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }

        // set initial sounds button state
        root.findViewById<MapboxSoundButton>(R.id.soundButtonRoute).unmute()


        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }



        routemapboxNavigation.startTripSession()

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

        try {
            routemapboxNavigation.registerRoutesObserver(routesObserver)
            routemapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
            routemapboxNavigation.registerLocationObserver(locationObserver)
            routemapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
            routemapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Restart the application after giving location permissions",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onStop() {
        super.onStop()
        routeMapView?.onStop()

        clearRouteAndStopNavigation()

        // unregister event listeners to prevent leaks or unnecessary resource consumption
        routemapboxNavigation.unregisterRoutesObserver(routesObserver)
        routemapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        routemapboxNavigation.unregisterLocationObserver(locationObserver)
        routemapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
        routemapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)

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

    private fun clearRouteAndStopNavigation() {

        clearObservers()
        hideUIElement()

        /**Clear set navigation paths to empty*/
        routemapboxNavigation.setNavigationRoutes(listOf<DirectionsRoute>().toNavigationRoutes())

        /** stop simulation*/
        mapboxReplayer.stop()
        NAVIGATION_IN_PROGRESS = false


    }

    private fun hideUIElement() {
        root.findViewById<MapboxSoundButton>(R.id.soundButtonRoute).visibility = View.INVISIBLE
        root.findViewById<MapboxManeuverView>(R.id.maneuverViewRoute).visibility = View.INVISIBLE
        root.findViewById<MapboxRouteOverviewButton>(R.id.routeOverviewRoute).visibility =
            View.INVISIBLE
        root.findViewById<CardView>(R.id.tripProgressCardRoute).visibility = View.INVISIBLE

    }


}
