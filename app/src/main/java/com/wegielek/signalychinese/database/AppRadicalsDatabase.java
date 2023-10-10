package com.wegielek.signalychinese.database;

import android.content.Context;

import androidx.room.Room;

public class AppRadicalsDatabase {
    private static final String LOG_TAG = AppDictionaryDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "Dictionary.db";
    private static AppDictionaryDatabase mInstance;


    public static AppDictionaryDatabase getInstance(final Context context) {
        if(mInstance == null) {
            synchronized (LOCK) {
                mInstance = Room.databaseBuilder(context.getApplicationContext(), AppDictionaryDatabase.class, DATABASE_NAME)
                        .createFromAsset("radicals.db").build();
            }
        }
        return mInstance;
    }

}
