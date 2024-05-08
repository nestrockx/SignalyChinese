package com.wegielek.signalychinese.database

import androidx.room.Dao
import androidx.room.Query
import com.google.common.util.concurrent.ListenableFuture

@Dao
interface SentencesDao {

    @Query("SELECT * FROM sentences WHERE simplified_sign LIKE '%' || :word || '%' ORDER BY LENGTH(simplified_sign) ASC")
    fun findSimplifiedSentences(word: String): ListenableFuture<List<Sentences>>

    @Query("SELECT * FROM sentences WHERE traditional_sign LIKE '%' || :word || '%' ORDER BY LENGTH(traditional_sign) ASC")
    fun findTraditionalSentences(word: String): ListenableFuture<List<Sentences>>

    @Query("SELECT * FROM sentences WHERE traditional_sign LIKE '%' || :traditionalWord || '%' OR simplified_sign LIKE '%' || :simplifiedWord || '%' ORDER BY LENGTH(traditional_sign) ASC")
    fun findAllSentences(simplifiedWord: String, traditionalWord: String): ListenableFuture<List<Sentences>>

}