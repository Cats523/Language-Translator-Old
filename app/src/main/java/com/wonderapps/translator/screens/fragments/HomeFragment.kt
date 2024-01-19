@file:Suppress("DEPRECATION")

package com.wonderapps.translator.screens.fragments

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import com.wonderapps.translator.R
import com.wonderapps.translator.adapter.RecyclerAdapter
import com.wonderapps.translator.databinding.FragmentHomeBinding
import com.wonderapps.translator.model.SharedViewModel
import com.wonderapps.translator.screens.FullScreenTextActivity
import com.wonderapps.translator.top_level_functions.getLanguages_for_translate_fragment
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage
import com.wonderapps.translator.utils.helper.SavedInstancesValueData
import com.wonderapps.translator.utils.network.BottomSheetFragment
import com.wonderapps.translator.utils.network.NetworkStatusReceiver
import java.util.Locale


@Suppress("DEPRECATION")
class HomeFragment : Fragment(), TextToSpeech.OnInitListener,
    TextToSpeech.OnUtteranceCompletedListener {
    private lateinit var bottomSheetFragment: BottomSheetFragment
    private var networkChangeReceiver = NetworkStatusReceiver()
    val internetCheck: MutableList<Boolean> = mutableListOf()
    private var textToSpeech: TextToSpeech? = null
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var languageList: ArrayList<ConversationLanguage>
    private lateinit var binding: FragmentHomeBinding
    private lateinit var secondCardLanguage: String
    private lateinit var progressDialog: ProgressDialog
    private var editTextData = ""
    private var transledTextData = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, null, false)
        val view = binding.root
        Log.d("screenChangeCode", "onCreate: hello 120 ")
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        bottomSheetFragment = BottomSheetFragment()

        registerNetworkBroadcastReceiver()
        languageList = ArrayList()
        textToSpeech = TextToSpeech(activity) {}
        getLanguages_for_translate_fragment(languageList)
        Log.d("screenChangeCode", "onCreate: hello 10 ")
        editTextData = SavedInstancesValueData.editTextData
        transledTextData = SavedInstancesValueData.translatedTextData

        if (editTextData.isNotEmpty() && transledTextData.isNotEmpty()) {
            binding.secondCardView.visibility = View.VISIBLE
            binding.editTextForTranslationText.setText(editTextData)
            binding.translationTextView.text = transledTextData
            binding.fullScreenIconOnFirstCard.visibility = View.VISIBLE
            binding.clearTextBtn.visibility = View.VISIBLE
        }

        val sharedPreferences1 = requireContext().getSharedPreferences(
            "Translate_Language_Preference", Context.MODE_PRIVATE
        )
        binding.language1TextViewInHomeFragment.text =
            sharedPreferences1.getString("Language_1_Name", "").toString()

        secondCardLanguage =
            sharedPreferences1.getString("Language_2_Name", "").toString()
        binding.language2TextViewInHomeFragment.text =
            sharedPreferences1.getString("Language_2_Name", "").toString()
        binding.language1TextViewInHomeFragment.setOnClickListener {
            binding.language1TextViewInHomeFragment.text =
                sharedPreferences1.getString("Language_1_Name", "").toString()
            showDialog1InHomeFragment(1)
        }
        networkChangeReceiver.getNetworkStatusLiveData()
            .observe(requireActivity()) { isNetworkAvailable ->
                if (!isNetworkAvailable) {
                    internetCheck.add(false)
                } else {
                    val indexOfFalse = internetCheck.indexOf(false)
                    if (indexOfFalse != -1) {
                        internetCheck.removeAt(indexOfFalse)
                    }

                }
            }
        binding.microphoneOnCardview1.setOnClickListener {

//            initialized = true
            if (binding.language2TextViewInHomeFragment.text.equals(getString(R.string.SelectLanguage))) {
                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.choose_languages_first_dialog)
                dialog.findViewById<Button>(R.id.btnOkToCancelLanguageSelectDialog)
                    .setOnClickListener {
                        dialog.cancel()
                    }
                dialog.create()
                dialog.show()
            } else {


                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    sharedPreferences1.getString("Language_1_Code", "")
                )
