package com.wegielek.signalychinese.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
class History {
    @PrimaryKey(autoGenerate = true)
    var uid = 0

    @ColumnInfo(name = "time")
    var time = ""

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

