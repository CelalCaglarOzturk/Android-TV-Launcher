package nl.ndat.tvlauncher.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityEvent
import nl.ndat.tvlauncher.LauncherActivity
import nl.ndat.tvlauncher.util.LauncherConstants
import timber.log.Timber

class LauncherAccessibilityService : AccessibilityService() {

    private lateinit var prefs: SharedPreferences
    private var lastForegroundPackage: String? = null
    private var lastEventTime: Long = 0

    companion object {
        private const val PREFS_NAME = "launcher_settings"
        private const val KEY_SUPPRESS_ORIGINAL_LAUNCHER = "suppress_original_launcher"
        private const val KEY_SUPPRESS_LAUNCHER_ONLY_EXTERNAL = "suppress_launcher_only_external"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        Timber.d("Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        try {
            val packageName = event.packageName
            if (packageName == null) {
                Timber.d("Event has null package name, skipping")
                return
            }
            
            val packageNameStr = packageName.toString()
            val tvLauncherPackage = this.packageName
            val currentTime = System.currentTimeMillis()

            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (shouldDebounceEvent(currentTime)) {
                    return
                }
                lastEventTime = currentTime

                if (!LauncherConstants.SystemLaunchers.isSystemLauncher(packageNameStr)) {
                    lastForegroundPackage = packageNameStr
                }

                val suppressEnabled = prefs.getBoolean(KEY_SUPPRESS_ORIGINAL_LAUNCHER, false)
                if (!suppressEnabled) return

                if (LauncherConstants.SystemLaunchers.isSystemLauncher(packageNameStr)) {
                    val onlyExternal = prefs.getBoolean(KEY_SUPPRESS_LAUNCHER_ONLY_EXTERNAL, false)
                    
                    if (onlyExternal && lastForegroundPackage == tvLauncherPackage) {
                        Timber.d("System launcher opened from TV Launcher, not redirecting")
                        return
                    }
                    
                    Timber.d("System launcher detected: $packageNameStr, redirecting to TV Launcher")
                    launchTvLauncher()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onAccessibilityEvent")
        }
    }

    override fun onInterrupt() {
        Timber.d("Accessibility service interrupted")
    }

    private fun shouldDebounceEvent(currentTime: Long): Boolean {
        return (currentTime - lastEventTime) < LauncherConstants.Accessibility.EVENT_DEBOUNCE_MS
    }

    private fun launchTvLauncher() {
        try {
            val intent = Intent(this, LauncherActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            startActivity(intent)
            Timber.d("Launched TV Launcher successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error launching TV Launcher")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Accessibility service destroyed")
    }
}