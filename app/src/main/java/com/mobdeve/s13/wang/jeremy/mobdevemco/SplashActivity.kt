package com.mobdeve.s13.wang.jeremy.mobdevemco

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.SplashBinding
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat

class SplashActivity : ComponentActivity() {
    private lateinit var splashLogo: ImageView
    private lateinit var splashText: TextView
    private lateinit var endSplash: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)

        Handler(Looper.getMainLooper()).postDelayed({
            splashLogo = binding.ivSplashLogo
            splashText = binding.tvSplashText
            endSplash = binding.ivEndSplash
            val screenHeight = resources.displayMetrics.heightPixels
            splashLogo.translationY = -screenHeight.toFloat()
            splashLogo.visibility = android.view.View.VISIBLE

            splashLogo.post {
                splashLogo.animate()
                    .translationY(0f)
                    .setDuration(1000)
                    .withEndAction {
                        splashText.setTypeface(splashText.typeface, android.graphics.Typeface.BOLD)
                        val text = binding.tvSplashText.text.toString()
                        val spannable = SpannableString(text)

                        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.green))
                        spannable.setSpan(colorSpan, 8, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        splashText.text = spannable
                        splashText.visibility = android.view.View.VISIBLE
                        splashText.alpha = 0f

                        val splashLogoTranslationX = -(splashText.width / 2f) - 8f
                        val textViewTranslationX = (splashLogo.width / 2f) + 8f

                        splashLogo.animate()
                            .translationX(splashLogoTranslationX)
                            .setDuration(500)
                            .start()
                        endSplash.x -= (splashText.width / 2f) - 8f

                        splashText.animate()
                            .translationX(textViewTranslationX)
                            .alpha(1f)
                            .setDuration(500)
                            .withEndAction {

                                endSplash.visibility = android.view.View.VISIBLE

                                endSplash.apply {
                                    scaleX = 0f
                                    scaleY = 0f
                                    alpha = 1f

                                    animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .alpha(1f)
                                        .setDuration(500)
                                        .start()
                                }
                            }
                            .start()
                    }

            }
        }, 500)
    }
}
