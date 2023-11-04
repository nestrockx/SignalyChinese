package com.wegielek.signalychinese.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.databinding.FragmentStrokesBinding
import com.wegielek.signalychinese.enums.CharacterMode
import com.wegielek.signalychinese.utils.Utils.Companion.dpToPixels
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenHeight
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenWidth
import com.wegielek.signalychinese.utils.Utils.Companion.isScreenRotated
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel
import com.wegielek.signalychinese.views.LearnStrokesCanvasView

class StrokesFragment : Fragment(R.layout.fragment_strokes) {
    private var mBinding: FragmentStrokesBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentStrokesBinding.inflate(inflater, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val definitionViewModel = ViewModelProvider(requireActivity()).get(
            DefinitionViewModel::class.java
        )
        val learnStrokesCanvasView =
            view.findViewById<LearnStrokesCanvasView>(R.id.learnStrokesCanvasView)
        definitionViewModel.word.observe(
            viewLifecycleOwner
        ) { s: Dictionary ->
            learnStrokesCanvasView.setHanziCharacter(
                s.simplifiedSign[0]
            )
        }
        if (!isScreenRotated(requireContext())) learnStrokesCanvasView.setDimensions(
            getScreenWidth(
                requireActivity()
            ) - dpToPixels(requireContext(), 128f)
        ) else learnStrokesCanvasView.setDimensions(
            getScreenHeight(requireActivity()) - dpToPixels(requireContext(), 128f)
        )
        mBinding!!.presentBtn.setOnClickListener { v -> learnStrokesCanvasView.setMode(CharacterMode.PRESENTATION) }
        mBinding!!.learnCBtn.setOnClickListener { v -> learnStrokesCanvasView.setMode(CharacterMode.LEARN) }
        onBackPressed()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }
}