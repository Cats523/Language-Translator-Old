package com.wonderapps.translator.screens

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import com.wonderapps.translator.R
import com.wonderapps.translator.databinding.ActivityLoadImageBinding

@Suppress("DEPRECATION")
class LoadImageActivity : AppCompatActivity() {
    private lateinit var adContainerView: FrameLayout
    private var mInterstitialAd: InterstitialAd? = null
    lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityLoadImageBinding
    private lateinit var modelManager: RemoteModelManager
    private lateinit var sharedPreferences1: SharedPreferences
    private lateinit var firstCardLanguage: String
    private lateinit var firstLanguageCode: String
    private lateinit var firstLanguageModelCode: String
    private lateinit var secondCardLanguage: String
    private lateinit var secondLanguageCode: String
    private lateinit var secondLanguageModelCode: String
    private lateinit var ocrImageText: String
    private lateinit var languageIdentifier: LanguageIdentifier
    private lateinit var imagePreferences: SharedPreferences
    private lateinit var edtranslatedTextor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_load_image)
        setContentView(binding.root)
        adContainerView = findViewById(R.id.adViewContainer)

        loadBanner()
        modelManager = RemoteModelManager.getInstance()
        setSupportActionBar(binding.loadImageActivityToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val intent = intent
        val uri = intent.getStringExtra("crop_image_uri")
        ocrImageText = intent.getStringExtra("ocr_text_from_image").toString()
        languageIdentifier = LanguageIdentification.getClient()


        if (uri != null) {
            imagePreferences = getSharedPreferences("IMAGE_PREFERENCES", Context.MODE_PRIVATE)
            edtranslatedTextor = imagePreferences.edit()
            edtranslatedTextor.putString("image_uri", uri.toString())
            edtranslatedTextor.putString("imageText", ocrImageText)
            edtranslatedTextor.apply()
        }

        sharedPreferences1 = getSharedPreferences("OCR_LANGUAGE_PREFERENCE", Context.MODE_PRIVATE)
        sharedPreferences1.getString("OCR_FIRST_LANGUAGE_NAME", "").toString()
        firstCardLanguage = sharedPreferences1.getString("OCR_FIRST_LANGUAGE_NAME", "").toString()
        firstLanguageModelCode =
            sharedPreferences1.getString("OCR_FIRST_LANGUAGE_MODEL_CODE", "").toString()
        firstLanguageCode = sharedPreferences1.getString("OCR_FIRST_LANGUAGE_CODE", "").toString()

        secondCardLanguage = sharedPreferences1.getString("OCR_SECOND_LANGUAGE_NAME", "").toString()
        secondLanguageModelCode =
            sharedPreferences1.getString("OCR_SECOND_LANGUAGE_MODEL_CODE", "").toString()
        secondLanguageCode = sharedPreferences1.getString("OCR_SECOND_LANGUAGE_CODE", "").toString()



        try {
            var languageName = ""
            languageIdentifier.identifyLanguage(ocrImageText)
                .addOnSuccessListener { languageCode ->
                    val languageMappings = mapOf(
                        "af" to "Afrikaans",
                        "en" to "English",
                        "fr" to "French",
                        "de" to "German",
                        "sq" to "Albanian",
                        "es" to "Spanish",
                        "ca" to "Catalan",
                        "ms" to "Malay",
                        "da" to "Danish",
                        "fi" to "Finnish",
                        "ht" to "Haitian Creole",
                        "hu" to "Hungarian",
                        "pl" to "Polish",
                        "tr" to "Turkish",
                        "vi" to "Vietnamese",
                        "cy" to "Welsh",
                        "sv" to "Swedish",
                        "sl" to "Slovenian",
                        "sk" to "Slovak",
                        "is" to "Icelandic",
                        "it" to "Italian",
                        "lt" to "Lithuanian",
                        "ro" to "Romanian",
                        "pt" to "Portuguese",
                        "no" to "Norwegian",
                        "id" to "Indonesian"
                    )
                    languageName =
                        languageMappings[languageCode] ?: "Undetermined Language"
                    binding.languageDetectedTextView.text = languageName
                    binding.translationText.text = ocrImageText
                }
                .addOnFailureListener { exception ->

                    Toast.makeText(applicationContext, exception.toString(), Toast.LENGTH_LONG)
                        .show()
                }

            if (uri == null) {
                imagePreferences = getSharedPreferences("IMAGE_PREFERENCES", Context.MODE_PRIVATE)

                binding.imageViewInLoadImageActivity.setImageURI(
                    Uri.parse(
                        imagePreferences.getString(
                            "image_uri",
                            ""
                        )
                    )
                )
                val text = imagePreferences.getString("imageText", "").toString()

                binding.translationText.text = text

            } else {
                binding.imageViewInLoadImageActivity.setImageURI(Uri.parse(uri))
            }
        } catch (e: Exception) {

            Intent(this@LoadImageActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        } finally {

        }

        binding.translateOcrText.setOnClickListener {
            if (hasInternetConnection()) {
                translateOnline()
                progressDialog = ProgressDialog(this@LoadImageActivity)
                progressDialog.setTitle(getString(R.string.ad_is_loading))
                progressDialog.setCancelable(false)
                progressDialog.create()
                progressDialog.show()
                loadInterstitialAd()
            } else {
                Toast.makeText(
                    applicationContext, getString(R.string.PleaseConnectToInternet),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun hasInternetConnection(): Boolean {

        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun translateOnline() {

        secondCardLanguage = "English"

        if (binding.translationText.text.equals("") || binding.translationText.text.isEmpty()) {
            Toast.makeText(applicationContext, "No text detected", Toast.LENGTH_LONG).show()
        } else {
            when (secondCardLanguage) {

                "Afrikaans" -> translateAndUpdateUI(
                    Language.AFRIKAANS,
                    binding.translationText.text.toString()
                )

                "Albanian" -> translateAndUpdateUI(
                    Language.ALBANIAN,
                    binding.translationText.text.toString()
                )

                "Arabic" -> translateAndUpdateUI(
                    Language.ARABIC,
                    binding.translationText.text.toString()
                )

                "Belarusian" -> translateAndUpdateUI(
                    Language.BELARUSIAN,
                    binding.translationText.text.toString()
                )

                "Bulgarian" -> translateAndUpdateUI(
                    Language.BULGARIAN,
                    binding.translationText.text.toString()
                )

                "Bengali" -> translateAndUpdateUI(
                    Language.BENGALI,
                    binding.translationText.text.toString()
                )

                "Catalan" -> translateAndUpdateUI(
                    Language.CATALAN,
                    binding.translationText.text.toString()
                )

                "Chinese" -> translateAndUpdateUI(
                    Language.CHINESE,
                    binding.translationText.text.toString()
                )

                "Croatian" -> translateAndUpdateUI(
                    Language.CROATIAN,
                    binding.translationText.text.toString()
                )

                "Czech" -> translateAndUpdateUI(
                    Language.CZECH,
                    binding.translationText.text.toString()
                )

                "Danish" -> translateAndUpdateUI(
                    Language.DANISH,
                    binding.translationText.text.toString()
                )

                "Dutch" -> translateAndUpdateUI(
                    Language.DUTCH,
                    binding.translationText.text.toString()
                )

                "English" -> translateAndUpdateUI(
                    Language.ENGLISH,
                    binding.translationText.text.toString()
                )

                "Estonian" -> translateAndUpdateUI(
                    Language.ESTONIAN,
                    binding.translationText.text.toString()
                )

                "French" -> translateAndUpdateUI(
                    Language.FRENCH,
                    binding.translationText.text.toString()
                )

                "Finnish" -> translateAndUpdateUI(
                    Language.FINNISH,
                    binding.translationText.text.toString()
                )

                "German" -> translateAndUpdateUI(
                    Language.GERMAN,
                    binding.translationText.text.toString()
                )

                "Georgian" -> translateAndUpdateUI(
                    Language.GEORGIAN,
                    binding.translationText.text.toString()
                )

                "Greek" -> translateAndUpdateUI(
                    Language.GREEK,
                    binding.translationText.text.toString()
                )

                "Galician" -> translateAndUpdateUI(
                    Language.GALICIAN,
                    binding.translationText.text.toString()
                )

                "Gujarati" -> translateAndUpdateUI(
                    Language.GUJARATI,
                    binding.translationText.text.toString()
                )

                "Hebrew" -> translateAndUpdateUI(
                    Language.HEBREW,
                    binding.translationText.text.toString()
                )

                "Hindi" -> translateAndUpdateUI(
                    Language.HINDI,
                    binding.translationText.text.toString()
                )

                "Haitian Creole" -> translateAndUpdateUI(
                    Language.HAITIAN_CREOLE,
                    binding.translationText.text.toString()
                )

                "Hungarian" -> translateAndUpdateUI(
                    Language.HUNGARIAN,
                    binding.translationText.text.toString()
                )

                "Indonesian" -> translateAndUpdateUI(
                    Language.INDONESIAN,
                    binding.translationText.text.toString()
                )

                "Icelandic" -> translateAndUpdateUI(
                    Language.ICELANDIC,
                    binding.translationText.text.toString()
                )

                "Irish" -> translateAndUpdateUI(
                    Language.IRISH,
                    binding.translationText.text.toString()
                )

                "Italian" -> translateAndUpdateUI(
                    Language.ITALIAN,
                    binding.translationText.text.toString()
                )

                "Japanese" -> translateAndUpdateUI(
                    Language.JAPANESE,
                    binding.translationText.text.toString()
                )

                "Kannada" -> translateAndUpdateUI(
                    Language.KANNADA,
                    binding.translationText.text.toString()
                )

                "Korean" -> translateAndUpdateUI(
                    Language.KOREAN,
                    binding.translationText.text.toString()
                )

                "Latvian" -> translateAndUpdateUI(
                    Language.LATVIAN,
                    binding.translationText.text.toString()
                )

                "Lithuanian" -> translateAndUpdateUI(
                    Language.LITHUANIAN,
                    binding.translationText.text.toString()
                )

                "Macedonian" -> translateAndUpdateUI(
                    Language.MACEDONIAN,
                    binding.translationText.text.toString()
                )

                "Malay" -> translateAndUpdateUI(
                    Language.MALAY,
                    binding.translationText.text.toString()
                )

                "Maltese" -> translateAndUpdateUI(
                    Language.MALTESE,
                    binding.translationText.text.toString()
                )

                "Norwegian" -> translateAndUpdateUI(
                    Language.NORWEGIAN,
                    binding.translationText.text.toString()
                )

                "Persian" -> translateAndUpdateUI(
                    Language.PERSIAN,
                    binding.translationText.text.toString()
                )

                "Polish" -> translateAndUpdateUI(
                    Language.POLISH,
                    binding.translationText.text.toString()
                )

                "Portuguese" -> translateAndUpdateUI(
                    Language.PORTUGUESE,
                    binding.translationText.text.toString()
                )

                "Romanian" -> translateAndUpdateUI(
                    Language.ROMANIAN,
                    binding.translationText.text.toString()
                )

                "Russian" -> translateAndUpdateUI(
                    Language.RUSSIAN,
                    binding.translationText.text.toString()
                )

                "Slovak" -> translateAndUpdateUI(
                    Language.SLOVAK,
                    binding.translationText.text.toString()
                )

                "Slovenian" -> translateAndUpdateUI(
                    Language.SLOVENIAN,
                    binding.translationText.text.toString()
                )

                "Spanish" -> translateAndUpdateUI(
                    Language.SPANISH,
                    binding.translationText.text.toString()
                )

                "Swedish" -> translateAndUpdateUI(
                    Language.SWEDISH,
                    binding.translationText.text.toString()
                )

                "Swahili(Kenya)" -> translateAndUpdateUI(
                    Language.SWAHILI,
                    binding.translationText.text.toString()
                )

                "Tamil" -> translateAndUpdateUI(
                    Language.TAMIL,
                    binding.translationText.text.toString()
                )

                "Telugu" -> translateAndUpdateUI(
                    Language.TELUGU,
                    binding.translationText.text.toString()
                )

                "Thai" -> translateAndUpdateUI(
                    Language.THAI,
                    binding.translationText.text.toString()
                )

                "Turkish" -> translateAndUpdateUI(
                    Language.TURKISH,
                    binding.translationText.text.toString()
                )

                "Ukranian" -> translateAndUpdateUI(
                    Language.UKRAINIAN,
                    binding.translationText.text.toString()
                )

                "Urdu" -> translateAndUpdateUI(
                    Language.URDU,
                    binding.translationText.text.toString()
                )

                "Vietnamese" -> translateAndUpdateUI(
                    Language.VIETNAMESE,
                    binding.translationText.text.toString()
                )

                "Welsh" -> translateAndUpdateUI(
                    Language.WELSH,
                    binding.translationText.text.toString()
                )
            }
        }
    }

    private fun translateAndUpdateUI(lan: String, res: String) {
        try {
            val translateAPI = TranslateAPI(Language.AUTO_DETECT, lan, res)
            translateAPI.setTranslateListener(object : TranslateAPI.TranslateListener {
                override fun onSuccess(translatedText: String) {

                }

                override fun onFailure(errorText: String) {

                }
            })
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }


    // Function to load the interstitial ad
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,
            resources.getString(R.string.interstitial_ad_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {


                    mInterstitialAd = interstitialAd
                    navigateToNewActivity("")
                    showInterstitialAd()

                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    progressDialog.dismiss()
                    navigateToNewActivity("")
                    super.onAdFailedToLoad(loadAdError)
//                    loadInterstitialAd()

                }
            })
    }

    // Function to show the interstitial ad
    private fun showInterstitialAd() {

        mInterstitialAd?.show(this@LoadImageActivity)
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {

            }

            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null
                progressDialog.dismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Handle ad failed to show event
                mInterstitialAd = null

//                loadInterstitialAd()
            }

            override fun onAdImpression() {
                // Handle ad impression event
            }

            override fun onAdShowedFullScreenContent() {
                // Handle ad showed event
            }
        }
    }

    private fun navigateToNewActivity(translatedText: String) {
        imagePreferences =
            getSharedPreferences("IMAGE_PREFERENCES", Context.MODE_PRIVATE)
        val intent = Intent(applicationContext, OCRTranslationActivity::class.java)
        intent.putExtra("ocr_translation", imagePreferences.getString("imageText", "").toString())
        intent.putExtra(
            "ocr_text",
            imagePreferences.getString("imageText", "").toString()
        )
        intent.putExtra("ocr_first_language", firstCardLanguage)
        intent.putExtra("ocr_second_language", secondCardLanguage)

        startActivity(intent)
        finish()
    }

    private fun loadBanner() {
        val adView = AdView(this);
        adView.adUnitId = resources.getString(R.string.banner_ad_id)
        adView.setAdSize(AdSize.BANNER)
        val extras = Bundle()
        extras.putString("collapsible", "bottom")

        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        adView.loadAd(adRequest)
        adContainerView.addView(adView)

    }

}