package com.example.hotelapp.classes

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

object SnackBarUtils {

    fun showShort(context: Context, view: View, @StringRes stringRes: Int, vararg formatArgs: Any) {
        val message = context.getString(stringRes, *formatArgs)
        showInternal(view, message, Snackbar.LENGTH_SHORT)
    }

    fun showLong(context: Context, view: View, @StringRes stringRes: Int, vararg formatArgs: Any) {
        val message = context.getString(stringRes, *formatArgs)
        showInternal(view, message, Snackbar.LENGTH_LONG)
    }

    private fun showInternal(view: View, message: String, duration: Int) {
        val isError = message.contains("error", ignoreCase = true) ||
                message.contains("fail", ignoreCase = true) ||
                message.contains("exception", ignoreCase = true)

        val snackBar = Snackbar.make(view, message, duration)

        if (isError) {
            snackBar.setBackgroundTint(Color.parseColor("#FCE8E6"))
            snackBar.setTextColor(Color.parseColor("#B3261E"))
        }

        snackBar.show()
    }
}
