package com.wegielek.signalychinese.utils

import android.speech.tts.TextToSpeech
import android.util.Log
import com.wegielek.signalychinese.SignalyChineseApplication
import java.util.Locale

object TextToSpeechManager {

    private lateinit var ttsCH: TextToSpeech
    private lateinit var ttsPL: TextToSpeech

    val instanceCH: TextToSpeech by lazy {
        ttsCH = TextToSpeech(SignalyChineseApplication.instance.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val langResult = ttsCH.isLanguageAvailable(Locale("zh_CN"))
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported")
                } else {
                    ttsCH.language = Locale("zh_CN")
                }
            } else {
                throw IllegalStateException("TextToSpeech initialization failed.")
            }
        }
        ttsCH
    }

    val instancePL: TextToSpeech by lazy {
        ttsPL = TextToSpeech(SignalyChineseApplication.instance.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val langResult = ttsPL.isLanguageAvailable(Locale("pl_PL"))
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported")
                } else {
                    ttsPL.language = Locale("pl_PL")
                }
            } else {
                throw IllegalStateException("TextToSpeech initialization failed.")
            }
        }
        ttsPL
    }

    fun speakCH(text: String) {
        instanceCH.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speakPL(text: String) {
        instancePL.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}