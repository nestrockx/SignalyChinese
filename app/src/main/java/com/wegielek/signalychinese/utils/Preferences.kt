package com.wegielek.signalychinese.utils

import android.content.Context
import android.content.SharedPreferences
import com.wegielek.signalychinese.SignalyChineseApplication

class Preferences {

    companion object {
        private fun getPreferences(): SharedPreferences {
            return SignalyChineseApplication.instance.applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        }

        fun getTtsAlertShow(key: String?): Boolean {
            return getPreferences().getBoolean(key, false)
        }

        fun setTtsAlertShow(key: String?, value: Boolean) {
            getPreferences().edit().putBoolean(key, value).apply()
        }

        fun setMicLanguage(value: String?) {
            getPreferences().edit().putString("mic_language", value).apply()
        }

        fun getMicLanguage(): String? {
            return getPreferences().getString("mic_language", "zh-CN")
        }

        fun setSearchMode(value: String?) {
            getPreferences().edit().putString("search_mode", value).apply()
        }

        fun getSearchMode(): String? {
            return getPreferences().getString("search_mode", "normal")
        }

        fun isDefaultFlashCardGroupSetup(): Boolean {
            return getPreferences().getBoolean("flash_card_setup", false)
        }

        fun setDefaultFlashCardGroupSetup(value: Boolean) {
            getPreferences().edit().putBoolean("flash_card_setup", value).apply()
        }

        fun setFlashCardsReversed(reversed: Boolean) {
            getPreferences().edit().putBoolean("flash_cards_reversed", reversed).apply()
        }

        fun isFlashCardsReversed(): Boolean {
            return getPreferences().getBoolean("flash_cards_reversed", false)
        }

        fun setWritingCharacterType(value: String?) {
            getPreferences().edit().putString("writing_type", value).apply()
        }

        fun getWritingCharacterType(): String? {
            return getPreferences().getString("writing_type", "simplified")
        }
    }
}