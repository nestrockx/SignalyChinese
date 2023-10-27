package com.wegielek.signalychinese.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flash_cards")
public class FlashCards {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @NonNull
    @ColumnInfo(name = "group")
    public String group = "";

    @NonNull
    @ColumnInfo(name = "traditional_sign")
    public String traditionalSign = "";

    @NonNull
    @ColumnInfo(name = "simplified_sign")
    public String simplifiedSign = "";

    @NonNull
    @ColumnInfo(name = "pronunciation")
    public String pronunciation = "";

    @NonNull
    @ColumnInfo(name = "pronunciation_phonetic")
    public String pronunciationPhonetic = "";

    @NonNull
    @ColumnInfo(name = "translation")
    public String translation = "";
}
