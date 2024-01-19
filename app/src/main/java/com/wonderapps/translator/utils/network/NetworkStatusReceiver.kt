package com.wonderapps.translator.utils.network


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkStatusReceiver : BroadcastReceiver() {
    private val networkStatusLiveData = MutableLiveData<Boolean>()
    override fun onReceive(context: Context?, intent: Intent?) {
        val isNetworkAvailable = isNetworkAvailable(context)
        networkStatusLiveData.postValue(isNetworkAvailable)


    }

    fun getNetworkStatusLiveData(): LiveData<Boolean> {
        return networkStatusLiveData
    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork

        if (network != null) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }

        return false
    }
}




