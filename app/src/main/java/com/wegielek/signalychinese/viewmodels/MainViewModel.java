package com.wegielek.signalychinese.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainViewModel extends ViewModel {

    public MutableLiveData<List<String>> dictionaryResultsList = new MutableLiveData<>();

    public MainViewModel() {
        dictionaryResultsList.setValue(Collections.emptyList());
    }

    public void addResults(List<String> searchResults) {
        dictionaryResultsList.setValue(searchResults);
    }


}
