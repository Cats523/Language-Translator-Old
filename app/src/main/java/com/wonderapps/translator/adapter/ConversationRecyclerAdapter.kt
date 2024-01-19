package com.wonderapps.translator.adapter

import TextToSpeechSingleton
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wonderapps.translator.R
import com.wonderapps.translator.databinding.FragmentConversionBinding
import com.wonderapps.translator.interfaces.ConversationInterface
import com.wonderapps.translator.model.ChatModel
import com.wonderapps.translator.model.LTDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.properties.Delegates


class ConversationRecyclerAdapter(
    var context: Context,
    var binding: FragmentConversionBinding,
    var chatsModelList: ArrayList<ChatModel>,
    var cardText: TextView,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ConversationInterface {

    val textToSpeech = TextToSpeechSingleton.textToSpeech

    var check by Delegates.notNull<Int>()
    var currentposition: Int = 0


    private val database by lazy { LTDatabase.getDatabase(context).ConversationDAO() }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        return when (viewType) {
            ChatModel.LAYOUT_ONE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.new_chat_item_left, parent, false)
                return ViewHolder1(view)
            }

            ChatModel.LAYOUT_TWO -> {
                val view2 = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_item_right, parent, false)
                return ViewHolder2(view2)
            }

            else -> {
                val view2 = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_item_right, parent, false)
                return ViewHolder2(view2)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatModel = chatsModelList[position]

        cardText = binding.bottomSheetInclude.cardText
        secondCardLanguage = binding.language2TextViewInConversations.text.toString()
        check = chatModel.viewType

        when (check) {
            ChatModel.LAYOUT_ONE -> {
                val viewHolder1 = holder as ViewHolder1
                viewHolder1.spokenMenu.setOnClickListener {
                    showPopupMenu(viewHolder1, position)
                }

//                handleTranslationText(viewHolder1, chatModel)

                sharedPreferences1 =
                    context.getSharedPreferences("CHAT_LANGUAGE_PREFERENCE", Context.MODE_PRIVATE)
                viewHolder1.textSpoken.text = chatModel.spokenText
                viewHolder1.textTranslation.text = chatModel.translatedText
                if (chatModel.lang2 == "Persian") {
                    viewHolder1.speak.visibility = View.GONE


                } else {
                    viewHolder1.speak.visibility = View.VISIBLE
                    viewHolder1.speak.setOnClickListener {
                        speakTranslatedText(
                            chatModel.lang2,
                            viewHolder1.textTranslation.text.toString(),
                            viewHolder1.speak,
                            viewHolder1.speakStop
                        )
                    }
                    viewHolder1.speakStop.setOnClickListener {
                        viewHolder1.speak.visibility = View.VISIBLE
                        viewHolder1.speakStop.visibility = View.GONE
                        textToSpeech.stop()

                    }
                }

            }

            ChatModel.LAYOUT_TWO -> {
                val viewHolder2 = holder as ViewHolder2
                viewHolder2.spokenMenu.setOnClickListener {
                    showPopupMenu(viewHolder2, position)
                }

//                handleSpokenText(viewHolder2, chatModel)

                sharedPreferences1 =
                    context.getSharedPreferences("CHAT_LANGUAGE_PREFERENCE", Context.MODE_PRIVATE)
                viewHolder2.textSpoken2.text = chatModel.spokenText.toString()
                viewHolder2.textTranslation2.text = chatModel.translatedText.toString()

                viewHolder2.speak.setOnClickListener {

                    speakTranslatedText(
                        chatModel.lang2,
                        viewHolder2.textTranslation2.text.toString(),
                        viewHolder2.speak,
                        viewHolder2.speakStop
                    )
                }
                viewHolder2.speakStop.setOnClickListener {
                    viewHolder2.speak.visibility = View.VISIBLE
                    viewHolder2.speakStop.visibility = View.GONE
                    textToSpeech.stop()

                }
            }
        }
    }

    private fun showPopupMenu(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val popupMenu = PopupMenu(context, viewHolder.itemView.findViewById(R.id.spoken_menu))
        popupMenu.inflate(R.menu.conversation_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.fullScreenMenuItem -> {
                    showFullScreenDialog(
                        chatsModelList[position].spokenText,
                        chatsModelList[position].translatedText
                    )
                    true
                }

                R.id.copyMenuItem -> {
                    copyToClipboard(viewHolder, position)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun handleTranslationText(viewHolder: ViewHolder1, chatModel: ChatModel) {
        if (chatModel.translatedText.contains("[")) {
            chatModel.translatedText = chatModel.translatedText.replace("[", "").replace("]", "")
            textToSpeech.setSpeechRate(0.7f)
//            textToSpeech.speak(
//                viewHolder.textTranslation.text.toString(),
//                TextToSpeech.QUEUE_FLUSH,
//                null,
//                ""
//            )
            addConversation(currentposition)
        }
    }

    private fun handleSpokenText(viewHolder: ViewHolder2, chatModel: ChatModel) {
        if (chatModel.spokenText.contains("[")) {
            chatModel.spokenText = chatModel.spokenText.replace("[", "").replace("]", "")
            textToSpeech.setSpeechRate(0.7f)
//            textToSpeech.speak(
//                viewHolder.textTranslation2.text.toString(),
//                TextToSpeech.QUEUE_FLUSH,
//                null,
//                ""
//            )
            addConversation(currentposition)
        }
    }

    private fun speakTranslatedText(
        language: String,
        text: String,
        speakBtn: ImageButton,
        speakStopBtn: ImageButton
    ) {
        val locale = when (language) {
            "Afrikaans" -> Locale.forLanguageTag("af")
            "Albanian" -> Locale.forLanguageTag("sq")
            "Arabic" -> Locale.forLanguageTag("ar")
            "Belarusian" -> Locale.forLanguageTag("be")
            "Bulgarian" -> Locale.forLanguageTag("bg")
            "Bengali" -> Locale.forLanguageTag("bn-BD")
            "Catalan" -> Locale.forLanguageTag("ca-ES")
            "Chinese" -> Locale.forLanguageTag("zh")
            "Croatian" -> Locale.forLanguageTag("hr-HR")
            "Czech" -> Locale.forLanguageTag("cs-CZ")
            "Danish" -> Locale.forLanguageTag("da-DK")
            "Dutch" -> Locale.forLanguageTag("nl-BE")
            "English" -> Locale.forLanguageTag("en")
            "Estonian" -> Locale.forLanguageTag("et-EE")
            "French" -> Locale.forLanguageTag("fr-FR")
            "Finnish" -> Locale.forLanguageTag("fi-FI")
            "German" -> Locale.forLanguageTag("de")
            "Georgian" -> Locale.forLanguageTag("ka-GE")
            "Greek" -> Locale.forLanguageTag("el-GR")
            "Galician" -> Locale.forLanguageTag("gl-ES")
            "Gujarati" -> Locale.forLanguageTag("gu-IN")
            "Hebrew" -> Locale.forLanguageTag("iw")
            "Hindi" -> Locale.forLanguageTag("hi")
            "Haitian Creole" -> Locale.forLanguageTag("ht")
            "Hungarian" -> Locale.forLanguageTag("hu-HU")
            "Indonesian" -> Locale.forLanguageTag("id-ID")
            "Icelandic" -> Locale.forLanguageTag("is-IS")
            "Irish" -> Locale.forLanguageTag("ga")
            "Italian" -> Locale.forLanguageTag("it")
            "Japanese" -> Locale.forLanguageTag("ja")
            "Kannada" -> Locale.forLanguageTag("kn-IN")
            "Korean" -> Locale.forLanguageTag("ko-KR")
            "Latvian" -> Locale.forLanguageTag("lv-LV")
            "Lithuanian" -> Locale.forLanguageTag("lt-LT")
            "Macedonian" -> Locale.forLanguageTag("mk-MK")
            "Malay" -> Locale.forLanguageTag("ms-MY")
            "Norwegian" -> Locale.forLanguageTag("no-NO")
            "Persian" -> Locale.forLanguageTag("fa-IR")
            "Polish" -> Locale.forLanguageTag("pl-PL")
            "Portuguese" -> Locale.forLanguageTag("pt-BR")
            "Romanian" -> Locale.forLanguageTag("ro-RO")
            "Russian" -> Locale.forLanguageTag("ru")
            "Slovak" -> Locale.forLanguageTag("sk-SK")
            "Slovenian" -> Locale.forLanguageTag("sl-SI")
            "Spanish" -> Locale.forLanguageTag("es-ES")
            "Swedish" -> Locale.forLanguageTag("sv-SE")
            "Swahili" -> Locale.forLanguageTag("sw-KE")
            "Tamil" -> Locale.forLanguageTag("ta-IN")
            "Telugu" -> Locale.forLanguageTag("te-IN")
            "Thai" -> Locale.forLanguageTag("th-TH")
            "Turkish" -> Locale.forLanguageTag("tk")
            "Ukrainian" -> Locale.forLanguageTag("uk-UA")
            "Urdu" -> Locale.forLanguageTag("ur-PK")
            "Vietnamese" -> Locale.forLanguageTag("vi-VN")
            else -> Locale.getDefault()
        }

        textToSpeech.language = locale

        textToSpeech.setSpeechRate(0.7f)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        val handler = Handler(Looper.getMainLooper())

        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // This method is called when text-to-speech starts speaking.
                handler.post {
                    speakBtn.visibility = View.GONE
                    speakStopBtn.visibility = View.VISIBLE
                }
            }

            override fun onDone(utteranceId: String?) {
                // This method is called when text-to-speech is done speaking.
                handler.post {
                    speakBtn.visibility = View.VISIBLE
                    speakStopBtn.visibility = View.GONE
                }
            }

            override fun onError(utteranceId: String?) {
                // Handle any errors that occur during speech
                Log.e("speakTranslatedText", "Speech error")
            }
        })


    }


    private fun showFullScreenDialog(spokenText: String, translatedText: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout, null)
        val textView = view.findViewById<TextView>(R.id.cardText)
        textView.text = "$spokenText\n$translatedText"

        val params = ViewGroup.LayoutParams(MATCH_PARENT, 800)
        val bottomSheetDialog = BottomSheetDialog(view.context)
        bottomSheetDialog.setContentView(view, params)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetDialog.show()
    }

    private fun copyToClipboard(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val textToCopy = when (viewHolder) {
            is ViewHolder1 -> viewHolder.textSpoken.text.toString() + viewHolder.textTranslation.text.toString()
            is ViewHolder2 -> viewHolder.textSpoken2.text.toString() + viewHolder.textTranslation2.text.toString()
            else -> ""
        }
        val clip = ClipData.newPlainText("label", textToCopy)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, R.string.CopiedToClipboard, Toast.LENGTH_LONG).show()
    }

    private fun addConversation(position: Int) {

        GlobalScope.launch(Dispatchers.IO) {
            val chatModel = chatsModelList[chatsModelList.size - 1]
            database.insertData(chatModel)
        }
    }


    override fun getItemCount(): Int {
        return chatsModelList.size
    }

    override fun getItemViewType(position: Int): Int {

        var check = chatsModelList[position].viewType

        when (check) {

            1 -> {
                return ChatModel.LAYOUT_ONE
            }

            2 -> {
                return ChatModel.LAYOUT_TWO
            }
        }
        return 100
    }


    class ViewHolder1(var itemView: View) : RecyclerView.ViewHolder(itemView) {

        var leftCard = itemView.findViewById<CardView>(R.id.left_card)
        var spokenMenu = itemView.findViewById<View>(R.id.spoken_menu)
        var textSpoken: TextView = itemView.findViewById(R.id.text_spoken)
        var textTranslation = itemView.findViewById<TextView>(R.id.text_translation)!!
        var flagTranslate = itemView.findViewById<ImageView>(R.id.translationLanguageFlag)
        var flagSpoken = itemView.findViewById<ImageView>(R.id.spoken_flag)
        var speak = itemView.findViewById<ImageButton>(R.id.speakBtnInConversation)
        var speakStop = itemView.findViewById<ImageButton>(R.id.speakBtnStopInConversation)

    }

    class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textSpoken2: TextView = itemView.findViewById<TextView>(R.id.text_spoken)
        var textTranslation2 = itemView.findViewById<TextView>(R.id.text_translation)!!
        var speak = itemView.findViewById<ImageButton>(R.id.speakBtnInConversation)
        var speakStop = itemView.findViewById<ImageButton>(R.id.stopPeakBtn)

        var spokenMenu = itemView.findViewById<View>(R.id.spoken_menu)
    }

    override lateinit var sharedPreferences1: SharedPreferences
    override lateinit var editor1: SharedPreferences.Editor
    override lateinit var editor2: SharedPreferences.Editor
    override lateinit var firstCardLanguage: String
    override lateinit var firstLanguageVoiceCode: String
    override lateinit var secondCardLanguage: String
    override lateinit var secondLanguageVoiceCode: String
    override var firstLanguageFlag by Delegates.notNull<Int>()
    override var secondLanguageFlag by Delegates.notNull<Int>()
    override lateinit var firstLanguageCode: String
    override lateinit var secondLanguageCode: String
    override var firstLanguagePosition by Delegates.notNull<Int>()
    override var secondLanguagePosition by Delegates.notNull<Int>()
}