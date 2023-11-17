package com.wegielek.signalychinese.views.fragments

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.databinding.FragmentStrokesBinding
import com.wegielek.signalychinese.enums.CharacterMode
import com.wegielek.signalychinese.utils.Utils.Companion.dpToPixels
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenHeight
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenWidth
import com.wegielek.signalychinese.utils.Utils.Companion.isScreenRotated
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel
import com.wegielek.signalychinese.views.DefinitionWordActivity
import com.wegielek.signalychinese.views.LearnStrokesCanvasView


class StrokesFragment : Fragment(R.layout.fragment_strokes) {
    lateinit var mBinding: FragmentStrokesBinding
    private lateinit var charArray: CharArray
    private lateinit var definitionViewModel: DefinitionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentStrokesBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        definitionViewModel = (activity as DefinitionWordActivity).definitionViewModel
        val learnStrokesCanvasView =
            view.findViewById<LearnStrokesCanvasView>(R.id.learnStrokesCanvasView)
        learnStrokesCanvasView.setViewModel(definitionViewModel)
        definitionViewModel.word.observe(
            viewLifecycleOwner
        ) { s: Dictionary ->
            charArray = s.simplifiedSign.plus(s.traditionalSign).toSet().toCharArray()
            mBinding.characterIndex.text = getString(R.string.characters_number_placeholder, definitionViewModel.index + 1, charArray.size)
            learnStrokesCanvasView.setHanziCharacter(
                charArray[definitionViewModel.index], definitionViewModel.characterMode
            )
            setHighlights()
        }
        if (!isScreenRotated(requireContext())) learnStrokesCanvasView.setDimensions(
            getScreenWidth(
                requireActivity()
            ) - dpToPixels(requireContext(), 64f)
        ) else learnStrokesCanvasView.setDimensions(
            getScreenHeight(requireActivity()) - dpToPixels(requireContext(), 128f)
        )

        mBinding.restartBtn.visibility = View.INVISIBLE
        mBinding.restartBtn.setOnClickListener {
            learnStrokesCanvasView.setMode(CharacterMode.LEARN)
        }

        if (definitionViewModel.characterMode == CharacterMode.PRESENTATION) {
            mBinding.learnCBtn.text = getString(R.string.draw)
            mBinding.restartBtn.visibility = View.INVISIBLE
        } else if (definitionViewModel.characterMode == CharacterMode.LEARN) {
            mBinding.restartBtn.visibility = View.VISIBLE
            mBinding.learnCBtn.text = getString(R.string.present)
        }

        mBinding.learnCBtn.setOnClickListener {
            if (definitionViewModel.characterMode == CharacterMode.PRESENTATION) {
                learnStrokesCanvasView.setMode(CharacterMode.LEARN)
                mBinding.learnCBtn.text = getString(R.string.present)
                mBinding.restartBtn.visibility = View.VISIBLE
            } else if (definitionViewModel.characterMode == CharacterMode.LEARN) {
                learnStrokesCanvasView.setMode(CharacterMode.PRESENTATION)
                mBinding.learnCBtn.text = getString(R.string.draw)
                mBinding.restartBtn.visibility = View.INVISIBLE
            }
        }

        mBinding.nextCharacter.setOnClickListener {
            definitionViewModel.index++
            if (definitionViewModel.index < charArray.size) {
                learnStrokesCanvasView.setHanziCharacter(
                    charArray[definitionViewModel.index], definitionViewModel.characterMode
                )
                setHighlights()
            } else {
                definitionViewModel.index--
            }
            mBinding.characterIndex.text = getString(R.string.characters_number_placeholder, definitionViewModel.index + 1, charArray.size)
        }

        mBinding.prevCharacter.setOnClickListener{
            definitionViewModel.index--
            if (definitionViewModel.index >= 0) {
                learnStrokesCanvasView.setHanziCharacter(
                    charArray[definitionViewModel.index], definitionViewModel.characterMode
                )
                setHighlights()
            } else {
                definitionViewModel.index++
            }
            mBinding.characterIndex.text = getString(R.string.characters_number_placeholder, definitionViewModel.index + 1, charArray.size)
        }

        //val str = SpannableString((context as DefinitionWordActivity).binding.definitionCharactersTraditonalTv.text)
        //str.setSpan(BackgroundColorSpan(requireContext().getColor(R.color.grey)), 0, 1, 0)
        //(context as DefinitionWordActivity).binding.definitionCharactersTraditonalTv.text = str
    }

    override fun onResume() {
        super.onResume()
        setOnBackPressed()
    }

    private fun setHighLightedText(tv: TextView, textToHighlight: String, color: Int) {
        val tvt = tv.text.toString()
        var ofe = tvt.indexOf(textToHighlight, 0)
        val wordToSpan: Spannable = SpannableString(tv.text)
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(textToHighlight, ofs)
            if (ofe == -1) break else {
                wordToSpan.setSpan(
                    BackgroundColorSpan(color),
                    ofe,
                    ofe + textToHighlight.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE)
            }
            ofs = ofe + 1
        }
    }

    private fun setHighlights() {
        removeHighlights()
        setHighLightedText(
            (context as DefinitionWordActivity).binding.definitionCharactersSimplifiedTv,
            charArray[definitionViewModel.index].toString(),
            requireContext().getColor(R.color.white_highlight)
        )
        setHighLightedText(
            (context as DefinitionWordActivity).binding.definitionCharactersTraditonalTv,
            charArray[definitionViewModel.index].toString(),
            requireContext().getColor(R.color.white_highlight)
        )
    }

    private fun removeHighlights() {
        (context as DefinitionWordActivity).binding.definitionCharactersTraditonalTv.text =
            SpannableString((context as DefinitionWordActivity).binding.definitionCharactersTraditonalTv.text.toString())
        (context as DefinitionWordActivity).binding.definitionCharactersSimplifiedTv.text =
            SpannableString((context as DefinitionWordActivity).binding.definitionCharactersSimplifiedTv.text.toString())
    }

    private fun setOnBackPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            })
    }

    override fun onStop() {
        super.onStop()
        removeHighlights()
    }
}