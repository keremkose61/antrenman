package com.pplantreman.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val splashDuration = 2500L
    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        if (!isFinishing && !isDestroyed) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide action bar
        supportActionBar?.hide()

        // Views
        val logo = findViewById<ImageView>(R.id.ivSplashLogo)
        val appName = findViewById<TextView>(R.id.tvAppName)
        val slogan = findViewById<TextView>(R.id.tvSlogan)

        // Initial setup for animations
        logo?.apply { alpha = 0f; scaleX = 0.5f; scaleY = 0.5f }
        appName?.apply { alpha = 0f; translationY = 50f }
        slogan?.apply { alpha = 0f }

        // Core Animations
        val logoFade = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f)
        val logoScaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.5f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.5f, 1f)
        
        val nameFade = ObjectAnimator.ofFloat(appName, View.ALPHA, 0f, 1f)
        val nameSlide = ObjectAnimator.ofFloat(appName, View.TRANSLATION_Y, 50f, 0f)
        
        val sloganFade = ObjectAnimator.ofFloat(slogan, View.ALPHA, 0f, 1f)

        // Execute Animations sequentially
        AnimatorSet().apply {
            playTogether(logoFade, logoScaleX, logoScaleY)
            duration = 800
            interpolator = DecelerateInterpolator()
            start()
        }

        AnimatorSet().apply {
            playTogether(nameFade, nameSlide)
            duration = 600
            startDelay = 400
            interpolator = DecelerateInterpolator()
            start()
        }

        AnimatorSet().apply {
            playTogether(sloganFade)
            duration = 800
            startDelay = 800
            start()
        }

        // Navigate after delay
        handler.postDelayed(navigateRunnable, splashDuration)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(navigateRunnable)
    }
}
