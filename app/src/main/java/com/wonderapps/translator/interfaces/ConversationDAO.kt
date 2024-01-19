package com.wonderapps.translator.interfaces


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wonderapps.translator.model.ChatModel



@Dao
interface ConversationDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun insertData(chatModel: ChatModel)

    @Query("SELECT * FROM conversation_table")
     fun getData() : List<ChatModel>


    @Update
     fun updateData(chatModel: ChatModel)


    @Query("DELETE FROM conversation_table")
     fun deleteAllData()


    @Delete
     fun deleteSingleChatItem(chatModel: ChatModel)

}