package com.example.ecoroute.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoroute.R
import com.example.ecoroute.adapters.EVCarsListAdapter
import com.example.ecoroute.adapters.OnItemClickListener
import com.example.ecoroute.models.DLSState
import com.example.ecoroute.models.EVCar
import com.example.ecoroute.models.astar.Node
import com.example.ecoroute.models.responses.GeoCodedQueryResponse
import com.example.ecoroute.utils.*
import com.example.ecoroute.utils.MapUtils.CAR_AGE_THETA
import com.example.ecoroute.utils.MapUtils.CAR_PASSENGER_THETA
import com.example.ecoroute.utils.MapUtils.MAXIMUM_CHARGE
import com.example.ecoroute.utils.MapUtils.MAXIMUM_NODES
import com.example.ecoroute.utils.MapUtils.buildStepPointsFromGeometry
import com.example.ecoroute.utils.MapUtils.compareByHeuristic
import com.example.ecoroute.utils.MapUtils.eucledianDistance
import com.example.ecoroute.utils.MapUtils.mapToManeuverType
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.concat
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.eq
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.geometryType
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.literal
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.getLayerAs
import com.mapbox.maps.extension.style.layers.properties.generated.SymbolPlacement
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
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
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
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
import com.mapbox.search.*
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.turf.TurfJoins
import com.mapbox.vision.VisionManager
import com.mapbox.vision.ar.VisionArManager
import com.mapbox.vision.ar.core.models.Route
import com.mapbox.vision.ar.core.models.RoutePoint
import com.mapbox.vision.ar.view.gl.VisionArView
import com.mapbox.vision.mobile.core.interfaces.VisionEventsListener
import com.mapbox.vision.mobile.core.models.position.GeoCoordinate
import com.mapbox.vision.utils.VisionLogger
import kotlinx.android.synthetic.main.activity_ar_navigation.*
import kotlinx.android.synthetic.main.navigation_search.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.properties.Delegates

@SuppressLint("LogNotTimber")
class NavigationActivity : AppCompatActivity(), OnItemClickListener {


    /**
     * AR Variables
     */

    private lateinit var mapboxArView: VisionArView
    private var visionManagerWasInit = false
    private var navigationWasStarted = false

    private val arLocationEngine by lazy {
        LocationEngineProvider.getBestLocationEngine(this)
    }

    private val arLocationEngineRequest by lazy {
        LocationEngineRequest.Builder(0)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setFastestInterval(1000)
            .build()
    }

