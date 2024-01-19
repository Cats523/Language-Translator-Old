package com.wonderapps.translator.utils.services


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gusakov.library.PulseCountDown
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import com.torrydo.floatingbubbleview.FloatingBubbleListener
import com.torrydo.floatingbubbleview.service.expandable.BubbleBuilder
import com.torrydo.floatingbubbleview.service.expandable.ExpandableBubbleService
import com.torrydo.floatingbubbleview.service.expandable.ExpandedBubbleBuilder
import com.wonderapps.translator.R
import com.wonderapps.translator.adapter.HorizontalListViewRv
import com.wonderapps.translator.adapter.OnItemClickListener
import com.wonderapps.translator.top_level_functions.getLanguages_for_translate_fragment
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage
import com.wonderapps.translator.utils.helper.SaveText

class UpdatableImageView(context: Context) : AppCompatImageView(context) {

    private var defaultWidth = 100
    private var defaultHeight = 100


    init {

        layoutParams = ViewGroup.LayoutParams(defaultWidth, defaultHeight)
    }


    fun updateImage(imageId: Int, width: Int = defaultWidth, height: Int = defaultHeight) {
        val drawable = resources.getDrawable(imageId, null)

        setImageDrawable(drawable)

        layoutParams.width = width
        layoutParams.height = height


        requestLayout()
    }
}


class MyService : ExpandableBubbleService() {
    private var customLayout: View? = null
    private lateinit var languageList: ArrayList<ConversationLanguage>
    private var undoCustomLayout: View? = null

    var i = 0
    var actualText: TextView? = null
    var translatedTextView: TextView? = null
    private var isTranslationInProgress = false


