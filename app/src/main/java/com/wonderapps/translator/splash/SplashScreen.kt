package com.wonderapps.translator.splash


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.ads.GDPRMessage
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.wonderapps.translator.R
import com.wonderapps.translator.screens.MainActivity
import com.wonderapps.translator.utils.helper.AdStatus
import com.wonderapps.translator.utils.network.NetworkStatusReceiver
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class SplashScreen : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private var remainingTime: Long = 6000
    private lateinit var lottieAnimationView: LottieAnimationView

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)
        lottieAnimationView = findViewById(R.id.lottieAnimation)



        if (savedInstanceState != null) {
            remainingTime = savedInstanceState.getLong("remainingTime")

        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putLong("remainingTime", remainingTime)
    }

    private fun isNetworkConnected(context: Context): Boolean {
        var result = false
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = isCapableNetwork(this, this.activeNetwork)
            } else {
                val networkInfo = this.allNetworks
                for (tempNetworkInfo in networkInfo) {
                    if (isCapableNetwork(this, tempNetworkInfo))
                        result = true
                }
            }
        }

        return result
    }

    override fun onResume() {
        super.onResume()
        val status = isNetworkConnected(this@SplashScreen)


        if (status) {

            val consent = GDPRMessage(this)
            consent.consentMessageRequest()
            consent.getConsent {
                loadInterstitialAd()
                startCountdownTimer()
            }

        } else {

            Handler().postDelayed({
                navigateToNewActivity()
            }, 4000)
        }
    }

    private fun isCapableNetwork(cm: ConnectivityManager, network: Network?): Boolean {
        cm.getNetworkCapabilities(network)?.also {
            if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            }
        }
        return false
    }

    private fun startCountdownTimer() {

        lottieAnimationView.playAnimation()
        timer?.cancel() // Cancel any previous
        timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                remainingTime -= 1000

                if (mInterstitialAd != null) {

                    remainingTime = 0
                }
                if (remainingTime <= 0) {
                    runOnUiThread {
                        if (mInterstitialAd != null) {
                            showInterstitialAd()
                        } else {
                            AdStatus.isAdDismissed = true
                            navigateToNewActivity()
                        }
                    }
                    timer?.cancel()
                }
            }
        }
        timer?.scheduleAtFixedRate(task, 1000, 1000)

    }

    private fun pauseCountdownTimer() {
        timer?.cancel()
        timerTask?.cancel()

    }

    override fun onPause() {
        super.onPause()
        pauseCountdownTimer()
    }

    // Function to load the interstitial ad
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,
            getResources().getString(R.string.splash_interstitial_ad_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {

                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    AdStatus.isAdDismissed = true
                    navigateToNewActivity()
                }
            })
    }

    // Function to show the interstitial ad
    private fun showInterstitialAd() {
        navigateToNewActivity()

        mInterstitialAd?.show(this@SplashScreen)
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {

            }

            override fun onAdDismissedFullScreenContent() {


                mInterstitialAd = null
                AdStatus.isAdDismissed = true
                finish()

            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Handle ad failed to show event
                mInterstitialAd = null
            }

            override fun onAdImpression() {
            }

            override fun onAdShowedFullScreenContent() {
                // Handle ad showed event
            }
        }
    }

    private fun navigateToNewActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


}
