package com.wegielek.signalychinese.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.database.Sentences
import com.wegielek.signalychinese.enums.CharacterMode
import com.wegielek.signalychinese.repository.DictionaryRepository

class DefinitionViewModel(application: Application) : AndroidViewModel(application) {
    var wrapContentHeight: Int = 0
    var index: Int = 0
    var isAdjusted: Boolean = false
    var isSetup: Boolean = false
    val word = MutableLiveData<Dictionary>()
    val characterMode = MutableLiveData<CharacterMode>()
    private val lastMode = MutableLiveData<CharacterMode>()
    private val mDictionaryRepository: DictionaryRepository

    init {
        word.value = Dictionary()
        characterMode.value = CharacterMode.PRESENTATION
        mDictionaryRepository = DictionaryRepository(application)
    }

    fun findAllSentences(simplifiedWord: String, traditionalWord: String): ListenableFuture<List<Sentences>> {
        return mDictionaryRepository.findAllSentences(simplifiedWord, traditionalWord)
    }

    fun findSimplifiedSentences(word: String): ListenableFuture<List<Sentences>> {
        return mDictionaryRepository.findSimplifiedSentences(word)
    }

    fun findTraditionalSentences(word: String): ListenableFuture<List<Sentences>> {
        return mDictionaryRepository.findTraditionalSentences(word)
    }

    fun setLastMode(characterMode: CharacterMode) {
        lastMode.value = characterMode
    }

    fun getLastMode(): CharacterMode {
        return lastMode.value!!
    }

    fun setCharacterMode(characterMode: CharacterMode) {
        this.characterMode.value = characterMode
    }

    fun getCharacterMode(): CharacterMode {
        return characterMode.value!!
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
