package com.wonderapps.translator.screens

import android.app.ActionBar
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import com.wonderapps.translator.R
import com.wonderapps.translator.adapter.OCRTranslateAdapter
import com.wonderapps.translator.databinding.ActivityOcrtranslationBinding
import com.wonderapps.translator.top_level_functions.getLanguages_for_translate_fragment
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage
import java.util.Locale

class OCRTranslationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOcrtranslationBinding
    private lateinit var firstCardLanguage: String
    private lateinit var secondCardLanguage: String
    private lateinit var languageList: ArrayList<ConversationLanguage>
    private lateinit var textToSpeech: TextToSpeech
    private var adapter: OCRTranslateAdapter? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var nativeAdView: NativeAdView

    private lateinit var ocrTranslationActivity: OCRTranslationActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ocrtranslation)
        setContentView(binding.root)

        nativeAdView = findViewById(R.id.native_ad_view)
        MobileAds.initialize(this)
        loadAd()
        ocrTranslationActivity = OCRTranslationActivity()

        setSupportActionBar(binding.ocrTranslationActivityToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        textToSpeech = TextToSpeech(applicationContext, {})
        languageList = ArrayList()
        getLanguages_for_translate_fragment(languageList)

        val intent = intent
        binding.ocrTranslationTextView.text = intent.getStringExtra("ocr_translation").toString()
        firstCardLanguage = intent.getStringExtra("ocr_first_language").toString()
        binding.language1Name.text = intent.getStringExtra("ocr_first_language").toString()
        secondCardLanguage = intent.getStringExtra("ocr_second_language").toString()
        binding.language2Name.text = intent.getStringExtra("ocr_second_language").toString()
        binding.editTextOnOcrTranslateActivity.setText(intent.getStringExtra("ocr_text").toString())


        binding.ocrTranslationActivityToolbar.setNavigationOnClickListener {
            onBackPressed()
        }


        binding.editTextOnOcrTranslateActivity.addTextChangedListener {

            if (binding.editTextOnOcrTranslateActivity.text.isEmpty() || binding.editTextOnOcrTranslateActivity.text.isNullOrEmpty() || binding.editTextOnOcrTranslateActivity.text.isBlank() || binding.editTextOnOcrTranslateActivity.text.toString()
                    .isNullOrBlank()
            ) {
                binding.ocrTranslationTextView.text = ""

                try {
                    if (textToSpeech.isSpeaking) {
                        textToSpeech.stop()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                binding.ocrTranslationTextView.visibility = View.VISIBLE
                translateOnline(binding.editTextOnOcrTranslateActivity.text.toString())
            }
        }


        binding.fullScreenOcrTranslation1.setOnClickListener {
            if (binding.editTextOnOcrTranslateActivity.text.isEmpty() || binding.editTextOnOcrTranslateActivity.text.isBlank() || binding.editTextOnOcrTranslateActivity.text.isNullOrEmpty() || binding.editTextOnOcrTranslateActivity.text.isNullOrBlank()) {

            } else {
                val intent = Intent(applicationContext, FullScreenTextActivity::class.java)
                intent.putExtra("LanguageName", binding.language1Name.text.toString())
                intent.putExtra("position", 1)
                intent.putExtra("text", binding.editTextOnOcrTranslateActivity.text.toString())
                startActivity(intent)
            }
        }

        binding.fullScreenOcrTranslation2.setOnClickListener {
            if (binding.editTextOnOcrTranslateActivity.text.isEmpty() || binding.editTextOnOcrTranslateActivity.text.isNullOrEmpty() || binding.editTextOnOcrTranslateActivity.text.isBlank() || binding.editTextOnOcrTranslateActivity.text.isNullOrBlank()) {

            } else {
                val intent = Intent(applicationContext, FullScreenTextActivity::class.java)
                intent.putExtra("LanguageName", binding.language2Name.text.toString())
                intent.putExtra("position", 2)
                intent.putExtra("text", binding.ocrTranslationTextView.text.toString())
                startActivity(intent)
            }
        }

        binding.copyBtn1.setOnClickListener {
            if (binding.editTextOnOcrTranslateActivity.text.isNotEmpty() || binding.editTextOnOcrTranslateActivity.text.isNotBlank()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                    "label",
                    binding.editTextOnOcrTranslateActivity.text.toString()
                )
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(
                    applicationContext,
                    getString(R.string.CopiedToClipboard),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.NoTextFoundToCopy),
                    Toast.LENGTH_LONG
                ).show()
            }
        }


        binding.volumeBtn.setOnClickListener {

            textToSpeech.speak(
                binding.ocrTranslationTextView.text.toString(),
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
            val handler = Handler(Looper.getMainLooper())

            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // This method is called when text-to-speech starts speaking.
                    handler.post {
                        binding.volumeBtn.visibility = View.GONE
                        binding.stopSpeakBtn.visibility = View.VISIBLE
                    }
                }

                override fun onDone(utteranceId: String?) {
                    // This method is called when text-to-speech is done speaking.
                    handler.post {
                        binding.volumeBtn.visibility = View.VISIBLE
                        binding.stopSpeakBtn.visibility = View.GONE
                    }
                }

                override fun onError(utteranceId: String?) {
                    // Handle any errors that occur during speech
                    Log.e("speakTranslatedText", "Speech error")
                }
            })
        }
        binding.stopSpeakBtn.setOnClickListener {
            binding.volumeBtn.visibility = View.VISIBLE
            binding.stopSpeakBtn.visibility = View.GONE
            textToSpeech.stop()
        }

        binding.copyBtn2.setOnClickListener {

            if (binding.ocrTranslationTextView.text.isEmpty() || binding.ocrTranslationTextView.text.isBlank() || binding.ocrTranslationTextView.text.isNullOrBlank() || binding.ocrTranslationTextView.text.isNullOrEmpty()) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.NoTranslationFoundToCopy),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                    ClipData.newPlainText("label", binding.ocrTranslationTextView.text.toString())
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(
                    applicationContext,
                    getString(R.string.CopiedToClipboard),
                    Toast.LENGTH_LONG
                ).show()
            }
        }


        binding.shareBtn1.setOnClickListener {

            if (binding.editTextOnOcrTranslateActivity.text.equals("") || binding.editTextOnOcrTranslateActivity.text.isEmpty() || binding.editTextOnOcrTranslateActivity.text.isBlank()) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.NoTextFoundToShare),
                    Toast.LENGTH_LONG
                ).show()
            } else {

                shareOcrText()
            }
        }

        binding.shareBtn2.setOnClickListener {

            if (binding.ocrTranslationTextView.text.equals("") || binding.ocrTranslationTextView.text.isEmpty() || binding.ocrTranslationTextView.text.isBlank() || binding.ocrTranslationTextView.text.isNullOrEmpty() || binding.ocrTranslationTextView.text.isNullOrBlank()) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.NoTranslationFoundToShare),
                    Toast.LENGTH_LONG
                ).show()
            } else {

                shareOcrTranslation()
            }
        }

        binding.language2Name.setOnClickListener {
            openDialog2()
        }

        textToSpeech.setOnUtteranceCompletedListener {
            binding.volumeBtn.setImageResource(R.drawable.speaker_gray)
        }
    }

    fun translateOnline(textInserted: String): Boolean {

//            var url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl="+firstLang+"&tl="+secondLang+"&dt=t&q="+textInserted"


        var secondCardLanguage = binding.language2Name.text.toString()


        if (textInserted.equals("") || textInserted.isEmpty() || binding.editTextOnOcrTranslateActivity.text.equals(
                ""
            ) || binding.editTextOnOcrTranslateActivity.text.isNullOrEmpty() || binding.editTextOnOcrTranslateActivity.text.isNullOrBlank()
        ) {
            binding.ocrTranslationTextView.text = ""
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

        return true
    }

    private fun translateAndUpdateUI(lan: String, textInserted: String) {
        try {
            val translateAPI = TranslateAPI(Language.AUTO_DETECT, lan, textInserted)
            translateAPI.setTranslateListener(object : TranslateAPI.TranslateListener {
                override fun onSuccess(translatedText: String) {
                    binding.ocrTranslationTextView.text = translatedText.toString()
                }

                override fun onFailure(ErrorText: String) {

                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        try {
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareOcrText() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            binding.editTextOnOcrTranslateActivity.text.toString()
        );
        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareVia)))
    }

    fun shareOcrTranslation() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, binding.ocrTranslationTextView.text.toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareVia)))
    }

    fun openDialog2() {
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_recycler_view)
        dialog.setTitle(R.string.SelectLanguage)
        recyclerView = dialog.findViewById<RecyclerView>(R.id.languagesRecyclerView)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                resources.configuration.orientation
            )
        )
        adapter = OCRTranslateAdapter(this, languageList, 2, dialog, binding, textToSpeech)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        val searchView = dialog.findViewById<EditText>(R.id.search_view_on_dialog)
        dialog.window?.setLayout(
            ActionBar.LayoutParams.FILL_PARENT,
            ActionBar.LayoutParams.FILL_PARENT
        )
        dialog.create()
        dialog.show()

        searchView.addTextChangedListener {
            filterList2(it.toString().trim(), dialog, searchView)
        }
    }

    private fun loadAd() {

        val adLoader = AdLoader.Builder(this, resources.getString(R.string.native_ad_id))
            .forNativeAd { ad: NativeAd ->
                val adImage: NativeAd.Image? = ad.images.getOrNull(0)


                val drawable = adImage?.drawable

                val headline = findViewById<TextView>(R.id.primary)
                headline.text = ad.headline
                nativeAdView.headlineView = headline

                val advertiser = findViewById<TextView>(R.id.secondary)
                advertiser.text = ad.advertiser
                nativeAdView.advertiserView = advertiser


                val icon = findViewById<ImageView>(R.id.icon)
                icon.setImageDrawable(ad.icon?.drawable)
                nativeAdView.iconView = icon
                val starRating: Double = ad.starRating ?: 2.5
                binding.ratingBar.rating = starRating.toFloat()

                val button = findViewById<Button>(R.id.cta)
                nativeAdView.callToActionView = button
                button.text = ad.store ?: ""
                nativeAdView.setNativeAd(ad)


            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    loadAd()

                }
            })


            .build()
        val adRequest = AdRequest.Builder().build()
        adLoader.loadAd(adRequest)
    }

    fun filterList2(text: String, dialog: Dialog, searchView: EditText) {
        val filteredList: java.util.ArrayList<ConversationLanguage> =
            java.util.ArrayList<ConversationLanguage>()
        for (item in languageList) {
            if (item.languageName.toLowerCase().contains(text.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
                adapter = OCRTranslateAdapter(this, filteredList, 2, dialog, binding, textToSpeech)
                recyclerView.adapter = adapter
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.NotFound), Toast.LENGTH_LONG)
                .show()
        } else {

        }
    }
}