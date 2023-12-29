package com.wegielek.signalychinese.database

import androidx.room.Dao
import androidx.room.Query
import com.google.common.util.concurrent.ListenableFuture

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionary WHERE traditional_sign = :searchQuery OR simplified_sign = :searchQuery ORDER BY LENGTH(traditional_sign) ASC")
    fun searchSingleCH(searchQuery: String): ListenableFuture<List<Dictionary>>

    @Query("SELECT * FROM dictionary WHERE traditional_sign LIKE :searchQuery || '%' OR simplified_sign LIKE :searchQuery || '%' ORDER BY LENGTH(traditional_sign) ASC")
    fun searchByWordCH(searchQuery: String): ListenableFuture<List<Dictionary>>

    @Query("SELECT * FROM dictionary WHERE translation LIKE :searchQuery || ' %' OR translation LIKE '%/ ' || :searchQuery || ' %' OR translation LIKE '%) ' || :searchQuery || ' %' OR translation LIKE '%. ' || :searchQuery || ' %' OR pronunciation_phonetic LIKE :searchQuery || ' %' OR pronunciation LIKE :searchQuery || ' %' ORDER BY LENGTH(traditional_sign) ASC")
    fun searchByWordPL(searchQuery: String): ListenableFuture<List<Dictionary>>

    @Query("SELECT * FROM dictionary WHERE traditional_sign LIKE '%' || :searchQuery || '%' OR simplified_sign LIKE '%' || :searchQuery || '%' ORDER BY LENGTH(traditional_sign) ASC")
    fun searchByWordCHAll(searchQuery: String): ListenableFuture<List<Dictionary>>

    @Query("SELECT * FROM dictionary WHERE translation LIKE '%' || :searchQuery || '%' OR pronunciation_phonetic LIKE '%' || :searchQuery || '%' OR pronunciation LIKE '%' || :searchQuery || '%' ORDER BY LENGTH(traditional_sign) ASC")
    fun searchByWordPLAll(searchQuery: String): ListenableFuture<List<Dictionary>>
}