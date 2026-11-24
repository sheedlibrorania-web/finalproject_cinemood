package com.example.cinemood.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import com.example.cinemood.R
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object PosterLoader {
    private val executor = Executors.newFixedThreadPool(2)
    private val cache = object : LruCache<String, Bitmap>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
    fun load(view: ImageView, path: String?) {
        view.tag = path
        view.setImageResource(R.drawable.cm_reel)
        if (path == null || !path.matches(Regex("/[A-Za-z0-9._-]+"))) return
        cache.get(path)?.let { view.setImageBitmap(it); return }
        executor.execute {
            val bitmap = runCatching {
                val connection = URL("https://image.tmdb.org/t/p/w342$path").openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 8000; connection.readTimeout = 8000
                    if (connection.responseCode == 200) connection.inputStream.use { BitmapFactory.decodeStream(it) } else null
                } finally { connection.disconnect() }
            }.getOrNull()
            if (bitmap != null) {
                cache.put(path, bitmap)
                view.post { if (view.tag == path) view.setImageBitmap(bitmap) }
            }
        }
    }
}
