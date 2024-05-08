package com.wegielek.signalychinese.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sentences")
class Sentences {
    @PrimaryKey(autoGenerate = true)
    var uid = 0

    @ColumnInfo(name = "simplified_sign")
    var simplifiedSign = ""

    @ColumnInfo(name = "traditional_sign")
    var traditionalSign = ""

    @ColumnInfo(name = "pronunciation")
    var pronunciation = ""

    @ColumnInfo(name = "translation")
    var translation = ""
}