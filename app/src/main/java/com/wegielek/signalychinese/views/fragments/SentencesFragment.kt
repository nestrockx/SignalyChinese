package com.wegielek.signalychinese.views.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.wegielek.signalychinese.adapters.SentencesAdapter
import com.wegielek.signalychinese.database.Sentences
import com.wegielek.signalychinese.databinding.FragmentSentencesBinding
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel
import com.wegielek.signalychinese.views.DefinitionWordActivity

class SentencesFragment : Fragment() {

    private lateinit var binding: FragmentSentencesBinding
    private lateinit var mSentencesAdapter: SentencesAdapter
    private lateinit var mDefinitionViewModel: DefinitionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSentencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDefinitionViewModel = (activity as DefinitionWordActivity).definitionViewModel

        binding.sentencesRv.layoutManager = LinearLayoutManager(context)
        mSentencesAdapter = SentencesAdapter(requireContext())
        binding.sentencesRv.adapter = mSentencesAdapter

        val future = mDefinitionViewModel.findSimplifiedSentences(
            mDefinitionViewModel.word.value!!.simplifiedSign
        )
        Futures.addCallback<List<Sentences>>(future, object : FutureCallback<List<Sentences>> {
            override fun onSuccess(result: List<Sentences>) {
                if (result.isNotEmpty()) {
                    mSentencesAdapter.setData(result)
                    binding.sentencesPb.visibility = View.INVISIBLE
                } else {
                    Toast.makeText(context, "No sentences", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(t: Throwable) {
                t.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))

        setOnBackPressed()
    }

    private fun setOnBackPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }
}