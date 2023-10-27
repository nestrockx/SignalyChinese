package com.wegielek.signalychinese.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static SharedPreferences getPreferences(Context context, String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static boolean getTtsAlertShow(Context context, String key) {
        return getPreferences(context, "PREFS").getBoolean(key, false);
    }

    public static void setTtsAlertShow(Context context, String key, boolean value) {
        getPreferences(context, "PREFS").edit().putBoolean(key, value).apply();
    }



}
