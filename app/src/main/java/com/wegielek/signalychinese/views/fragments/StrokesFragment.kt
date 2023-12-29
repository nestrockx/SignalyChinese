package com.wegielek.signalychinese.views.fragments

import android.content.Intent
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
import com.wegielek.signalychinese.views.MainActivity
import com.wegielek.signalychinese.views.MainLearnStrokesCanvasView


class StrokesFragment : Fragment(R.layout.fragment_strokes) {
    private lateinit var binding: FragmentStrokesBinding
    private lateinit var mCharArray: CharArray
    private lateinit var mDefinitionViewModel: DefinitionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStrokesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDefinitionViewModel = (activity as DefinitionWordActivity).definitionViewModel
        val mainLearnStrokesCanvasView =
            view.findViewById<MainLearnStrokesCanvasView>(R.id.learnStrokesCanvasView)
        mainLearnStrokesCanvasView.setViewModel(mDefinitionViewModel)
        mDefinitionViewModel.word.observe(
            viewLifecycleOwner
        ) { s: Dictionary ->
            mCharArray = s.simplifiedSign.plus(s.traditionalSign).toSet().toCharArray()
            binding.characterIndex.text = getString(R.string.characters_number_placeholder,
                mDefinitionViewModel.index + 1, mCharArray.size)
            mainLearnStrokesCanvasView.setHanziCharacter(
                mCharArray[mDefinitionViewModel.index], true
            )
            setHighlights()
        }
        if (!isScreenRotated(requireContext())) mainLearnStrokesCanvasView.setDimensions(
            getScreenWidth(
                requireActivity()
            ) - dpToPixels(requireContext(), 64f)
        ) else mainLearnStrokesCanvasView.setDimensions(
            getScreenHeight(requireActivity()) - dpToPixels(requireContext(), 128f)
        )

        binding.restartPresentBtn.visibility = View.INVISIBLE
        binding.restartPresentBtn.setOnClickListener {
            mDefinitionViewModel.setCharacterMode(CharacterMode.PRESENTATION)
        }

        binding.restartDrawBtn.visibility = View.INVISIBLE
        binding.restartDrawBtn.setOnClickListener {
            mDefinitionViewModel.setCharacterMode(CharacterMode.LEARN)
        }

        mDefinitionViewModel.characterMode.observe(viewLifecycleOwner) {
            if (it === CharacterMode.PRESENTATION) {
                binding.restartDrawBtn.visibility = View.INVISIBLE
                binding.restartPresentBtn.visibility = View.VISIBLE
                binding.learnCBtn.text = getString(R.string.draw)
            } else if (it === CharacterMode.LEARN) {
                binding.restartDrawBtn.visibility = View.VISIBLE
                binding.restartPresentBtn.visibility = View.INVISIBLE
                binding.learnCBtn.text = getString(R.string.present)
            } else if (it === CharacterMode.NOT_FOUND) {
                binding.restartDrawBtn.visibility = View.INVISIBLE
            }
        }

        binding.learnCBtn.setOnClickListener {
            if (mDefinitionViewModel.getCharacterMode() === CharacterMode.PRESENTATION) {
                mDefinitionViewModel.setCharacterMode(CharacterMode.LEARN)
            } else if (mDefinitionViewModel.getCharacterMode() === CharacterMode.LEARN) {
                mDefinitionViewModel.setCharacterMode(CharacterMode.PRESENTATION)
            }
        }

        binding.nextCharacter.setOnClickListener {
            mDefinitionViewModel.index++
            if (mDefinitionViewModel.index < mCharArray.size) {
                mainLearnStrokesCanvasView.setHanziCharacter(
                    mCharArray[mDefinitionViewModel.index], true
                )
                setHighlights()
            } else {
                mDefinitionViewModel.index--
            }
            binding.characterIndex.text = getString(R.string.characters_number_placeholder, mDefinitionViewModel.index + 1, mCharArray.size)
        }

        binding.prevCharacter.setOnClickListener{
            mDefinitionViewModel.index--
            if (mDefinitionViewModel.index >= 0) {
                mainLearnStrokesCanvasView.setHanziCharacter(
                    mCharArray[mDefinitionViewModel.index], true
                )
                setHighlights()
            } else {
                mDefinitionViewModel.index++
            }
            binding.characterIndex.text = getString(R.string.characters_number_placeholder, mDefinitionViewModel.index + 1, mCharArray.size)
        }

        binding.searchCharBtn.setOnClickListener {
            val intent = Intent(it.context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("searchWord", mCharArray[mDefinitionViewModel.index].toString())
            it.context.startActivity(intent)
        }
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
            mCharArray[mDefinitionViewModel.index].toString(),
            requireContext().getColor(R.color.white_highlight)
        )
        setHighLightedText(
            (context as DefinitionWordActivity).binding.definitionCharactersTraditonalTv,
            mCharArray[mDefinitionViewModel.index].toString(),
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