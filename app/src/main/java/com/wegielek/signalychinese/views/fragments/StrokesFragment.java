package com.wegielek.signalychinese.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel;
import com.wegielek.signalychinese.views.LearnStrokesView;

import java.util.Arrays;

public class StrokesFragment extends Fragment {


    public StrokesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_strokes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DefinitionViewModel definitionViewModel = new ViewModelProvider(requireActivity()).get(DefinitionViewModel.class);
        LearnStrokesView learnStrokesView = view.findViewById(R.id.learnStrokesView);

        definitionViewModel.word.observe(getViewLifecycleOwner(), s -> {
            learnStrokesView.setHanziCharacter(s.split("/")[1].charAt(0));
        });



    }
}