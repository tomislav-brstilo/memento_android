package com.tomo.memento
import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class Post(
    val id: Int,
    val imageurl: String,
    val caption: String?,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val username: String,
    val profile_photo: String,
) {
    // ✅ Member function
    fun toJsonElement(): JsonElement {
        val json = JsonObject().apply {
            addProperty("id", id)
            addProperty("imageurl", imageurl)
            addProperty("caption", caption ?: "")
            addProperty("username", username)
            addProperty("profile_photo", profile_photo)
            addProperty("latitude", latitude)
            addProperty("longitude", longitude)
            addProperty("timestamp", timestamp)
        }
        return json
    }

    companion object {
        fun fromJson(element: JsonElement): Post {
            android.util.Log.d("POST_JSON", element.toString()) // ✅ Log the incoming JSON here
            val obj = element.asJsonObject
            return Post(
                id = obj.get("id").asInt,
                imageurl = obj.get("imageurl").asString,
                caption = obj.get("caption")?.asString,
                latitude = obj.get("latitude").asDouble,
                longitude = obj.get("longitude").asDouble,
                timestamp = obj.get("timestamp").asString,
                username = obj.get("username").asString,
                profile_photo = obj.get("profile_photo").asString
            )
        }
    }
}
