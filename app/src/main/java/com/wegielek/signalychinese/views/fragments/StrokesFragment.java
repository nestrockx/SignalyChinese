package com.wegielek.signalychinese.views.fragments;

import static com.wegielek.signalychinese.utils.Utils.dpToPixels;
import static com.wegielek.signalychinese.utils.Utils.getScreenHeight;
import static com.wegielek.signalychinese.utils.Utils.getScreenWidth;
import static com.wegielek.signalychinese.utils.Utils.isScreenRotated;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.databinding.FragmentStrokesBinding;
import com.wegielek.signalychinese.enums.CharacterMode;
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel;
import com.wegielek.signalychinese.views.LearnStrokesCanvasView;

public class StrokesFragment extends Fragment {

    private FragmentStrokesBinding mBinding;

    public StrokesFragment() {
        super(R.layout.fragment_strokes);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentStrokesBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DefinitionViewModel definitionViewModel = new ViewModelProvider(requireActivity()).get(DefinitionViewModel.class);
        LearnStrokesCanvasView learnStrokesCanvasView = view.findViewById(R.id.learnStrokesCanvasView);

        definitionViewModel.word.observe(getViewLifecycleOwner(), s -> learnStrokesCanvasView.setHanziCharacter(s.split("/")[1].charAt(0)));

        if (!isScreenRotated(requireContext()))
            learnStrokesCanvasView.setDimensions(getScreenWidth(requireActivity()) - dpToPixels(requireContext(), 32));
        else
            learnStrokesCanvasView.setDimensions(getScreenHeight(requireActivity()) - dpToPixels(requireContext(), 128));

        mBinding.presentBtn.setOnClickListener(v ->
                learnStrokesCanvasView.setMode(CharacterMode.PRESENTATION)
        );

        mBinding.learnCBtn.setOnClickListener(v ->
                learnStrokesCanvasView.setMode(CharacterMode.LEARN)
        );

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