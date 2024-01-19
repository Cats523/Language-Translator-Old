package com.wonderapps.translator.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "conversation_table")
data class ChatModel(

    @ColumnInfo(name = "spoken_text")
    var spokenText : String,

    @ColumnInfo(name = "translated_text")
    var translatedText: String,

    @ColumnInfo(name = "view_type")
    val viewType : Int,

    @ColumnInfo(name = "Language_1")
    val lang1 : String,

    @ColumnInfo(name = "Language_2")
    val lang2 : String) {


    @PrimaryKey(autoGenerate = true)
    var id : Int = 0

    companion object{
        const val LAYOUT_ONE = 1
        const val LAYOUT_TWO = 2
    }
}