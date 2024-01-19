package com.wonderapps.translator.screens.fragments

import TextToSpeechSingleton.textToSpeech
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import com.wonderapps.translator.R
import com.wonderapps.translator.adapter.ConversationLanguageAdapter
import com.wonderapps.translator.adapter.ConversationRecyclerAdapter
import com.wonderapps.translator.databinding.FragmentConversionBinding
import com.wonderapps.translator.model.ChatModel
import com.wonderapps.translator.model.ConversationModel
import com.wonderapps.translator.model.LTDatabase
import com.wonderapps.translator.top_level_functions.getLanguages_for_translate_fragment
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage
import initializeTextToSpeech
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.properties.Delegates


@Suppress("DEPRECATION")
class ConversionFragment : Fragment() {
    private lateinit var firstCardLanguage: String
    private lateinit var firstLanguageCode: String
    private lateinit var firstLanguageVoiceCode: String
    private lateinit var secondCardLanguage: String
    private lateinit var secondLanguageVoiceCode: String
    private lateinit var secondLanguageCode: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences1: SharedPreferences
    private lateinit var binding: FragmentConversionBinding
    private var adapter: ConversationRecyclerAdapter? = null
    private lateinit var conversationModel: ArrayList<ConversationModel>
    private lateinit var conversationLanguageList: ArrayList<ConversationLanguage>
    private lateinit var chatsModelList: ArrayList<ChatModel>
    private lateinit var audioResult: String
    private lateinit var toolbar: TextView
    private var position1 by Delegates.notNull<Int>()
    private var position2 by Delegates.notNull<Int>()
    private lateinit var languageAdapter: ConversationLanguageAdapter
    var flag = false

    lateinit var cardText: TextView
    private val database by lazy { LTDatabase.getDatabase(requireContext()).ConversationDAO() }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializeTextToSpeech(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_conversion, null, false)
        val view = binding.root
        cardText = binding.bottomSheetInclude.cardText
        toolbar = requireActivity().findViewById(R.id.clear_text_toolbar)
        toolbar.setOnClickListener {
            clearConversation()
        }
        removePhoneKeypadInConversationFragment()
        conversationLanguageList = ArrayList()
        getLanguages_for_translate_fragment(conversationLanguageList)
        textToSpeech = TextToSpeech(requireContext(), {}, "com.google.android.tts")
        getData()
        chatsModelList = ArrayList()
        conversationModel = ArrayList()
        sharedPreferences1 =
            requireContext().getSharedPreferences("CHAT_LANGUAGE_PREFERENCE", Context.MODE_PRIVATE)
        binding.language1TextViewInConversations.text =
            sharedPreferences1.getString("CHAT_FIRST_LANGUAGE_NAME", "").toString()
        binding.language2TextViewInConversations.text =
            sharedPreferences1.getString("CHAT_SECOND_LANGUAGE_NAME", "").toString()
        firstCardLanguage = sharedPreferences1.getString("CHAT_FIRST_LANGUAGE_NAME", "").toString()
        secondCardLanguage =
            sharedPreferences1.getString("CHAT_SECOND_LANGUAGE_NAME", "").toString()
        firstLanguageVoiceCode =
            sharedPreferences1.getString("CHAT_FIRST_LANGUAGE_VOICE_CODE", "").toString()
        secondLanguageVoiceCode =
            sharedPreferences1.getString("CHAT_SECOND_LANGUAGE_VOICE_CODE", "").toString()
        firstLanguageCode = sharedPreferences1.getString("CHAT_FIRST_LANGUAGE_CODE", "").toString()
        secondLanguageCode =
            sharedPreferences1.getString("CHAT_SECOND_LANGUAGE_CODE", "").toString()
        position1 = sharedPreferences1.getInt("CHAT_FIRST_LANGUAGE_POSITION", -1)
        position2 = sharedPreferences1.getInt("CHAT_SECOND_LANGUAGE_POSITION", -1)
        binding.recyclerViewChats.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewChats.layoutManager = layoutManager
        if (binding.language1TextViewInConversations.text.equals("")) {
            binding.language1TextViewInConversations.text = "English"
        }

