package com.wegielek.signalychinese.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flash_cards")
class FlashCards {
    @PrimaryKey(autoGenerate = true)
    var uid = 0

    @ColumnInfo(name = "group")
    var group = ""

    @ColumnInfo(name = "traditional_sign")
    var traditionalSign = ""

    @ColumnInfo(name = "simplified_sign")
    var simplifiedSign = ""

    @ColumnInfo(name = "pronunciation")
    var pronunciation = ""

    @ColumnInfo(name = "pronunciation_phonetic")
    var pronunciationPhonetic = ""

    @ColumnInfo(name = "translation")
    var translation = ""
}
