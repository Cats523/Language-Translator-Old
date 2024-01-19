package com.wonderapps.translator.utils.helper

object AdCounterManager {
    private var adCounter = 0

    fun incrementAdCounter() {
        adCounter++
    }

    fun resetAdCounter() {
        adCounter = 0
    }

    fun getAdCounter(): Int {
        return adCounter
    }
}
