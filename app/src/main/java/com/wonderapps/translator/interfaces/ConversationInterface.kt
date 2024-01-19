package com.wonderapps.translator.interfaces

import android.content.SharedPreferences

interface ConversationInterface {



    var sharedPreferences1 : SharedPreferences

    var editor1 : SharedPreferences.Editor
    var editor2 : SharedPreferences.Editor



     var firstCardLanguage : String
     var firstLanguageVoiceCode : String
      var secondCardLanguage : String
     var secondLanguageVoiceCode : String

     var firstLanguageFlag : Int
     var secondLanguageFlag : Int


     var firstLanguageCode : String
     var secondLanguageCode : String




    var firstLanguagePosition : Int
    var secondLanguagePosition : Int

}