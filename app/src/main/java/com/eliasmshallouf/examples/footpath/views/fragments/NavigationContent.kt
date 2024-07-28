package com.eliasmshallouf.examples.footpath.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.eliasmshallouf.examples.footpath.R
import com.eliasmshallouf.examples.footpath.databinding.CardContentNavigateBinding
import com.eliasmshallouf.examples.footpath.viewmodels.MainActivityViewModel
import com.mapbox.geojson.Point

class NavigationContent: Fragment() {
    private var _cardContentNavigateBinding: CardContentNavigateBinding? = null
    private val cardContentNavigateBinding get() = _cardContentNavigateBinding!!
    private val mainActivityViewModel by lazy {
        ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
    }

    private lateinit var startLocationObserver: Observer<Point?>
    private lateinit var destLocationObserver: Observer<Point?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _cardContentNavigateBinding = CardContentNavigateBinding.inflate(inflater, container, false)

        startLocationObserver = Observer {
            updateState(cardContentNavigateBinding.setStartTv, it)
        }
        mainActivityViewModel.startLocation.observe(requireActivity(), startLocationObserver)

        destLocationObserver = Observer {
            updateState(cardContentNavigateBinding.setEndTv, it)
        }
        mainActivityViewModel.destinationLocation.observe(requireActivity(), destLocationObserver)

        cardContentNavigateBinding.goBtn.setOnClickListener {
            if(mainActivityViewModel.startLocation.value == null) {
                Toast.makeText(requireContext(),
                    getString(R.string.please_select_start_address), Toast.LENGTH_LONG).show()
                error(cardContentNavigateBinding.setStartTv)
                return@setOnClickListener
            }

            if(mainActivityViewModel.destinationLocation.value == null) {
                Toast.makeText(requireContext(),
                    getString(R.string.please_select_dest_address), Toast.LENGTH_LONG).show()
                error(cardContentNavigateBinding.setEndTv)
                return@setOnClickListener
            }

            mainActivityViewModel.updateState(getString(R.string.finding_the_shortest_path))
            mainActivityViewModel.startNavigation()
        }

        cardContentNavigateBinding.resetBtn.setOnClickListener {
            mainActivityViewModel.reset()
        }

        cardContentNavigateBinding.setStartTv.setOnClickListener {
            mainActivityViewModel.updateClickTarget(if(mainActivityViewModel.clickTarget.value != 1) 1 else 0)
        }

        cardContentNavigateBinding.setEndTv.setOnClickListener {
            mainActivityViewModel.updateClickTarget(if(mainActivityViewModel.clickTarget.value != 2) 2 else 0)
        }

        return cardContentNavigateBinding.root
    }

    private fun updateState(tv: TextView, point: Point?) {
        tv.setBackgroundResource(if(point == null) R.drawable.tv_bg else R.drawable.tv_bg_set)
        tv.setTextColor(ContextCompat.getColor(requireContext(), if(point == null) R.color.black else R.color.black))
    }

    private fun error(tv: TextView) {
        tv.setBackgroundResource(R.drawable.tv_bg_error)
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivityViewModel.startLocation.removeObserver(startLocationObserver)
        mainActivityViewModel.destinationLocation.removeObserver(destLocationObserver)
        _cardContentNavigateBinding = null
    }
}