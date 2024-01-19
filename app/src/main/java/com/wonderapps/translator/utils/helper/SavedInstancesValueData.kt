package com.wonderapps.translator.utils.helper

object SavedInstancesValueData {
    var editTextData: String = ""
    var translatedTextData: String = ""
    fun setData(t1: String, t2: String) {
        editTextData = t1
        translatedTextData = t2
    }
}