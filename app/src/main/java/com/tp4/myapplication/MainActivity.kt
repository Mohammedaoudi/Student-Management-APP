package com.tp4.myapplication

import android.content.Intent
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var logo: View
    private lateinit var appTitle: View
    private lateinit var progressBar: View
    private lateinit var loadingText: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logo = findViewById(R.id.logo)
        appTitle = findViewById(R.id.appTitle)
        progressBar = findViewById(R.id.progressBar)

        startAnimations()

        Handler().postDelayed({
            val intent = Intent(this, List_Student::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }

    private fun startAnimations() {
        val logoFade = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f)
        val logoSlide = ObjectAnimator.ofFloat(logo, View.TRANSLATION_Y, -100f, 0f)

        val titleFade = ObjectAnimator.ofFloat(appTitle, View.ALPHA, 0f, 1f)
        val titleSlide = ObjectAnimator.ofFloat(appTitle, View.TRANSLATION_Y, 50f, 0f)

        AnimatorSet().apply {
            playTogether(logoFade, logoSlide, titleFade, titleSlide)
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