        if (binding.language2TextViewInConversations.text.equals("")) {
            binding.language2TextViewInConversations.text = getString(R.string.SelectLanguage)
        }
        binding.leftChatFab.setOnClickListener {
            recordAudio1()

        }
        binding.rightChatFab.setOnClickListener {
            recordAudio2()
        }
        binding.swapLanguagesBtnInConversationFragment.setOnClickListener {
            swapLanguages()
        }


        binding.language1TextViewInConversations.setOnClickListener {
            openDialog1(1)
        }
        binding.language2TextViewInConversations.setOnClickListener {
            openDialog1(2)
        }
        return view
    }

    private fun filterListInConversationFragment1(
        text: String, dialog: Dialog, i: Int
    ) {
        val filteredList = ArrayList<ConversationLanguage>()
        for (item in conversationLanguageList) {
            if (item.languageName.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
                languageAdapter =
                    ConversationLanguageAdapter(requireContext(), filteredList, i, binding, dialog)
                recyclerView.adapter = languageAdapter
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(context, getString(R.string.NotFound), Toast.LENGTH_LONG).show()
        }
    }

    private fun openDialog1(position: Int) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_recycler_view)
        dialog.setTitle(getString(R.string.SelectLanguage))
        recyclerView = dialog.findViewById(R.id.languagesRecyclerView)
        val searchView = dialog.findViewById<EditText>(R.id.search_view_on_dialog)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(), resources.configuration.orientation
            )
        )

        val adapter = ConversationLanguageAdapter(
            requireContext(), conversationLanguageList, position, binding, dialog
        )
        recyclerView.adapter = adapter
        dialog.window?.setLayout(
            ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT
        )
        dialog.create()
        dialog.show()

        searchView.addTextChangedListener {
            filterListInConversationFragment1(
                it.toString().trim(),
                dialog,
                position
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearConversation() {

        try {
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setCancelable(true)
        alertDialog.setTitle(getString(R.string.ClearTitle))
        alertDialog.setMessage(getString(R.string.ClearConversationMsg))
        alertDialog.setPositiveButton(
            getString(R.string.Yes)
        ) { _, _ ->

            lifecycleScope.launch(Dispatchers.IO) {
                database.deleteAllData()
                chatsModelList.clear()
                conversationModel.clear()
                withContext(Dispatchers.Main) {
                    ConversationRecyclerAdapter(
                        requireContext(),
                        binding,
                        chatsModelList,
                        cardText
                    )
                    adapter!!.notifyDataSetChanged()
                    adapter = ConversationRecyclerAdapter(
                        requireContext(),
                        binding,
                        chatsModelList,
                        cardText
                    )
                    binding.recyclerViewChats.adapter = adapter
                    toolbar.visibility = View.GONE
                }
            }

        }.setNegativeButton(
            getString(R.string.No)
        ) { dialogInterface, _ ->
            dialogInterface.cancel()
            alertDialog.setCancelable(true)
        }.create().show()
    }

    private fun swapLanguages() {
        val languagePreference = "CHAT_LANGUAGE_PREFERENCE"
        val firstLanguageKey = "CHAT_FIRST_LANGUAGE_NAME"
        val firstLanguageVoiceKey = "CHAT_FIRST_LANGUAGE_VOICE_CODE"
        val firstLanguageCodeKey = "CHAT_FIRST_LANGUAGE_CODE"
        val secondLanguageKey = "CHAT_SECOND_LANGUAGE_NAME"
        val secondLanguageCodeKey = "CHAT_SECOND_LANGUAGE_CODE"
        val secondLanguageVoiceKey = "CHAT_SECOND_LANGUAGE_VOICE_CODE"

        val sharedPreferences =
            requireContext().getSharedPreferences(languagePreference, Context.MODE_PRIVATE)

        val language1Name = sharedPreferences.getString(firstLanguageKey, "") ?: ""
        val language1VoiceCode = sharedPreferences.getString(firstLanguageVoiceKey, "") ?: ""
        val language1Code = sharedPreferences.getString(firstLanguageCodeKey, "") ?: ""

        val language2Name = sharedPreferences.getString(secondLanguageKey, "") ?: ""
        val language2Code = sharedPreferences.getString(secondLanguageCodeKey, "") ?: ""
        val language2VoiceCode = sharedPreferences.getString(secondLanguageVoiceKey, "") ?: ""

        if (language2Name == requireContext().resources.getString(R.string.SelectLanguage)) {
            // Handle case where the second language is not selected
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.choose_languages_first_dialog)
            dialog.findViewById<Button>(R.id.btnOkToCancelLanguageSelectDialog).setOnClickListener {
                dialog.cancel()
            }
            dialog.create()
            dialog.show()
        } else {
            // Rotate the swap button
            val animationDuration = 500L
            val rotateAnimation = RotateAnimation(
                0F,
                180F,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            rotateAnimation.duration = animationDuration
            rotateAnimation.interpolator = LinearInterpolator()
            rotateAnimation.fillAfter = true
            binding.swapLanguagesBtnInConversationFragment.startAnimation(rotateAnimation)

            // Swap language preferences
            val editor = sharedPreferences.edit()

            editor.putString(firstLanguageCodeKey, language2Code)
            editor.putString(firstLanguageKey, language2Name)
            editor.putString(firstLanguageVoiceKey, language2VoiceCode)
            editor.putString(secondLanguageKey, language1Name)
            editor.putString(secondLanguageCodeKey, language1Code)
            editor.putString(secondLanguageVoiceKey, language1VoiceCode)

            editor.apply()

            // Update TextViews with swapped language names
            binding.language1TextViewInConversations.text = language2Name
            binding.language2TextViewInConversations.text = language1Name
        }
    }

    private fun removePhoneKeypadInConversationFragment() {
        try {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun recordAudio1() {
        sharedPreferences1 =
            requireContext().getSharedPreferences("CHAT_LANGUAGE_PREFERENCE", Context.MODE_PRIVATE)
        firstLanguageVoiceCode =
            sharedPreferences1.getString("CHAT_FIRST_LANGUAGE_VOICE_CODE", "").toString()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, firstLanguageVoiceCode)
        startActivityForResult(intent, 6000)
    }

    private fun recordAudio2() {
        secondLanguageVoiceCode =
            sharedPreferences1.getString("CHAT_SECOND_LANGUAGE_VOICE_CODE", "").toString()


        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, secondLanguageVoiceCode)
        startActivityForResult(intent, 7000)
    }


    private fun getData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Perform the database query on a background thread
            chatsModelList = database.getData() as ArrayList<ChatModel>

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                adapter = ConversationRecyclerAdapter(
                    requireContext(),
                    binding,
                    chatsModelList,  // Use the locally retrieved list
                    cardText
                )
                binding.recyclerViewChats.scrollToPosition(chatsModelList.size - 1)
                binding.recyclerViewChats.adapter = adapter
                if (chatsModelList.isEmpty()) {

                    toolbar.visibility = View.GONE
                } else {

                    toolbar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun translateOnline(lan: String, position: Int) {

        when (lan) {

            "Afrikaans" -> translateAndUpdateUI(Language.AFRIKAANS, audioResult, position, "af")
            "Albanian" -> translateAndUpdateUI(Language.ALBANIAN, audioResult, position, "sq")
            "Arabic" -> translateAndUpdateUI(Language.ARABIC, audioResult, position, "ar")
            "Belarusian" -> translateAndUpdateUI(Language.BELARUSIAN, audioResult, position, "be")
            "Bulgarian" -> translateAndUpdateUI(Language.BULGARIAN, audioResult, position, "bg")
            "Bengali" -> translateAndUpdateUI(Language.BENGALI, audioResult, position, "bn-BD")
            "Catalan" -> translateAndUpdateUI(Language.CATALAN, audioResult, position, "ca-ES")
            "Chinese" -> translateAndUpdateUI(Language.CHINESE, audioResult, position, "zh")
            "Croatian" -> translateAndUpdateUI(Language.CROATIAN, audioResult, position, "hr-HR")
            "Czech" -> translateAndUpdateUI(Language.CZECH, audioResult, position, "cs-CZ")
            "Danish" -> translateAndUpdateUI(Language.DANISH, audioResult, position, "da-DK")
            "Dutch" -> translateAndUpdateUI(Language.DUTCH, audioResult, position, "nl-BE")
            "English" -> translateAndUpdateUI(Language.ENGLISH, audioResult, position, "en")
            "Estonian" -> translateAndUpdateUI(Language.ESTONIAN, audioResult, position, "et-EE")
            "French" -> translateAndUpdateUI(Language.FRENCH, audioResult, position, "fr-FR")
            "Finnish" -> translateAndUpdateUI(Language.FINNISH, audioResult, position, "fi-FI")
            "German" -> translateAndUpdateUI(Language.GERMAN, audioResult, position, "de")
            "Georgian" -> translateAndUpdateUI(Language.GEORGIAN, audioResult, position, "ka-GE")
            "Greek" -> translateAndUpdateUI(Language.GREEK, audioResult, position, "el-GR")
            "Galician" -> translateAndUpdateUI(Language.GALICIAN, audioResult, position, "gl-ES")
            "Gujarati" -> translateAndUpdateUI(Language.GUJARATI, audioResult, position, "gu-IN")
            "Hebrew" -> translateAndUpdateUI(Language.HEBREW, audioResult, position, "he-IL")
            "Hindi" -> translateAndUpdateUI(Language.HINDI, audioResult, position, "hi")
            "Haitian Creole" -> translateAndUpdateUI(
                Language.HAITIAN_CREOLE,
                audioResult,
                position,
                "ht"
            )

            "Hungarian" -> translateAndUpdateUI(Language.HUNGARIAN, audioResult, position, "hu-HU")
            "Indonesian" -> translateAndUpdateUI(
                Language.INDONESIAN,
                audioResult,
                position,
                "id-ID"
            )

            "Icelandic" -> translateAndUpdateUI(Language.ICELANDIC, audioResult, position, "is-IS")
            "Irish" -> translateAndUpdateUI(Language.IRISH, audioResult, position, "ga")
            "Italian" -> translateAndUpdateUI(Language.ITALIAN, audioResult, position, "it")
            "Japanese" -> translateAndUpdateUI(Language.JAPANESE, audioResult, position, "ja")
            "Kannada" -> translateAndUpdateUI(Language.KANNADA, audioResult, position, "kn-IN")
            "Korean" -> translateAndUpdateUI(Language.KOREAN, audioResult, position, "ko-KR")
            "Latvian" -> translateAndUpdateUI(Language.LATVIAN, audioResult, position, "lv-LV")
            "Lithuanian" -> translateAndUpdateUI(
                Language.LITHUANIAN,
                audioResult,
                position,
                "lt-LT"
            )

            "Macedonian" -> translateAndUpdateUI(
                Language.MACEDONIAN,
                audioResult,
                position,
                "mk-MK"
            )

            "Malay" -> translateAndUpdateUI(Language.MALAY, audioResult, position, "ms-MY")
            "Maltese" -> translateAndUpdateUI(Language.MALTESE, audioResult, position, "mt_MT")
            "Norwegian" -> translateAndUpdateUI(Language.NORWEGIAN, audioResult, position, "no-NO")
            "Persian" -> translateAndUpdateUI(Language.PERSIAN, audioResult, position, "fa-IR")

            "Polish" -> translateAndUpdateUI(Language.POLISH, audioResult, position, "pl-PL")
            "Portuguese" -> translateAndUpdateUI(
                Language.PORTUGUESE,
                audioResult,
                position,
                "pt-BR"
            )

            "Romanian" -> translateAndUpdateUI(Language.ROMANIAN, audioResult, position, "ro-RO")
            "Russian" -> translateAndUpdateUI(Language.RUSSIAN, audioResult, position, "ru")
            "Slovak" -> translateAndUpdateUI(Language.SLOVAK, audioResult, position, "sk-SK")
            "Slovenian" -> translateAndUpdateUI(Language.SLOVENIAN, audioResult, position, "sl-SI")
            "Spanish" -> translateAndUpdateUI(Language.SPANISH, audioResult, position, "es-ES")
            "Swedish" -> translateAndUpdateUI(Language.SWEDISH, audioResult, position, "sv-SE")
            "Swahili" -> translateAndUpdateUI(Language.SWAHILI, audioResult, position, "sw-KE")
            "Tamil" -> translateAndUpdateUI(Language.TAMIL, audioResult, position, "ta-IN")
            "Telugu" -> translateAndUpdateUI(Language.TELUGU, audioResult, position, "te-IN")
            "Thai" -> translateAndUpdateUI(Language.THAI, audioResult, position, "th-TH")
            "Turkish" -> translateAndUpdateUI(Language.TURKISH, audioResult, position, "tk")
            "Ukranian" -> translateAndUpdateUI(Language.UKRAINIAN, audioResult, position, "uk-UA")

            "Urdu" -> translateAndUpdateUI(Language.URDU, audioResult, position, "ur-PK")
            "Vietnamese" -> translateAndUpdateUI(
                Language.VIETNAMESE,
                audioResult,
                position,
                "vi-VN"
            )

            "Welsh" -> translateAndUpdateUI(Language.WELSH, audioResult, position, "cy")
        }
    }

    @SuppressLint("InflateParams")
    private fun translateAndUpdateUI(
        lan: String,
        audioResult: String,
        layoutPos: Int,
        lanCode: String
    ) {
        try {

            val translateAPI = TranslateAPI(Language.AUTO_DETECT, lan, audioResult)

            translateAPI.setTranslateListener(object : TranslateAPI.TranslateListener {
                override fun onSuccess(translatedText: String) {
                    textToSpeech.language = Locale.forLanguageTag(lanCode)
                    textToSpeech.setSpeechRate(0.7f)

                    textToSpeech.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "")


                    chatsModelList.add(
                        ChatModel(
                            audioResult,
                            translatedText,
                            layoutPos,
                            firstLanguageCode,
                            secondCardLanguage
                        )
                    )

                    val adapter = ConversationRecyclerAdapter(
                        context!!, binding, chatsModelList, cardText
                    )
                    binding.recyclerViewChats.scrollToPosition(chatsModelList.size - 1)
                    binding.recyclerViewChats.adapter = adapter
                }

                override fun onFailure(errorText: String) {

                }
            })
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 6000 && data != null) {
            toolbar.visibility = View.VISIBLE
            audioResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toString()
            secondCardLanguage =
                sharedPreferences1.getString("CHAT_SECOND_LANGUAGE_NAME", "").toString()
            translateOnline(secondCardLanguage, 1)

            conversationModel.add(
                ConversationModel(
                    sharedPreferences1.getString(
                        "CHAT_FIRST_LANGUAGE_NAME", ""
                    ).toString(),
                    sharedPreferences1.getString("CHAT_SECOND_LANGUAGE_NAME", "").toString(),
                    firstLanguageVoiceCode,
                    secondLanguageVoiceCode
                )
            )

        }
        if (requestCode == 7000 && data != null) {

            toolbar.visibility = View.VISIBLE
            audioResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toString()
            firstCardLanguage =
                sharedPreferences1.getString("CHAT_FIRST_LANGUAGE_NAME", "").toString()

            translateOnline(firstCardLanguage, 2)
            conversationModel.add(
                ConversationModel(
                    sharedPreferences1.getString(
                        "CHAT_FIRST_LANGUAGE_NAME", ""
                    ).toString(),
                    sharedPreferences1.getString("CHAT_SECOND_LANGUAGE_NAME", "").toString(),
                    firstLanguageVoiceCode,
                    secondLanguageVoiceCode
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("lifeCycleMethodCAlled", "onPause: ${textToSpeech.isSpeaking} ")
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
    }

}