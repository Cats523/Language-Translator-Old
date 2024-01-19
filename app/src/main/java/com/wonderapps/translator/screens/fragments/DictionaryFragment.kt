package com.wonderapps.translator.screens.fragments

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.wonderapps.translator.R
import com.wonderapps.translator.databinding.FragmentDictionaryBinding
import com.wonderapps.translator.utils.network.BottomSheetFragment
import com.wonderapps.translator.utils.network.NetworkStatusReceiver
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class DictionaryFragment : Fragment() {
    private lateinit var bottomSheetFragment: BottomSheetFragment
    private var networkChangeReceiver = NetworkStatusReceiver()
    lateinit var binding: FragmentDictionaryBinding
    private val internetCheck: MutableList<Boolean> = mutableListOf()
    private var URL: String = "https://api.dictionaryapi.dev/api/v2/entries/en/"
    private lateinit var queue: RequestQueue
    private lateinit var progressDialog: ProgressDialog
    private lateinit var wordSearched: String
    private lateinit var textToSpeech: TextToSpeech
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bottomSheetFragment = BottomSheetFragment()

        registerNetworkBroadcastReceiver()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dictionary, container, false)

        val view = binding.root

        progressDialog = ProgressDialog(requireContext())
        textToSpeech = TextToSpeech(activity) {}
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
        binding.autoCompleteTextView.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val inputMethodManager =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    binding.autoCompleteTextView.windowToken,
                    0
                )


                wordSearched = binding.autoCompleteTextView.text.toString().trim()
                when (true) {

                    (false in internetCheck) -> {
                        internetWarning()
                    }

                    else -> {
                        progressDialog.setTitle(R.string.Searching)
                        progressDialog.setCancelable(true)
                        progressDialog.create()
                        progressDialog.show()
                        if (wordSearched == "") {
                            Toast.makeText(
                                requireContext(),
                                "Field are empty ",
                                Toast.LENGTH_LONG
                            ).show()
                            progressDialog.dismiss()
                        } else {
                            lifecycleScope.launch {
                                searchWordOnline(wordSearched.trim())
                            }
                        }
                    }
                }
                return@OnEditorActionListener true
            }
            false
        })

        binding.wordPronounciationBtn.setOnClickListener {
            textToSpeech.speak(

                binding.wordSearched.text.toString(),
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        }

        val autoCompleteTextView =
            view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        val wordAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            readWordsFromFile(requireContext())
        )
        autoCompleteTextView.setAdapter(wordAdapter)
        autoCompleteTextView.threshold = 1

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    val filteredWords = getMatchingWords(s.toString())
                    wordAdapter.clear()
                    wordAdapter.addAll(filteredWords)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        autoCompleteTextView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {

                if (event.rawX >= (autoCompleteTextView.right - autoCompleteTextView.compoundDrawables[2].bounds.width())) {

                    val inputMethodManager =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(
                        binding.autoCompleteTextView.windowToken,
                        0
                    )
                    wordSearched = binding.autoCompleteTextView.text.toString().trim()
                    when (true) {

                        (false in internetCheck) -> {
                            internetWarning()
                        }

                        else -> {
                            progressDialog.setTitle(R.string.Searching)
                            progressDialog.setCancelable(true)
                            progressDialog.create()
                            progressDialog.show()
                            if (wordSearched == "") {
                                Toast.makeText(
                                    requireContext(),
                                    "Field are empty ",
                                    Toast.LENGTH_LONG
                                ).show()
                                progressDialog.dismiss()
                            } else {
                                lifecycleScope.launch {
                                    searchWordOnline(wordSearched.trim())
                                }
                            }
                        }
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedWord = wordAdapter.getItem(position)
            autoCompleteTextView.setText(selectedWord)
            autoCompleteTextView.setSelection(selectedWord!!.length)
            val inputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                binding.autoCompleteTextView.windowToken,
                0
            )
            when (true) {

                (false in internetCheck) -> {
                    internetWarning()
                }

                else -> {
                    progressDialog.setTitle(R.string.Searching)
                    progressDialog.setCancelable(true)
                    progressDialog.create()
                    progressDialog.show()
                    lifecycleScope.launch {
                        searchWordOnline(selectedWord.trim())
                    }
                }
            }


        }



        return view
    }

    fun readWordsFromFile(context: Context): List<String> {
        val resourceId = R.raw.my_words
        val words: MutableList<String> = mutableListOf()

        try {
            val inputStream: InputStream = context.resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                words.add(line!!)
            }

            inputStream.close()

        } catch (e: Exception) {
            Log.e("FileReadingExample", "Error reading file: ${e.message}")
        }

        return words
    }

    private fun searchWordOnline(word: String) {
        val dictionaryApiUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/"
        val requestUrl = "$dictionaryApiUrl$word"

        val request = JsonArrayRequest(Request.Method.GET, requestUrl, null,
            { response ->
                progressDialog.dismiss()
                try {
                    val jsonObject = response.getJSONObject(0)

                    binding.wordSearched.apply {
                        text = ""
                        binding.wordPronounciationBtn.visibility = View.VISIBLE
                        visibility = View.VISIBLE
                        text = word
                    }

                    val phoneticsArray = jsonObject.getJSONArray("phonetics")

                    if (phoneticsArray.length() > 0) {

                        for (i in 0 until phoneticsArray.length()) {
                            val phoneticObject = phoneticsArray.getJSONObject(i)
                            val phoneticText = phoneticObject.optString("text")

                            if (phoneticText.isNotEmpty()) {
                                binding.phoneticOfSearchedWord.apply {
                                    text = ""
                                    visibility = View.VISIBLE
                                    append(phoneticText)
                                }

                                break
                            }
                        }
                    } else {

                        binding.phoneticOfSearchedWord.visibility = View.GONE
                    }


                    val meanings = jsonObject.getJSONArray("meanings")
                    if (meanings.length() > 0) {
                        // Choose a random meaning index
                        val randomMeaningIndex = (0 until meanings.length()).random()
                        val meaning = meanings.getJSONObject(randomMeaningIndex)

                        val partOfSpeech = meaning.optString("partOfSpeech")
                        if (partOfSpeech.isNotEmpty()) {
                            binding.partOfSpeechDummy.visibility = View.VISIBLE
                            binding.partOfSpeech.apply {
                                visibility = View.VISIBLE
                                text = partOfSpeech
                            }
                        } else {
                            binding.partOfSpeech.visibility = View.GONE
                            binding.partOfSpeechDummy.visibility = View.GONE
                        }

                        val definitions = meaning.getJSONArray("definitions")
                        if (definitions.length() > 0) {
                            val randomDefinitionIndex = (0 until definitions.length()).random()
                            val definition = definitions.getJSONObject(randomDefinitionIndex)
                                .optString("definition")
                            if (definition.isNotEmpty()) {
                                binding.detailsLinearLayout.visibility = View.VISIBLE
                                binding.definition.apply {
                                    visibility = View.VISIBLE
                                    text = definition
                                }
                            } else {
                                binding.definition.visibility = View.GONE
                                binding.detailsLinearLayout.visibility = View.GONE
                            }

                            val example = definitions.getJSONObject(randomDefinitionIndex)
                                .optString("example")
                            if (example.isNotEmpty()) {
                                binding.exampleDummy.visibility = View.VISIBLE
                                binding.examples.apply {
                                    visibility = View.VISIBLE
                                    text = example
                                }
                            } else {
                                binding.examples.visibility = View.GONE
                                binding.exampleDummy.visibility = View.GONE
                            }
                        }
                    }
                } catch (e: Exception) {
                    handleDictionaryError()
                }
            },
            {
                wordSearched = ""
                URL = dictionaryApiUrl
                progressDialog.cancel()
                showNotFoundErrorDialog()
            })

        queue = Volley.newRequestQueue(requireContext())
        queue.add(request)
    }

    private fun handleDictionaryError() {
        URL = "https://api.dictionaryapi.dev/api/v2/entries/en/"
        wordSearched = ""
        progressDialog.dismiss()
    }

    private fun showNotFoundErrorDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(R.string.NotFound)
        alertDialog.setMessage(R.string.NotFoundMsg)
        alertDialog.setPositiveButton(R.string.Ok) { _, _ ->
            alertDialog.setCancelable(true)
        }
        alertDialog.setNegativeButton("") { _, _ -> }

        alertDialog.create().show()
    }

    private fun internetWarning() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Internet")
        alertDialog.setMessage(getString(R.string.internet_warning_message))
        alertDialog.setPositiveButton(R.string.Ok) { _, _ ->
            alertDialog.setCancelable(true)
        }
        alertDialog.setNegativeButton("") { _, _ -> }

        alertDialog.create().show()
    }

    private fun getMatchingWords(input: String): List<String> {
        val allWords = readWordsFromFile(requireContext())
        val filteredWords = allWords.filter { it.startsWith(input, ignoreCase = true) }
        return filteredWords.take(5)
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
    }


    override fun onPause() {
        super.onPause()
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }

    }
}

