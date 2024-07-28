package com.eliasmshallouf.examples.footpath.views.utils

import android.os.Build
import android.util.Log
import android.view.SurfaceView
import android.view.View
import androidx.annotation.RequiresApi
import com.mapbox.maps.MapView

@RequiresApi(Build.VERSION_CODES.N)
fun MapView.hideExtraViews() =
    (0..this.childCount)
        .map { this.getChildAt(it) }
        .filter { it != null && it !is SurfaceView }
        .forEach {
            this.removeView(it)
        }