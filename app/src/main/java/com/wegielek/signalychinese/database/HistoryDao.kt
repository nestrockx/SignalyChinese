package com.wegielek.signalychinese.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.common.util.concurrent.ListenableFuture

@Dao
interface HistoryDao : RadicalsDao {
    @get:Query("SELECT * FROM search_history ORDER BY time DESC LIMIT 50")
    val wholeHistory: LiveData<List<History>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addHistoryRecord(history: History): ListenableFuture<Void?>

    @Query("DELETE FROM search_history")
    fun deleteWholeHistory(): ListenableFuture<Void?>
}