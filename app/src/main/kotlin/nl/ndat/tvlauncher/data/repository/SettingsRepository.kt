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

        // Animation Keys
        private const val KEY_ENABLE_ANIMATIONS = "enable_animations" // Master switch
        private const val KEY_ANIM_APP_ICON = "anim_app_icon"
        private const val KEY_ANIM_CHANNEL_ROW = "anim_channel_row"
        private const val KEY_ANIM_CHANNEL_MOVE = "anim_channel_move"
        private const val KEY_ANIM_APP_MOVE = "anim_app_move"
        private const val KEY_ANIM_TRANSITION = "anim_transition"

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

    // Animation States
    private val _enableAnimations = MutableStateFlow(prefs.getBoolean(KEY_ENABLE_ANIMATIONS, true))
    val enableAnimations = _enableAnimations.asStateFlow()

    private val _animAppIcon = MutableStateFlow(prefs.getBoolean(KEY_ANIM_APP_ICON, true))
    val animAppIcon = _animAppIcon.asStateFlow()

    private val _animChannelRow = MutableStateFlow(prefs.getBoolean(KEY_ANIM_CHANNEL_ROW, true))
    val animChannelRow = _animChannelRow.asStateFlow()

    private val _animChannelMove = MutableStateFlow(prefs.getBoolean(KEY_ANIM_CHANNEL_MOVE, true))
    val animChannelMove = _animChannelMove.asStateFlow()

    private val _animAppMove = MutableStateFlow(prefs.getBoolean(KEY_ANIM_APP_MOVE, true))
    val animAppMove = _animAppMove.asStateFlow()

    private val _animTransition = MutableStateFlow(prefs.getBoolean(KEY_ANIM_TRANSITION, true))
    val animTransition = _animTransition.asStateFlow()

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

    // Animation Setters
    fun setEnableAnimations(enable: Boolean) {
        prefs.edit { putBoolean(KEY_ENABLE_ANIMATIONS, enable) }
        _enableAnimations.value = enable
    }

    fun toggleEnableAnimations() {
        setEnableAnimations(!_enableAnimations.value)
    }

    fun setAnimAppIcon(enable: Boolean) {
        prefs.edit { putBoolean(KEY_ANIM_APP_ICON, enable) }
        _animAppIcon.value = enable
    }

    fun setAnimChannelRow(enable: Boolean) {
        prefs.edit { putBoolean(KEY_ANIM_CHANNEL_ROW, enable) }
        _animChannelRow.value = enable
    }

    fun setAnimChannelMove(enable: Boolean) {
        prefs.edit { putBoolean(KEY_ANIM_CHANNEL_MOVE, enable) }
        _animChannelMove.value = enable
    }

    fun setAnimAppMove(enable: Boolean) {
        prefs.edit { putBoolean(KEY_ANIM_APP_MOVE, enable) }
        _animAppMove.value = enable
    }

    fun setAnimTransition(enable: Boolean) {
        prefs.edit { putBoolean(KEY_ANIM_TRANSITION, enable) }
        _animTransition.value = enable
    }

    fun resetSettings() {
        prefs.edit { clear() }
        _appCardSize.value = DEFAULT_APP_CARD_SIZE
        _channelCardSize.value = DEFAULT_CHANNEL_CARD_SIZE
        _hiddenInputs.value = emptySet()
        _showMobileApps.value = false

        // Reset animations
        _enableAnimations.value = true
        _animAppIcon.value = true
        _animChannelRow.value = true
        _animChannelMove.value = true
        _animAppMove.value = true
        _animTransition.value = true
    }
}
