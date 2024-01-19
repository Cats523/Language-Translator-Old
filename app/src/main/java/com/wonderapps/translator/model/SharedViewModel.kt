package com.wonderapps.translator.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel: ViewModel() {
    val changeLanguage = MutableLiveData<String>()

    fun setLanguage(language: String) {
        changeLanguage.value = language
    }
}