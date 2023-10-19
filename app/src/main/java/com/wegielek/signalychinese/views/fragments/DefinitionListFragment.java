package com.wegielek.signalychinese.views.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.adapters.DefinitionListAdapter;
import com.wegielek.signalychinese.databinding.FragmentDefinitionListBinding;
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel;

import java.util.Arrays;

public class DefinitionListFragment extends Fragment {

    private FragmentDefinitionListBinding mBinding;

    private DefinitionListAdapter mDefinitionListAdapter;

    public DefinitionListFragment() {
        super(R.layout.fragment_definition_list);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentDefinitionListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DefinitionViewModel definitionViewModel = new ViewModelProvider(requireActivity()).get(DefinitionViewModel.class);
        definitionViewModel.word.observe(getViewLifecycleOwner(), s ->
                mDefinitionListAdapter.setData(Arrays.asList(Arrays.copyOfRange(s.split("/"), 4, s.split("/").length))));

        mBinding.definitionListRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mDefinitionListAdapter = new DefinitionListAdapter(getContext());
        mBinding.definitionListRv.setAdapter(mDefinitionListAdapter);



        onBackPressed();
    }

    private void onBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        });
    }

}