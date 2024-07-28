package com.eliasmshallouf.examples.footpath.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import com.eliasmshallouf.examples.footpath.R
import com.eliasmshallouf.examples.footpath.databinding.ActivityMainBinding
import com.eliasmshallouf.examples.footpath.viewmodels.MainActivityViewModel
import com.eliasmshallouf.examples.footpath.views.fragments.LoadingContent
import com.eliasmshallouf.examples.footpath.views.fragments.NavigationContent
import com.eliasmshallouf.examples.footpath.views.utils.loadBitmap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

class MainActivity : AppCompatActivity() {
    private companion object {
        private const val REQUEST_LOCATION_CODE = 201
    }

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private val locationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private var currentLine: PolylineAnnotation? = null
    private lateinit var polylineAnnotationManager: PolylineAnnotationManager

    private var homeMarker: PointAnnotation? = null
    private var startMarker: PointAnnotation? = null
    private var destMarker: PointAnnotation? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainActivityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        if (Build.VERSION.SDK_INT in 21..29) {
            window.statusBarColor = Color.TRANSPARENT
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LAYOUT_STABLE
        } else if (Build.VERSION.SDK_INT >= 30) {
            window.statusBarColor = Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        val initialCameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(-74.0066, 40.7135))
            .pitch(0.0)
            .zoom(12.0)
            .bearing(0.0)
            .build()

        val mapInitOptions = MapInitOptions(
            context = this,
            cameraOptions = initialCameraOptions,
            textureView = true
        )
        mapView = MapView(this, mapInitOptions)
        mainBinding.mapViewContainer.addView(mapView)

        map = mapView.mapboxMap
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
        polylineAnnotationManager = mapView.annotations.createPolylineAnnotationManager()

        startLocationService()

        map.addOnMapClickListener {
            Log.d("click-location", "${it.longitude()}, ${it.latitude()}")

            when (mainActivityViewModel.clickTarget.value) {
                1 -> mainActivityViewModel.setStartLocation(it)
                2 -> mainActivityViewModel.setDestinationLocation(it)
            }
            true
        }

        mainActivityViewModel
            .currentLocation
            .observe(this) {
                homeMarker?.let { hm -> pointAnnotationManager.delete(hm) }

                if (it == null)
                    return@observe

                mapView.camera.easeTo(CameraOptions.Builder()
                    .center(Point.fromLngLat(it.longitude, it.latitude))
                    .pitch(0.0)
                    .zoom(12.0)
                    .bearing(0.0)
                    .build()
                )

                homeMarker = addAnnotationToMap(Point.fromLngLat(it.longitude, it.latitude), R.drawable.ic_home)
            }

        mainActivityViewModel
            .loadingMessage
            .observe(this) {
                TransitionManager.beginDelayedTransition(mainBinding.cardContent.parent as ViewGroup)
                when(it) {
                    "" -> {
                        supportFragmentManager.beginTransaction().replace(
                            mainBinding.cardContent.id,
                            NavigationContent()
                        ).commit()
                    }
                    else -> {
                        supportFragmentManager.beginTransaction()
                            .replace(mainBinding.cardContent.id, LoadingContent.newInstance(it)).commit()
                    }
                }
                TransitionManager.endTransitions(mainBinding.cardContent.parent as ViewGroup)
        }
        
        mainActivityViewModel
            .clickTarget
            .observe(this) {
                when(it) {
                    0 -> mainBinding.trackTv.visibility = View.GONE
                    1 -> {
                        with(mainBinding.trackTv) {
                            this.visibility = View.VISIBLE
                            this.text = context.getString(R.string.select_the_start_address)
                        }
                    }
                    2 -> {
                        with(mainBinding.trackTv) {
                            this.visibility = View.VISIBLE
                            this.text = context.getString(R.string.select_the_destination_address)
                        }
                    }
                }
        }

        mainActivityViewModel
            .direction
            .observe(this) {
                if(currentLine != null)
                    polylineAnnotationManager.delete(currentLine!!)

                if(it == null)
                    return@observe

                currentLine = polylineAnnotationManager.create(PolylineAnnotationOptions()
                    .withPoints(it.routes[0].geometry.coordinates.map { pair -> Point.fromLngLat(pair[0], pair[1]) })
                    .withLineWidth(3.0)
                    .withLineColor("#B300FFFF")
                )
            }

        mainActivityViewModel
            .startLocation
            .observe(this) {
                if(startMarker != null)
                    pointAnnotationManager.delete(startMarker!!)

                startMarker = it?.let { p -> addAnnotationToMap(p, R.drawable.ic_start_location) }
            }

        mainActivityViewModel
            .destinationLocation
            .observe(this) {
                if(destMarker != null)
                    pointAnnotationManager.delete(destMarker!!)

                destMarker = it?.let { p -> addAnnotationToMap(p, R.drawable.ic_dest_location) }
            }
    }

    private fun startLocationService() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_CODE
            )
            return
        }

        trackLocation()
    }

    @SuppressLint("MissingPermission")
    private fun trackLocation() {
        locationProviderClient.lastLocation.addOnSuccessListener {
            if (it == null)
                return@addOnSuccessListener

            mainActivityViewModel.onLocationChanged(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode != REQUEST_LOCATION_CODE)
            return

        if ((grantResults.isEmpty() ||
                grantResults[0] != PackageManager.PERMISSION_GRANTED))
            return

        trackLocation()
    }

    private fun addAnnotationToMap(point: Point, @DrawableRes icon: Int): PointAnnotation =
        this@MainActivity
            .loadBitmap(icon)
            ?.let {
            pointAnnotationManager.create(PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
            )
        }!!

    override fun onDestroy() {
        super.onDestroy()
    }
}