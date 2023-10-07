package com.wegielek.signalychinese.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.wegielek.signalychinese.DictionaryApplication;
import com.wegielek.signalychinese.database.AppDatabase;
import com.wegielek.signalychinese.database.Dictionary;
import com.wegielek.signalychinese.database.DictionaryDao;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class DictionaryRepository {

    private DictionaryDao dictionaryDao;

    public DictionaryRepository(Application application) {
        dictionaryDao = AppDatabase.getInstance(application).dictionaryDao();
    }

    public LiveData<List<Dictionary>> getAllWords() {
        return dictionaryDao.getAllWords();
    }

    public LiveData<List<Dictionary>> searchByWord(String searchQuery) {
        return dictionaryDao.searchByWord(searchQuery);
    }

}
