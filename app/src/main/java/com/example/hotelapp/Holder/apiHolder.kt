package com.example.hotelapp.Holder
import android.util.Log
import okhttp3.*
import java.io.IOException

object apiHolder {
     var BASE_URL: String = " https://c52a-46-150-67-25.ngrok-free.app"
     private const val URL_SOURCE = "https://pastebin.com/raw/eY33mzuJ"

     fun fetchBaseUrl(onSuccess: () -> Unit = {}) {
          val client = OkHttpClient()
          val request = Request.Builder().url(URL_SOURCE).build()

          client.newCall(request).enqueue(object : Callback {
               override fun onFailure(call: Call, e: IOException) {
                    Log.e("apiHolder", "Failed to fetch BASE_URL: ${e.message}")
               }

               override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                         response.body?.string()?.trim()?.let {
                              BASE_URL = it
                              Log.d("apiHolder", "Fetched BASE_URL: $BASE_URL")
                              onSuccess()
                         }
                    } else {
                         Log.e("apiHolder", "Error fetching BASE_URL: ${response.code}")
                    }
               }
          })
     }
}
