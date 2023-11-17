package com.wegielek.signalychinese.utils

import android.content.Context
import android.content.SharedPreferences

class Preferences {

    companion object {
        private fun getPreferences(context: Context, name: String): SharedPreferences {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }

        fun getTtsAlertShow(context: Context, key: String?): Boolean {
            return getPreferences(context, "PREFS").getBoolean(key, false)
        }

        fun setTtsAlertShow(context: Context, key: String?, value: Boolean) {
            getPreferences(context, "PREFS").edit().putBoolean(key, value).apply()
        }

        fun setMicLanguage(context: Context, value: String?) {
            getPreferences(context, "PREFS").edit().putString("mic_language", value).apply()
        }

        fun getMicLanguage(context: Context): String? {
            return getPreferences(context, "PREFS").getString("mic_language", "zh-CN")
        }

        fun setSearchMode(context: Context, value: String?) {
            getPreferences(context, "PREFS").edit().putString("search_mode", value).apply()
        }

        fun getSearchMode(context: Context): String? {
            return getPreferences(context, "PREFS").getString("search_mode", "normal")
        }

        fun isDefaultFlashCardGroupSetup(context: Context): Boolean {
            return getPreferences(context, "PREFS").getBoolean("flash_card_setup", false)
        }

        fun setDefaultFlashCardGroupSetup(context: Context, value: Boolean) {
            getPreferences(context, "PREFS").edit().putBoolean("flash_card_setup", value).apply()
        }

        fun setFlashCardsReversed(context: Context, reversed: Boolean) {
            getPreferences(context, "PREFS").edit().putBoolean("flash_cards_reversed", reversed).apply()
        }

        fun isFlashCardsReversed(context: Context): Boolean {
            return getPreferences(context, "PREFS").getBoolean("flash_cards_reversed", false)
        }
    }
}