package com.wegielek.signalychinese.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.common.util.concurrent.ListenableFuture

@Dao
interface FlashCardsDao {
    @Query("SELECT * FROM flash_cards WHERE `group` = :group")
    fun getFlashCardGroup(group: String): LiveData<List<FlashCards>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFlashCardGroup(vararg flashCards: FlashCards): ListenableFuture<Void?>

    @Query("DELETE FROM flash_cards WHERE `group` = :group")
    fun deleteFlashCardGroup(group: String): ListenableFuture<Void?>

    @Query("SELECT EXISTS(SELECT * FROM flash_cards WHERE traditional_sign = :traditional AND simplified_sign = :simplified AND pronunciation = :pronunciation)")
    fun getFlashCard(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFlashCardToSaved(flashCards: FlashCards): ListenableFuture<Void?>

    @Query("DELETE FROM flash_cards WHERE traditional_sign = :traditional AND simplified_sign = :simplified AND pronunciation = :pronunciation")
    fun deleteFlashCard(
        traditional: String,
        simplified: String,
        pronunciation: String
    ): ListenableFuture<Void?>
}
