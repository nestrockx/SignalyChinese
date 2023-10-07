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

    @Query("SELECT * FROM dictionary WHERE translation LIKE :searchQuery || ' %' OR translation LIKE '%/ ' || :searchQuery || ' %' ORDER BY LENGTH(traditional_sign) ASC")
    LiveData<List<Dictionary>> searchByWordPL(String searchQuery);


    /*
    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<Result> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    Result findByName(String first, String last);


    @Insert
    void insertAll(Dictionary... dictionaries);

    @Delete
    void delete(Dictionary dictionary);
    */
}