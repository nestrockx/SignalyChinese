package com.wegielek.signalychinese.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.google.common.util.concurrent.ListenableFuture
import com.wegielek.signalychinese.database.AppDictionaryDatabase.Companion.getInstance
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.DictionaryDao
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.database.FlashCardsDao
import com.wegielek.signalychinese.database.History
import com.wegielek.signalychinese.database.HistoryDao
import com.wegielek.signalychinese.database.Radicals
import com.wegielek.signalychinese.database.RadicalsDao

class DictionaryRepository(application: Application) {
    private val dictionaryDao: DictionaryDao
    private val radicalsDao: RadicalsDao
    private val historyDao: HistoryDao
    private val flashCardsDao: FlashCardsDao

    init {
        dictionaryDao = getInstance(application)?.dictionaryDao() ?: throw NullPointerException("Database instance not available")
        radicalsDao = getInstance(application)?.radicalDao() ?: throw NullPointerException("Database instance not available")
        historyDao = getInstance(application)?.historyDao() ?: throw NullPointerException("Database instance not available")
        flashCardsDao = getInstance(application)?.flashCardsDao() ?: throw NullPointerException("Database instance not available")
    }

    fun getFlashCardGroup(group: String): LiveData<List<FlashCards>> {
        return flashCardsDao.getFlashCardGroup(group)
    }

    fun addFlashCardGroup(vararg flashCards: FlashCards): ListenableFuture<Void?> {
        return flashCardsDao.addFlashCardGroup(*flashCards)
    }

    fun deleteFlashCardGroup(group: String): ListenableFuture<Void?> {
        return flashCardsDao.deleteFlashCardGroup(group)
    }

    fun getFlashCard(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Boolean> {
        return flashCardsDao.getFlashCard(traditional, simplified, pronunciation)
    }

    fun addFlashCardToSaved(flashCards: FlashCards): ListenableFuture<Void?> {
        return flashCardsDao.addFlashCardToSaved(flashCards)
    }

    fun deleteFlashCard(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Void?> {
        return flashCardsDao.deleteFlashCard(traditional, simplified, pronunciation)
    }

    fun searchSingleCH(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryDao.searchSingleCH(searchQuery)
    }

    fun searchByWordCH(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryDao.searchByWordCH(searchQuery)
    }

    fun searchByWordCHAll(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryDao.searchByWordCHAll(searchQuery)
    }

    fun searchByWordPL(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryDao.searchByWordPL(searchQuery)
    }

    fun searchByWordPLAll(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryDao.searchByWordPLAll(searchQuery)
    }

    fun getRadicalsSection(section: String): ListenableFuture<List<Radicals>> {
        return radicalsDao.getRadicalsSection(section)
    }

    val wholeHistory: LiveData<List<History>>
        get() = historyDao.wholeHistory

    fun addHistoryRecord(history: History): ListenableFuture<Void?> {
        return historyDao.addHistoryRecord(history)
    }

    fun deleteWholeHistory(): ListenableFuture<Void?> {
        return historyDao.deleteWholeHistory()
    }
}
