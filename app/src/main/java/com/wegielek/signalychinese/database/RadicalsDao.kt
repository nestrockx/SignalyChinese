package com.wegielek.signalychinese.database

import androidx.room.Dao
import androidx.room.Query
import com.google.common.util.concurrent.ListenableFuture

@Dao
interface RadicalsDao {
    @Query("SELECT * FROM radicals WHERE section LIKE '%' || :section || '%'")
    fun getRadicalsSection(section: String): ListenableFuture<List<Radicals>>

}