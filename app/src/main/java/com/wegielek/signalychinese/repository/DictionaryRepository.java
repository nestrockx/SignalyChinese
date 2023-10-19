package com.wegielek.signalychinese.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.wegielek.signalychinese.database.AppDictionaryDatabase;
import com.wegielek.signalychinese.database.Dictionary;
import com.wegielek.signalychinese.database.DictionaryDao;
import com.wegielek.signalychinese.database.RadicalDao;
import com.wegielek.signalychinese.database.Radicals;

import java.util.List;

public class DictionaryRepository {

    private final DictionaryDao dictionaryDao;
    private final RadicalDao radicalDao;

    public DictionaryRepository(Application application) {
        dictionaryDao = AppDictionaryDatabase.getInstance(application).dictionaryDao();
        radicalDao = AppDictionaryDatabase.getInstance(application).radicalDao();
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

    public LiveData<List<Radicals>> getSection(String section) {
        return radicalDao.getSection(section);
    }

}
