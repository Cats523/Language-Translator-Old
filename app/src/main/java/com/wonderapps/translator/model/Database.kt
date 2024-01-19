package com.wonderapps.translator.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wonderapps.translator.interfaces.ConversationDAO



@Database  (entities = [ChatModel::class], version = 2, exportSchema = false)
abstract class LTDatabase : RoomDatabase() {

    abstract fun ConversationDAO() : ConversationDAO


    companion object {

        @Volatile
        private var INSTANCE: LTDatabase? = null

        fun getDatabase(context: Context): LTDatabase {

            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = buildDatabase(context)
                }
            }
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): LTDatabase {
            return Room.databaseBuilder(context.applicationContext, LTDatabase::class.java, "translator_app_database").build()
        }
    }

}