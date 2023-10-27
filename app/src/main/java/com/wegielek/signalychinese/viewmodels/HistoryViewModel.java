package com.wegielek.signalychinese.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.wegielek.signalychinese.database.SearchHistory;
import com.wegielek.signalychinese.repository.DictionaryRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final DictionaryRepository mDictionaryRepository;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        mDictionaryRepository = new DictionaryRepository(application);
    }

    public LiveData<List<SearchHistory>> getWholeHistory() {
        return mDictionaryRepository.getWholeHistory();
    }


}
