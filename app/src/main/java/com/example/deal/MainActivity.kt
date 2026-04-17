package com.example.deal

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        val logoCard = findViewById<MaterialCardView>(R.id.logoCard)
        val txtAppName = findViewById<View>(R.id.txtAppName)
        val txtTagline = findViewById<View>(R.id.txtTagline)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val txtLoginHint = findViewById<View>(R.id.txtLoginHint)
        val btnSignUp = findViewById<MaterialButton>(R.id.btnSignUp)
        val txtSignUpHint = findViewById<View>(R.id.txtSignUpHint)
        val circle1 = findViewById<View>(R.id.circle1)
        val circle2 = findViewById<View>(R.id.circle2)

        val views = listOf(
            logoCard,
            txtAppName,
            txtTagline,
            btnLogin,
            txtLoginHint,
            btnSignUp,
            txtSignUpHint
        )

        views.forEach { view ->
            view.alpha = 0f
            view.translationY = 40f
        }

        views.forEachIndexed { index, view ->
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(200L + index * 120L)
                .setDuration(600)
                .setInterpolator(DecelerateInterpolator(1.5f))
                .start()
        }

        logoCard.scaleX = 0.7f
        logoCard.scaleY = 0.7f
        logoCard.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(200)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator(1.1f))
            .start()

        animateFloating(circle1, 12f, 3000)
        animateFloating(circle2, -10f, 3500)

        setupSimplePress(btnLogin, LoginActivity::class.java)
        setupSimplePress(btnSignUp, SignUpActivity::class.java)
    }

    private fun setupSimplePress(button: MaterialButton, targetActivity: Class<*>) {
        button.setOnClickListener {
            button.animate().cancel()

            button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction {
                    button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .withEndAction {
                            startActivity(Intent(this, targetActivity))
                            overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        }
                        .start()
                }
                .start()
        }
    }

    private fun animateFloating(view: View, distance: Float, duration: Long) {
        val floatUp = ObjectAnimator.ofFloat(view, "translationY", 0f, distance).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
        }

        val floatDown = ObjectAnimator.ofFloat(view, "translationY", distance, 0f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
        }

        val set = AnimatorSet()
        set.playSequentially(floatUp, floatDown)
        set.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                set.start()
            }
        })
        set.start()
    }
}