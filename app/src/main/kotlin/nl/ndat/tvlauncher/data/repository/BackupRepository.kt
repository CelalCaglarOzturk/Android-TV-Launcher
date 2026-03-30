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
import nl.ndat.tvlauncher.util.LauncherConstants
import timber.log.Timber
import java.io.File

class BackupRepository(
    private val context: Context,
    private val database: DatabaseContainer,
    private val settingsRepository: SettingsRepository,
    private val channelRepository: ChannelRepository,
) {
    companion object {
        private val CURRENT_BACKUP_VERSION = LauncherConstants.Backup.CURRENT_VERSION
        private val MAX_BACKUP_HISTORY = LauncherConstants.Backup.MAX_HISTORY_COUNT
        private val BACKUP_DATE_FORMAT = LauncherConstants.Backup.DATE_FORMAT
    }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    private fun getBackupDirectory(): File {
        val backupDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            LauncherConstants.Backup.BACKUP_DIR_NAME
        )
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }

    private fun getBackupFile(): File {
        return File(getBackupDirectory(), LauncherConstants.Backup.BACKUP_FILE_NAME)
    }

    private fun getTimestampedBackupFile(): File {
        val timestamp = java.text.SimpleDateFormat(BACKUP_DATE_FORMAT, java.util.Locale.getDefault())
            .format(java.util.Date())
        return File(getBackupDirectory(), "tv_launcher_backup_$timestamp.json")
    }

    fun getBackupPath(): String {
        return getBackupFile().absolutePath
    }

    fun getBackupHistory(): List<BackupInfo> {
        val backupDir = getBackupDirectory()
        val backupFiles = backupDir.listFiles()?.filter {
            it.name.startsWith("tv_launcher_backup") && it.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        return backupFiles.mapNotNull { file ->
            try {
                val jsonString = file.readText()
                val backupData = json.decodeFromString<BackupData>(jsonString)
                BackupInfo(
                    timestamp = backupData.timestamp,
                    appCardSize = backupData.settings.appCardSize,
                    channelCardSize = backupData.settings.channelCardSize,
                    hiddenAppsCount = backupData.apps.count { it.hidden },
                    favoriteAppsCount = backupData.apps.count { it.favoriteOrder != null },
                    disabledChannelsCount = backupData.channels.count { !it.enabled },
                    fileName = file.name
                )
            } catch (e: Exception) {
                Timber.w(e, "Failed to read backup file: ${file.name}")
                null
            }
        }
    }

    fun backupExists(): Boolean {
        return getBackupFile().exists()
    }

    fun getBackupInfo(): BackupInfo? {
        return try {
            val backupFile = getBackupFile()
            if (!backupFile.exists()) return null
            
            val jsonString = backupFile.readText()
            val backupData = json.decodeFromString<BackupData>(jsonString)
            
            BackupInfo(
                timestamp = backupData.timestamp,
                appCardSize = backupData.settings.appCardSize,
                channelCardSize = backupData.settings.channelCardSize,
                hiddenAppsCount = backupData.apps.count { it.hidden },
                favoriteAppsCount = backupData.apps.count { it.favoriteOrder != null },
                disabledChannelsCount = backupData.channels.count { !it.enabled }
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to read backup info")
            null
        }
    }

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ requires MANAGE_EXTERNAL_STORAGE for full external storage access
            Environment.isExternalStorageManager()
        } else {
            // Android 10 and below requires WRITE_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    suspend fun createBackup() = withContext(Dispatchers.IO) {
        try {
            val backupData = createBackupData()
            val jsonString = json.encodeToString(BackupData.serializer(), backupData)

            // Save timestamped backup for history
            val timestampedFile = getTimestampedBackupFile()
            timestampedFile.writeText(jsonString)
            Timber.d("Timestamped backup created at: ${timestampedFile.absolutePath}")

            // Also save as the main backup file
            val backupFile = getBackupFile()
            backupFile.writeText(jsonString)
            Timber.d("Main backup created at: ${backupFile.absolutePath}")

            // Cleanup old backups to keep only the most recent ones
            cleanupOldBackups()
        } catch (e: Exception) {
            Timber.e(e, "Failed to create backup")
            throw e
        }
    }

    private fun cleanupOldBackups() {
        try {
            val backupDir = getBackupDirectory()
            val backupFiles = backupDir.listFiles()?.filter {
                it.name.startsWith("tv_launcher_backup_") && it.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() } ?: return

            // Keep only the most recent MAX_BACKUP_HISTORY backups
            if (backupFiles.size > MAX_BACKUP_HISTORY) {
                backupFiles.drop(MAX_BACKUP_HISTORY).forEach { file ->
                    Timber.d("Deleting old backup: ${file.name}")
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup old backups")
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

    suspend fun restoreFromSpecificBackup(backupFile: File) {
        try {
            withContext(Dispatchers.IO + NonCancellable) {
                try {
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

        // Settings (all settings)
        val settings = SettingsBackup(
            appCardSize = settingsRepository.appCardSize.value,
            channelCardSize = settingsRepository.channelCardSize.value,
            channelCardsPerRow = settingsRepository.channelCardsPerRow.value,
            showMobileApps = settingsRepository.showMobileApps.value,
            enableAnimations = settingsRepository.enableAnimations.value,
            animAppIcon = settingsRepository.animAppIcon.value,
            animChannelRow = settingsRepository.animChannelRow.value,
            animChannelMove = settingsRepository.animChannelMove.value,
            animAppMove = settingsRepository.animAppMove.value,
            toolbarItemsOrder = settingsRepository.toolbarItemsOrder.value,
            toolbarItemsEnabled = settingsRepository.toolbarItemsEnabled.value.toList(),
            hiddenInputs = settingsRepository.hiddenInputs.value.toList()
        )

        return BackupData(
            version = CURRENT_BACKUP_VERSION,
            timestamp = System.currentTimeMillis(),
            settings = settings,
            apps = apps,
            channels = channels,
            watchNextBlacklist = blacklist
        )
    }

    private suspend fun restoreBackupData(data: BackupData) {
        // Clear existing state before restore
        clearExistingState()

        // Restore Settings
        settingsRepository.setAppCardSize(data.settings.appCardSize)
        settingsRepository.setChannelCardSize(data.settings.channelCardSize)
        data.settings.channelCardsPerRow?.let { settingsRepository.setChannelCardsPerRow(it) }
        settingsRepository.setShowMobileApps(data.settings.showMobileApps)
        settingsRepository.setEnableAnimations(data.settings.enableAnimations)
        
        // Restore individual animation settings
        data.settings.animAppIcon?.let { settingsRepository.setAnimAppIcon(it) }
        data.settings.animChannelRow?.let { settingsRepository.setAnimChannelRow(it) }
        data.settings.animChannelMove?.let { settingsRepository.setAnimChannelMove(it) }
        data.settings.animAppMove?.let { settingsRepository.setAnimAppMove(it) }

        // Restore Toolbar Settings
        data.settings.toolbarItemsOrder?.let { settingsRepository.setToolbarItemsOrder(it) }
        data.settings.toolbarItemsEnabled?.let { enabledList ->
            val allItems = SettingsRepository.Companion.ToolbarItem.entries.map { it.name }
            allItems.forEach { itemName ->
                settingsRepository.setToolbarItemEnabled(itemName, enabledList.contains(itemName))
            }
        }

        // Restore Hidden Inputs
        data.settings.hiddenInputs?.let { inputs ->
            inputs.forEach { inputId ->
                settingsRepository.toggleInputHidden(inputId)
            }
        }

        // Restore Watch Next Blacklist
        data.watchNextBlacklist.forEach { packageName ->
            database.watchNextBlacklist.insert(packageName)
        }
        channelRepository.refreshWatchNextChannels()

        // Restore Apps
        restoreApps(data.apps)

        // Restore Channels
        restoreChannels(data.channels)

        // Trigger a full refresh to update UI
        channelRepository.refreshAllChannels()
    }

    private fun clearExistingState() {
        Timber.d("Clearing existing state before restore")
        
        // Clear all settings to defaults
        settingsRepository.resetSettings()
        
        // Clear watch next blacklist
        val currentBlacklist = database.watchNextBlacklist.getAll().executeAsList()
        currentBlacklist.forEach { packageName ->
            database.watchNextBlacklist.delete(packageName)
        }
        
        // Clear app favorites and hidden status
        val allApps = database.apps.getAllIncludingHidden().executeAsList()
        allApps.forEach { app ->
            if (app.hidden == 1L) {
                database.apps.unhideApp(app.id)
            }
            if (app.favoriteOrder != null) {
                database.apps.updateFavoriteRemove(app.id)
            }
        }
        
        // Clear channel customizations
        val previewChannels = database.channels.getByType(ChannelType.PREVIEW).executeAsList()
        previewChannels.forEach { channel ->
            if (!channel.enabled) {
                database.channels.enableChannel(channel.id)
            }
        }
        
        Timber.d("Existing state cleared")
    }

    private fun restoreApps(appsBackup: List<AppBackup>) {
        // 1. Restore Hidden status
        appsBackup.forEach { appBackup ->
            val existingApp = database.apps.getByPackageName(appBackup.packageName).executeAsOneOrNull()
            if (existingApp != null) {
                if (appBackup.hidden) {
                    database.apps.hideApp(existingApp.id)
                }
            }
        }

        // 2. Restore Favorites - clear existing first (already done in clearExistingState)
        // Add from backup, sorted by order
        appsBackup.filter { it.favoriteOrder != null }
            .sortedBy { it.favoriteOrder }
            .forEach { appBackup ->
                val existingApp = database.apps.getByPackageName(appBackup.packageName).executeAsOneOrNull()
                if (existingApp != null) {
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
    }

    private fun restoreChannels(channelsBackup: List<ChannelBackup>) {
        val currentChannels = database.channels.getByType(ChannelType.PREVIEW).executeAsList()

        channelsBackup.forEach { channelBackup ->
            // Find matching channel by package name and display name
            val match = currentChannels.find {
                it.packageName == channelBackup.packageName && it.displayName == channelBackup.displayName
            }

            if (match != null) {
                if (!channelBackup.enabled) {
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
    val version: Int,
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
    val channelCardsPerRow: Int? = null,
    val showMobileApps: Boolean = false,
    val enableAnimations: Boolean = true,
    val animAppIcon: Boolean? = null,
    val animChannelRow: Boolean? = null,
    val animChannelMove: Boolean? = null,
    val animAppMove: Boolean? = null,
    val toolbarItemsOrder: List<String>? = null,
    val toolbarItemsEnabled: List<String>? = null,
    val hiddenInputs: List<String>? = null,
    // Legacy fields for backwards compatibility (v1)
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

data class BackupInfo(
    val timestamp: Long,
    val appCardSize: Int,
    val channelCardSize: Int,
    val hiddenAppsCount: Int,
    val favoriteAppsCount: Int,
    val disabledChannelsCount: Int,
    val fileName: String = "tv_launcher_backup.json"
) {
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}