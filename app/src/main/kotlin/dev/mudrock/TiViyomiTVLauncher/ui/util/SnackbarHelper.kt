package dev.mudrock.TiViyomiTVLauncher.ui.util

import android.content.Context
import android.widget.Toast

object SnackbarHelper {
    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}