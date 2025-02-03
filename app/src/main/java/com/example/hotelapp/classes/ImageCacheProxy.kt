package com.example.hotelapp.classes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object ImageCacheProxy {
    private var cacheDir: File? = null
    private val downloadExecutor = Executors.newFixedThreadPool(3)
    private val mainHandler = Handler(Looper.getMainLooper())
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

    fun getImage(imageUrl: String, callback: (String) -> Unit) {
        if (cacheDir == null) {
            Log.e("ImageCacheProxy", "ERROR: ImageCacheProxy has not been initialized!")
            throw IllegalStateException("ImageCacheProxy has not been initialized. Call initialize(context) first.")
        }

        val fileName = imageUrl.hashCode().toString()
        val file = File(cacheDir, fileName)

        if (file.exists()) {
            Log.d("ImageCacheProxy", "Loading from cache: $file")
            mainHandler.post { callback(file.absolutePath) }
        } else {
            synchronized(this) {
                downloadExecutor.execute {
                    downloadAndCacheImage(imageUrl, file) {
                        mainHandler.post { callback(file.absolutePath) }
                    }
                }
            }
        }
    }

    private fun downloadAndCacheImage(imageUrl: String, file: File, onComplete: () -> Unit) {
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

            onComplete()
        } catch (e: Exception) {
            Log.e("ImageCacheProxy", "Download error: ${e.message}")
            e.printStackTrace()
        }
    }
}
