package com.efrivahmi.neighborstory.ui.welcome

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.efrivahmi.neighborstory.databinding.ActivityWelcomeBinding
import com.efrivahmi.neighborstory.ui.login.LoginNeighborActivity
import com.efrivahmi.neighborstory.ui.register.RegisterActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WelcomeNeighborActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        playAnimation()
        setupAction()
    }

    private fun playAnimation() {
        GlobalScope.launch(Dispatchers.Main) {
            ObjectAnimator.ofFloat(binding.ivWelcomeBg, View.TRANSLATION_X, -50f, 50f).apply {
                duration = 10000
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }.start()

            val titleAnim = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.tvWelcomeTitle, View.TRANSLATION_Y, -50f, 0f).setDuration(500),
                    ObjectAnimator.ofFloat(binding.tvWelcomeTitle, View.ALPHA, 0f, 1f).setDuration(500)
                )
            }

            val descAnim = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.tvWelcomeDesc, View.TRANSLATION_Y, 50f, 0f).setDuration(500),
                    ObjectAnimator.ofFloat(binding.tvWelcomeDesc, View.ALPHA, 0f, 1f).setDuration(500)
                )
            }

            val loginAnim = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.btnLoginWelcome, View.TRANSLATION_Y, 50f, 0f).setDuration(500),
                    ObjectAnimator.ofFloat(binding.btnLoginWelcome, View.ALPHA, 0f, 1f).setDuration(500)
                )
            }

            val signupAnim = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.btnRegisterWelcome, View.TRANSLATION_Y, 50f, 0f).setDuration(500),
                    ObjectAnimator.ofFloat(binding.btnRegisterWelcome, View.ALPHA, 0f, 1f).setDuration(500)
                )
            }

            val togetherAnim = AnimatorSet().apply {
                playTogether(loginAnim, signupAnim)
            }

            val fadingAnimation = AlphaAnimation(0f, 1f).apply {
                duration = 500
                startOffset = 500
            }

            AnimatorSet().apply {
                playSequentially(titleAnim, fadingAnimation.fadeIn(binding.ivWelcomeBg, 500), descAnim, togetherAnim)
                start()
            }
        }
    }

    private fun AlphaAnimation.fadeIn(view: View, duration: Long): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
            this.duration = duration
            view.startAnimation(this@fadeIn)
        }
    }

    private fun setupAction() {
        binding.apply {
            btnLoginWelcome.setOnClickListener {
                startActivity(Intent(this@WelcomeNeighborActivity, LoginNeighborActivity::class.java))
            }
            btnRegisterWelcome.setOnClickListener {
                startActivity(Intent(this@WelcomeNeighborActivity, RegisterActivity::class.java))
            }
        }
    }

    private fun setupView() {
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}