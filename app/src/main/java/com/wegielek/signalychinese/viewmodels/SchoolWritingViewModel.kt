package com.wegielek.signalychinese.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.enums.CharacterMode
import com.wegielek.signalychinese.repository.DictionaryRepository

class SchoolWritingViewModel(application: Application) : AndroidViewModel(application) {
    val flashCardsList: MutableLiveData<List<FlashCards>>
    val currentIndex: MutableLiveData<Int>
    val charArrayIndex: MutableLiveData<Int>
    val characterMode = MutableLiveData<CharacterMode>()
    val isContinue: MutableLiveData<Boolean>
    private val lastMode = MutableLiveData<CharacterMode>()
    private val mDictionaryRepository: DictionaryRepository

    init {
        mDictionaryRepository = DictionaryRepository(application)
        characterMode.value = CharacterMode.TEST
        lastMode.value = CharacterMode.TEST
        flashCardsList = MutableLiveData()
        currentIndex = MutableLiveData()
        charArrayIndex = MutableLiveData()
        isContinue = MutableLiveData()
        currentIndex.value = 0
        charArrayIndex.value = 0
        isContinue.value = false
    }

    fun getCharArrayIndex(): Int {
        return charArrayIndex.value!!
    }

    fun increaseCharArrayIndex(charArray: CharArray): Boolean {
        return if (charArrayIndex.value!! < charArray.size - 1) {
            charArrayIndex.value = charArrayIndex.value?.plus(1)
            true
        } else {
            false
        }
    }

    fun decreaseCharArrayIndex(): Boolean {
        return if (charArrayIndex.value!! > 0) {
            charArrayIndex.value = charArrayIndex.value?.minus(1)
            true
        } else {
            false
        }
    }

    fun getCurrentIndex(): Int {
        return currentIndex.value!!
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

}