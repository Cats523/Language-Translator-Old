package com.wonderapps.translator.screens

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.wonderapps.translator.R

class OverlayScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessibilityService = intent.getStringExtra("ACCESSIBILITY_SERVICE")
        if (accessibilityService.equals("ACCESSIBILITY_SERVICE")) {
            setContentView(R.layout.accessibility_service_layout)
        } else if (accessibilityService.equals("SHOW_DEMO")) {
            setContentView(R.layout.moveable_intro_screen)
        } else {
            setContentView(R.layout.display_over_other_app_layout)
        }
        val closeButton = findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            finish()
        }



    }
}
