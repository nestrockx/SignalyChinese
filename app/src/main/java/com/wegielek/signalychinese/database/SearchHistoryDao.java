package com.wegielek.signalychinese.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY time DESC LIMIT 50")
    LiveData<List<SearchHistory>> getWholeHistory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Void> addHistoryRecord(SearchHistory searchHistory);


}
