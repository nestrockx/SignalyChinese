package com.wegielek.signalychinese.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.adapters.DefinitionListAdapter
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.databinding.FragmentDefinitionListBinding
import com.wegielek.signalychinese.interfaces.DefinitionListRecyclerViewListener
import com.wegielek.signalychinese.utils.Utils.Companion.showPopup
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel
import com.wegielek.signalychinese.views.DefinitionWordActivity
import java.util.Arrays

class DefinitionListFragment : Fragment(R.layout.fragment_definition_list),
    DefinitionListRecyclerViewListener {
    private var mBinding: FragmentDefinitionListBinding? = null
    private var mDefinitionListAdapter: DefinitionListAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentDefinitionListBinding.inflate(inflater, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val definitionViewModel = ViewModelProvider(requireActivity()).get(
            DefinitionViewModel::class.java
        )
        definitionViewModel.word.observe(
            viewLifecycleOwner
        ) { s: Dictionary ->
            mDefinitionListAdapter!!.setData(
                Arrays.asList(
                    *s.translation.split(
                        "/".toRegex()
                    ).dropLastWhile { it.isEmpty() }
                        .toTypedArray()))
        }
        mBinding!!.definitionListRv.layoutManager = LinearLayoutManager(context)
        mDefinitionListAdapter = DefinitionListAdapter(requireContext(), this)
        mBinding!!.definitionListRv.adapter = mDefinitionListAdapter
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

    override fun showPopup(v: View, text: String) {
        showPopup(
            v,
            text.trim { it <= ' ' },
            "pl",
            "en",
            (requireActivity() as DefinitionWordActivity).mDefinitionViewModel.ttsPL
        )
    }
}