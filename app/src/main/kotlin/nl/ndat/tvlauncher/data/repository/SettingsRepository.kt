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
        private const val KEY_CHANNEL_CARD_SIZE = "channel_card_size"
        private const val KEY_HIDDEN_INPUTS = "hidden_inputs"
        private const val KEY_SHOW_MOBILE_APPS = "show_mobile_apps"
        private const val KEY_ENABLE_ANIMATIONS = "enable_animations"

        // Default height in dp
        const val DEFAULT_APP_CARD_SIZE = 90
        const val DEFAULT_CHANNEL_CARD_SIZE = 90
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _appCardSize = MutableStateFlow(prefs.getInt(KEY_APP_CARD_SIZE, DEFAULT_APP_CARD_SIZE))
    val appCardSize = _appCardSize.asStateFlow()

    private val _channelCardSize = MutableStateFlow(prefs.getInt(KEY_CHANNEL_CARD_SIZE, DEFAULT_CHANNEL_CARD_SIZE))
    val channelCardSize = _channelCardSize.asStateFlow()

    private val _hiddenInputs = MutableStateFlow(prefs.getStringSet(KEY_HIDDEN_INPUTS, emptySet()) ?: emptySet())
    val hiddenInputs = _hiddenInputs.asStateFlow()

    private val _showMobileApps = MutableStateFlow(prefs.getBoolean(KEY_SHOW_MOBILE_APPS, false))
    val showMobileApps = _showMobileApps.asStateFlow()

    private val _enableAnimations = MutableStateFlow(prefs.getBoolean(KEY_ENABLE_ANIMATIONS, true))
    val enableAnimations = _enableAnimations.asStateFlow()

    fun setAppCardSize(size: Int) {
        prefs.edit { putInt(KEY_APP_CARD_SIZE, size) }
        _appCardSize.value = size
    }

    fun setChannelCardSize(size: Int) {
        prefs.edit { putInt(KEY_CHANNEL_CARD_SIZE, size) }
        _channelCardSize.value = size
    }

    fun toggleInputHidden(inputId: String) {
        val current = _hiddenInputs.value.toMutableSet()
        if (current.contains(inputId)) {
            current.remove(inputId)
        } else {
            current.add(inputId)
        }
        prefs.edit { putStringSet(KEY_HIDDEN_INPUTS, current) }
        _hiddenInputs.value = current
    }

    fun setShowMobileApps(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_MOBILE_APPS, show) }
        _showMobileApps.value = show
    }

    fun toggleShowMobileApps() {
        setShowMobileApps(!_showMobileApps.value)
    }

    fun setEnableAnimations(enable: Boolean) {
        prefs.edit { putBoolean(KEY_ENABLE_ANIMATIONS, enable) }
        _enableAnimations.value = enable
    }

    fun toggleEnableAnimations() {
        setEnableAnimations(!_enableAnimations.value)
    }
}
