package nl.ndat.tvlauncher.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nl.ndat.tvlauncher.data.DatabaseContainer
import nl.ndat.tvlauncher.data.model.ChannelType
import timber.log.Timber
import java.io.File

class BackupRepository(
    private val context: Context,
    private val database: DatabaseContainer,
    private val settingsRepository: SettingsRepository,
    private val channelRepository: ChannelRepository,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val backupFileName = "tv_launcher_backup.json"

    private fun getBackupDirectory(): File {
        // Use Documents/TVLauncher directory to persist files after uninstall
        val backupDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "TVLauncher"
        )
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }

    private fun getBackupFile(): File {
        return File(getBackupDirectory(), backupFileName)
    }

    fun getBackupPath(): String {
        return getBackupFile().absolutePath
    }

    fun backupExists(): Boolean {
        return getBackupFile().exists()
    }

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    suspend fun createBackup() = withContext(Dispatchers.IO) {
        try {
            val backupData = createBackupData()
            val jsonString = json.encodeToString(backupData)

            val backupFile = getBackupFile()
            backupFile.writeText(jsonString)
            Timber.d("Backup created at: ${backupFile.absolutePath}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to create backup")
            throw e
        }
    }

    suspend fun restoreBackup() {
        try {
            withContext(Dispatchers.IO + NonCancellable) {
                try {
                    val backupFile = getBackupFile()
                    if (!backupFile.exists()) {
                        throw IllegalStateException("Backup file not found at: ${backupFile.absolutePath}")
                    }

                    val jsonString = backupFile.readText()
                    val backupData = json.decodeFromString<BackupData>(jsonString)
                    restoreBackupData(backupData)
                    Timber.d("Backup restored from: ${backupFile.absolutePath}")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to restore backup")
                    throw e
                }
            }
        } catch (e: CancellationException) {
            // Ignore cancellation as the operation completed via NonCancellable
        }
    }

    private fun createBackupData(): BackupData {
        // Apps
        val apps = database.apps.getAllIncludingHidden().executeAsList().map { app ->
            AppBackup(
                packageName = app.packageName,
                favoriteOrder = app.favoriteOrder?.toInt(),
                hidden = app.hidden == 1L,
                allAppsOrder = app.allAppsOrder?.toInt()
            )
        }

        // Channels (Only backup Preview channels configuration)
        val channels = database.channels.getByType(ChannelType.PREVIEW).executeAsList().map { channel ->
            ChannelBackup(
                packageName = channel.packageName,
                displayName = channel.displayName,
                enabled = channel.enabled,
                displayOrder = channel.displayOrder
            )
        }

        // Watch Next Blacklist
        val blacklist = database.watchNextBlacklist.getAll().executeAsList()

        // Settings
        val settings = SettingsBackup(
            appCardSize = settingsRepository.appCardSize.value,
            channelCardSize = settingsRepository.channelCardSize.value,
            showMobileApps = settingsRepository.showMobileApps.value,
            enableAnimations = settingsRepository.enableAnimations.value,
            toolbarItemsOrder = settingsRepository.toolbarItemsOrder.value,
            toolbarItemsEnabled = settingsRepository.toolbarItemsEnabled.value.toList()
        )

        return BackupData(
            timestamp = System.currentTimeMillis(),
            settings = settings,
            apps = apps,
            channels = channels,
            watchNextBlacklist = blacklist
        )
    }

    private suspend fun restoreBackupData(data: BackupData) {
        // Restore Settings
        settingsRepository.setAppCardSize(data.settings.appCardSize)
        settingsRepository.setChannelCardSize(data.settings.channelCardSize)
        settingsRepository.setShowMobileApps(data.settings.showMobileApps)
        settingsRepository.setEnableAnimations(data.settings.enableAnimations)

        // Restore Toolbar Settings (unified list)
        data.settings.toolbarItemsOrder?.let { settingsRepository.setToolbarItemsOrder(it) }
        data.settings.toolbarItemsEnabled?.let { enabledList ->
            val allItems = SettingsRepository.Companion.ToolbarItem.entries.map { it.name }
            allItems.forEach { itemName ->
                settingsRepository.setToolbarItemEnabled(itemName, enabledList.contains(itemName))
            }
        }

        // Restore Watch Next Blacklist
        val currentBlacklist = database.watchNextBlacklist.getAll().executeAsList()
        currentBlacklist.forEach { packageName ->
            database.watchNextBlacklist.delete(packageName)
        }

        data.watchNextBlacklist.forEach { packageName ->
            database.watchNextBlacklist.insert(packageName)
        }
        channelRepository.refreshWatchNextChannels()

        // Restore Apps
        val appsBackup = data.apps

        // 1. Restore Hidden status
        appsBackup.forEach { appBackup ->
            val existingApp = database.apps.getByPackageName(appBackup.packageName).executeAsOneOrNull()
            if (existingApp != null) {
                if (appBackup.hidden) {
                    database.apps.hideApp(existingApp.id)
                } else {
                    database.apps.unhideApp(existingApp.id)
                }
            }
        }

        // 2. Restore Favorites
        // Clear existing favorites
        val currentFavorites = database.apps.getAllFavorites().executeAsList()
        currentFavorites.forEach { app ->
            database.apps.updateFavoriteRemove(app.id)
        }

        // Add from backup, sorted by order
        appsBackup.filter { it.favoriteOrder != null }
            .sortedBy { it.favoriteOrder }
            .forEach { appBackup ->
                val existingApp = database.apps.getByPackageName(appBackup.packageName).executeAsOneOrNull()
                if (existingApp != null) {
                    // Add to favorites (appends to end)
                    database.apps.updateFavoriteAdd(existingApp.id)
                }
            }

        // 3. Restore All Apps Order
        appsBackup.filter { it.allAppsOrder != null }
            .sortedBy { it.allAppsOrder }
            .forEach { appBackup ->
                val existingApp = database.apps.getByPackageName(appBackup.packageName).executeAsOneOrNull()
                if (existingApp != null) {
                    database.apps.updateAllAppsOrder(order = appBackup.allAppsOrder!!.toLong(), id = existingApp.id)
                }
            }

        // Restore Channels
        val channelsBackup = data.channels
        val currentChannels = database.channels.getByType(ChannelType.PREVIEW).executeAsList()

        channelsBackup.forEach { channelBackup ->
            // Find matching channel by package name and display name
            val match = currentChannels.find {
                it.packageName == channelBackup.packageName && it.displayName == channelBackup.displayName
            }

            if (match != null) {
                if (channelBackup.enabled) {
                    database.channels.enableChannel(match.id)
                } else {
                    database.channels.disableChannel(match.id)
                }

                if (channelBackup.displayOrder != null) {
                    database.channels.updateDisplayOrderShift(order = channelBackup.displayOrder, id = match.id)
                }
            }
        }
    }
}

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long,
    val settings: SettingsBackup,
    val apps: List<AppBackup>,
    val channels: List<ChannelBackup>,
    val watchNextBlacklist: List<String>
)

@Serializable
data class SettingsBackup(
    val appCardSize: Int,
    val channelCardSize: Int = 90,
    val showMobileApps: Boolean = false,
    val enableAnimations: Boolean = true,
    val toolbarItemsOrder: List<String>? = null,
    val toolbarItemsEnabled: List<String>? = null,
    // Legacy fields for backwards compatibility
    val toolbarOrder: List<String>? = null,
    val toolbarEnabled: List<String>? = null,
    val toolbarClockEnabled: Boolean? = null,
    val toolbarClockPosition: String? = null
)

@Serializable
data class AppBackup(
    val packageName: String,
    val favoriteOrder: Int?,
    val hidden: Boolean,
    val allAppsOrder: Int? = null
)

@Serializable
data class ChannelBackup(
    val packageName: String,
    val displayName: String,
    val enabled: Boolean,
    val displayOrder: Long?
)
