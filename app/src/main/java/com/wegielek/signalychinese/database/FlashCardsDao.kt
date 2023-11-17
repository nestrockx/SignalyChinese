package com.wegielek.signalychinese.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.common.util.concurrent.ListenableFuture

@Dao
interface FlashCardsDao {
    @Query("SELECT * FROM flash_cards WHERE `group` = :group ORDER BY RANDOM()")
    fun getFlashCardGroup(group: String): LiveData<List<FlashCards>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFlashCardGroup(vararg flashCards: FlashCards): ListenableFuture<Void?>

    @Query("DELETE FROM flash_cards WHERE `group` = :group")
    fun deleteFlashCardGroup(group: String): ListenableFuture<Void?>

    @Query("SELECT EXISTS(SELECT * FROM flash_cards WHERE traditional_sign = :traditional AND simplified_sign = :simplified AND pronunciation = :pronunciation)")
    fun isFlashCardExists(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFlashCardToGroup(flashCards: FlashCards): ListenableFuture<Void?>

    @Query("DELETE FROM flash_cards WHERE traditional_sign = :traditional AND simplified_sign = :simplified AND pronunciation = :pronunciation")
    fun deleteFlashCard(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Void?>

    @Query("DELETE FROM flash_cards WHERE `group` = :group AND traditional_sign = :traditional AND simplified_sign = :simplified AND pronunciation = :pronunciation")
    fun deleteFlashCardFromGroup(
        group: String,
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Void?>

    @Query("SELECT DISTINCT `group` FROM flash_cards")
    fun getFlashCardsGroups(): LiveData<List<String>>

    @Query("SELECT DISTINCT `group` FROM flash_cards")
    fun getFlashCardsGroupsNonObserve(): ListenableFuture<List<String>>

    @Query("SELECT `group` FROM flash_cards WHERE traditional_sign = :traditional AND simplified_sign = :simplified AND pronunciation = :pronunciation")
    fun getFlashCardGroups(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<List<String>>
}
