package com.wonderapps.translator.adapter



import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wonderapps.translator.databinding.FragmentConversionBinding
import com.wonderapps.translator.databinding.SampleLanguageChatItemBinding
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage


class ConversationLanguageAdapter(
    var context: Context,
    private var conversationLanguageList: List<ConversationLanguage>,
    private var pos: Int,
    var binding: FragmentConversionBinding,
    private var dialog: Dialog
) : RecyclerView.Adapter<ConversationLanguageAdapter.ViewHolder>() {


    class ViewHolder(val binding: SampleLanguageChatItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SampleLanguageChatItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.languageFlag.setImageResource(conversationLanguageList[position].languageFlag)
        holder.binding.languageName.text = conversationLanguageList[position].languageName

        holder.binding.languageName.text = conversationLanguageList[position].languageName

        holder.itemView.setOnClickListener {

            if (pos == 1) {
                binding.language1TextViewInConversations.text =
                    conversationLanguageList[position].languageName
                var firstCardLanguage = conversationLanguageList[position].languageName
                var firstLanguageVoiceCode = conversationLanguageList[position].languageVoiceCode
                var firstLanguageCode = conversationLanguageList[position].languageCode
                var firstLanguageFlag = conversationLanguageList[position].languageFlag
                val sharedPreferences1 =
                    context.getSharedPreferences("CHAT_LANGUAGE_PREFERENCE", Context.MODE_PRIVATE)
                val editor1 = sharedPreferences1.edit()

                editor1.putString("CHAT_FIRST_LANGUAGE_NAME", firstCardLanguage)
                editor1.putString("CHAT_FIRST_LANGUAGE_VOICE_CODE", firstLanguageVoiceCode)
                editor1.putString("CHAT_FIRST_LANGUAGE_CODE", firstLanguageCode)
                editor1.putInt("CHAT_FIRST_LANGUAGE_POSITION", position)
                editor1.putInt("CHAT_FIRST_LANGUAGE_FLAG", firstLanguageFlag)
                editor1.apply()
                binding.language1TextViewInConversations.text = firstCardLanguage
                dialog.dismiss()
            }
            if (pos == 2) {
                binding.language2TextViewInConversations.text =
                    conversationLanguageList[position].languageName
                val secondCardLanguage = conversationLanguageList[position].languageName
                val secondLanguageVoiceCode = conversationLanguageList[position].languageVoiceCode
                val secondLanguageCode = conversationLanguageList[position].languageCode

                val sharedPreferences1 =
                    context.getSharedPreferences("CHAT_LANGUAGE_PREFERENCE", Context.MODE_PRIVATE)
                val editor2 = sharedPreferences1.edit()
                editor2.putString("CHAT_SECOND_LANGUAGE_NAME", secondCardLanguage)
                editor2.putString("CHAT_SECOND_LANGUAGE_VOICE_CODE", secondLanguageVoiceCode)
                editor2.putString("CHAT_SECOND_LANGUAGE_CODE", secondLanguageCode)
                editor2.putInt("CHAT_SECOND_LANGUAGE_POSITION", position)
                editor2.apply()
                binding.language2TextViewInConversations.text = secondCardLanguage
                dialog.dismiss()
            }
//            dialog.cancel()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return conversationLanguageList.size
    }


}