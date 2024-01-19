package com.wonderapps.translator.adapter


import android.app.Dialog
import android.content.Context
import android.speech.tts.TextToSpeech

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wonderapps.translator.R
import com.wonderapps.translator.databinding.ActivityOcrtranslationBinding
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI

class OCRTranslateAdapter(
    val context: Context,
    private val list: ArrayList<ConversationLanguage>,
    private var pos: Int,
    private var dialog: Dialog,
    var binding: ActivityOcrtranslationBinding,
    private var textToSpeech: TextToSpeech
) : RecyclerView.Adapter<OCRTranslateAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flagItem: ImageView = itemView.findViewById(R.id.languageFlag)
        val languageName: TextView = itemView.findViewById(R.id.languageName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.custom_recycler_language_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.languageName.text = list[position].languageName
        holder.flagItem.setImageResource(list[position].languageFlag)
        holder.itemView.setOnClickListener {

            if (pos == 1) {
                binding.language1Name.text = list[position].languageName
                dialog.cancel()
            }
            if (pos == 2) {
                binding.language2Name.text = list[position].languageName
                dialog.cancel()
                translateOnline(binding.editTextOnOcrTranslateActivity.text.toString())
                try {
                    if (textToSpeech.isSpeaking) {
                        textToSpeech.stop()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun translateOnline(textInserted: String) {

//            var url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl="+firstLang+"&tl="+secondLang+"&dt=actualText&q="+textInserted"

        val secondCardLanguage = binding.language2Name.text.toString()

        if (textInserted == "" || textInserted.isEmpty()) {
            //  binding.ocrTranslationTextView.text=""
        } else {
            when (secondCardLanguage) {

                "Afrikaans" -> translateAndUpdateUI(Language.AFRIKAANS, textInserted)
                "Albanian" -> translateAndUpdateUI(Language.ALBANIAN, textInserted)
                "Arabic" -> translateAndUpdateUI(Language.ARABIC, textInserted)
                "Belarusian" -> translateAndUpdateUI(Language.BELARUSIAN, textInserted)
                "Bulgarian" -> translateAndUpdateUI(Language.BULGARIAN, textInserted)
                "Bengali" -> translateAndUpdateUI(Language.BENGALI, textInserted)
                "Catalan" -> translateAndUpdateUI(Language.CATALAN, textInserted)
                "Chinese" -> translateAndUpdateUI(Language.CHINESE, textInserted)
                "Croatian" -> translateAndUpdateUI(Language.CROATIAN, textInserted)
                "Czech" -> translateAndUpdateUI(Language.CZECH, textInserted)
                "Danish" -> translateAndUpdateUI(Language.DANISH, textInserted)
                "Dutch" -> translateAndUpdateUI(Language.DUTCH, textInserted)
                "English" -> translateAndUpdateUI(Language.ENGLISH, textInserted)
                "Estonian" -> translateAndUpdateUI(Language.ESTONIAN, textInserted)
                "French" -> translateAndUpdateUI(Language.FRENCH, textInserted)
                "Finnish" -> translateAndUpdateUI(Language.FINNISH, textInserted)
                "German" -> translateAndUpdateUI(Language.GERMAN, textInserted)
                "Georgian" -> translateAndUpdateUI(Language.GEORGIAN, textInserted)
                "Greek" -> translateAndUpdateUI(Language.GREEK, textInserted)
                "Galician" -> translateAndUpdateUI(Language.GALICIAN, textInserted)
                "Gujarati" -> translateAndUpdateUI(Language.GUJARATI, textInserted)
                "Hebrew" -> translateAndUpdateUI(Language.HEBREW, textInserted)
                "Hindi" -> translateAndUpdateUI(Language.HINDI, textInserted)
                "Haitian Creole" -> translateAndUpdateUI(Language.HAITIAN_CREOLE, textInserted)
                "Hungarian" -> translateAndUpdateUI(Language.HUNGARIAN, textInserted)
                "Indonesian" -> translateAndUpdateUI(Language.INDONESIAN, textInserted)
                "Icelandic" -> translateAndUpdateUI(Language.ICELANDIC, textInserted)
                "Irish" -> translateAndUpdateUI(Language.IRISH, textInserted)
                "Italian" -> translateAndUpdateUI(Language.ITALIAN, textInserted)
                "Japanese" -> translateAndUpdateUI(Language.JAPANESE, textInserted)
                "Kannada" -> translateAndUpdateUI(Language.KANNADA, textInserted)
                "Korean" -> translateAndUpdateUI(Language.KOREAN, textInserted)
                "Latvian" -> translateAndUpdateUI(Language.LATVIAN, textInserted)
                "Lithuanian" -> translateAndUpdateUI(Language.LITHUANIAN, textInserted)
                "Macedonian" -> translateAndUpdateUI(Language.MACEDONIAN, textInserted)
                "Malay" -> translateAndUpdateUI(Language.MALAY, textInserted)
                "Maltese" -> translateAndUpdateUI(Language.MALTESE, textInserted)
                "Norwegian" -> translateAndUpdateUI(Language.NORWEGIAN, textInserted)
                "Persian" -> translateAndUpdateUI(Language.PERSIAN, textInserted)
                "Polish" -> translateAndUpdateUI(Language.POLISH, textInserted)
                "Portuguese" -> translateAndUpdateUI(Language.PORTUGUESE, textInserted)
                "Romanian" -> translateAndUpdateUI(Language.ROMANIAN, textInserted)
                "Russian" -> translateAndUpdateUI(Language.RUSSIAN, textInserted)
                "Slovak" -> translateAndUpdateUI(Language.SLOVAK, textInserted)
                "Slovenian" -> translateAndUpdateUI(Language.SLOVENIAN, textInserted)
                "Spanish" -> translateAndUpdateUI(Language.SPANISH, textInserted)
                "Swedish" -> translateAndUpdateUI(Language.SWEDISH, textInserted)
                "Swahili(Kenya)" -> translateAndUpdateUI(Language.SWAHILI, textInserted)
                "Tamil" -> translateAndUpdateUI(Language.TAMIL, textInserted)
                "Telugu" -> translateAndUpdateUI(Language.TELUGU, textInserted)
                "Thai" -> translateAndUpdateUI(Language.THAI, textInserted)
                "Turkish" -> translateAndUpdateUI(Language.TURKISH, textInserted)
                "Ukranian" -> translateAndUpdateUI(Language.UKRAINIAN, textInserted)

                "Urdu" -> translateAndUpdateUI(Language.URDU, textInserted)
                "Vietnamese" -> translateAndUpdateUI(Language.VIETNAMESE, textInserted)
                "Welsh" -> translateAndUpdateUI(Language.WELSH, textInserted)
            }
        }
    }

    private fun translateAndUpdateUI(lan: String, res: String) {
        try {
            val translateAPI = TranslateAPI(Language.AUTO_DETECT, lan, res)
            translateAPI.setTranslateListener(object : TranslateAPI.TranslateListener {
                override fun onSuccess(translatedText: String) {
                    binding.ocrTranslationTextView.text = translatedText
                }

                override fun onFailure(errorText: String) {

                }
            })
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }


}