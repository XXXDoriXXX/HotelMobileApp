package com.example.hotelapp.classes

import com.google.gson.JsonParser
import retrofit2.Response

object ErrorUtils {
    fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val json = JsonParser.parseString(errorBody).asJsonObject
            json["detail"]?.asString ?: response.message()
        } catch (e: Exception) {
            response.message()
        }
    }
}
