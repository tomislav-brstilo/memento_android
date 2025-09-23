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
    val user_uid: String
) {
    // ✅ Member function
    fun toJsonElement(): JsonElement {
        val json = JsonObject().apply {
            addProperty("id", id)
            addProperty("imageurl", imageurl)
            addProperty("caption", caption ?: "")
            addProperty("user_uid", user_uid)
            addProperty("latitude", latitude)
            addProperty("longitude", longitude)
            addProperty("timestamp", timestamp)
        }
        return json
    }

    companion object {
        fun fromJson(element: JsonElement): Post {
            val obj = element.asJsonObject
            return Post(
                id = obj.get("id").asInt,
                imageurl = obj.get("imageurl").asString,
                caption = obj.get("caption")?.asString,
                latitude = obj.get("latitude").asDouble,
                longitude = obj.get("longitude").asDouble,
                timestamp = obj.get("timestamp").asString,
                user_uid = obj.get("user_uid").asString
            )
        }
    }
}
