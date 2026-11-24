package com.example.cinemood

import org.json.JSONObject

data class Movie(
    val id: Int, val title: String, val year: Int, val genre: String, val overview: String,
    val posterPath: String? = null, val source: String = "TMDB"
) {
    fun toJson(): String = JSONObject().apply {
        put("id", id); put("title", title); put("year", year); put("genre", genre)
        put("overview", overview); put("poster", posterPath); put("source", source)
    }.toString()

    companion object {
        fun fromJson(value: String): Movie {
            val obj = JSONObject(value)
            return Movie(obj.getInt("id"), obj.getString("title"), obj.optInt("year"),
                obj.optString("genre"), obj.optString("overview"),
                obj.optString("poster").takeIf { it.isNotBlank() && it != "null" }, obj.getString("source"))
        }
    }
}
