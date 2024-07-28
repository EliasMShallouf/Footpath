package com.eliasmshallouf.examples.footpath.viewmodels

import android.location.Location
import android.location.LocationListener
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasmshallouf.examples.footpath.ktorclient.DirectionsApiClient
import com.eliasmshallouf.examples.footpath.ktorclient.routes.navigate
import com.eliasmshallouf.examples.footpath.models.response.Direction
import com.google.gson.Gson
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.internal.decodeStringToJsonTree

class MainActivityViewModel: ViewModel(), LocationListener {
    var currentLocation = MutableLiveData<Location>()
    var startLocation = MutableLiveData<Point?>()
    var destinationLocation = MutableLiveData<Point?>()
    var direction = MutableLiveData<Direction?>()

    var loadingMessage = MutableLiveData("")
    var clickTarget = MutableLiveData(0) // 0 for none, 1 for start, 2 for destination

    override fun onLocationChanged(location: Location) {
        currentLocation.value = location
    }

    fun setStartLocation(location: Point?) {
        startLocation.value = location
    }

    fun setDestinationLocation(location: Point?) {
        destinationLocation.value = location
    }

    fun updateState(msg: String) {
        loadingMessage.value = msg
    }

    fun updateClickTarget(target: Int) {
        clickTarget.value = target
    }

    fun startNavigation() {
        updateClickTarget(0)

        viewModelScope.launch(Dispatchers.IO) {
            val json = try {
                DirectionsApiClient
                    .navigate(startLocation.value!!, destinationLocation.value!!)
            } catch (e: Exception) {
                ""
            }

            viewModelScope.launch(Dispatchers.Main) {
                Log.d("json", json)

                if (json.isNotEmpty()) {
                    direction.value = Gson().fromJson(json, Direction::class.java)
                }

                updateState("")
            }
        }
    }

    fun reset() {
        direction.value = null
        setStartLocation(null)
        setDestinationLocation(null)
    }
}