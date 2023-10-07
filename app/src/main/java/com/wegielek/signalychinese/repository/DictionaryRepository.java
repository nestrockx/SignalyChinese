package com.wegielek.signalychinese.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.wegielek.signalychinese.database.AppDictionaryDatabase;
import com.wegielek.signalychinese.database.Dictionary;
import com.wegielek.signalychinese.database.DictionaryDao;

import java.util.List;

public class DictionaryRepository {

    private DictionaryDao dictionaryDao;

    public DictionaryRepository(Application application) {
        dictionaryDao = AppDictionaryDatabase.getInstance(application).dictionaryDao();
    }

    public LiveData<List<Dictionary>> getAllWords() {
        return dictionaryDao.getAllWords();
    }

    public LiveData<List<Dictionary>> searchSingleCH(String searchQuery) {
        return dictionaryDao.searchSingleCH(searchQuery);
    }

    public LiveData<List<Dictionary>> searchByWordCH(String searchQuery) {
        return dictionaryDao.searchByWordCH(searchQuery);
    }

    public LiveData<List<Dictionary>> searchByWordPL(String searchQuery) {
        return dictionaryDao.searchByWordPL(searchQuery);
    }

}
