package com.tomo.memento

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.tomo.memento.databinding.ActivityPostDetailBinding

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl   = intent.getStringExtra("imageurl")
        val caption    = intent.getStringExtra("caption")
        val username   = intent.getStringExtra("username")
        val profileUrl = intent.getStringExtra("profile_photo")
        val timestamp  = intent.getStringExtra("timestamp")

        // Post image
        Glide.with(this)
            .load(imageUrl)
            .into(binding.postImageView)

        // Username & caption
        binding.usernameTextView.text = username
        binding.postCaptionTextView.text = caption

        // Optional: profile picture
        Glide.with(this)
            .load(profileUrl)
            .into(binding.profileImageView)

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

