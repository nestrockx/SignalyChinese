package com.wegielek.signalychinese.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.adapters.DefinitionAdapter
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.databinding.FragmentDefinitionListBinding
import com.wegielek.signalychinese.interfaces.DefinitionListRecyclerViewListener
import com.wegielek.signalychinese.utils.Utils.Companion.showPopup
import com.wegielek.signalychinese.views.DefinitionWordActivity

class DefinitionListFragment : Fragment(R.layout.fragment_definition_list),
    DefinitionListRecyclerViewListener {
    private lateinit var binding: FragmentDefinitionListBinding
    private lateinit var mDefinitionAdapter: DefinitionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDefinitionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val definitionViewModel = (activity as DefinitionWordActivity).definitionViewModel
        definitionViewModel.word.observe(
            viewLifecycleOwner
        ) { dictionary: Dictionary ->
            mDefinitionAdapter.setData(
                listOf(
                    *dictionary.translation.split(
                        "/".toRegex()
                    ).dropLastWhile { it.isEmpty() }
                        .toTypedArray()))
        }
        binding.definitionListRv.layoutManager = LinearLayoutManager(context)
        mDefinitionAdapter = DefinitionAdapter(requireContext(), this)
        binding.definitionListRv.adapter = mDefinitionAdapter
    }

    override fun onResume() {
        super.onResume()
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

    override fun showPopup(v: View, tv: TextView, text: String) {
        showPopup(
            v,
            tv,
            text.trim { it <= ' ' },
            "pl",
            "en",
            ContextCompat.getColor(requireContext(), R.color.selection_color)
        )
    }
}