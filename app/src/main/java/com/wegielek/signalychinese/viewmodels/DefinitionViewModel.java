package com.wegielek.signalychinese.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class DefinitionViewModel extends AndroidViewModel {

    public MutableLiveData<String> word = new MutableLiveData<>();

    public DefinitionViewModel(@NonNull Application application) {
        super(application);
        word.setValue("");
    }

    public void setWord(String word) {
        this.word.setValue(word);
    }
}