//                record_Audio_in_translate_Fragment(intent, sharedPreferences1)
                startActivityForResult(intent, 5000)

            }
        }

        binding.language2TextViewInHomeFragment.setOnClickListener {

            showDialog1InHomeFragment(2)
        }
        binding.fullScreenIconOnFirstCard.setOnClickListener {
            try {
                val intent =
                    Intent(requireContext(), FullScreenTextActivity::class.java)
                val firstCardLanguage =
                    sharedPreferences1.getString("Language_1_Name", "").toString()
                intent.putExtra("LanguageName", firstCardLanguage)
                intent.putExtra("position", 1)
                intent.putExtra(
                    "text",
                    binding.editTextForTranslationText.text.toString()
                )
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.clearTextBtn.setOnClickListener {
            binding.editTextForTranslationText.setText("")
            binding.secondCardView.visibility = View.GONE
            binding.clearTextBtn.visibility = View.INVISIBLE
            binding.translationTextView.text = ""
            binding.fullScreenIconOnFirstCard.visibility = View.GONE
            SavedInstancesValueData.setData("", "")
        }


        binding.editTextForTranslationText.addTextChangedListener(object : TextWatcher {
            private var initialized = false
            override fun afterTextChanged(s: Editable) {


                if (!initialized) {
                    initialized = true
                    return
                }

                if (binding.editTextForTranslationText.text.isEmpty() || binding.editTextForTranslationText.text.equals(
                        ""
                    ) || binding.editTextForTranslationText.text.isBlank() || binding.editTextForTranslationText.text.isNullOrEmpty() || binding.editTextForTranslationText.text.isNullOrBlank()
                ) {
                    binding.fullScreenIconOnFirstCard.visibility = View.GONE
                    binding.clearTextBtn.visibility = View.INVISIBLE
                    binding.translationTextView.text = ""
                    binding.secondCardView.visibility = View.GONE
                } else {
                    binding.clearTextBtn.visibility = View.VISIBLE
                    binding.fullScreenIconOnFirstCard.visibility = View.VISIBLE
                    binding.translationTextView.text = ""
                    binding.secondCardView.visibility = View.VISIBLE


                    secondCardLanguage =
                        sharedPreferences1.getString("Language_2_Name", "").toString()
                    if (secondCardLanguage == "Persian") {

                        binding.volumeBtnCardView2.visibility = View.GONE

                    } else {
                        binding.volumeBtnCardView2.visibility = View.VISIBLE
                    }
                    when (true) {

                        (false in internetCheck) -> {
                            binding.translationTextView.text = ""
                            binding.translationTextView.text =
                                getString(R.string.ooooppss_network_not_available)

                        }

                        else -> {
                            binding.translationTextView.text = ""


                            translateOnline(
                                binding.editTextForTranslationText.text.toString(),
                                secondCardLanguage
                            )

                        }
                    }


                }
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {

            }
        })
        binding.fullScreenIconOnSecondCard.setOnClickListener {
            val intent = Intent(requireContext(), FullScreenTextActivity::class.java)

            secondCardLanguage =
                sharedPreferences1.getString("Language_2_Name", "").toString()
            intent.putExtra("LanguageName", secondCardLanguage)
            intent.putExtra("position", 2)
            intent.putExtra("text", binding.translationTextView.text.toString())
            startActivity(intent)
        }
        binding.shareTranslationText.setOnClickListener {
            if (binding.translationTextView.text.isNotEmpty() || binding.translationTextView.text.isNotBlank()) {
                shareTranslationTextInTranslateFragment()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.NoTranslationFoundToShare),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.swapLanguagesBtnInHomeFragment.setOnClickListener {

            swapLanguagesInTranslateFragment()

        }

        binding.copyContent.setOnClickListener {
            copyTranslationInHomeFragment()
        }
        binding.volumeBtnCardView2.setOnClickListener {
            if (binding.translationTextView.text.isNotEmpty() || binding.translationTextView.text.isNotBlank()) {
                val translatePreference =
                    requireActivity().getSharedPreferences(
                        "Translate_Language_Preference",
                        AppCompatActivity.MODE_PRIVATE
                    )
                val languageCode = translatePreference.getString("Language_2_Code", null)
                val locale = Locale(languageCode!!)
                textToSpeech?.language = locale

                textToSpeech?.speak(
                    binding.translationTextView.text.toString(),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
                val handler = Handler(Looper.getMainLooper())

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        // This method is called when text-to-speech starts speaking.
                        handler.post {
                            binding.volumeBtnCardView2.visibility = View.GONE
                            binding.stopPeakBtn.visibility = View.VISIBLE
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        // This method is called when text-to-speech is done speaking.
                        handler.post {
                            binding.volumeBtnCardView2.visibility = View.VISIBLE
                            binding.stopPeakBtn.visibility = View.GONE
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        // Handle any errors that occur during speech
                        Log.e("speakTranslatedText", "Speech error")
                    }
                })
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.NoTextToSpeak,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.stopPeakBtn.setOnClickListener {
            binding.volumeBtnCardView2.visibility = View.VISIBLE
            binding.stopPeakBtn.visibility = View.GONE
            textToSpeech!!.stop()
        }


        return view
    }

    private fun copyTranslationInHomeFragment() {

        if (binding.translationTextView.text.isNotEmpty() || binding.translationTextView.text.isNotBlank()) {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", binding.translationTextView.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                requireContext(), getString(R.string.CopiedToClipboard), Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.NoTextFoundToCopy), Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.changeLanguage.observe(viewLifecycleOwner) { newLanguage ->


            translateOnline(binding.editTextForTranslationText.text.toString(), newLanguage)

        }
    }

    private fun showDialog1InHomeFragment(position: Int) {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_recycler_view)
        dialog.setTitle(getString(R.string.SelectLanguage))
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.languagesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        val searchView = dialog.findViewById<EditText>(R.id.search_view_on_dialog)
        val adapter = RecyclerAdapter(
            sharedViewModel, requireContext(), languageList, position, binding, dialog, searchView
        )
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(), requireContext().resources.configuration.orientation
            )
        )
        recyclerView.adapter = adapter
        dialog.window?.setLayout(
            ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT
        )
        dialog.create()
        dialog.show()

        searchView.addTextChangedListener {

            filterList1InHomeFragment(
                sharedViewModel,
                requireContext(),
                it.toString().trim(),
                dialog,
                searchView,
                position,
                binding,
                recyclerView,
                languageList
            )
        }
    }

    private fun filterList1InHomeFragment(
        sharedViewModel: SharedViewModel,
        context: Context,
        text: String,
        dialog: Dialog,
        searchView: EditText,
        position: Int,
        binding: FragmentHomeBinding,
        recyclerView: RecyclerView,
        languageList: ArrayList<ConversationLanguage>
    ) {
        val filteredList: ArrayList<ConversationLanguage> = ArrayList()
        for (item in languageList) {
            if (item.languageName.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                filteredList.add(item)
                val adapter = RecyclerAdapter(
                    sharedViewModel, context, filteredList, position, binding, dialog, searchView
                )
                recyclerView.adapter = adapter
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(context, getString(R.string.NotFound), Toast.LENGTH_LONG).show()
        }
    }

    private fun swapLanguagesInTranslateFragment() {
        val sharedPreferences = requireContext().getSharedPreferences(
            "Translate_Language_Preference", Context.MODE_PRIVATE
        )
        val firstCardLanguage = sharedPreferences.getString("Language_1_Name", "").toString()
        val firstCardCode = sharedPreferences.getString("Language_1_Code", "").toString()

        secondCardLanguage = sharedPreferences.getString("Language_2_Name", "").toString()
        val secondCardCode = sharedPreferences.getString("Language_2_Code", "").toString()

        // Check if the second language is not selected
        if (binding.language2TextViewInHomeFragment.text == getString(R.string.SelectLanguage)) {
            showLanguageSelectDialog()
            return
        }

        // Check if the input text is empty or blank
        if (binding.editTextForTranslationText.text.isNullOrBlank()) {
            animateSwapButton()
            swapLanguages(
                sharedPreferences,
                firstCardLanguage,
                firstCardCode,
                secondCardLanguage,
                secondCardCode
            )
        } else {
            // Perform translation or swap animation here
            progressDialog = ProgressDialog(requireContext())
            progressDialog.setTitle(getString(R.string.Translating))
            progressDialog.setCancelable(false)
            progressDialog.create()
            progressDialog.show()

            binding.swapLanguagesBtnInHomeFragment.isEnabled = false
            animateSwapButton()

            swapLanguages(
                sharedPreferences,
                firstCardLanguage,
                firstCardCode,
                secondCardLanguage,
                secondCardCode
            )

            // Simulate translation process with a delay
            Handler(Looper.getMainLooper()).postDelayed({
                progressDialog.cancel()
                val swapText = binding.translationTextView.text
                binding.editTextForTranslationText.setText(swapText)
                binding.translationTextView.visibility = View.VISIBLE
                binding.swapLanguagesBtnInHomeFragment.isEnabled = true
            }, 2000)
        }
    }

    private fun animateSwapButton() {
        binding.swapLanguagesBtnInHomeFragment.animate().apply {
            val rotate = RotateAnimation(
                0F, 180F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            rotate.duration = 500
            rotate.interpolator = LinearInterpolator()
            rotate.fillAfter = true
            binding.swapLanguagesBtnInHomeFragment.startAnimation(rotate)
        }
    }

    private fun showLanguageSelectDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.choose_languages_first_dialog)
        dialog.findViewById<Button>(R.id.btnOkToCancelLanguageSelectDialog).setOnClickListener {
            dialog.cancel()
        }
        dialog.create()
        dialog.show()
    }

    private fun swapLanguages(
        sharedPreferences: SharedPreferences,
        firstLanguage: String,
        firstCode: String,
        secondCardLanguage: String,
        secondCardCode: String
    ) {
        val editor = sharedPreferences.edit()
        editor.putString("Language_1_Name", secondCardLanguage)
        editor.putString("Language_1_Code", secondCardCode)
        editor.putString("Language_2_Name", firstLanguage)
        editor.putString("Language_2_Code", firstCode)
        editor.apply()
        binding.language1TextViewInHomeFragment.text = secondCardLanguage
        binding.language2TextViewInHomeFragment.text = firstLanguage
    }

    fun translateOnline(textInserted: String, secondCardLanguage: String): Boolean {

//            var url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl="+firstLang+"&tl="+secondLang+"&dt=actualText&q="+textInserted"

        if (textInserted == "" || textInserted.isEmpty() || binding.editTextForTranslationText.text.equals(
                ""
            ) || binding.editTextForTranslationText.text.isNullOrEmpty() || binding.editTextForTranslationText.text.isNullOrBlank()
        ) {
            binding.translationTextView.text = ""

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
                "Swahili" -> translateAndUpdateUI(Language.SWAHILI, textInserted)
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

        return true
    }

    private fun translateAndUpdateUI(lan: String, textInserted: String) {

        try {
            val translateAPI = TranslateAPI(Language.AUTO_DETECT, lan, textInserted)


            translateAPI.setTranslateListener(object : TranslateAPI.TranslateListener {
                override fun onSuccess(translatedText: String) {

                    binding.translationTextView.text = translatedText

                    binding.translationTextView.movementMethod = ScrollingMovementMethod()
                }

                override fun onFailure(errorText: String) {

                }
            })
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }


    private fun shareTranslationTextInTranslateFragment() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, binding.translationTextView.text.toString())
        requireContext().startActivity(
            Intent.createChooser(
                shareIntent, getString(R.string.shareVia)
            )
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 5000 && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.editTextForTranslationText.setText(result!![0])
            binding.clearTextBtn.visibility = View.VISIBLE
            translateOnline(
                binding.editTextForTranslationText.text.toString(),
                secondCardLanguage
            )
            binding.secondCardView.visibility = View.VISIBLE

        }
    }

    private fun registerNetworkBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        activity?.registerReceiver(networkChangeReceiver, intentFilter)
    }

    private fun unregisterNetworkBroadcastReceiver() {
        try {
            if (networkChangeReceiver != null) activity?.unregisterReceiver(networkChangeReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkBroadcastReceiver()
        if (textToSpeech!!.isSpeaking) {
            textToSpeech!!.stop()
        }
        binding.translationTextView.text = ""
        binding.editTextForTranslationText.setText("")
    }

    override fun onPause() {
        super.onPause()

        if (textToSpeech!!.isSpeaking) {
            textToSpeech!!.stop()
        }
        val editTextData = binding.editTextForTranslationText.text.toString()
        val transledTextData = binding.translationTextView.text.toString()
        val value =
            getString(R.string.ooooppss_network_not_available)
        if (editTextData.isNotEmpty() && transledTextData.isNotEmpty() && transledTextData != value) {
            SavedInstancesValueData.setData(editTextData, transledTextData)
        }
        val sharedPreferences1 = requireContext().getSharedPreferences(
            "Translate_Language_Preference", Context.MODE_PRIVATE
        )
        sharedViewModel.setLanguage(sharedPreferences1.getString("Language_2_Name", "").toString())
    }

    @Deprecated("Deprecated in Java")
    override fun onUtteranceCompleted(utteranceId: String?) {
        //TODO
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech!!.setOnUtteranceCompletedListener(this)
        }
    }
}