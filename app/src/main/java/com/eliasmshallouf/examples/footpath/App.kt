package com.eliasmshallouf.examples.footpath

import android.app.Application
import com.eliasmshallouf.examples.footpath.ktorclient.DirectionsApiClient
import com.mapbox.common.MapboxOptions

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        MapboxOptions.accessToken = getString(R.string.mapbox_access_token)
        DirectionsApiClient.accessToken(getString(R.string.mapbox_access_token))
    }
}