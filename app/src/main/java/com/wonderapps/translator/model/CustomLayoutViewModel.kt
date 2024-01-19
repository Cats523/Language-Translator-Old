import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomLayoutViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences =
        application.getSharedPreferences("CustomLayoutPreferences", Context.MODE_PRIVATE)

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> get() = _text

    private val _textInserted = MutableLiveData<String>()
    val textInserted: LiveData<String> get() = _textInserted

    init {
        // Load initial state from SharedPreferences
        viewModelScope.launch(Dispatchers.IO) {
            _text.postValue(sharedPreferences.getString("text", "") ?: "")
            _textInserted.postValue(sharedPreferences.getString("textInserted", "") ?: "")
        }
    }

    fun saveState(text: String, textInserted: String) {
        // Save the state to SharedPreferences
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferences.edit()
                .putString("text", text)
                .putString("textInserted", textInserted)
                .apply()
        }
        // Update LiveData
        _text.postValue(text)
        _textInserted.postValue(textInserted)
    }
}
