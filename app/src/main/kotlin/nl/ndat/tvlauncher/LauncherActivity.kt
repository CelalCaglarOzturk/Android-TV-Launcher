package nl.ndat.tvlauncher

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.tvprovider.media.tv.TvContractCompat
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.repository.ChannelRepository
import nl.ndat.tvlauncher.data.repository.InputRepository
import nl.ndat.tvlauncher.ui.AppBase
import nl.ndat.tvlauncher.util.DefaultLauncherHelper
import nl.ndat.tvlauncher.util.FocusController
import timber.log.Timber

@SuppressLint("RestrictedApi")
val PERMISSION_READ_CHANNELS = TvContractCompat.PERMISSION_READ_TV_LISTINGS
val PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
val PERMISSIONS = listOf(PERMISSION_READ_CHANNELS, PERMISSION_WRITE_STORAGE)

class LauncherActivity : ComponentActivity() {
    private val defaultLauncherHelper: DefaultLauncherHelper by inject()
    private val focusController: FocusController by inject()
    private val appRepository: AppRepository by inject()
    private val inputRepository: InputRepository by inject()
    private val channelRepository: ChannelRepository by inject()

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Refresh channels when permission is granted
            if (permissions[PERMISSION_READ_CHANNELS] == true) lifecycleScope.launch { channelRepository.refreshAllChannels() }
        }

    private var shouldCheckAllFilesAccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppBase()
        }

        validateDefaultLauncher()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Timber.d("LauncherActivity: Starting refresh of all data")
                try {
                    Timber.d("LauncherActivity: Calling refreshAllApplications")
                    appRepository.refreshAllApplications()
                    Timber.d("LauncherActivity: refreshAllApplications completed")
                } catch (e: Exception) {
                    Timber.e(e, "LauncherActivity: Error refreshing applications")
                }
                try {
                    inputRepository.refreshAllInputs()
                } catch (e: Exception) {
                    Timber.e(e, "LauncherActivity: Error refreshing inputs")
                }
                try {
                    channelRepository.refreshAllChannels()
                } catch (e: Exception) {
                    Timber.e(e, "LauncherActivity: Error refreshing channels")
                }
                Timber.d("LauncherActivity: All refresh operations completed")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Request missing permissions (excluding storage on Android 11+ which needs special handling)
        val missingPermissions = PERMISSIONS
            .filter { permission ->
                // On Android 11+, WRITE_EXTERNAL_STORAGE is not requestable normally
                // We need MANAGE_EXTERNAL_STORAGE instead
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && permission == PERMISSION_WRITE_STORAGE) {
                    return@filter false
                }
                checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
            }
            .toTypedArray()
        if (missingPermissions.isNotEmpty()) permissionsLauncher.launch(missingPermissions)

        // For Android 11+, check and request MANAGE_EXTERNAL_STORAGE if needed
        // Note: This requires user to go to settings, so we don't auto-request it
        // But we check if it's granted for backup functionality
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Log that all files access is not granted - user can enable it via backup dialog
                Timber.d("MANAGE_EXTERNAL_STORAGE permission not granted - backup will prompt user")
            }
        }
    }

    private fun validateDefaultLauncher() {
        if (!defaultLauncherHelper.isDefaultLauncher() && defaultLauncherHelper.canRequestDefaultLauncher()) {
            val intent = defaultLauncherHelper.requestDefaultLauncherIntent()
            @Suppress("DEPRECATION")
            if (intent != null) {
                shouldCheckAllFilesAccess = true
                startActivityForResult(intent, REQUEST_DEFAULT_LAUNCHER)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DEFAULT_LAUNCHER) {
            // User returned from default launcher selection
            // Check if we need to prompt for all files access
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && shouldCheckAllFilesAccess) {
                shouldCheckAllFilesAccess = false
                if (!Environment.isExternalStorageManager()) {
                    showAllFilesAccessDialog()
                }
            }
        }
    }

    private fun showAllFilesAccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.backup_permission_title))
            .setMessage(getString(R.string.backup_permission_message))
            .setPositiveButton(getString(R.string.backup_permission_open_settings)) { _, _ ->
                // Open app settings where user can manage permissions
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    companion object {
        private const val REQUEST_DEFAULT_LAUNCHER = 100
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            focusController.requestFocusReset()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Map "menu" key to a long press on the dpad because the compose TV library doesn't do that yet
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            event.startTracking()
            val longPressEvent = KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_DPAD_CENTER,
                1
            )
            return dispatchKeyEvent(longPressEvent)
        }

        return super.onKeyDown(keyCode, event)
    }
}
