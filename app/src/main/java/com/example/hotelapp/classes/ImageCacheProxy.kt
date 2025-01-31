package com.example.hotelapp.classes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.hotelapp.ProfileFragment
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


object ImageCacheProxy {
    private var cacheDir: File? = null

    fun initialize(context: Context) {
        cacheDir = File(context.cacheDir, "images").apply { mkdirs() }
        Log.d("ImageCacheProxy", "Initialized with cacheDir: ${cacheDir?.absolutePath}")
    }
    fun clearCachedImage(imageUrl: String) {
        if (cacheDir == null) return

        val fileName = imageUrl.hashCode().toString()
        val file = File(cacheDir, fileName)

        if (file.exists()) {
            file.delete()
            Log.d("ImageCacheProxy", "Deleted cached image: $fileName")
        }
    }

    fun clearAllCache() {
        if (cacheDir == null) return

        cacheDir?.listFiles()?.forEach { file ->
            file.delete()
            Log.d("ImageCacheProxy", "Deleted cached file: ${file.name}")
        }
    }

    fun getImage(imageUrl: String): String {
        if (cacheDir == null) {
            Log.e("ImageCacheProxy", "ERROR: ImageCacheProxy has not been initialized!")
            throw IllegalStateException("ImageCacheProxy has not been initialized. Call initialize(context) first.")
        }

        val fileName = imageUrl.hashCode().toString()
        val file = File(cacheDir, fileName)

        return if (file.exists()) {
            Log.d("ImageCacheProxy", "Loading from cache: $file")
            file.absolutePath
        } else {
            downloadAndCacheImage(imageUrl, file)
            imageUrl // Повертаємо URL, якщо кешу ще немає
        }
    }

    private fun downloadAndCacheImage(imageUrl: String, file: File) {
        Thread {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val input = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                input.close()

                file.outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, it)
                }
                Log.d("ImageCacheProxy", "Downloaded and cached: $imageUrl")
            } catch (e: Exception) {
                Log.e("ImageCacheProxy", "Download error: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }
}