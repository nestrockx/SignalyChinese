package com.wegielek.signalychinese.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Radicals {
    @PrimaryKey(autoGenerate = true)
    var uid = 0

    @ColumnInfo(name = "section")
    var section = ""

    @ColumnInfo(name = "radicals")
    var radicals = ""
}