    override fun onCreate() {
        try {
            super.onCreate()
            minimize()

        } catch (e: Exception) {

        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {

        super.onTaskRemoved(rootIntent)
    }

    override fun configBubble(): BubbleBuilder {

        return try {

            val image1 = UpdatableImageView(this)
            image1.updateImage(R.drawable.app_icon, 100, 100)

            BubbleBuilder(this)
                .bubbleView(image1).bubbleStyle(null).startLocation(0, 0)
                .startLocationPx(0, 0)
                .enableAnimateToEdge(true)
                .addFloatingBubbleListener(object : FloatingBubbleListener {


                    private val longPressDuration = 100
                    private val handler = Handler()
                    private var longPressRunnable: Runnable? = null

                    override fun onFingerMove(x: Float, y: Float) {

                        if ((!isServiceRunning(MyService::class.java) || !isServiceRunning(
                                LanguageTranslatorAccessibilityService::class.java
                            )
                                    ) && i == 0
                        ) {

                            i++
                            showMessege("please Turn On Accessibility Service To Read Content")
                            return
                        }
                        Log.d("fingerIsMoving", "onFingerMove: ")
                        if (isTranslationInProgress) {

                            return
                        }
                        longPressRunnable?.let { handler.removeCallbacks(it) }

                        longPressRunnable = Runnable {
                            try {
                                Log.d("fingerIsMoving", "Long Press: ")
                                val savedContentInfo =
                                    LanguageTranslatorAccessibilityService.ContentManager.getVisibleNodes()
                                Log.d("fingerIsMoving", "Long Press: ${savedContentInfo.size} ")
                                for (contentInfo in savedContentInfo) {
                                    Log.d("fingerIsMoving", "Long Press:${contentInfo.viewType} ")
                                    if (contentInfo.viewType == "UnknownView") {
                                        continue
                                    }

                                    if (isCoordinateInRange(
                                            contentInfo.bounds,
                                            x.toInt(),
                                            y.toInt()
                                        )
                                    ) {

                                        if (contentInfo.viewType == "EditText" && contentInfo.text.isNotEmpty()) {
                                            translateAndUpdateUI(
                                                contentInfo.text,
                                                contentInfo.bounds,
                                                contentInfo.viewType,
                                                contentInfo.nodeInfo
                                            )
                                        } else if (contentInfo.viewType == "LinearLayout" && contentInfo.contentDescription.isNotEmpty()) {

                                            translateAndUpdateUI(
                                                contentInfo.contentDescription,
                                                contentInfo.bounds,
                                                contentInfo.viewType,
                                                contentInfo.nodeInfo
                                            )
                                        } else {
                                            translateAndUpdateUI(
                                                contentInfo.text,
                                                contentInfo.bounds,
                                                contentInfo.viewType,
                                                contentInfo.nodeInfo
                                            )
                                        }

                                    }
                                }
                            } finally {

                                isTranslationInProgress = false
                            }
                        }


                        handler.postDelayed(longPressRunnable!!, longPressDuration.toLong())
                    }

                    override fun onFingerUp(x: Float, y: Float) {
                        i = 0

                        image1.updateImage(R.drawable.app_icon)
                        longPressRunnable?.let { handler.removeCallbacks(it) }

                    }

                    override fun onFingerDown(x: Float, y: Float) {

                        image1.updateImage(R.drawable.maginifying_glass)


                    }
                })
        } catch (e: Exception) {

            BubbleBuilder(this)
        }
    }

    private fun showMessege(message: String) {
        Toast.makeText(
            this@MyService,
            message, Toast.LENGTH_LONG
        ).show()
    }

    override fun configExpandedBubble(): ExpandedBubbleBuilder {
        return try {


            val expandedView = LayoutInflater.from(this).inflate(R.layout.layout_view_test, null)
            actualText = expandedView.findViewById(R.id.textviewData)
            translatedTextView = expandedView.findViewById(R.id.textviewData2)
            actualText!!.text = SaveText.getText()
            translatedTextView!!.text = SaveText.getTranslatedText()
            languageList = ArrayList()
            getLanguages_for_translate_fragment(languageList)

            val recyclerView: RecyclerView = expandedView.findViewById(R.id.recyclerView)
            recyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            val adapter = HorizontalListViewRv(languageList, object : OnItemClickListener {
                override fun onItemClick(language: ConversationLanguage) {

                    try {
                        val cardOneText = actualText!!.text

                        if (cardOneText.isNotEmpty()) {
                            val translateAPI = TranslateAPI(
                                Language.AUTO_DETECT,
                                language.languageCode,
                                cardOneText.toString()
                            )
                            translateAPI.setTranslateListener(object :
                                TranslateAPI.TranslateListener {
                                override fun onSuccess(translatedText: String) {

                                    translatedTextView!!.text = translatedText
                                }

                                override fun onFailure(errorText: String) {
                                    showMessege(errorText)

                                }
                            })
                        }
                    } catch (e: Exception) {
                        Log.e("MyService", "Error in translateAndUpdateUI", e)
                    }
                }
            })
            recyclerView.adapter = adapter


            expandedView.findViewById<View>(R.id.btn).setOnClickListener {
                minimize()
                if (customLayout!!.parent != null) {


                    val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                    windowManager.removeView(customLayout)
                    customLayout = null
                }
            }

            ExpandedBubbleBuilder(this)
                .expandedView(expandedView).onDispatchKeyEvent {
                    if (it.keyCode == KeyEvent.KEYCODE_BACK) {
                        minimize()
                    }
                    null
                }.startLocation(0, 0)
                .draggable(true)
                .style(null)
                .fillMaxWidth(true)
                .enableAnimateToEdge(true)
                .dimAmount(0.6f)
        } catch (e: Exception) {
            Log.e("MyService", "Error in configExpandedBubble", e)
            ExpandedBubbleBuilder(this)
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private var previousText: String? = null
    private fun updateEditText(nodeInfo: AccessibilityNodeInfo, text: String) {
        try {
            previousText = nodeInfo.text?.toString()
            val bundle = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text
                )
            }
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
            showUndoCustomLayout(nodeInfo)
        } catch (e: Exception) {
            Log.e("MyService", "Error in updateEditText", e)
        }
    }


    private fun undoEditTextUpdate(nodeInfo: AccessibilityNodeInfo) {

        if (!previousText.isNullOrEmpty()) {
            val bundle = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, previousText
                )
            }
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
        }
    }


