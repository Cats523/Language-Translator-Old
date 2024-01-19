package com.wonderapps.translator.utils.services


import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.wonderapps.translator.R
import com.wonderapps.translator.screens.MainActivity
import com.wonderapps.translator.utils.helper.AccessbiltyServiceTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LanguageTranslatorAccessibilityService : AccessibilityService() {
    private lateinit var rootNodeInfo: AccessibilityNodeInfo
    private val CHANNEL_ID = "MyServiceChannel"
    private val NOTIFICATION_ID = 1

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_WINDOWS_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
                AccessibilityEvent.TYPE_VIEW_CLICKED,
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    rootNodeInfo = rootInActiveWindow

                    processContentChangeAsync(rootNodeInfo)
                }
            }
        } catch (e: Exception) {

        }
    }

    override fun onServiceConnected() {
        try {
            super.onServiceConnected()
            AccessbiltyServiceTracker.isServiceRunning = true

            showServiceActiveNotification()
            startForeground(NOTIFICATION_ID, buildNotification())
        } catch (e: Exception) {

        }
    }

    private fun showServiceActiveNotification() {
        try {

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "MyService Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            Log.e("LanguageTranslatorAccessibilityService", "Error in showServiceActiveNotification", e)
        }
    }

    private fun buildNotification(): Notification {
        return try {
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
            )

            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Accessibility Service is Active")
                .setContentText("This service is running in the background.")
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        } catch (e: Exception) {
            Log.e("LanguageTranslatorAccessibilityService", "Error in buildNotification", e)
            NotificationCompat.Builder(this, CHANNEL_ID)
                .build()
        }
    }

    override fun onInterrupt() {
        try {
            // Handle interruption
        } catch (e: Exception) {
            Log.e("LanguageTranslatorAccessibilityService", "Error in onInterrupt", e)
        }
    }

    private fun processContentChangeAsync(node: AccessibilityNodeInfo) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val visibleNodes = withContext(Dispatchers.Default) {
                    traverseAndCollectVisibleNodes(node)
                }
                ContentManager.saveVisibleNodes(visibleNodes)
            } catch (e: Exception) {
                Log.e("LanguageTranslatorAccessibilityService", "Error in processContentChangeAsync", e)
            }
        }
    }

    private fun traverseAndCollectVisibleNodes(node: AccessibilityNodeInfo): List<ContentInfo> {
        val result = mutableListOf<ContentInfo>()
        if (node.isVisibleToUser) {
            val contentInfo = extractContentInfo(node)
            result.add(contentInfo)
        }
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            result.addAll(traverseAndCollectVisibleNodes(childNode))
        }
        return result
    }

    private fun extractContentInfo(node: AccessibilityNodeInfo): ContentInfo {
        return try {
            val bounds = Rect()
            AccessibilityNodeInfoCompat.wrap(node).getBoundsInScreen(bounds)
            ContentInfo(
                text = node.text?.toString() ?: "",
                bounds = bounds,
                viewType = getViewType(node),
                nodeInfo = node,
                contentDescription = node.contentDescription?.toString()
                    ?: ""
            )
        } catch (e: Exception) {
            Log.e("LanguageTranslatorAccessibilityService", "Error in extractContentInfo", e)
            ContentInfo("", Rect(), "UnknownView", node, "")
        }
    }

    private fun getViewType(node: AccessibilityNodeInfo): String {
        return try {

            node.className?.let {
                val parts = it.split('.')
                if (parts.isNotEmpty()) {
                    parts.last()
                } else {
                    "UnknownView"
                }
            } ?: "UnknownView"
        } catch (e: Exception) {
            "UnknownView"
        }
    }



    data class ContentInfo(
        val text: String,
        val bounds: Rect,
        val viewType: String,
        val nodeInfo: AccessibilityNodeInfo,
        val contentDescription: String
    )

    object ContentManager {
        private var visibleNodes: List<ContentInfo> = emptyList()

        fun saveVisibleNodes(nodes: List<ContentInfo>) {
            try {
                visibleNodes = nodes
            } catch (e: Exception) {
                Log.e("LanguageTranslatorAccessibilityService", "Error in saveVisibleNodes", e)
            }
        }

        fun getVisibleNodes(): List<ContentInfo> {
            return try {
                visibleNodes
            } catch (e: Exception) {
                Log.e("LanguageTranslatorAccessibilityService", "Error in getVisibleNodes", e)
                emptyList()
            }
        }
    }
}










