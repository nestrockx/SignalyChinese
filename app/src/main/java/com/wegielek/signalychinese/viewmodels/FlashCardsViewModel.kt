package com.wegielek.signalychinese.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.repository.DictionaryRepository

class FlashCardsViewModel(application: Application) : AndroidViewModel(application) {
    private val mDictionaryRepository: DictionaryRepository

    val flashCardsList: MutableLiveData<List<FlashCards>>
    val currentIndex: MutableLiveData<Int>

    init {
        mDictionaryRepository = DictionaryRepository(application)
        flashCardsList = MutableLiveData()
        currentIndex = MutableLiveData()
        currentIndex.value = 0
    }

    fun increaseIndex(): Boolean {
        return if (currentIndex.value!! < flashCardsList.value!!.size - 1) {
            currentIndex.value = currentIndex.value?.plus(1)
            true
        } else {
            false
        }
    }

    fun decreaseIndex(): Boolean {
        return if (currentIndex.value!! > 0) {
            currentIndex.value = currentIndex.value?.minus(1)
            true
        } else {
            false
        }
    }

    fun getFlashCardGroup(group: String): LiveData<List<FlashCards>> {
        return mDictionaryRepository.getFlashCardGroup(group)
    }

    fun getFlashCardsList(): List<FlashCards>? {
        return flashCardsList.value
    }

    fun setFlashCardsList(flashCards: List<FlashCards>) {
        flashCardsList.value = flashCards
    }


}