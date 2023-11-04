package com.wegielek.signalychinese.utils

import android.content.Context
import android.content.SharedPreferences

class Preferences {

    companion object {
        private fun getPreferences(context: Context, name: String): SharedPreferences {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }

        @JvmStatic fun getTtsAlertShow(context: Context, key: String?): Boolean {
            return getPreferences(context, "PREFS").getBoolean(key, false)
        }

        @JvmStatic fun setTtsAlertShow(context: Context, key: String?, value: Boolean) {
            getPreferences(context, "PREFS").edit().putBoolean(key, value).apply()
        }

        @JvmStatic fun setMicLanguage(context: Context, value: String?) {
            getPreferences(context, "PREFS").edit().putString("mic_language", value).apply()
        }

        @JvmStatic fun getMicLanguage(context: Context): String? {
            return getPreferences(context, "PREFS").getString("mic_language", "zh-CN")
        }

        @JvmStatic fun setSearchMode(context: Context, value: String?) {
            getPreferences(context, "PREFS").edit().putString("search_mode", value).apply()
        }

        @JvmStatic fun getSearchMode(context: Context): String? {
            return getPreferences(context, "PREFS").getString("search_mode", "normal")
        }
    }
}