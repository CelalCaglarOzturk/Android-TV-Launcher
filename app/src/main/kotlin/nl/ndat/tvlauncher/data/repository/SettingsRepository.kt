package nl.ndat.tvlauncher.data.repository

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    context: Context
) {
    companion object {
        private const val PREFS_NAME = "launcher_settings"
        private const val KEY_APP_CARD_SIZE = "app_card_size"

        // Default height in dp
        const val DEFAULT_APP_CARD_SIZE = 90
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _appCardSize = MutableStateFlow(prefs.getInt(KEY_APP_CARD_SIZE, DEFAULT_APP_CARD_SIZE))
    val appCardSize = _appCardSize.asStateFlow()

    fun setAppCardSize(size: Int) {
        prefs.edit { putInt(KEY_APP_CARD_SIZE, size) }
        _appCardSize.value = size
    }
}
