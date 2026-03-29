package nl.ndat.tvlauncher.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import nl.ndat.tvlauncher.LauncherActivity
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import org.koin.android.ext.android.inject

class LauncherAccessibilityService : AccessibilityService() {

    private val settingsRepository: SettingsRepository by inject()
    private var lastForegroundPackage: String? = null

    private val systemLauncherPackages = listOf(
        "com.google.android.tvlauncher",
        "com.android.tv.launcher",
        "com.google.android.apps.tv.launcherx",
        "com.android.launcher",
        "com.android.launcher2"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        val tvLauncherPackage = this.packageName

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Track the last app that was in foreground (before system launcher)
            if (!systemLauncherPackages.contains(packageName)) {
                lastForegroundPackage = packageName
            }

            if (!settingsRepository.suppressOriginalLauncher.value) return

            // System launcher detected
            if (systemLauncherPackages.contains(packageName)) {
                // Check if "only intercept from other apps" is enabled
                if (settingsRepository.suppressLauncherOnlyExternal.value) {
                    // If we were in TV Launcher before system launcher opened, don't redirect
                    if (lastForegroundPackage == tvLauncherPackage) {
                        return
                    }
                }
                launchTvLauncher()
            }
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    private fun launchTvLauncher() {
        val intent = Intent(this, LauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        startActivity(intent)
    }
}