    private val locationCallback by lazy {
        object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {}

            override fun onFailure(exception: Exception) {}
        }
    }

    private fun setArRenderOptions(visionArView: VisionArView) {
        // enable fence rendering
        visionArView.setFenceVisible(true)
    }


    private var NAVIGATION_IN_PROGRESS = false
    private val priorityQueue = PriorityQueue<Node>(compareByHeuristic)
    private val nodeMap = mutableSetOf<Point>()
    private var countIso = 0
    private var stationMap = mutableMapOf<Point, Int>()
    private var outputLog = ""
    private lateinit var searchEngine: SearchEngine
    private lateinit var options: SearchOptions
    private var difference_in_elevation = 0
    private var currentHeight = 0
    private var nextHeight = 0
    private val ASTAR = "ASTAR"
    private val elevationMap = mutableMapOf<Point, Int>()
    private var close_list_points = mutableListOf<Point>()

    private var radius by Delegates.notNull<Double>()
    private lateinit var center: Point
    private lateinit var mapStyle: Style

    private lateinit var fabNavigate: FloatingActionButton
    lateinit var mtbExtraLims: MaterialButton
    private lateinit var mtbRemainingCharge: MaterialButton

    private lateinit var etSource: TextInputEditText
    private lateinit var sourceSearchResultView: SearchResultsView

    private lateinit var etDestination: TextInputEditText
    private lateinit var destinationSearchResultView: SearchResultsView

    private lateinit var chargingSlider: Slider
    private lateinit var tvCharging: TextView

    private var sourceSearchPoint: Point? = null
    private var destinationSearchPoint: Point? = null
    private var initialSOC: Double? = null

    private lateinit var rvcars: RecyclerView
    private lateinit var mtbaddcar: MaterialButton
    private lateinit var evcartypeadapter: EVCarsListAdapter

    //Vehicle Profile
    private val pref = PreferenceUtil
    private var CAR_PASSENGER = 1
    private var VEHICLE_PROFILE = 0.0

    private lateinit var rvcartypes: RecyclerView
    private lateinit var etCarAge: EditText
    private lateinit var etCarPlugType: EditText
    private lateinit var etCarChargingSpeed: EditText
    private lateinit var CAR_TYPE_IDX: EVCar
    private var CAR_IDX: EVCar? = null


    private var MAP_READY = false
    private val viewmodel: NavigationViewModel by viewModels()
    private var times = 0
    private var parentPoint: Point? = null
    private var currentPoint: Point? = null
    private var currentFeatures: List<Feature>? = null
    private var currentState: DLSState = DLSState(currentPoint, currentFeatures)
    private var isochroneStateList = mutableListOf<DLSState>()
    private var isochroneCenters = mutableListOf<Point>()
    private var contourFeatures = mutableListOf<FeatureCollection>()
    private lateinit var pb: ProgressBar
    private lateinit var csl_view: ConstraintLayout
    private val ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID = "ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID"
    private val ISOCHRONE_FILL_LAYER = "ISOCHRONE_FILL_LAYER"
    private val ISOCHRONE_LINE_LAYER = "ISOCHRONE_LINE_LAYER"
    private val TIME_LABEL_LAYER_ID = "TIME_LABEL_LAYER_ID"
    private val MAP_CLICK_SOURCE_ID = "MAP_CLICK_SOURCE_ID"
    private val MAP_CLICK_MARKER_ICON_ID = "MAP_CLICK_MARKER_ICON_ID"
    private val MAP_CLICK_MARKER_LAYER_ID = "MAP_CLICK_MARKER_LAYER_ID"
    private var usePolygon = true
    private val mapboxReplayer = MapboxReplayer()
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var maneuverApi: MapboxManeuverApi
    private lateinit var tripProgressApi: MapboxTripProgressApi
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()
    private lateinit var routeArrowView: MapboxRouteArrowView
    private val BUTTON_ANIMATION_DURATION = 1500L
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                findViewById<MapboxSoundButton>(R.id.soundButton).muteAndExtend(
                    BUTTON_ANIMATION_DURATION
                )
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                findViewById<MapboxSoundButton>(R.id.soundButton).unmuteAndExtend(
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
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->

        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = mapboxMap.getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    this,
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                findViewById<MapboxManeuverView>(com.example.ecoroute.R.id.maneuverView).visibility =
                    View.VISIBLE
                findViewById<MapboxManeuverView>(com.example.ecoroute.R.id.maneuverView).renderManeuvers(
                    maneuvers
                )
            }
        )

        findViewById<MapboxTripProgressView>(R.id.tripProgressView).render(
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
                mapboxMap.getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

            viewportDataSource.onRouteChanged(
                routeUpdateResult.navigationRoutes.toDirectionsRoutes().first().toNavigationRoute()
            )
            viewportDataSource.evaluate()
        } else {
            val style = mapboxMap.getStyle()
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
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation
    private val navigationLocationProvider = NavigationLocationProvider()
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var sv: ScrollView
    private lateinit var tv: TextView
    val searchCallback = object : SearchSelectionCallback {


        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {

            if (suggestions.isEmpty()) {
                Log.e(ASTAR, "No search suggestions found")
            } else {

                for (r in suggestions) {
                    Log.e(
                        ASTAR,
                        "result name, country: ${r.name} and ${r.address} and ${r.address?.country}"
                    )
                }


            }

        }


        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            val location = result.coordinate
            Log.e(ASTAR, "Search result's location: $location")
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
        }

        override fun onError(e: Exception) {
            Log.e(ASTAR, "Search error", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.ecoroute.R.layout.activity_navigation)


        mapView = findViewById(R.id.mapView)
        mapboxMap = mapView.getMapboxMap()
        mapboxArView = findViewById(R.id.mapbox_ar_view)

        sv = findViewById(R.id.sv_details)
        tv = findViewById(R.id.tv_details)
        pb = findViewById(R.id.pb_navigation)
        csl_view = findViewById(R.id.csl_navigation)
        fabNavigate = findViewById(R.id.fab_navigate)
        mtbExtraLims = findViewById(R.id.mtb_extralims)
        mtbRemainingCharge = findViewById(R.id.mtb_remaining_Charge)

        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }

        fabNavigate.setOnClickListener {
            if (MAP_READY && mapboxMap.getStyle() != null) {

                navigateWithCharge(mapboxMap.getStyle()!!)

            }
        }
    }

    private fun navigateWithCharge(style: Style) {

        //Create a dialog with source, destination and SOC
        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        createDialog(originPoint, style)

    }


    @SuppressLint("StringFormatInvalid", "SetTextI18n")
    private fun createDialog(originPoint: Point, style: Style) {
        val d = AlertDialog.Builder(this)
        val v = layoutInflater.inflate(R.layout.navigation_search, null)
        d.setView(v)

        etSource = v.findViewById(R.id.navigation_query_ui_source_location)
        etDestination = v.findViewById(R.id.navigation_query_ui_destination_location)

        chargingSlider = v.findViewById(R.id.navigation_sliderEvCharge)

        sourceSearchResultView =
            v.findViewById(R.id.navigation_search_results_view_source)
        destinationSearchResultView =
            v.findViewById(R.id.navigation_search_results_view_destination)

        tvCharging = v.findViewById(R.id.navigation_tvCharging)

        tvCharging.text = resources.getString(R.string.charging) + " 20.0%"


        rvcars = v.findViewById(R.id.rv_evcars)
        mtbaddcar = v.findViewById(R.id.mtbAddCar)


        //Search Fields
        setUpSearchResultView()
        addSearchResultViewListeners()
        addQueryListeners()


        loadCars()
        mtbaddcar.setOnClickListener {

            mtbaddcar.visibility = View.GONE
            rvcars.visibility = View.GONE


            //Show the csl
            val csl = v.findViewById<ConstraintLayout>(R.id.csl_add_car)
            csl.visibility = View.VISIBLE

            rvcartypes = v.findViewById(R.id.rv_addcars)
            etCarAge = v.findViewById(R.id.et_carage)
            etCarPlugType = v.findViewById(R.id.et_carplug)
            etCarChargingSpeed = v.findViewById(R.id.et_carchargingspeed)

            loadCarTypes()

            v.findViewById<MaterialButton>(R.id.mtb_cancel_add_car).setOnClickListener {
                csl.visibility = View.GONE
                mtbaddcar.visibility = View.VISIBLE
                rvcars.visibility = View.VISIBLE
            }

            v.findViewById<MaterialButton>(R.id.mtb_check_add_car).setOnClickListener {
                addEVCar()
                csl.visibility = View.GONE
                mtbaddcar.visibility = View.VISIBLE
                rvcars.visibility = View.VISIBLE
                loadCars()
            }


        }

        chargingSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                initialSOC = slider.value.toDouble()
                tvCharging.text =
                    resources.getString(R.string.charging) + " " + slider.value.toInt()
                        .toString() + "%"
            }
        })


        d.setNegativeButton(resources.getString(R.string.go)) { _, _ ->

            if (!v.findViewById<EditText>(R.id.et_passenger).text.isNullOrBlank()) {
                CAR_PASSENGER = v.findViewById<EditText>(R.id.et_passenger).text.toString().toInt()
            }


            //No new car added or selected
            if (CAR_IDX == null) {
                Toast.makeText(this, "No car added", Toast.LENGTH_SHORT).show()
            } else if (destinationSearchPoint == null) {
                Toast.makeText(this, "Destination Location not defined", Toast.LENGTH_SHORT).show()
            }

            if (sourceSearchPoint == null) {
                Toast.makeText(
                    this,
                    "Source Location not defined. Using current location",
                    Toast.LENGTH_SHORT
                ).show()
                sourceSearchPoint = originPoint
            }
            if (destinationSearchPoint != null && !etDestination.text.isNullOrBlank()) {
                astarInitiate(sourceSearchPoint!!, destinationSearchPoint!!, style)
            }
        }

        d.create()
        d.show()
    }


    private fun addEVCar() {

        val evCar = InitOptions.getEVCarsList()[evcartypeadapter.currentCarPosition]
        if (!etCarAge.text.isNullOrBlank()) {
            evCar.carAge = etCarAge.text.toString()
        }

        if (!etCarPlugType.text.isNullOrBlank()) {
            evCar.carType.plugType = etCarPlugType.text.toString()
        }

        if (!etCarChargingSpeed.text.isNullOrBlank()) {
            evCar.carType.chargingSpeed = etCarChargingSpeed.text.toString().toDouble()
        }

        pref.setCarsInPreference(evCar)

        CAR_IDX = evCar
        Log.e(ASTAR, "CAR IDX = $CAR_IDX")

    }

    private fun loadCars() {
        //Get the cars from Preference Util
        val evcars = pref.getCarsFromPreference()
        val evcaradapter = EVCarsListAdapter(this)
        evcaradapter.lst = evcars

        evcaradapter.currentCarPosition = if (pref._lastUsedCarIndex != "") {
            pref._lastUsedCarIndex!!.toInt()
        } else {
            0
        }

        rvcars.layoutManager = LinearLayoutManager(ApplicationUtils.getContext())
        rvcars.adapter = evcaradapter

        if (evcars.size > 0) {
            CAR_IDX = evcars[evcaradapter.currentCarPosition]
        }

    }

    private fun loadCarTypes() {

        val evcars = InitOptions.getEVCarsList()
        evcartypeadapter = EVCarsListAdapter(this)
        evcartypeadapter.lst = evcars
        evcartypeadapter.currentCarPosition = 0
        rvcartypes.layoutManager = LinearLayoutManager(ApplicationUtils.getContext())
        rvcartypes.adapter = evcartypeadapter
    }

    override fun clickThisItem(_listItem: EVCar) {
        Log.e(ASTAR, "Car Clicked")
        CAR_TYPE_IDX = _listItem
        CAR_IDX = _listItem
    }

    private fun addQueryListeners() {

        etSource.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
                if (!s.toString().isEmpty()) {
                    sourceSearchResultView.search(s.toString())
                }


            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                sourceSearchResultView.visibility = View.VISIBLE
            }

            override fun afterTextChanged(e: Editable) {


            }
        })

        etDestination.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
                if (!s.toString().isEmpty()) {
                    destinationSearchResultView.search(s.toString())
                }


            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                destinationSearchResultView.visibility = View.VISIBLE
            }

            override fun afterTextChanged(e: Editable) {


            }
        })
    }

    private fun addSearchResultViewListeners() {


        sourceSearchResultView.addSearchListener(object : SearchResultsView.SearchListener {


            override fun onHistoryItemClicked(historyRecord: HistoryRecord) {

                etSource.setText(historyRecord.name.toString(), TextView.BufferType.EDITABLE)
                sourceSearchPoint = historyRecord.coordinate
                sourceSearchResultView.visibility = View.GONE
            }

            override fun onSearchResult(searchResult: SearchResult, responseInfo: ResponseInfo) {
                etSource.setText(searchResult.name.toString(), TextView.BufferType.EDITABLE)
                sourceSearchPoint = searchResult.coordinate

                sourceSearchResultView.visibility = View.GONE
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

        destinationSearchResultView.addSearchListener(object : SearchResultsView.SearchListener {


            override fun onHistoryItemClicked(historyRecord: HistoryRecord) {

                etDestination.setText(historyRecord.name.toString(), TextView.BufferType.EDITABLE)
                destinationSearchPoint = historyRecord.coordinate
                destinationSearchResultView.visibility = View.GONE
            }

            override fun onSearchResult(searchResult: SearchResult, responseInfo: ResponseInfo) {
                etDestination.setText(searchResult.name.toString(), TextView.BufferType.EDITABLE)
                destinationSearchPoint = searchResult.coordinate

                destinationSearchResultView.visibility = View.GONE
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

        sourceSearchResultView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL),
                searchEngineSettings = SearchEngineSettings(accessToken),
                offlineSearchEngineSettings = OfflineSearchEngineSettings(accessToken)
            )
        )
        destinationSearchResultView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL),
                searchEngineSettings = SearchEngineSettings(accessToken),
                offlineSearchEngineSettings = OfflineSearchEngineSettings(accessToken)
            )
        )
    }

    private fun onMapReady() {


        mapboxMap.loadStyle(
            style(styleUri = Style.TRAFFIC_DAY) {


            }, object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {

                    UiUtils().bitmapFromDrawableRes(
                        this@NavigationActivity,
                        R.drawable.ic_baseline_location_on_24
                    )?.let {
                        style.addImage(
                            MAP_CLICK_MARKER_ICON_ID, it
                        )

                    }


                    style.addSource(geoJsonSource(id = MAP_CLICK_SOURCE_ID))
                    style.addSource(geoJsonSource(ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID))

                    style.addLayer(symbolLayer(MAP_CLICK_MARKER_LAYER_ID, MAP_CLICK_SOURCE_ID) {
                        iconImage(MAP_CLICK_MARKER_ICON_ID)
                        iconIgnorePlacement(true)
                        iconAllowOverlap(true)
                        iconOffset(listOf(0.0, -4.0))
                    })

                    initLineLayer(style)
                    initFillLayer(style)

                    val originLocation = navigationLocationProvider.lastLocation
                    val originPoint = originLocation?.let {
                        Point.fromLngLat(it.longitude, it.latitude)
                    } ?: return


                    mapView.gestures.addOnMapClickListener {
//                        if (!NAVIGATION_IN_PROGRESS) {
//                            astarInitiate(originPoint, it, style)
//                        }
                        //Do nothing when touched up
                        true
                    }



                    mapView.gestures.addOnMapLongClickListener {
                        if (!NAVIGATION_IN_PROGRESS) {
                            findRoute(it)
                        }

                        true
                    }
                    MAP_READY = true


                }
            }

        )


        mapView.location.apply {
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@NavigationActivity,
                    com.example.ecoroute.R.drawable.mapbox_navigation_puck_icon
                )
            )
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }



        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this.applicationContext)
                    .accessToken(getString(R.string.mapbox_access_token))
