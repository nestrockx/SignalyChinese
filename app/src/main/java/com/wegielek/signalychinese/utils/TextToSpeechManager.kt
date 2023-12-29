package com.wegielek.signalychinese.utils

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.SignalyChineseApplication
import com.wegielek.signalychinese.utils.Utils.Companion.removeHighlights
import com.wegielek.signalychinese.utils.Utils.Companion.setHighLightedText
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

    fun speakCH(textView: TextView, text: String, color: Int) {
        val range = "rangeCH"
        instanceCH.speak(text, TextToSpeech.QUEUE_FLUSH, null, range)
        instanceCH.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {

            }
            override fun onDone(utteranceId: String?) {
                removeHighlights(textView)
            }
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                if (utteranceId == range) {
                    removeHighlights(textView)
                    setHighLightedText(textView, text.substring(start, end), color)
                }
            }
            override fun onError(utteranceId: String?, errorCode: Int) {

            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {

            }
        })
    }

    fun speakPL(text: String) {
        instancePL.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speakPL(textView: TextView, text: String, color: Int) {
        val range = "rangePL"
        instancePL.speak(text, TextToSpeech.QUEUE_FLUSH, null, range)
        instancePL.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {

            }
            override fun onDone(utteranceId: String?) {
                removeHighlights(textView)
            }
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                if (utteranceId == range) {
                    removeHighlights(textView)
                    setHighLightedText(textView, text.substring(start, end), color)
                }
            }
            override fun onError(utteranceId: String?, errorCode: Int) {

            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {

            }
        })
    }

}