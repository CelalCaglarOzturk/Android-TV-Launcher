package nl.ndat.tvlauncher.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import nl.ndat.tvlauncher.LauncherActivity
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import org.koin.android.ext.android.inject

class LauncherAccessibilityService : AccessibilityService() {

    private val settingsRepository: SettingsRepository by inject()

    private val systemLauncherPackages = listOf(
        "com.google.android.tvlauncher",
        "com.android.tv.launcher",
        "com.google.android.apps.tv.launcherx",
        "com.android.launcher",
        "com.android.launcher2",
        "com.google.android.launcher"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (!settingsRepository.suppressOriginalLauncher.value) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            val packageName = event.packageName?.toString() ?: return

            if (systemLauncherPackages.contains(packageName)) {
                launchTvLauncher()
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyEvent(event)

        if (!settingsRepository.suppressOriginalLauncher.value) return super.onKeyEvent(event)

        if (event.keyCode == KeyEvent.KEYCODE_HOME && event.action == KeyEvent.ACTION_DOWN) {
            launchTvLauncher()
            return true
        }

        return super.onKeyEvent(event)
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
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }
}