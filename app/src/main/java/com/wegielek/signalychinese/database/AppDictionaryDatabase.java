package com.wegielek.signalychinese.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Dictionary.class, Radicals.class, SearchHistory.class, FlashCards.class}, version = 1, exportSchema = false)
public abstract class AppDictionaryDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "dictionary.db";
    private static AppDictionaryDatabase mInstance;

    public abstract DictionaryDao dictionaryDao();

    public abstract RadicalsDao radicalDao();

    public abstract SearchHistoryDao historyDao();

    public abstract FlashCardsDao flashCardsDao();

    public static AppDictionaryDatabase getInstance(final Context context) {
        if(mInstance == null) {
            synchronized (LOCK) {
                mInstance = Room.databaseBuilder(context.getApplicationContext(), AppDictionaryDatabase.class, DATABASE_NAME)
                        .createFromAsset("dictionary.db").build();
            }
        }
        return mInstance;
    }
}
