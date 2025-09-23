package com.tomo.memento

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.tomo.memento.databinding.ActivityPostDetailBinding

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve extras
        val imageUrl = intent.getStringExtra("imageurl")
        val caption = intent.getStringExtra("caption")

        // Load image
        Glide.with(this)
            .load(imageUrl)
            .into(binding.postImageView)

        binding.captionTextView.text = caption ?: ""
    }
}
