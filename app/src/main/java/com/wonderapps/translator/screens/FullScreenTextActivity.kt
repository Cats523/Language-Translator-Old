package com.wonderapps.translator.screens

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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wonderapps.translator.R
import com.wonderapps.translator.databinding.ActivityFullScreenTextBinding
import kotlin.properties.Delegates

class FullScreenTextActivity : AppCompatActivity() {
    lateinit var binding: ActivityFullScreenTextBinding
    lateinit var languageName: String
    var position by Delegates.notNull<Int>()
    lateinit var text: String
    private lateinit var textToSpeech: TextToSpeech


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@FullScreenTextActivity,
            R.layout.activity_full_screen_text
        )
        setContentView(binding.root)

        setSupportActionBar(binding.fullScreenToolBarActivity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        var intent = intent
        languageName = intent.getStringExtra("LanguageName").toString()
        if (languageName == "Persian") {

            binding.volumeBtnInFullScreenTextActivity.visibility = View.GONE

        } else {
            binding.volumeBtnInFullScreenTextActivity.visibility = View.VISIBLE
        }
        position = intent.getIntExtra("position", 0)

        text = intent.getStringExtra("text").toString()

        textToSpeech = TextToSpeech(this, {})

        binding.textFullScreen.setText(text).toString()
        binding.languageNameInFullScreenTextActivity.setText(languageName).toString()
        binding.fullScreenIconInFullScreenTextActivity.setImageResource(R.drawable.ic_baseline_close_fullscreen_24)

        binding.fullScreenIconInFullScreenTextActivity.setOnClickListener {
            onBackPressed()
        }


        binding.fullScreenToolBarActivity.setNavigationOnClickListener {
            onBackPressed()
        }


        binding.copyContentInFullScreenTextActivity.setOnClickListener {

            if (binding.textFullScreen.text.isNotBlank() || binding.textFullScreen.text.isNotEmpty() || (!(binding.textFullScreen.text.equals(
                    ""
                )))
            ) {
                copyContent()
            } else {
                Toast.makeText(applicationContext, R.string.NoTextFoundToCopy, Toast.LENGTH_LONG)
                    .show()
            }
        }


        binding.shareTranslationTextInFullScreenTextActivity.setOnClickListener {
            shareTranslation()
        }

        binding.volumeBtnInFullScreenTextActivity.setOnClickListener {
            if (binding.textFullScreen.text.isNotEmpty() || (!(binding.textFullScreen.text.equals(""))) || binding.textFullScreen.text.isNotBlank()) {
                textToSpeech.speak(
                    binding.textFullScreen.text.toString(),
                    TextToSpeech.QUEUE_FLUSH, null, ""
                )
                val handler = Handler(Looper.getMainLooper())

                textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        // This method is called when text-to-speech starts speaking.
                        handler.post {
                            binding.volumeBtnInFullScreenTextActivity.visibility = View.GONE
                            binding.stopSpeakingBtn.visibility = View.VISIBLE
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        // This method is called when text-to-speech is done speaking.
                        handler.post {
                            binding.volumeBtnInFullScreenTextActivity.visibility = View.VISIBLE
                            binding.stopSpeakingBtn.visibility = View.GONE
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        // Handle any errors that occur during speech
                        Log.e("speakTranslatedText", "Speech error")
                    }
                })
            } else {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.NoTextToSpeak),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.stopSpeakingBtn.setOnClickListener {
            binding.volumeBtnInFullScreenTextActivity.visibility = View.VISIBLE
            binding.stopSpeakingBtn.visibility = View.GONE
            textToSpeech.stop()
        }

        if (position == 1) {
            binding.copyContentInFullScreenTextActivity.visibility = View.GONE
            binding.shareTranslationTextInFullScreenTextActivity.visibility = View.GONE
            binding.volumeBtnInFullScreenTextActivity.visibility = View.GONE
        }
    }

    fun shareTranslation() {
        if (binding.textFullScreen.text.isNotEmpty() || (!(binding.textFullScreen.text.equals(""))) || binding.textFullScreen.text.isNotBlank()) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, binding.textFullScreen.text.toString());
            startActivity(Intent.createChooser(shareIntent, getString(R.string.shareVia)))
        } else {
            Toast.makeText(applicationContext, R.string.NoTextFoundToShare, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyContent() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", binding.textFullScreen.text.toString())
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(applicationContext, R.string.CopiedToClipboard, Toast.LENGTH_LONG).show()
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

}