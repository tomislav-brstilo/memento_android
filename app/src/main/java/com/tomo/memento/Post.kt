package com.tomo.memento
import com.google.gson.annotations.SerializedName

data class Post(
    val id: Int,
    @SerializedName("imageurl") val imageUrl: String,
    val caption: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String?
)