//                    .locationEngine(replayLocationEngine)
                    .build()
            )
        }



        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
        navigationCamera = NavigationCamera(
            mapboxMap,
            mapView.camera,
            viewportDataSource
        )

        mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )

        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->


            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> findViewById<MapboxRecenterButton>(R.id.recenter).visibility =
                    View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE ->

                    findViewById<MapboxRecenterButton>(R.id.recenter).visibility =
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
        val distanceFormatterOptions = mapboxNavigation.navigationOptions.distanceFormatterOptions

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(this)
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            this,
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            this,
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )

        /** initialize route line, the withRouteLineBelowLayerId is specified to place
        the route line below road labels layer on the map
        the value of this option will depend on the style that you are using
        and under which layer the route line should be placed on the map layers stack */

        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)


        findViewById<ImageView>(R.id.stop).setOnClickListener {
            clearRouteAndStopNavigation()
        }
        findViewById<MapboxRecenterButton>(R.id.recenter).setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            findViewById<MapboxRouteOverviewButton>(R.id.routeOverview).showTextAndExtend(
                BUTTON_ANIMATION_DURATION
            )
        }
        findViewById<MapboxRouteOverviewButton>(R.id.routeOverview).setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            findViewById<MapboxRecenterButton>(R.id.recenter).showTextAndExtend(
                BUTTON_ANIMATION_DURATION
            )
        }
        findViewById<MapboxSoundButton>(R.id.soundButton).setOnClickListener {
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }

        // set initial sounds button state
        findViewById<MapboxSoundButton>(R.id.soundButton).unmute()


        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        initateSearchEngine()

        initiateAR()

        mapboxNavigation.startTripSession()


    }


    private fun initiateAR() {

        try {
            arLocationEngine.requestLocationUpdates(
                arLocationEngineRequest,
                locationCallback,
                mainLooper
            )


        } catch (se: SecurityException) {
            VisionLogger.e(ASTAR, se.toString())
            Log.e(ASTAR, "Security exception with AR. ${se.message} and cause = ${se.cause}")
        }


    }

    private fun initateSearchEngine() {
        searchEngine =
            MapboxSearchSdk.createSearchEngine(SearchEngineSettings(resources.getString(R.string.mapbox_access_token)))

    }

    private fun astarInitiate(originPoint: Point, destinationPoint: Point, style: Style) {
        clearRoute()
        val soc = if (initialSOC != null) {
            Log.e(ASTAR, "Initial SOC: " + initialSOC)
            MapUtils.convertChargeToSOC(initialSOC!!)
        } else {
            MAXIMUM_CHARGE
        }

        radius = 1.5 * eucledianDistance(originPoint, destinationPoint)
        center = MapUtils.getCenter(originPoint, destinationPoint)
        Log.e(ASTAR, "Radius = $radius and center = $center")

        /**Vehicle Profile*/
        VEHICLE_PROFILE =
            CAR_IDX!!.carAge.toInt() * CAR_AGE_THETA + +CAR_IDX!!.carType.carEffectFactor + CAR_PASSENGER * CAR_PASSENGER_THETA

        Log.e(ASTAR, "Car type = $CAR_IDX and passengers = $CAR_PASSENGER")

        val currentNode =
            Node(originPoint, 0.0, eucledianDistance(originPoint, destinationPoint), soc, null)
        val destinationNode =
            Node(destinationPoint, eucledianDistance(originPoint, destinationPoint), 0.0, 0, null)


        priorityQueue.add(currentNode)
        astar_callIsochrone(currentNode, destinationNode, style)


    }

    private fun astar_callIsochrone(currentNode: Node, destinationNode: Node, style: Style) {

        if (currentNode.node_point != null) {


            clearObservers()
            close_list_points.add(currentNode.node_point)
            Log.e(
                ASTAR,
                "\nCreating a geofence around ${currentNode.node_point} with charge ${currentNode.soc}"
            )


            viewmodel.mapboxIsochrone(
                currentNode.node_point,
                usePolygon,
                resources.getString(R.string.mapbox_access_token),
                currentNode.soc
            ).observe(this, Observer { mReponse ->

                if (viewmodel.isochroneMapboxFeature.value != null) {
                    UiUtils().hideProgress(csl_view, pb, this)

                    if (mReponse != null) {
                        //For this unvisited node
                        currentNode.features = mReponse
                        nodeMap.add(currentNode.node_point)

                        makeContour(
                            style,
                            FeatureCollection.fromFeature(currentNode.features!!.get(0))
                        )
                        countIso++

                        if (astar_checkDestination(currentNode, destinationNode)) {
                            //The destination lies here
                            Log.e(
                                ASTAR,
                                "\nDestination found in after station with center ${currentNode.node_point}"
                            )

                            UiUtils().showSnackbar(
                                csl_view,
                                "Destination reached in $countIso step(s)"
                            )

                            astarRouteSelection(destinationNode.node_point!!)


                        } else {
                            //Else the destination does not lie here. Need to find 3 most admissible points
                            astar_callGeocode(currentNode, destinationNode, style)
                        }


                    } else {

                        Log.e(
                            ASTAR,
                            "Failed Isochrone with currentPoint = ${currentNode.node_point.toString()} and message ${viewmodel.messageMapboxIsochrone.value.toString()}"
                        )
                    }
                } else {

                    UiUtils().showProgress(csl_view, pb, this)
                }


            })


        } else {

            Log.e(
                ASTAR,
                "Called for Isochrone with currentPoint = NULL"
            )
            UiUtils().showSnackbar(csl_view, "Can not reach destination")
        }

    }


    private fun astar_callGeocode(currentNode: Node, destinationNode: Node, style: Style) {

        clearObservers()

        viewmodel.getGeocodeQuery(
            geocodeURLBuilder(
                currentNode.node_point!!,
                currentNode.features!!
            )
        )
            .observe(this, Observer { mReponse ->

                if (viewmodel.geocodeQueryResponse.value != null) {
                    UiUtils().hideProgress(csl_view, pb, this)
                    if (mReponse != null) {


                        atstar_findAdmissibleNodes(mReponse, currentNode, destinationNode, style)
                        if (priorityQueue.isEmpty()) {

                            Log.e(
                                ASTAR,
                                "\nNo valid stations found. Can not reach destination point ${destinationNode.node_point!!}"
                            )

                            UiUtils().showSnackbar(csl_view, "Can not reach destination.")

                        } else {

                            Log.e(
                                ASTAR,
                                "\nUsing " + priorityQueue.peek()!!.node_point.toString() + " as a station after " + (stationMap[priorityQueue.peek()!!.node_point]?.minus(
                                    1
                                )).toString() + " stations"
                            )

                            astar_callIsochrone(priorityQueue.peek()!!, destinationNode, style)
                        }


                    } else {

                        Log.e(
                            ASTAR,
                            "Failed Geocode with currentPoint = ${currentNode.node_point.toString()} and message ${viewmodel.messageGeocode.value.toString()}"
                        )
                    }
                } else {
                    UiUtils().showProgress(csl_view, pb, this)
                }


            })

    }

    private fun atstar_findAdmissibleNodes(
        mReponse: GeoCodedQueryResponse,
        currentNode: Node,
        destinationNode: Node,
        style: Style
    ) {


        if (!priorityQueue.isEmpty()) {


            if (currentNode.node_point != null) {

                //Remove the currentNode from pq
                priorityQueue.remove().node_point?.let { nodeMap.add(it) }

                val parent_gn = currentNode.g_n
                var p: Point? = null
                var p_gn = 0.0
                var eData = 0.0
                var p_hn = 0.0
                var pNode = Node(null, 0.0, 0.0, 0, null)
                val childrenPrirorityQueue = PriorityQueue<Node>(compareByHeuristic)

                for (e in 0 until mReponse.features.size) {

                    p = Point.fromLngLat(
                        mReponse.features[e].center[0],
                        mReponse.features[e].center[1]
                    )


                    /**
                     * We have already counted this. Or it is really far away from destination and lies outside the circle
                     */
                    if (nodeMap.contains(p) || !MapUtils.pointInAdmissibleCircle(
                            center,
                            p,
                            radius
                        )
                    ) {
                        continue
                    }

                    currentHeight = 0
                    nextHeight = 0



                    eData = (nextHeight - currentHeight).toDouble()
                    p_gn = eucledianDistance(p, currentNode.node_point) + parent_gn + eData
                    p_hn = eucledianDistance(p, destinationNode.node_point!!)
                    pNode = Node(p, p_gn, p_hn, MAXIMUM_CHARGE, null)


                    childrenPrirorityQueue.add(pNode)


                }

                var cnt = MAXIMUM_NODES
                while (!childrenPrirorityQueue.isEmpty() && cnt > 0) {

                    if (nodeMap.contains(childrenPrirorityQueue.peek()!!.node_point)) {
                        childrenPrirorityQueue.remove()
                        continue
                    }
                    priorityQueue.add(childrenPrirorityQueue.peek())
                    childrenPrirorityQueue.peek()!!.node_point?.let { nodeMap.add(it) }
                    stationMap[childrenPrirorityQueue.peek()!!.node_point!!] = countIso
                    childrenPrirorityQueue.remove()
                    cnt--;
                }

            }
        }

    }

    private fun processElevations(features: List<Feature>, t: Int): Int {
        var f = 0
        for (x in features) {
            f += x.getStringProperty("ele").toInt()
        }

        if (t == 0) {
            currentHeight = f / features.size

        } else {
            nextHeight = f / features.size
        }

        return f / features.size

    }

    private fun astar_checkDestination(
        currentNode: Node,
        destinationNode: Node
    ): Boolean {
        return evaluateDestination(destinationNode.node_point!!, currentNode.features)
    }

    private fun geocodeURLBuilder(
        originPoint: Point,
        featureList: List<Feature>
    ): String {
        return resources.getString(
            R.string.mapbox_geocode_query,
            "mapbox.places",
            resources.getString(R.string.ev_geocode_query),
            MapUtils.returnMapboxAcceptedLngLat(originPoint),
            MapUtils.generateBBOXFromFeatures(featureList),
            resources.getString(R.string.mapbox_access_token)
        )
    }


    private fun evaluateDestination(
        destinationPoint: Point,
        features: List<Feature>?
    ): Boolean {
        if (features == null) {
            return false
        }

        return TurfJoins.inside(destinationPoint, features[0].geometry() as Polygon)
    }

    private fun makeContour(style: Style, body: FeatureCollection) {

        val source =
            style.getSourceAs<GeoJsonSource>(ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID)

        if (source != null && body.features()!!.size > 0) {
            source.featureCollection(body)


        }

        //Line Layer
        if (!usePolygon) {
            mapboxMap.getStyle { style ->
                var timeLabelSymbolLayer: SymbolLayer

                // Check to see whether the LineLayer for time labels has already been created
                if (style.getLayerAs<SymbolLayer>(TIME_LABEL_LAYER_ID) == null) {
                    timeLabelSymbolLayer = SymbolLayer(
                        TIME_LABEL_LAYER_ID,
                        ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID
                    )
                    timeLabelSymbolLayer = styleLineLayer(timeLabelSymbolLayer)
                    style.addLayer(timeLabelSymbolLayer)

                } else {
                    styleLineLayer(style.getLayerAs<SymbolLayer>(TIME_LABEL_LAYER_ID)!!)
                }
            }
        }


    }

    /** Use the Maps SDK's data-driven styling properties to style the
    the time label LineLayer */

    private fun styleLineLayer(timeLabelLayerToStyle: SymbolLayer): SymbolLayer {

        return timeLabelLayerToStyle.apply {
            textField(concat(get("contour"), literal(" MIN")))
            textFont(listOf("DIN Offc Pro Bold", "Roboto Black"))
            symbolPlacement(SymbolPlacement.LINE)
            textAllowOverlap(true)
            textPadding(1.0)
            textMaxAngle(90.0)
            textLetterSpacing(0.1)
            textHaloColor(Color.parseColor("#343332"))
            textColor(Color.parseColor("#000000"))
            textHaloWidth(4.0)

        }


    }

    /**
     * Add a FillLayer so that that polygons returned by the Isochrone API response can be displayed
     */
    private fun initFillLayer(style: Style) {
        // Create and style a FillLayer based on information in the Isochrone API response
        val isochroneFillLayer =
            FillLayer(ISOCHRONE_FILL_LAYER, ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID)

        isochroneFillLayer.fillColor(Color.parseColor("#4286f4"))
        isochroneFillLayer.fillOpacity(0.4)
        isochroneFillLayer.filter(eq(geometryType(), literal("Polygon")))
        style.addLayerBelow(isochroneFillLayer, MAP_CLICK_MARKER_LAYER_ID)
    }

    /**
     * Add a LineLayer so that that lines returned by the Isochrone API response can be displayed
     */
    private fun initLineLayer(style: Style) {
        // Create and style a LineLayer based on information in the Isochrone API response
        val isochroneLineLayer =
            LineLayer(
                ISOCHRONE_LINE_LAYER,
                ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID
            )

        isochroneLineLayer.lineColor(Color.parseColor("#4286f4"))
        isochroneLineLayer.lineWidth(5.0)
        isochroneLineLayer.lineOpacity(.8)
        isochroneLineLayer.filter(eq(geometryType(), literal("LineString")))
        style.addLayerBelow(isochroneLineLayer, MAP_CLICK_MARKER_LAYER_ID)
    }

    private fun findRoute(point: Point) {

        Toast.makeText(this, point.toJson().toString(), Toast.LENGTH_LONG).show()
        addAnnotationToMap(point.longitude(), point.latitude())

        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        val destinationPoint = point


        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(listOf(originPoint, destinationPoint))
                .bearingsList(
                    listOf(
                        Bearing.builder()
                            .angle(originLocation.bearing.toDouble())
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {

                    setRouteAndStartNavigation(routes, originPoint, destinationPoint)

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

    private fun astarRouteSelection(destinationPoint: Point) {


        if (close_list_points.isEmpty()) {
            return findRoute(destinationPoint)
        }


        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        if (close_list_points.last() != destinationPoint) {
            close_list_points.add(destinationPoint)
        }



        Log.e(ASTAR, "Directional API close_list  ${close_list_points.toString()}")


        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(close_list_points.toList())
                .bearingsList(
                    MapUtils.getDirectionBearings(originLocation, close_list_points).toList()
                )
                .layersList(MapUtils.getDirectionLayers(close_list_points))
                .build(),
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {


                    setRouteAndStartNavigation(routes, originPoint, destinationPoint)

                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {

                    Log.e(
                        ASTAR,
                        "Failed Direction API because ${reasons.toString()}"
                    )
                    Toast.makeText(
                        this@NavigationActivity,
                        "Failed = $reasons",
                        Toast.LENGTH_LONG
                    )
                        .show()
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
        sourcePoint: Point,
        destinationPoint: Point
    ) {

        NAVIGATION_IN_PROGRESS = true
        mapboxNavigation.setNavigationRoutes(routes.toNavigationRoutes())

        startSimulation(routes.first())

        val pathUtil = PathUtils()
        pathUtil.modelIntermediateRoute(
            routes,
            this,
            resources.getString(R.string.mapbox_access_token)
        )

        pathUtil.ELEVATION_STATUS.observe(this, Observer { elevationCompleted ->
            if (elevationCompleted == true) {

                mtbExtraLims.visibility = View.VISIBLE

                mtbExtraLims.text = resources.getString(
                    R.string.profileparams,
                    "${pathUtil.maneuverStatistics + pathUtil.elevationStatistics}"
                ) + "\n+$VEHICLE_PROFILE"

                pathUtil.ELEVATION_STATUS.value = false
            }
        })


        /** show UI elements*/
        findViewById<MapboxSoundButton>(R.id.soundButton).visibility = View.VISIBLE
        findViewById<MapboxRouteOverviewButton>(R.id.routeOverview).visibility =
            View.VISIBLE
        findViewById<CardView>(R.id.tripProgressCard).visibility = View.VISIBLE
        /** move the camera to overview when new route is available */
        navigationCamera.requestNavigationCameraToOverview()

        val route = routes.first()
        startVisionManager(route)

    }

    private fun addAnnotationToMap(
        nodeLongitude: Double,
        nodeLatitude: Double
    ) {

        UiUtils().bitmapFromDrawableRes(
            this,
            R.drawable.ic_baseline_location_on_24
        )?.let {
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(nodeLongitude, nodeLatitude))
                .withIconImage(it)
            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    override fun onStart() {
        super.onStart()

        // register event listeners
        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

//        if (mapboxNavigation.getNavigationRoutes().toDirectionsRoutes().isEmpty()) {
//            mapboxReplayer.pushEvents(
//                listOf(
//                    ReplayRouteMapper.mapToUpdateLocation(
//                        eventTimestamp = 0.0,
//                        point = Point.fromLngLat(
//                            -122.39726512303575,
//                            37.785128345296805
//                        )
//                    )
//                )
//            )
//            mapboxReplayer.playFirstLocation()
//        }
    }

    override fun onStop() {
        super.onStop()

        //Clear viewmodel observers
        clearRoute()
        clearObservers()
        // unregister event listeners to prevent leaks or unnecessary resource consumption
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)

        destinationSearchPoint = null
        initialSOC = null

    }

    override fun onDestroy() {
        super.onDestroy()
        clearObservers()
        MapboxNavigationProvider.destroy()
        mapboxReplayer.finish()
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
        MAP_READY = false
        VEHICLE_PROFILE = 0.0
    }

    private fun clearRouteAndStopNavigation() {
        /** clear*/
        mapboxNavigation.setNavigationRoutes(listOf<DirectionsRoute>().toNavigationRoutes())

        /** stop simulation*/
        mapboxReplayer.stop()

        /** hide UI elements*/
        findViewById<MapboxSoundButton>(R.id.soundButton).visibility = View.INVISIBLE
        findViewById<MapboxManeuverView>(R.id.maneuverView).visibility = View.INVISIBLE
        findViewById<MapboxRouteOverviewButton>(R.id.routeOverview).visibility =
            View.INVISIBLE
        findViewById<CardView>(R.id.tripProgressCard).visibility = View.INVISIBLE
        mtbExtraLims.visibility = View.GONE

        currentPoint = null
        currentFeatures = null
        currentState = DLSState(currentPoint, currentFeatures)
        isochroneStateList = mutableListOf<DLSState>()
        isochroneCenters = mutableListOf<Point>()
        contourFeatures = mutableListOf<FeatureCollection>()

        clearRoute()
        NAVIGATION_IN_PROGRESS = false

        //Stop VisioManager
        stopVisionManager()

    }

    private fun startSimulation(route: DirectionsRoute) {
        mapboxReplayer.run {
            stop()
            clearEvents()
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    private fun clearRoute() {

        clearObservers()
        priorityQueue.clear()
        nodeMap.clear()
        stationMap.clear()
        countIso = 0
        outputLog = ""
        elevationMap.clear()
        close_list_points.clear()

    }

    private fun clearElevationObserver() {
        viewmodel.successfulMapboxTileQuery.removeObservers(this)
        viewmodel.successfulMapboxTileQuery.value = null
        viewmodel.messageMapboxTileQuery.removeObservers(this)
        viewmodel.messageMapboxTileQuery.value = null
        viewmodel.tileQueryMapboxFeature.removeObservers(this)
        viewmodel.tileQueryMapboxFeature.value = null
    }

    private fun clearObservers() {

        clearElevationObserver()
        viewmodel.successfulGeocode.removeObservers(this)
        viewmodel.successfulGeocode.value = null
        viewmodel.successfulMapboxIsochrone.removeObservers(this)
        viewmodel.successfulMapboxIsochrone.value = null
        viewmodel.successfulIsochrone.removeObservers(this)
        viewmodel.successfulIsochrone.value = null

        viewmodel.messageGeocode.removeObservers(this)
        viewmodel.messageGeocode.value = null
        viewmodel.messageMapboxIsochrone.removeObservers(this)
        viewmodel.messageMapboxIsochrone.value = null
        viewmodel.messageIsochrone.removeObservers(this)
        viewmodel.messageIsochrone.value = null

        viewmodel.isochroneMapboxFeature.removeObservers(this)
        viewmodel.isochroneMapboxFeature.value = null
        viewmodel.isochronePolygonResponse.removeObservers(this)
        viewmodel.isochronePolygonResponse.value = null
        viewmodel.geocodeQueryResponse.removeObservers(this)
        viewmodel.geocodeQueryResponse.value = null


    }

    override fun onBackPressed() {
        super.onBackPressed()
        clearRouteAndStopNavigation()
        clearObservers()
        finish()
    }

    private companion object {

        private const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun DirectionsRoute.getRoutePoints(): Array<RoutePoint> {
        val routePoints = arrayListOf<RoutePoint>()
        legs()?.forEach { leg ->
            leg.steps()?.forEach { step ->
                val maneuverPoint = RoutePoint(
                    GeoCoordinate(
                        latitude = step.maneuver().location().latitude(),
                        longitude = step.maneuver().location().longitude()
                    ),
                    step.maneuver().type().mapToManeuverType()
                )
                routePoints.add(maneuverPoint)

                step.geometry()
                    ?.buildStepPointsFromGeometry()
                    ?.map { geometryStep ->
                        RoutePoint(
                            GeoCoordinate(
                                latitude = geometryStep.latitude(),
                                longitude = geometryStep.longitude()
                            )
                        )
                    }
                    ?.let { stepPoints ->
                        routePoints.addAll(stepPoints)
                    }
            }
        }

        return routePoints.toTypedArray()
    }

    private fun startVisionManager(route: DirectionsRoute) {

        try {
            if (!visionManagerWasInit) {
                // Create and start VisionManager.
                VisionManager.create()

                VisionManager.start()
                VisionManager.visionEventsListener = object : VisionEventsListener {}


                VisionArManager.create(VisionManager)

                mapboxArView.setArManager(VisionArManager)
                setArRenderOptions(mapboxArView)

                visionManagerWasInit = true

                VisionArManager.setRoute(
                    Route(
                        route.getRoutePoints(),
                        route.duration().toFloat(),
                        "POUN-START",
                    )
                )

                mapboxArView.visibility = View.VISIBLE
                mapView.visibility = View.GONE


            }
        } catch (e: java.lang.Exception) {
            Log.e(ASTAR, "Exception in starting AR manager ${e.cause} and ${e.message}")
        }

    }

    private fun stopVisionManager() {
        if (visionManagerWasInit) {
            VisionArManager.destroy()
            VisionManager.stop()
            VisionManager.destroy()
            visionManagerWasInit = false
        }
    }


}