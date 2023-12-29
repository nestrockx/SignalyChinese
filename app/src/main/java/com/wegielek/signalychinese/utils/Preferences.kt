package com.wegielek.signalychinese.utils

import android.content.Context
import android.content.SharedPreferences
import com.wegielek.signalychinese.SignalyChineseApplication

class Preferences {

    companion object {
        private fun getPreferences(name: String): SharedPreferences {
            return SignalyChineseApplication.instance.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        }

        fun getTtsAlertShow(key: String?): Boolean {
            return getPreferences("PREFS").getBoolean(key, false)
        }

        fun setTtsAlertShow(key: String?, value: Boolean) {
            getPreferences("PREFS").edit().putBoolean(key, value).apply()
        }

        fun setMicLanguage(value: String?) {
            getPreferences("PREFS").edit().putString("mic_language", value).apply()
        }

        fun getMicLanguage(): String? {
            return getPreferences("PREFS").getString("mic_language", "zh-CN")
        }

        fun setSearchMode(value: String?) {
            getPreferences("PREFS").edit().putString("search_mode", value).apply()
        }

        fun getSearchMode(): String? {
            return getPreferences("PREFS").getString("search_mode", "normal")
        }

        fun isDefaultFlashCardGroupSetup(): Boolean {
            return getPreferences("PREFS").getBoolean("flash_card_setup", false)
        }

        fun setDefaultFlashCardGroupSetup(value: Boolean) {
            getPreferences("PREFS").edit().putBoolean("flash_card_setup", value).apply()
        }

        fun setFlashCardsReversed(reversed: Boolean) {
            getPreferences("PREFS").edit().putBoolean("flash_cards_reversed", reversed).apply()
        }

        fun isFlashCardsReversed(): Boolean {
            return getPreferences("PREFS").getBoolean("flash_cards_reversed", false)
        }

        fun setWritingCharacterType(value: String?) {
            getPreferences("PREFS").edit().putString("writing_type", value).apply()
        }

        fun getWritingCharacterType(): String? {
            return getPreferences("PREFS").getString("writing_type", "simplified")
        }
    }
}