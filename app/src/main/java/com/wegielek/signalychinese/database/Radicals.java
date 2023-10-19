package com.wegielek.signalychinese.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Radicals {
    @PrimaryKey
    public int uid;

    @NonNull
    @ColumnInfo(name = "section")
    public String section = "";

    @NonNull
    @ColumnInfo(name = "radicals")
    public String radicals = "";
}
