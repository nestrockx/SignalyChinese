package com.wegielek.signalychinese.database

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary")
class Dictionary : Parcelable {
    @PrimaryKey(autoGenerate = true)
    public var uid = 0

    @ColumnInfo(name = "traditional_sign")
    var traditionalSign: String

    @ColumnInfo(name = "simplified_sign")
    var simplifiedSign: String

    @ColumnInfo(name = "pronunciation")
    var pronunciation: String

    @ColumnInfo(name = "pronunciation_phonetic")
    var pronunciationPhonetic: String

    @ColumnInfo(name = "translation")
    var translation: String
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(uid)
        dest.writeString(traditionalSign)
        dest.writeString(simplifiedSign)
        dest.writeString(pronunciation)
        dest.writeString(pronunciationPhonetic)
        dest.writeString(translation)
    }

    constructor() {
        traditionalSign = ""
        simplifiedSign = ""
        pronunciation = ""
        pronunciationPhonetic = ""
        translation = ""
    }

    @Ignore
    constructor(
        uid: Int,
        traditionalSign: String,
        simplifiedSign: String,
        pronunciation: String,
        pronunciationPhonetic: String,
        translation: String
    ) {
        this.uid = uid
        this.traditionalSign = traditionalSign
        this.simplifiedSign = simplifiedSign
        this.pronunciation = pronunciation
        this.pronunciationPhonetic = pronunciationPhonetic
        this.translation = translation
    }

    companion object {
        @JvmField
        val CREATOR: Creator<Dictionary?> = object : Creator<Dictionary?> {
            override fun createFromParcel(inx: Parcel): Dictionary {
                return Dictionary(
                    inx.readInt(),
                    inx.readString()!!,
                    inx.readString()!!,
                    inx.readString()!!,
                    inx.readString()!!,
                    inx.readString()!!
                )
            }

            override fun newArray(size: Int): Array<Dictionary?> {
                return arrayOfNulls(size)
            }
        }
    }
}


