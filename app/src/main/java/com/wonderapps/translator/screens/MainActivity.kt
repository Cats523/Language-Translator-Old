package com.wonderapps.translator.screens


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.infideap.drawerbehavior.Advance3DDrawerLayout
import com.wonderapps.translator.R
import com.wonderapps.translator.databinding.FragmentDictionaryBinding
import com.wonderapps.translator.top_level_functions.helloMoto
import com.wonderapps.translator.utils.helper.AdCounterManager
import com.wonderapps.translator.utils.helper.AdStatus
import com.wonderapps.translator.utils.network.NetworkStatusReceiver
import com.wonderapps.translator.utils.services.LanguageTranslatorAccessibilityService
import com.wonderapps.translator.utils.services.MyService
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private val OVERLAY_PERMISSION_REQUEST_CODE = 101
    private val ACCESSIBILITY_PERMISSION_REQUEST_CODE = 102
    private lateinit var adContainerView: FrameLayout
    private var checkedItem = 7
    var trackValue = 0
    private var reviewManager: ReviewManager? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var appUpdateManager: AppUpdateManager? = null
    private lateinit var drawerLayout: Advance3DDrawerLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navigationView: NavigationView
    private lateinit var appConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar
    private lateinit var languages: Array<Language>
    private val PREFS_NAME = "MyPrefs"
    private val SELECTED_LANGUAGE_KEY = "selected_language"
    private var i = 0
    private var networkChangeReceiver = NetworkStatusReceiver()
    lateinit var binding: FragmentDictionaryBinding
    override fun onRestart() {
        super.onRestart()
        Log.d("abghvhgs", "onRestart: ")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appLanguagePreferences = getSharedPreferences(LANGUAGE_PREFERENCE_KEY, MODE_PRIVATE)
        val currentSystemLanguage = Locale.getDefault()


        val languageCode =
            appLanguagePreferences.getString(LANGUAGE_PREFERENCE_VALUE_KEY, "") ?: "en"



        setContentView(R.layout.activity_main)
        Log.d("screenChangeCode", "onCreate: hello 120 $languageCode")
        val tapTargetPreferences = getSharedPreferences("TapTargetPreferences", MODE_PRIVATE)
        val tapTargetShownBefore = tapTargetPreferences.getBoolean("TapTargetShownBefore", false)
        if (!tapTargetShownBefore) {
            showTapTarget()
            tapTargetPreferences.edit().putBoolean("TapTargetShownBefore", true).apply()
        }
        adContainerView = findViewById(R.id.adViewContainer)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        checkedItem = sharedPreferences.getInt(SELECTED_LANGUAGE_KEY, 7)
        registerNetworkBroadcastReceiver()
        loadBanner()
        val translatePreference =
            getSharedPreferences("Translate_Language_Preference", MODE_PRIVATE)
        val translatePreferenceEditor = translatePreference.edit()
        val firstLanguageName = translatePreference.getString("Language_1_Name", null)
        val firstLanguageCode = translatePreference.getString("Language_1_Code", null)
        languages = arrayOf(
            Language("Afrikaans", "af"),
            Language("Arabic", "ar"),
            Language("Bengali", "bn"),
            Language("Chinese", "zh"),
            Language("Czech", "cs"),
            Language("Danish", "da"),
            Language("Dutch", "nl"),
            Language("English", "en"),
            Language("Finnish", "fi"),
            Language("German", "de"),
            Language("Hindi", "hi"),
            Language("Indonesian", "in"),
            Language("Italian", "it"),
            Language("Japanese", "ja"),
            Language("Korean", "ko"),
            Language("Malay", "ms"),
            Language("Persian", "fa"),
            Language("Polish", "pl"),
            Language("Portuguese", "pt"),
            Language("Russian", "ru"),
            Language("Spanish", "es"),
            Language("Swedish", "sv"),
            Language("Tamil", "ta"),
            Language("Thai", "th"),
            Language("Turkish", "tr"),
            Language("Ukrainian", "uk"),
            Language("Urdu", "ur"),
            Language("Vietnamese", "vi")
        )
        val defaultLanguage = languages.find { it.code == currentSystemLanguage.language }

        if ((firstLanguageName == null || firstLanguageCode == null)) {
            if (defaultLanguage != null) {
                translatePreferenceEditor.putString("Language_2_Name", defaultLanguage.name)
                translatePreferenceEditor.putString("Language_2_Code", defaultLanguage.code)
                translatePreferenceEditor.putString("Language_1_Name", "English")
                translatePreferenceEditor.putString("Language_1_Code", "en")
                translatePreferenceEditor.apply()
            } else {
                translatePreferenceEditor.putString("Language_2_Name", "Arabic")
                translatePreferenceEditor.putString("Language_2_Code", "ar")
                translatePreferenceEditor.putString("Language_1_Name", "English")
                translatePreferenceEditor.putString("Language_1_CodeLanguage_1_Code", "en")
                translatePreferenceEditor.apply()
            }
        }
        val conversationPreference = getSharedPreferences("CHAT_LANGUAGE_PREFERENCE", MODE_PRIVATE)
        val conversationEditor = conversationPreference.edit()
        val chatFirstLanguageName =
            conversationPreference.getString("CHAT_FIRST_LANGUAGE_NAME", null)
        val chatFirstLanguageVoiceCode =
            conversationPreference.getString("CHAT_FIRST_LANGUAGE_VOICE_CODE", null)
        val chatFirstLanguageCode =
            conversationPreference.getString("CHAT_FIRST_LANGUAGE_CODE", null)

        if ((chatFirstLanguageName == null || chatFirstLanguageVoiceCode == null || chatFirstLanguageCode == null) && defaultLanguage != null) {
            conversationEditor.putString("CHAT_FIRST_LANGUAGE_NAME", "English")
            conversationEditor.putString("CHAT_FIRST_LANGUAGE_CODE", "en")
            conversationEditor.putString("CHAT_FIRST_LANGUAGE_VOICE_CODE", "en")
            conversationEditor.putString("CHAT_SECOND_LANGUAGE_NAME", defaultLanguage.name)
            conversationEditor.putString("CHAT_SECOND_LANGUAGE_CODE", defaultLanguage.code)
            conversationEditor.putString("CHAT_SECOND_LANGUAGE_VOICE_CODE", defaultLanguage.code)

            conversationEditor.apply()
        } else {
            conversationEditor.putString("CHAT_FIRST_LANGUAGE_NAME", "English")
            conversationEditor.putString("CHAT_FIRST_LANGUAGE_CODE", "en")
            conversationEditor.putString("CHAT_FIRST_LANGUAGE_VOICE_CODE", "en")
            conversationEditor.putString("CHAT_SECOND_LANGUAGE_NAME", "Arabic")
            conversationEditor.putString("CHAT_SECOND_LANGUAGE_CODE", "ar")
            conversationEditor.putString("CHAT_SECOND_LANGUAGE_VOICE_CODE", "ar")

            conversationEditor.apply()
        }


        val ocrPreference = getSharedPreferences("OCR_LANGUAGE_PREFERENCE", MODE_PRIVATE)
        val ocrPreferenceEditor = ocrPreference.edit()
        val ocrFirstLanguageName = ocrPreference.getString("OCR_FIRST_LANGUAGE_NAME", null)
        val ocrFirstLanguageCode = ocrPreference.getString("OCR_FIRST_LANGUAGE_CODE", null)
        val ocrFirstLanguageModelCode =
            ocrPreference.getString("OCR_FIRST_LANGUAGE_MODEL_CODE", null)

        if (ocrFirstLanguageName == null || ocrFirstLanguageCode == null || ocrFirstLanguageModelCode == null) {
            ocrPreferenceEditor.putString("OCR_FIRST_LANGUAGE_NAME", "English")
            ocrPreferenceEditor.putString("OCR_FIRST_LANGUAGE_CODE", "en")
            ocrPreferenceEditor.putString("OCR_FIRST_LANGUAGE_MODEL_CODE", "eng")
            ocrPreferenceEditor.apply()
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolbar_main_activity)
        val toolbar_textView = findViewById<TextView>(R.id.toolbar_textview_title)

        setSupportActionBar(toolbar)

        getSupportActionBar()?.setDisplayShowTitleEnabled(false);

        toolbar.setTitle("");
        toolbar.setSubtitle("");
        Log.d(
            "checkToolbarlan", "onCreate: ${
                toolbar.title
            }"
        )
        bottomNav = findViewById(R.id.bottomNavigationView)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        appConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.conversionFragment,
                R.id.OCRFragment,
                R.id.dictionaryFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appConfiguration)
        bottomNav.setupWithNavController(navController)
        navigationView = findViewById(R.id.navigationView)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer
        )
        drawerLayout.addDrawerListener(toggle)

        val versionName: String = applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName, 0
        ).versionName
        "${getString(R.string.app_name)} $versionName".also {
            navigationView.getHeaderView(0)
                .findViewById<TextView>(R.id.navigation_header_text_view).text = it
        }


        toggle.drawerArrowDrawable.color = Color.WHITE
        val moveAbleIconBtn = findViewById<TextView>(R.id.moveAbleIconBtn)
        moveAbleIconBtn.setOnClickListener {


            if (!Settings.canDrawOverlays(this)) {

                requestOverlayPermission()
                return@setOnClickListener
            }

            if (!isServiceRunning(MyService::class.java) || !isServiceRunning(
                    LanguageTranslatorAccessibilityService::class.java
                )
            ) {

                startForegroundServiceAndAccessibility()
                showUserConsentDialog()
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> toolbar_textView.text = getString(R.string.Translate)
                R.id.conversionFragment -> toolbar_textView.text = getString(R.string.Conversation)
                R.id.dictionaryFragment -> toolbar_textView.text = getString(R.string.Dictionary)
                R.id.OCRFragment -> toolbar_textView.text = getString(R.string.OCR)


            }
            val toolbar = findViewById<TextView>(R.id.clear_text_toolbar)
            AdCounterManager.incrementAdCounter()
            val currentAdCounter = AdCounterManager.getAdCounter()

            if (currentAdCounter == 6) {
                showInterstitialAd()

                AdCounterManager.resetAdCounter()
            }
            if (destination.id == R.id.homeFragment || destination.id == R.id.OCRFragment || destination.id == R.id.dictionaryFragment) {
                helloMoto = 0
                toolbar.visibility = View.GONE
            }
            if (destination.id in arrayOf(
                    R.id.homeFragment,
                    R.id.conversionFragment,
                    R.id.OCRFragment,
                    R.id.dictionaryFragment
                )
            ) {
                toggle.drawerArrowDrawable.color = Color.WHITE
                toggle.syncState()
            }
        }




        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.shareWithFriends -> {
                    try {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val shareBody =
                        "Download Language Translator App On Play Store: https://play.google.com/store/apps/details?id=com.wonderapps.translator"
                    val shareSub = "Type, speak & translate"
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                    startActivity(shareIntent)
                }

                R.id.changeLanguage -> {
                    try {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    languageDialog2()
                }

                R.id.privacyPolicy -> {
                    try {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val url = "https://wonderappsstudio.blogspot.com/2020/11/PrivacyPolicy.html"
                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    i.setPackage("com.android.chrome")
                    try {
                        startActivity(i)
                    } catch (e: ActivityNotFoundException) {
                        i.setPackage(null)
                        startActivity(i)
                    }
                }
            }
            return@setNavigationItemSelectedListener true
        }

    }


    private fun getAdSize(): AdSize {
        return AdSize.BANNER
    }


    private fun loadBanner() {
        val adView = AdView(this)
        adView.adUnitId = resources.getString(R.string.banner_ad_id)
        adView.setAdSize(getAdSize())
        val extras = Bundle()
        extras.putString("collapsible", "bottom")

        val adRequest =
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()

        adView.loadAd(adRequest)
        adContainerView.addView(adView)

    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,
            resources.getString(R.string.interstitial_ad_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {


                    mInterstitialAd = interstitialAd


                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null


                    super.onAdFailedToLoad(loadAdError)
//                    loadInterstitialAd()

                }
            })
    }

    // Function to show the interstitial ad
    private fun showInterstitialAd() {

        mInterstitialAd?.show(this@MainActivity)
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {

            }

            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null

            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Handle ad failed to show event
                mInterstitialAd = null

//                loadInterstitialAd()
            }

            override fun onAdImpression() {
                // Handle ad impression event
            }

            override fun onAdShowedFullScreenContent() {
                // Handle ad showed event
            }
        }
    }

    private fun showTapTarget() {
        TapTargetView.showFor(this,
            TapTarget.forView(
                findViewById<View>(R.id.moveAbleIconBtn),
                "Dynamic Translation Button",
                "Turn on This feature to Translate the Text from any where without opening App . Just Drag the Movable Head and translation are ready"
            ).outerCircleColor(R.color.purple_500).outerCircleAlpha(0.96f)
                .targetCircleColor(R.color.white).titleTextSize(20).titleTextColor(R.color.white)
                .descriptionTextSize(10).descriptionTextColor(R.color.white)
                .textColor(R.color.white).textTypeface(Typeface.SANS_SERIF).dimColor(R.color.black)
                .drawShadow(true).cancelable(false).tintTarget(false).transparentTarget(false)
                .targetRadius(60),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)

                }
            })
    }

    private fun languageDialog2() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val alertDialogBuilder = AlertDialog.Builder(this)
        var tempCheckedItem = checkedItem

        alertDialogBuilder.apply {
            setTitle(getString(R.string.SelectLanguage))

            setSingleChoiceItems(
                languages.map { it.name }.toTypedArray(), checkedItem
            ) { _, which ->
                // Update tempCheckedItem when the user selects an item
                tempCheckedItem = which

            }

            setPositiveButton(getString(R.string.Ok)) { _, _ ->
                // Save the selected language position
                checkedItem = tempCheckedItem
                with(sharedPreferences.edit()) {
                    putInt(SELECTED_LANGUAGE_KEY, checkedItem)
                    apply()
                }
                val selectedLanguage = languages[checkedItem]

                restartActivity(selectedLanguage.code)

            }

            setNegativeButton(getString(R.string.Cancel)) { dialog, _ -> dialog.dismiss() }

            create().show()
        }


    }

    private fun showRateApp(ctx: Activity?) {
        try {
            reviewManager = ReviewManagerFactory.create(ctx!!)
            val request: Task<ReviewInfo?> = reviewManager?.requestReviewFlow()!!
            request.addOnCompleteListener { task: Task<ReviewInfo?> ->
                if (task.isSuccessful) {
                    // We can get the ReviewInfo object
                    val reviewInfo = task.result
                    val flow: Task<Void?> = reviewManager?.launchReviewFlow(ctx, reviewInfo!!)!!
                    flow.addOnCompleteListener { }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showRateApp(this)
        //review
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_exit_dialog)
        val exitBtn = dialog.findViewById<Button>(R.id.exitBtn)
        exitBtn.setOnClickListener {
            finishAffinity()
        }
        val cancelBtn = dialog.findViewById<TextView>(R.id.cancelBtn)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.create()
        dialog.show()

    }

    private fun setLanguage(activity: Activity, language: String) {

        try {


            val languagePreferences = getSharedPreferences(LANGUAGE_PREFERENCE_KEY, MODE_PRIVATE)
            val languagePreferenceEditor = languagePreferences.edit()
            languagePreferenceEditor.putString(LANGUAGE_PREFERENCE_VALUE_KEY, language)
            languagePreferenceEditor.apply()
            val radioButtonPreferences =
                getSharedPreferences(RADIO_BUTTON_PREFERENCE_KEY, MODE_PRIVATE)
            val myEditor = radioButtonPreferences.edit()
            myEditor.putInt(CHECKED_RADIO_BUTTON_KEY, checkedItem)
            myEditor.apply()
            val appLocale = LocaleListCompat.forLanguageTags(language)
            AppCompatDelegate.setApplicationLocales(appLocale)
        } catch (e: Exception) {
            Log.e("setLanguage", e.message.toString())
        }
    }

    private fun getAllPermissions(): Boolean {
        val permissions = mutableListOf<String>()
        if (!hasPermissions()) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.CAMERA)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this@MainActivity, permissions.toTypedArray(), 10000)
            return true
        }
        return false
    }

    private fun hasPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 5000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Camera Permission Granted", Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_LONG)
                    .show()
            }
        } else if (requestCode == 7000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Storage Permission Granted", Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(this@MainActivity, "Storage Permission Denied", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun inAppUp() {
        val task: Task<AppUpdateInfo> = appUpdateManager!!.appUpdateInfo
        task.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() === UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.FLEXIBLE
                )
            ) {
                try {
                    appUpdateManager!!.startUpdateFlowForResult(
                        appUpdateInfo, AppUpdateType.FLEXIBLE, this@MainActivity, 555
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
        appUpdateManager!!.registerListener(installStateUpdatedListener)
    }

    // updates installation
    private val installStateUpdatedListener: InstallStateUpdatedListener =
        InstallStateUpdatedListener {

            if (it.installStatus() === InstallStatus.DOWNLOADED) {
                showCompleted()
            }
        }

    //show completed update installed
    private fun showCompleted() {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content), "New App Is Ready", Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction(
            "install"
        ) { appUpdateManager!!.completeUpdate() }
        snackbar.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()

        Log.d("screenChangeCode", "onCreate: hello 120 ")
        val tapTargetPreferences = getSharedPreferences("Demo", MODE_PRIVATE)
        val tapTargetShownBefore = tapTargetPreferences.getBoolean("Demo", false)
        if ((isServiceRunning(MyService::class.java) && isServiceRunning(
                LanguageTranslatorAccessibilityService::class.java
            )) && !tapTargetShownBefore
        ) {
            tapTargetPreferences.edit().putBoolean("Demo", true).apply()
            val overlayIntent = Intent(this, OverlayScreen::class.java)
            overlayIntent.putExtra("ACCESSIBILITY_SERVICE", "SHOW_DEMO")
            startActivity(overlayIntent)

        }
        loadInterstitialAd()
        if (AdStatus.isAdDismissed) {
            AdStatus.isAdDismissed = false
            getAllPermissions()
            if (i == 0) {
                i++
                inAppUp()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (Settings.canDrawOverlays(this)) {
                    startForegroundServiceAndAccessibility()
                    showUserConsentDialog()

                } else {
                    // Handle overlay permission denial
                    showPermissionDeniedMessage("Overlay permission required")
                }
            }

        }
    }

    private fun registerNetworkBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
    }

    private fun unregisterNetworkBroadcastReceiver() {
        try {
            if (networkChangeReceiver != null) unregisterReceiver(networkChangeReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestOverlayPermission() {

        val intent =
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        Handler().postDelayed({
            val overlayIntent = Intent(this, OverlayScreen::class.java)
            overlayIntent.putExtra("ACCESSIBILITY_SERVICE", "DISPLAY_OVER_APP_SERVICE")
            startActivity(overlayIntent)
        }, 100)
    }

    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent, ACCESSIBILITY_PERMISSION_REQUEST_CODE)
        Handler().postDelayed({
            val overlayIntent = Intent(this, OverlayScreen::class.java)
            overlayIntent.putExtra("ACCESSIBILITY_SERVICE", "ACCESSIBILITY_SERVICE")
            startActivity(overlayIntent)
        }, 100)

    }

    private fun showUserConsentDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_user_consent, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<Button>(R.id.btnAccept).setOnClickListener {
            bottomSheetDialog.dismiss()
            requestAccessibilityPermission()

        }

        bottomSheetDialog.show()
    }

    private fun showPermissionDeniedMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startForegroundServiceAndAccessibility() {

        ContextCompat.startForegroundService(this, Intent(this, MyService::class.java))
    }

    @SuppressLint("ServiceCast")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkBroadcastReceiver()
    }

    private fun restartActivity(code: String) {
        Handler().postDelayed({
            setLanguage(this@MainActivity, code)

        }, 30)
        val refresh = Intent(this, MainActivity::class.java)
        startActivity(refresh)
        finish()

    }

    data class Language(val name: String, val code: String)
    companion object {
        const val LANGUAGE_PREFERENCE_KEY = "APP_LANGUAGE_PREFERENCE"
        const val LANGUAGE_PREFERENCE_VALUE_KEY = "LANGUAGE_PREFERENCE_VALUE"
        const val RADIO_BUTTON_PREFERENCE_KEY = "SELECTED_RADIO_BUTTON_PREFERENCE"
        const val CHECKED_RADIO_BUTTON_KEY = "CHECKED_RADIO_BUTTON"
    }

}