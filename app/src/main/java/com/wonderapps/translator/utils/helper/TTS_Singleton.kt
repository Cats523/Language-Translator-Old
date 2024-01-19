import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log

object TextToSpeechSingleton {
    lateinit var textToSpeech: TextToSpeech
}

fun initializeTextToSpeech(context: Context) {
    TextToSpeechSingleton.textToSpeech = TextToSpeech(context) { status ->
        if (status != TextToSpeech.ERROR) {
            Log.d("succeSSFULLy", "initializeTextToSpeech: ")
        }
    }
}
