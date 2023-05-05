package com.example.ecoroute.ui


import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import com.example.ecoroute.R
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import com.mapbox.navigation.ui.voice.view.MapboxSoundButton
import kotlinx.android.synthetic.main.activity_visual_path.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.ecoroute.utils.MapUtils
import com.example.ecoroute.utils.UiUtils
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
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
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.*
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.view.MapboxRecenterButton
import com.mapbox.navigation.ui.maps.camera.view.MapboxRouteOverviewButton
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.tripprogress.model.*
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import java.util.*

@ExperimentalPreviewMapboxNavigationAPI
class VisualPathActivity : AppCompatActivity() {
    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 0L
    }

    private var NAVIGATION_IN_PROGRESS = true
    private val uiUtilInstance = UiUtils()
    private lateinit var annotationApi: AnnotationPlugin
    private lateinit var pointAnnotationManager: PointAnnotationManager


    private val mapboxReplayer = MapboxReplayer()

    /**
     * Debug tool that mocks location updates with an input from the [mapboxReplayer].
     */
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)


    /**
     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
     */
    private lateinit var navigationCamera: NavigationCamera

    /**
     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
     */
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    /*
     * Below are generated camera padding values to ensure that the route fits well on screen while
     * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
     */
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    /**
     * Generates updates for the [MapboxManeuverView] to display the upcoming maneuver instructions
     * and remaining distance to the maneuver point.
     */
    private lateinit var maneuverApi: MapboxManeuverApi

    /**
     * Generates updates for the [MapboxTripProgressView] that include remaining time and distance to the destination.
     */
    private lateinit var tripProgressApi: MapboxTripProgressApi

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private lateinit var routeLineApi: MapboxRouteLineApi

    /**
     * Draws route lines on the map based on the data from the [routeLineApi]
     */
    private lateinit var routeLineView: MapboxRouteLineView

    /**
     * Generates updates for the [routeArrowView] with the geometries and properties of maneuver arrows that should be drawn on the map.
     */
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    /**
     * Draws maneuver arrows on the map based on the data [routeArrowApi].
     */
    private lateinit var routeArrowView: MapboxRouteArrowView

    /**
     * Stores and updates the state of whether the voice instructions should be played as they come or muted.
     */
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                findViewById<MapboxSoundButton>(R.id.path_soundButton).muteAndExtend(
                    BUTTON_ANIMATION_DURATION
                )
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                findViewById<MapboxSoundButton>(R.id.path_soundButton).unmuteAndExtend(
                    BUTTON_ANIMATION_DURATION
                )
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }

    /**
     * Extracts message that should be communicated to the driver about the upcoming maneuver.
     * When possible, downloads a synthesized audio file that can be played back to the driver.
     */
    private lateinit var speechApi: MapboxSpeechApi

    /**
     * Plays the synthesized audio files with upcoming maneuver instructions
     * or uses an on-device Text-To-Speech engine to communicate the message to the driver.
     * NOTE: do not use lazy initialization for this class since it takes some time to initialize
     * the system services required for on-device speech synthesis. With lazy initialization
     * there is a high risk that said services will not be available when the first instruction
     * has to be played. [MapboxVoiceInstructionsPlayer] should be instantiated in
     * `Activity#onCreate`.
     */
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    /**
     * Observes when a new voice instruction should be played.
     */
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

    /**
     * Based on whether the synthesized audio file is available, the callback plays the file
     * or uses the fall back which is played back using the on-device Text-To-Speech engine.
     */
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    // play the instruction via fallback text-to-speech engine
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    // play the sound file from the external generator
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }

    /**
     * When a synthesized audio file was downloaded, this callback cleans up the disk after it was played.
     */
    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
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

    /**
     * Gets notified with progress along the currently active route.
     */
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = findViewById<MapView>(R.id.path_mapView).getMapboxMap().getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
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
                findViewById<MapboxManeuverView>(R.id.path_maneuverView).visibility = View.VISIBLE
                findViewById<MapboxManeuverView>(R.id.path_maneuverView).renderManeuvers(maneuvers)
            }
        )

        // update bottom trip progress summary
        findViewById<MapboxTripProgressView>(R.id.path_tripProgressView).render(
            tripProgressApi.getTripProgress(routeProgress)
        )
    }

    /**
     * Gets notified whenever the tracked routes change.
     *
     * A change can mean:
     * - routes get changed with [MapboxNavigation.setRoutes]
     * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
     * - driver got off route and a reroute was executed
     */
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(
                routeUpdateResult.navigationRoutes
            ) { value ->
                findViewById<MapView>(R.id.path_mapView).getMapboxMap().getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

            // update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()
        } else {
            // remove the route line and route arrow from the map
            val style = findViewById<MapView>(R.id.path_mapView).getMapboxMap().getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }


    private val mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
        MapboxNavigationProvider.retrieve()
    } else {
        MapboxNavigationProvider.create(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                //.locationEngine(replayLocationEngine)
                .build()
        )
    }
    private val TAG = VisualPathActivity::class.java.simpleName

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visual_path)

        val coordinate_string = intent.getStringExtra("coordinate")

        Log.e(TAG, coordinate_string.toString())

        annotationApi = findViewById<MapView>(R.id.path_mapView).annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()

        // initialize Navigation Camera
        viewportDataSource =
            MapboxNavigationViewportDataSource(findViewById<MapView>(R.id.path_mapView).getMapboxMap())
        navigationCamera = NavigationCamera(
            findViewById<MapView>(R.id.path_mapView).getMapboxMap(),
            findViewById<MapView>(R.id.path_mapView).camera,
            viewportDataSource
        )
        // set the animations lifecycle listener to ensure the NavigationCamera stops
        // automatically following the user location when the map is interacted with
        findViewById<MapView>(R.id.path_mapView).camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
            // shows/hide the recenter button depending on the camera state
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> findViewById<MapboxRecenterButton>(R.id.path_recenter).visibility =
                    View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE -> findViewById<MapboxRecenterButton>(R.id.path_recenter).visibility =
                    View.VISIBLE
            }
        }
        // set the padding values depending on screen orientation and visible view layout
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.followingPadding = followingPadding
        }

        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = DistanceFormatterOptions.Builder(this).build()

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

        // initialize route line, the withRouteLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)


        findViewById<MapView>(R.id.path_mapView).getMapboxMap().loadStyle(style(styleUri = Style.TRAFFIC_DAY) {
        }, object : Style.OnStyleLoaded {
            override fun onStyleLoaded(style: Style) {
                findViewById<MapView>(R.id.path_mapView).gestures.addOnMapLongClickListener { it ->
                    true
                }
            }
        }
        )

        // initialize view interactions
        findViewById<ImageView>(R.id.path_stop).setOnClickListener {
            clearRouteAndStopNavigation()
        }
        findViewById<MapboxRecenterButton>(R.id.path_recenter).setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            findViewById<MapboxRouteOverviewButton>(R.id.path_routeOverview).showTextAndExtend(
                BUTTON_ANIMATION_DURATION
            )
        }
        findViewById<MapboxRouteOverviewButton>(R.id.path_routeOverview).setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            findViewById<MapboxRecenterButton>(R.id.path_recenter).showTextAndExtend(
                BUTTON_ANIMATION_DURATION
            )
        }
        findViewById<MapboxSoundButton>(R.id.path_soundButton).setOnClickListener {
            // mute/unmute voice instructions
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }

        // set initial sounds button state
        findViewById<MapboxSoundButton>(R.id.path_soundButton).unmute()

        findViewById<MapView>(R.id.path_mapView).location.apply {
            setLocationProvider(navigationLocationProvider)
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@VisualPathActivity,
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            enabled = true
        }



        start_navigation(coordinate_string)
    }

    private fun start_navigation(coordinateString: String?) {


        if(coordinateString == null || coordinateString.isEmpty()){
            Toast.makeText(this, "Unable to load navigation path", Toast.LENGTH_SHORT).show()
            return
        }
        val coordiantes = coordinateString.split(';')
        val path_list = mutableListOf<Point>()
        for (item in coordiantes) {
            if(item.isEmpty()){
                continue
            }
            val i = item.split(',')
            path_list.add(Point.fromLngLat(i[0].toDouble(),i[1].toDouble()))
        }


        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(path_list.toList())
                .layersList(MapUtils.getDirectionLayers(path_list))
                .build(),
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {

                    setRouteAndStartNavigation(routes, path_list, routerOrigin)

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

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                // comment out the location engine setting block to disable simulation
                .locationEngine(replayLocationEngine)
                .build()
        )

        // initialize location puck
        findViewById<MapView>(R.id.path_mapView).location.apply {
            setLocationProvider(navigationLocationProvider)
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@VisualPathActivity,
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            enabled = true
        }


    }

    override fun onStart() {
        super.onStart()
        findViewById<MapView>(R.id.path_mapView).onStart()

        try {
            mapboxNavigation.registerRoutesObserver(routesObserver)
            mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
            mapboxNavigation.registerLocationObserver(locationObserver)
            mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
            mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Restart the application after giving location permissions",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onStop() {
        super.onStop()
        findViewById<MapView>(R.id.path_mapView)?.onStop()

        clearRouteAndStopNavigation()

        // unregister event listeners to prevent leaks or unnecessary resource consumption
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)

    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxReplayer.finish()
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
    }

    private fun replayOriginLocation() {
//        mapboxReplayer.pushEvents(
//            listOf(
//                ReplayRouteMapper.mapToUpdateLocation(
//                    Date().time.toDouble(),
//                    Point.fromLngLat(-122.39726512303575, 37.785128345296805)
//                )
//            )
//        )
//        mapboxReplayer.playFirstLocation()
//        mapboxReplayer.playbackSpeed(3.0)
    }

    private fun setRouteAndStartNavigation(
        routes: List<DirectionsRoute>, path_list: MutableList<Point>, routerOrigin: RouterOrigin
    ) {

        NAVIGATION_IN_PROGRESS = true
        Log.e(TAG, "Routes: $routes" )
        mapboxNavigation.setNavigationRoutes(routes.toNavigationRoutes(routerOrigin))

        //mark the source and destination
        mark(path_list[0].latitude(), path_list[0].longitude(), R.drawable.source_location)
        mark(path_list[path_list.size-1].latitude(), path_list[path_list.size-1].longitude(), R.drawable.destination_locatiom)


//        findViewById<MapboxSoundButton>(R.id.path_soundButton).visibility = View.VISIBLE
//        findViewById<MapboxManeuverView>(R.id.path_maneuverView).visibility = View.VISIBLE
//        findViewById<MapboxRouteOverviewButton>(R.id.path_routeOverview).visibility = View.VISIBLE
//        findViewById<CardView>(R.id.path_tripProgressCard).visibility = View.VISIBLE


        //startSimulation(routes.first())

        navigationCamera.requestNavigationCameraToOverview()


    }

    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setNavigationRoutes(listOf())
        NAVIGATION_IN_PROGRESS = false
        // stop simulation
        mapboxReplayer.stop()

        // hide UI elements
        findViewById<MapboxSoundButton>(R.id.path_soundButton).visibility = View.INVISIBLE
        findViewById<MapboxManeuverView>(R.id.path_maneuverView).visibility = View.INVISIBLE
        findViewById<MapboxRouteOverviewButton>(R.id.path_routeOverview).visibility = View.INVISIBLE
        findViewById<CardView>(R.id.path_tripProgressCard).visibility = View.INVISIBLE
    }

    private fun mark(_latitude: Double, _longitude: Double, dr: Int) {

        uiUtilInstance.bitmapFromDrawableRes(
            this, dr
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




