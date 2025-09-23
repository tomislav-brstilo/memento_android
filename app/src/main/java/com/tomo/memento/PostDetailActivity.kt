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

        val imageUrl = intent.getStringExtra("imageurl")
        val caption = intent.getStringExtra("caption")
        val user = intent.getStringExtra("user")
        val timestamp = intent.getStringExtra("timestamp")

        // Load photo (Glide recommended)
        Glide.with(this)
            .load(imageUrl)
            .into(binding.postImageView)

        // Set caption
        binding.postCaptionTextView.text = "$caption"

        // Example click listeners
        binding.likeButton.setOnClickListener {
            // TODO: Add logic
        }
        binding.commentButton.setOnClickListener {
            // TODO: Add logic
        }
        binding.shareButton.setOnClickListener {
            // TODO: Add logic
        }
        binding.saveButton.setOnClickListener {
            // TODO: Add logic
        }

        // TODO: Populate comments dynamically (RecyclerView recommended)
    }
}

