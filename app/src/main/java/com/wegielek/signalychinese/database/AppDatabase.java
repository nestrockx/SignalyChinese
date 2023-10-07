package com.wegielek.signalychinese.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Dictionary.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "Dictionary.db";
    private static AppDatabase mInstance;

    public abstract DictionaryDao dictionaryDao();

    public static AppDatabase getInstance(final Context context) {
        if(mInstance == null) {
            synchronized (LOCK) {
                mInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                        .createFromAsset("dictionary.db").build();
            }
        }
        return mInstance;
    }
}
