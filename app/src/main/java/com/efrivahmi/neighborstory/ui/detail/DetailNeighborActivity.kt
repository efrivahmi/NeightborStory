package com.efrivahmi.neighborstory.ui.detail

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.efrivahmi.neighborstory.data.response.ListStoryItem
import com.efrivahmi.neighborstory.databinding.ActivityDetailNeighborBinding

@Suppress("DEPRECATION")
class DetailNeighborActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailNeighborBinding
    private var loadingTimer: CountDownTimer? = null
    private var isLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailNeighborBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = intent.getParcelableExtra<ListStoryItem>("DATA")
        val photo = data?.photoUrl
        val name = data?.name
        val description = data?.description

        Glide.with(this)
            .load(photo)
            .into(binding.rvImage)
        binding.tvName.text = name
        binding.tvDescription.text = description

        val loadingDuration = 2000L
        loadingTimer = object : CountDownTimer(loadingDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if (isLoading) {
                    showLoading(false)
                }
            }
        }
        loadingTimer?.start()

        showLoading(true)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar4.visibility = if (isLoading) View.VISIBLE else View.GONE
        this.isLoading = isLoading
        if (!isLoading) {
            loadingTimer?.cancel()
        }
    }
}
