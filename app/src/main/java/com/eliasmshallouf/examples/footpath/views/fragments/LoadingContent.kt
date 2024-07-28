package com.eliasmshallouf.examples.footpath.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.eliasmshallouf.examples.footpath.R
import com.eliasmshallouf.examples.footpath.databinding.CardContentLoadingBinding

class LoadingContent: Fragment() {
    companion object {
        private const val MSG_ARG = "msg"

        fun newInstance(msg: String) = LoadingContent().apply {
            arguments = Bundle().apply {
                putString(MSG_ARG, msg)
            }
        }
    }

    private var _loadingBinding: CardContentLoadingBinding? = null
    private val loadingBinding get() = _loadingBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _loadingBinding = CardContentLoadingBinding.inflate(inflater, container, false)

        loadingBinding.msg.text = arguments?.getString(MSG_ARG, "") ?: ""

        return loadingBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _loadingBinding = null
    }
}