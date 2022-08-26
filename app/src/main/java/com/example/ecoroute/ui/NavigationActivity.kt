package com.example.ecoroute.ui


import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ecoroute.R
import com.example.ecoroute.models.responses.IsochronePolygonResponse
import com.example.ecoroute.utils.LocationPermissionHelper
import com.example.ecoroute.utils.MapConstants
import com.example.ecoroute.utils.UiUtils
import com.example.ecoroute.utils.routeutils.RouteGraphUtil
import com.example.ecoroute.utils.routeutils.RouteModelling
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.api.isochrone.IsochroneCriteria
import com.mapbox.api.isochrone.MapboxIsochrone
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.concat
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.eq
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.geometryType
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
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
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import com.mapbox.navigation.ui.voice.view.MapboxSoundButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.*


class NavigationActivity : AppCompatActivity() {


    private val viewmodel: NavigationViewModel by viewModels()
    private lateinit var pb: ProgressBar
    private lateinit var csl_view: ConstraintLayout

    private val ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID = "ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID"
    private val ISOCHRONE_FILL_LAYER = "ISOCHRONE_FILL_LAYER"
    private val ISOCHRONE_LINE_LAYER = "ISOCHRONE_LINE_LAYER"
    private val TIME_LABEL_LAYER_ID = "TIME_LABEL_LAYER_ID"
    private val MAP_CLICK_SOURCE_ID = "MAP_CLICK_SOURCE_ID"
    private val MAP_CLICK_MARKER_ICON_ID = "MAP_CLICK_MARKER_ICON_ID"
    private val MAP_CLICK_MARKER_LAYER_ID = "MAP_CLICK_MARKER_LAYER_ID"
    private var usePolygon = false

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

//        findViewById<MapboxTripProgressView>(R.id.tripProgressView).render(
//            tripProgressApi.getTripProgress(routeProgress)
//        )
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.ecoroute.R.layout.activity_navigation)

        mapView = findViewById(R.id.mapView)
        mapboxMap = mapView.getMapboxMap()

        sv = findViewById(R.id.sv_details)
        tv = findViewById(R.id.tv_details)
        pb = findViewById(R.id.pb_navigation)
        csl_view = findViewById(R.id.csl_navigation)

        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }


    }

    private fun onMapReady() {


//        // load map style
//        mapboxMap.loadStyleUri(
//            Style.MAPBOX_STREETS
//        ) { it ->
//
//
//
//            initFillLayer(it)
//            initLineLayer(it)
//            //This is on style loaded
//            mapView.gestures.addOnMapLongClickListener { point ->
//                findRoute(point)
//                true
//            }
//
//            mapView.gestures.addOnMapClickListener { point ->
//                createIsochronePolygons(point, style = it)
//                true
//            }
//
//
//        }


        mapboxMap.loadStyle(
            style(styleUri = Style.MAPBOX_STREETS) {


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

                    mapView.gestures.addOnMapLongClickListener {
                        findRoute(it)
                        true
                    }

                    mapView.gestures.addOnMapClickListener {
                        createIsochronePolygons(it, style)
                        true
                    }
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
            viewportDataSource.overviewPadding = MapConstants.landscapeOverviewPadding
            viewportDataSource.followingPadding = MapConstants.landscapeFollowingPadding
        } else {
            viewportDataSource.overviewPadding = MapConstants.overviewPadding
            viewportDataSource.followingPadding = MapConstants.followingPadding
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


//        findViewById<ImageView>(R.id.stop).setOnClickListener {
//            clearRouteAndStopNavigation()
//        }
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


        mapboxNavigation.startTripSession()
    }

    private fun createIsochronePolygons(originPoint: Point, style: Style) {

        Toast.makeText(this, originPoint.toJson().toString(), Toast.LENGTH_LONG).show()

        val mapboxIsochroneRequest = MapboxIsochrone.builder()
            .accessToken(getString(com.example.ecoroute.R.string.mapbox_access_token))
            .profile(IsochroneCriteria.PROFILE_DRIVING)
            .addContoursMinutes(60)
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

                Log.e("Navigation", "In response")
                if (response.body() != null && response.body()!!.features() != null) {
                    Log.e("Navigation", "Response body not null value = \n ${response.body()}")
                    val source =
                        style.getSourceAs<GeoJsonSource>(ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID)

                    if (source != null && response.body()!!.features()!!.size > 0) {
                        source.featureCollection(response.body()!!)
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

                    val currentZoom = mapboxMap.cameraState.zoom

                    if (currentZoom > 14) {

                        mapboxMap.setCamera(
                            CameraOptions.Builder()
                                .zoom(currentZoom - 4.5)
                                .build()
                        )

                    }


                }
            }

            override fun onFailure(call: Call<FeatureCollection?>?, throwable: Throwable) {
                UiUtils().showSnackbar(
                    csl_view,
                    UiUtils().returnStateMessageForThrowable(throwable)
                )
                UiUtils().logThrowables("Navigation", throwable)
            }
        })


    }

    private fun initViews(mResponse: IsochronePolygonResponse?) {

        if (mResponse != null) {

            Toast.makeText(this, mResponse.toString(), Toast.LENGTH_LONG).show()

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

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                }
            }
        )

    }

    private fun setRouteAndStartNavigation(
        routes: List<DirectionsRoute>,
        sourcePoint: Point,
        destinationPoint: Point
    ) {


        mapboxNavigation.setNavigationRoutes(routes.toNavigationRoutes())

        val src = RouteGraphUtil.convertTerminalsToNode(sourcePoint, "SOURCE")
        val dst = RouteGraphUtil.convertTerminalsToNode(destinationPoint, "DESTINATION")

        val routeGraph = RouteGraphUtil.getRouteGraphInstance(routes, src, dst)


        if (routeGraph != null) {


            var c = ""

            //Model the graph

            val graphPath =
                RouteModelling.modelGraph(routeGraph, src, dst, 1000.0)
            val modelledGraphPath = graphPath.first
            val modelledGraphLogs = graphPath.second


            c += "\nSOURCE:  ${src.node_longitude},${src.node_latitude},${src.node_description},${src.node_weight},${src.node_time},${src.node_height}"
            c += "\nDST: ${dst.node_longitude},${dst.node_latitude},${dst.node_description},${dst.node_weight},${dst.node_time},${dst.node_height}"

            //Add the graph details
            c += "\nGraphNodes: size = ${routeGraph.graph_nodes.size}:"
            for (j in 0 until routeGraph.graph_nodes.size) {
                val i = routeGraph.graph_nodes[j]
                c += "\n ${i.node_longitude},${i.node_latitude},${i.node_description},${i.node_weight},${i.node_time},${i.node_height}"
            }

            c += "\nGraphEdges: size = ${routeGraph.graph_edges.size}:"
            for (j in 0 until routeGraph.graph_edges.size) {
                val i = routeGraph.graph_edges[j]!!
                c += "\n" + i.edge_start_node.node_longitude.toString() + "," + i.edge_end_node.node_longitude.toString() + ": " + i.edge_weight.toString()
            }


            c += modelledGraphLogs
            c += "\nModelled distance =  ${modelledGraphPath.second} with path : \n"

            for (i in 0 until modelledGraphPath.first.size) {
                c += modelledGraphPath.first[i].node_longitude.toString() + " , "

                if (modelledGraphPath.first[i].node_description == "EV") {
                    addAnnotationToMap(
                        modelledGraphPath.first[i].node_longitude,
                        modelledGraphPath.first[i].node_latitude
                    )
                }
            }

            sv.visibility = View.VISIBLE
            tv.text = c


        }

        startSimulation(routes.first())


        /** show UI elements*/
        findViewById<MapboxSoundButton>(R.id.soundButton).visibility = View.VISIBLE
        findViewById<MapboxRouteOverviewButton>(R.id.routeOverview).visibility = View.VISIBLE
//        findViewById<CardView>(R.id.tripProgressCard).visibility = View.VISIBLE

        /** move the camera to overview when new route is available */
        navigationCamera.requestNavigationCameraToOverview()


    }

    private fun addAnnotationToMap(nodeLongitude: Double, nodeLatitude: Double) {

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

        if (mapboxNavigation.getNavigationRoutes().toDirectionsRoutes().isEmpty()) {
            mapboxReplayer.pushEvents(
                listOf(
                    ReplayRouteMapper.mapToUpdateLocation(
                        eventTimestamp = 0.0,
                        point = Point.fromLngLat(-122.39726512303575, 37.785128345296805)
                    )
                )
            )
            mapboxReplayer.playFirstLocation()
        }
    }

    override fun onStop() {
        super.onStop()

        // unregister event listeners to prevent leaks or unnecessary resource consumption
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        MapboxNavigationProvider.destroy()
        mapboxReplayer.finish()
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
    }

    private fun clearRouteAndStopNavigation() {
        /** clear*/
        mapboxNavigation.setNavigationRoutes(listOf<DirectionsRoute>().toNavigationRoutes())

        /** stop simulation*/
        mapboxReplayer.stop()

        /** hide UI elements*/
//        findViewById<MapboxSoundButton>(R.id.soundButton).visibility = View.INVISIBLE
//        findViewById<MapboxManeuverView>(R.id.maneuverView).visibility = View.INVISIBLE
//        findViewById<MapboxRouteOverviewButton>(R.id.routeOverview).visibility = View.INVISIBLE
//        findViewById<CardView>(R.id.tripProgressCard).visibility = View.INVISIBLE
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

    private companion object {

        private const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}