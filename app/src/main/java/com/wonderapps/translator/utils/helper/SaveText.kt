package com.wonderapps.translator.utils.helper




object SaveText {

    private var text = ""
    var tarnslatedText = ""


    fun setText(data: String) {
        text = data
    }

    fun setTranslatedText(data: String) {
        tarnslatedText = data
    }

    fun getTranslatedText(): String {
        return tarnslatedText
    }

    fun getText(): String {
        if (text.isEmpty()) {
            return "text Not Found"
        }
        return text
    }


}

