package com.wegielek.signalychinese.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.wegielek.signalychinese.database.AppDictionaryDatabase;
import com.wegielek.signalychinese.database.Dictionary;
import com.wegielek.signalychinese.database.DictionaryDao;
import com.wegielek.signalychinese.database.FlashCards;
import com.wegielek.signalychinese.database.FlashCardsDao;
import com.wegielek.signalychinese.database.SearchHistoryDao;
import com.wegielek.signalychinese.database.RadicalsDao;
import com.wegielek.signalychinese.database.Radicals;
import com.wegielek.signalychinese.database.SearchHistory;

import java.util.List;

public class DictionaryRepository {

    private final DictionaryDao dictionaryDao;
    private final RadicalsDao radicalsDao;
    private final SearchHistoryDao searchHistoryDao;
    private final FlashCardsDao flashCardsDao;

    public DictionaryRepository(Application application) {
        dictionaryDao = AppDictionaryDatabase.getInstance(application).dictionaryDao();
        radicalsDao = AppDictionaryDatabase.getInstance(application).radicalDao();
        searchHistoryDao = AppDictionaryDatabase.getInstance(application).historyDao();
        flashCardsDao = AppDictionaryDatabase.getInstance(application).flashCardsDao();
    }

    public LiveData<List<FlashCards>> getFlashCardGroup(String group) {
        return flashCardsDao.getFlashCardGroup(group);
    }

    public ListenableFuture<Void> addFlashCardGroup(FlashCards... flashCards) {
        return flashCardsDao.addFlashCardGroup(flashCards);
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

    public LiveData<List<Radicals>> getRadicalsSection(String section) {
        return radicalsDao.getRadicalsSection(section);
    }

    public LiveData<List<SearchHistory>> getWholeHistory() {
        return searchHistoryDao.getWholeHistory();
    }

    public ListenableFuture<Void> addHistoryRecord(SearchHistory searchHistory) {
        return searchHistoryDao.addHistoryRecord(searchHistory);
    }

}
