package com.wegielek.signalychinese.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RadicalsDao {

    @Query("SELECT * FROM radicals WHERE section LIKE '%' || :section || '%'")
    LiveData<List<Radicals>> getRadicalsSection(String section);

}
