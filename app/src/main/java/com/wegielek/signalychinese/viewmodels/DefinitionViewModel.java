package com.wegielek.signalychinese.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.wegielek.signalychinese.repository.DictionaryRepository;

public class DefinitionViewModel extends AndroidViewModel {

    public MutableLiveData<String> word = new MutableLiveData<>();

    private final DictionaryRepository mDictionaryRepository;

    public DefinitionViewModel(@NonNull Application application) {
        super(application);
        word.setValue("");
        mDictionaryRepository = new DictionaryRepository(application);
    }

    public void setWord(String word) {
        this.word.setValue(word);
    }

}
