package com.wegielek.signalychinese.viewmodels

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.repository.DictionaryRepository
import java.util.Locale

class DefinitionViewModel(application: Application) : AndroidViewModel(application) {
    var word = MutableLiveData<Dictionary>()
    private val mDictionaryRepository: DictionaryRepository
    lateinit var ttsCH: TextToSpeech
    lateinit var ttsPL: TextToSpeech

    init {
        word.value = Dictionary()
        mDictionaryRepository = DictionaryRepository(application)
        ttsCH = TextToSpeech(
            application.applicationContext
        ) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                val langResult = ttsCH.isLanguageAvailable(Locale("zh_CN"))
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported")
                } else {
                    ttsCH.language = Locale("zh_CN")
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed")
            }
        }
        ttsPL = TextToSpeech(application.applicationContext) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                val langResult = ttsPL.isLanguageAvailable(Locale("pl_PL"))
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported")
                } else {
                    ttsPL.language = Locale("pl_PL")
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed")
            }
        }
    }

    fun setWord(word: Dictionary) {
        this.word.value = word
    }

    fun getFlashCard(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Boolean> {
        return mDictionaryRepository.getFlashCard(
            traditional, simplified,
            pronunciation
        )
    }

    fun addFlashCardToSaved(flashCards: FlashCards): ListenableFuture<Void?> {
        return mDictionaryRepository.addFlashCardToSaved(flashCards)
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
}
