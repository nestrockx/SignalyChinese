package com.wegielek.signalychinese.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DictionaryDao {

    @Query("SELECT * FROM dictionary")
    LiveData<List<Dictionary>> getAllWords();

    @Query("SELECT * FROM dictionary " +
            "WHERE traditional_sign = :searchQuery OR simplified_sign = :searchQuery ORDER BY LENGTH(traditional_sign) ASC")
    LiveData<List<Dictionary>> searchSingleCH(String searchQuery);

    @Query("SELECT * FROM dictionary " +
            "WHERE traditional_sign LIKE :searchQuery || '%' OR simplified_sign LIKE :searchQuery || '%' ORDER BY LENGTH(traditional_sign) ASC")
    LiveData<List<Dictionary>> searchByWordCH(String searchQuery);

    @Query("SELECT * FROM dictionary WHERE translation LIKE :searchQuery || ' %' OR translation LIKE '%/ ' || :searchQuery || ' %' OR translation LIKE '%) ' || :searchQuery || ' %' OR translation LIKE '%. ' || :searchQuery || ' %' OR pronunciation_phonetic LIKE :searchQuery || ' %' ORDER BY LENGTH(traditional_sign) ASC")
    LiveData<List<Dictionary>> searchByWordPL(String searchQuery);

}