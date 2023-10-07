package com.wegielek.signalychinese.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DictionaryDao {

    @Query("SELECT * FROM dictionary")
    LiveData<List<Dictionary>> getAllWords();

    @Query("SELECT * FROM dictionary " +
            "WHERE traditional_sign LIKE '%' || :searchQuery || '%' OR simplified_sign LIKE '%' || :searchQuery || '%' OR translation LIKE '%' || :searchQuery || '%'")
    LiveData<List<Dictionary>> searchByWord(String searchQuery);

    /*
    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<Result> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    Result findByName(String first, String last);
    */

    @Insert
    void insertAll(Dictionary... dictionaries);

    @Delete
    void delete(Dictionary dictionary);

}