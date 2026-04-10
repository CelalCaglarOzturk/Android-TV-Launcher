package dev.mudrock.tiviyomitvlauncher.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import dev.mudrock.tiviyomitvlauncher.R
import dev.mudrock.tiviyomitvlauncher.data.repository.AppRepository
import dev.mudrock.tiviyomitvlauncher.data.repository.BackupInfo
import dev.mudrock.tiviyomitvlauncher.data.repository.BackupRepository
import dev.mudrock.tiviyomitvlauncher.data.repository.ChannelRepository
import dev.mudrock.tiviyomitvlauncher.data.repository.SettingsRepository
import org.koin.compose.koinInject
import timber.log.Timber
import java.io.IOException

@Composable
fun LauncherSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val settingsRepository = koinInject<SettingsRepository>()
    val backupRepository = koinInject<BackupRepository>()
    val channelRepository = koinInject<ChannelRepository>()
    val appRepository = koinInject<AppRepository>()
    val appCardSize by settingsRepository.appCardSize.collectAsStateWithLifecycle()
    val showMobileApps by settingsRepository.showMobileApps.collectAsStateWithLifecycle()
    val enableAnimations by settingsRepository.enableAnimations.collectAsStateWithLifecycle()
    val suppressOriginalLauncher by settingsRepository.suppressOriginalLauncher.collectAsStateWithLifecycle()
    val suppressLauncherOnlyExternal by settingsRepository.suppressLauncherOnlyExternal.collectAsStateWithLifecycle()
    val developerMode by settingsRepository.developerMode.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Pre-resolve string resources for use in coroutines
    val resetSuccessMsg = stringResource(R.string.settings_reset_success)
    val restoreSuccessMsg = stringResource(R.string.restore_success)
    val restoreFailedPermissionMsg = stringResource(R.string.restore_failed_permission)
    val restoreFailedMsg = stringResource(R.string.restore_failed)
    val backupSuccessMsg = stringResource(R.string.backup_success)
    val backupFailedPermissionMsg = stringResource(R.string.backup_failed_permission)
    val backupFailedMsg = stringResource(R.string.backup_failed)
    val restoreNotFoundMsg = stringResource(R.string.restore_not_found)

    var showWatchNextSettings by remember { mutableStateOf(false) }
    var showInputsSettings by remember { mutableStateOf(false) }
    var showAnimationsSettings by remember { mutableStateOf(false) }
    var showToolbarPlacementSettings by remember { mutableStateOf(false) }
    var showChannelSettings by remember { mutableStateOf(false) }
    var showAboutSettings by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var showRestorePreviewDialog by remember { mutableStateOf<BackupInfo?>(null) }
    var showBackgroundSettings by remember { mutableStateOf(false) }

    fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val packageName = context.packageName
        val serviceName = "$packageName/dev.mudrock.tiviyomitvlauncher.accessibility.LauncherAccessibilityService"
        return enabledServices.contains(serviceName)
    }

    if (showResetConfirmDialog) {
        ResetConfirmDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            onConfirm = {
                showResetConfirmDialog = false
                scope.launch {
                    // Reset all settings
                    settingsRepository.resetSettings()
                    settingsRepository.clearHiddenInputs()
                    
                    // Reset apps (clear favorites, hidden status, custom order)
                    appRepository.resetApps()
                    
                    // Reset channels (enable all, clear custom order)
                    channelRepository.resetChannels()
                    
                    // Clear watch next blacklist
                    channelRepository.clearWatchNextBlacklist()
                    
                    // Refresh apps and channels to apply changes
                    appRepository.refreshAllApplications()
                    channelRepository.refreshAllChannels()
                    
                    Toast.makeText(
                        context,
                        resetSuccessMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    } else if (showPermissionDialog) {
        PermissionRequiredDialog(
            onDismissRequest = { showPermissionDialog = false },
            onOpenSettings = {
                showPermissionDialog = false
                // Open app settings where user can manage permissions
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
                context.startActivity(intent)
            }
        )
    } else if (showWatchNextSettings) {
        WatchNextSettingsDialog(onDismissRequest = { showWatchNextSettings = false })
    } else if (showInputsSettings) {
        InputsSettingsDialog(onDismissRequest = { showInputsSettings = false })
    } else if (showAnimationsSettings) {
        AnimationsSettingsDialog(onDismissRequest = { showAnimationsSettings = false })
    } else if (showToolbarPlacementSettings) {
        ToolbarPlacementSettingsDialog(onDismissRequest = { showToolbarPlacementSettings = false })
    } else if (showChannelSettings) {
        ChannelSettingsDialog(onDismissRequest = { showChannelSettings = false })
    } else if (showAboutSettings) {
        AboutSettingsDialog(onDismissRequest = { showAboutSettings = false })
    } else if (showAccessibilityDialog) {
        AccessibilityRequiredDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            onOpenSettings = {
                showAccessibilityDialog = false
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        )
    } else if (showBackgroundSettings) {
        BackgroundSettingsDialog(onDismissRequest = { showBackgroundSettings = false })
    } else if (showRestorePreviewDialog != null) {
        val backupInfo = showRestorePreviewDialog!!
        RestorePreviewDialog(
            backupInfo = backupInfo,
            onDismissRequest = { showRestorePreviewDialog = null },
            onConfirm = {
                showRestorePreviewDialog = null
                scope.launch {
                    try {
                        backupRepository.restoreBackup()
                        Toast.makeText(
                            context,
                            restoreSuccessMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        Timber.e(e, "Restore failed - IO exception (likely permission)")
                        Toast.makeText(
                            context,
                            restoreFailedPermissionMsg,
                            Toast.LENGTH_LONG
                        ).show()
                        showPermissionDialog = true
                    } catch (e: SecurityException) {
                        Timber.e(e, "Restore failed - security exception")
                        Toast.makeText(
                            context,
                            restoreFailedPermissionMsg,
                            Toast.LENGTH_LONG
                        ).show()
                        showPermissionDialog = true
                    } catch (e: CancellationException) {
                        // Ignore
                    } catch (e: Exception) {
                        Timber.e(e, "Restore failed")
                        Toast.makeText(
                            context,
                            restoreFailedMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    } else {
        Dialog(onDismissRequest = onDismissRequest) {
            // Use Surface for the dialog background (non-clickable)
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.width(400.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_launcher),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // === Appearance Section ===
                    SettingsSectionHeader(title = stringResource(R.string.settings_section_appearance))

                    // App Card Size
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_app_card_size, appCardSize),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row {
                            SmallButton(onClick = {
                                settingsRepository.setAppCardSize(
                                    (appCardSize - 10).coerceAtLeast(
                                        50
                                    )
                                )
                            }) {
                                Text("-")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallButton(onClick = {
                                settingsRepository.setAppCardSize(
                                    (appCardSize + 10).coerceAtMost(
                                        200
                                    )
                                )
                            }) {
                                Text("+")
                            }
                        }
                    }

                    // Show Mobile Apps Toggle
                    CompactSettingsItem(
                        title = stringResource(R.string.apps_show_mobile),
                        description = stringResource(R.string.settings_show_mobile_apps_description),
                        onClick = { settingsRepository.toggleShowMobileApps() },
                        trailingContent = {
                            Switch(
                                checked = showMobileApps,
                                onCheckedChange = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )

                    // Background Settings
                    CompactSettingsItem(
                        title = stringResource(R.string.settings_background),
                        description = stringResource(R.string.settings_background_description),
                        onClick = { showBackgroundSettings = true }
                    )

                    // Animations Settings
                    CompactSettingsItem(
                        title = stringResource(R.string.settings_animations),
                        description = stringResource(R.string.settings_enable_animations_description),
                        onClick = { showAnimationsSettings = true }
                    )

                    // === Layout Section ===
                    SettingsSectionHeader(title = stringResource(R.string.settings_section_layout))

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_toolbar_placement),
                        onClick = { showToolbarPlacementSettings = true }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_channel),
                        onClick = { showChannelSettings = true }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.channel_watch_next),
                        onClick = { showWatchNextSettings = true }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_inputs),
                        onClick = { showInputsSettings = true }
                    )

                    // === Behavior Section ===
                    SettingsSectionHeader(title = stringResource(R.string.settings_section_behavior))

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_suppress_launcher),
                        description = if (suppressOriginalLauncher) {
                            if (isAccessibilityServiceEnabled()) {
                                stringResource(R.string.settings_accessibility_enabled)
                            } else {
                                stringResource(R.string.settings_accessibility_disabled)
                            }
                        } else {
                            stringResource(R.string.settings_suppress_launcher_description)
                        },
                        onClick = {
                            if (suppressOriginalLauncher) {
                                // Disabling - no permission needed
                                settingsRepository.setSuppressOriginalLauncher(false)
                            } else {
                                // Enabling - check permission
                                if (!isAccessibilityServiceEnabled()) {
                                    showAccessibilityDialog = true
                                } else {
                                    settingsRepository.setSuppressOriginalLauncher(true)
                                }
                            }
                        },
                        trailingContent = {
                            Switch(
                                checked = suppressOriginalLauncher,
                                onCheckedChange = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )

                    if (suppressOriginalLauncher) {
                        CompactSettingsItem(
                            title = stringResource(R.string.settings_suppress_only_external),
                            description = stringResource(R.string.settings_suppress_only_external_description),
                            onClick = { settingsRepository.toggleSuppressLauncherOnlyExternal() },
                            trailingContent = {
                                Switch(
                                    checked = suppressLauncherOnlyExternal,
                                    onCheckedChange = null,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        )
                    }

                    // === Data Section ===
                    SettingsSectionHeader(title = stringResource(R.string.settings_section_data))

                    CompactSettingsItem(
                        title = stringResource(R.string.backup),
                        onClick = {
                            if (!backupRepository.hasStoragePermission()) {
                                showPermissionDialog = true
                                return@CompactSettingsItem
                            }
                            scope.launch {
                                try {
                                    backupRepository.createBackup()
                                    Toast.makeText(
                                        context,
                                        backupSuccessMsg,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: IOException) {
                                    Timber.e(e, "Backup failed - IO exception (likely permission)")
                                    Toast.makeText(
                                        context,
                                        backupFailedPermissionMsg,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showPermissionDialog = true
                                } catch (e: SecurityException) {
                                    Timber.e(e, "Backup failed - security exception")
                                    Toast.makeText(
                                        context,
                                        backupFailedPermissionMsg,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showPermissionDialog = true
                                } catch (e: Exception) {
                                    Timber.e(e, "Backup failed")
                                    Toast.makeText(
                                        context,
                                        backupFailedMsg,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.restore),
                        onClick = {
                            if (!backupRepository.hasStoragePermission()) {
                                showPermissionDialog = true
                                return@CompactSettingsItem
                            }
                            val backupInfo = backupRepository.getBackupInfo()
                            if (backupInfo == null) {
                                Toast.makeText(
                                    context,
                                    restoreNotFoundMsg,
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@CompactSettingsItem
                            }
                            showRestorePreviewDialog = backupInfo
                        }
                    )

                    // === System Section ===
                    SettingsSectionHeader(title = stringResource(R.string.settings_section_system))

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_developer_mode),
                        description = stringResource(R.string.settings_developer_mode_description),
                        onClick = { settingsRepository.toggleDeveloperMode() },
                        trailingContent = {
                            Switch(
                                checked = developerMode,
                                onCheckedChange = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_about),
                        onClick = { showAboutSettings = true }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_reset),
                        description = stringResource(R.string.settings_reset_description),
                        onClick = {
                            showResetConfirmDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactSettingsItem(
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        onClick = onClick,
        shape = CardDefaults.shape(shape = MaterialTheme.shapes.small),
        colors = CardDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            pressedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        ),
        scale = CardDefaults.scale(focusedScale = 1.0f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            if (trailingContent != null) {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    trailingContent()
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SmallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        scale = ButtonDefaults.scale(focusedScale = 1.05f),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        content()
    }
}

@Composable
private fun PermissionRequiredDialog(
    onDismissRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.backup_permission_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.backup_permission_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.colors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.close))
                    }
                    Button(onClick = onOpenSettings) {
                        Text(stringResource(R.string.backup_permission_open_settings))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.settings_reset_confirm_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.colors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(stringResource(R.string.settings_reset_confirm_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessibilityRequiredDialog(
    onDismissRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_suppress_launcher),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.settings_accessibility_required),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.colors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.close))
                    }
                    Button(onClick = onOpenSettings) {
                        Text(stringResource(R.string.settings_open_settings))
                    }
                }
            }
        }
    }
}

@Composable
private fun RestorePreviewDialog(
    backupInfo: BackupInfo,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.restore_preview_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(
                        R.string.restore_preview_message,
                        backupInfo.getFormattedDate(),
                        backupInfo.appCardSize,
                        backupInfo.channelCardSize,
                        backupInfo.hiddenAppsCount,
                        backupInfo.favoriteAppsCount,
                        backupInfo.disabledChannelsCount
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.colors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(onClick = onConfirm) {
                        Text(stringResource(R.string.restore_button))
                    }
                }
            }
        }
    }
}