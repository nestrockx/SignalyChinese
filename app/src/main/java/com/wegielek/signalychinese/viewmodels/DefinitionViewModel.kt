package com.wegielek.signalychinese.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.enums.CharacterMode
import com.wegielek.signalychinese.repository.DictionaryRepository

class DefinitionViewModel(application: Application) : AndroidViewModel(application) {
    var characterMode = CharacterMode.PRESENTATION
    var word = MutableLiveData<Dictionary>()
    var wrapContentHeight: Int = 0
    var index: Int = 0
    var isAdjusted: Boolean = false
    var isSetup: Boolean = false
    private val mDictionaryRepository: DictionaryRepository

    init {
        word.value = Dictionary()
        mDictionaryRepository = DictionaryRepository(application)
    }

    fun setWord(word: Dictionary) {
        this.word.value = word
    }

    fun isFlashCardExists(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Boolean> {
        return mDictionaryRepository.isFlashCardExists(
            traditional, simplified,
            pronunciation
        )
    }

    fun addFlashCardToGroup(flashCards: FlashCards): ListenableFuture<Void?> {
        return mDictionaryRepository.addFlashCardToGroup(flashCards)
    }

    fun deleteFlashCard(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Void?> {
        return mDictionaryRepository.deleteFlashCard(
            traditional, simplified,
            pronunciation
        )
    }

    fun deleteFlashCardFromGroup(
        group: String,
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Void?> {
        return mDictionaryRepository.deleteFlashCardFromGroup(
            group, traditional, simplified, pronunciation
        )
    }

    fun getFlashCardsGroupsNonObserve(): ListenableFuture<List<String>> {
        return mDictionaryRepository.getFlashCardsGroupsNonObserve()
    }

    fun getFlashCardGroups(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<List<String>> {
        return mDictionaryRepository.getFlashCardGroups(traditional, simplified, pronunciation)
    }
}
