package com.wegielek.signalychinese.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.enums.CharacterMode
import com.wegielek.signalychinese.repository.DictionaryRepository

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    val word = MutableLiveData<Dictionary>()
    private val mDictionaryRepository: DictionaryRepository

    init {
        word.value = Dictionary()
        mDictionaryRepository = DictionaryRepository(application)
    }

    fun getFlashCardsGroups(): LiveData<List<String>> {
        return mDictionaryRepository.getFlashCardsGroups()
    }

    fun deleteFlashCardGroup(group: String): ListenableFuture<Void?> {
        return mDictionaryRepository.deleteFlashCardGroup(group)
    }

}