package com.example.hotelapp.utils

import android.util.Base64
import com.example.hotelapp.classes.User
import org.json.JSONObject

object JwtUtils {
    fun parseTokenToUser(token: String): User {
        val parts = token.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Invalid JWT token")

        val payload = parts[1]
        val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT))
        val jsonObject = JSONObject(decodedPayload)
        return User(
            id = 0,
            first_name = jsonObject.optString("first_name", "Unknown"),
            last_name = jsonObject.optString("last_name", "Unknown"),
            email = jsonObject.optString("email", "Unknown"),
            phone = jsonObject.optString("phone", "Unknown"),
            birth_date = jsonObject.optString("birth_date", "Unknown")
        )

    }
}
