package com.example.hotelapp.classes

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackBarUtils {
    fun showShort(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showLong(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }
}