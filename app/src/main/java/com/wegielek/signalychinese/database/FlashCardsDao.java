package com.wegielek.signalychinese.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface FlashCardsDao {

    @Query("SELECT * FROM flash_cards WHERE `group` = :group")
    LiveData<List<FlashCards>> getFlashCardGroup(String group);

    @Insert
    ListenableFuture<Void> addFlashCardGroup(FlashCards... flashCards);

}