    private fun isCoordinateInRange(bounds: Rect, x: Int, y: Int): Boolean {
        return bounds.contains(x, y)
    }

    private fun showUndoCustomLayout(nodeInfo: AccessibilityNodeInfo) {

        try {
            val bounds = Rect()
            nodeInfo.getBoundsInScreen(bounds)

            if (undoCustomLayout != null && undoCustomLayout!!.parent != null) {

                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                windowManager.removeView(undoCustomLayout)
                undoCustomLayout = null
            }

            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            undoCustomLayout = inflater.inflate(R.layout.undo_custom_layout, null) as View

            val btn = undoCustomLayout!!.findViewById<TextView>(R.id.undoBtn)
            val pulseCountDown =
                undoCustomLayout!!.findViewById<PulseCountDown>(R.id.pulseCountDown)
            pulseCountDown.start {
                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                windowManager.removeView(undoCustomLayout)
                undoCustomLayout = null
            }
            btn.setOnClickListener {

                undoEditTextUpdate(nodeInfo)
                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                windowManager.removeView(undoCustomLayout)
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            val centerX = screenWidth / 2
            val centerY = screenHeight / 2
            val relativeX = bounds.centerX() - centerX
            val relativeY = bounds.top - centerY - bounds.height()
            params.x = relativeX
            params.y = relativeY

            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.addView(undoCustomLayout, params)

        } catch (e: Exception) {
            Log.e("MyService", "Error in showUndoCustomLayout", e)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun showCustomLayout(bounds: Rect, text: String, textInserted: String) {
        try {

            if (customLayout == null) {
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                customLayout = inflater.inflate(R.layout.custom_layout, null) as View
            }
            val textView = customLayout!!.findViewById<TextView>(R.id.textView)

            if (text != textView.text) {
                if (text.isNotEmpty()) {
                    SaveText.setText(textInserted)
                    SaveText.setTranslatedText(text)
                }

                textView.text = text
            } else {
                return
            }

            if (customLayout!!.parent != null) {

                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                windowManager.removeView(customLayout)
            }


            customLayout!!.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {

                        expand()
                        actualText!!.text = SaveText.getText()
                        translatedTextView!!.text = SaveText.getTranslatedText()
                        true
                    }


                    else -> false
                }
            }

            val params = WindowManager.LayoutParams(
                bounds.width(),
                bounds.height(),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            val centerX = screenWidth / 2
            val centerY = screenHeight / 2
            val relativeX = bounds.centerX() - centerX
            val relativeY = bounds.top - 30 - centerY
            params.x = relativeX
            params.y = relativeY

            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.addView(customLayout, params)

        } catch (e: Exception) {
            Log.e("MyService", "Error in showCustomLayout", e)
        }
    }


    private fun translateAndUpdateUI(
        textInserted: String,
        bounds: Rect,
        viewType: String,
        nodeInfo: AccessibilityNodeInfo
    ) {

        try {
            Log.d("valueOfthhf", "translateAndUpdateUI: ")
            isTranslationInProgress = true
            val translatePreference =
                getSharedPreferences("Translate_Language_Preference", MODE_PRIVATE)
            val lan = translatePreference.getString(
                "Language_2_Code", ""
            )

            if (textInserted.isNotEmpty()) {
                val translateAPI = TranslateAPI(Language.AUTO_DETECT, lan, textInserted)

                translateAPI.setTranslateListener(object : TranslateAPI.TranslateListener {
                    override fun onSuccess(translatedText: String) {
                        Log.d("valueOfthhf", "translateAndUpdateUI:$translatedText ")
                        try {

                            if (viewType == "EditText") {
                                updateEditText(nodeInfo, translatedText)
                            } else {
                                showCustomLayout(bounds, translatedText, textInserted)
                            }
                        } finally {

                            isTranslationInProgress = false
                        }
                    }

                    override fun onFailure(errorText: String) {
                        try {
                            showMessege(errorText)

                        } finally {

                            isTranslationInProgress = false
                        }

                    }
                })
            }
        } catch (e: Exception) {

        }
    }


}
