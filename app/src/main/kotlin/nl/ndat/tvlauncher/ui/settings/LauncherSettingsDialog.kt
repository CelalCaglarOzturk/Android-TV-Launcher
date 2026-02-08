package nl.ndat.tvlauncher.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.BackupRepository
import nl.ndat.tvlauncher.data.repository.ChannelRepository
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import org.koin.compose.koinInject

@Composable
fun LauncherSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val settingsRepository = koinInject<SettingsRepository>()
    val backupRepository = koinInject<BackupRepository>()
    val channelRepository = koinInject<ChannelRepository>()
    val appCardSize by settingsRepository.appCardSize.collectAsState()
    val channelCardSize by settingsRepository.channelCardSize.collectAsState()
    val showMobileApps by settingsRepository.showMobileApps.collectAsState()
    val enableAnimations by settingsRepository.enableAnimations.collectAsState()
    val scope = rememberCoroutineScope()

    var showWatchNextSettings by remember { mutableStateOf(false) }
    var showInputsSettings by remember { mutableStateOf(false) }

    if (showWatchNextSettings) {
        WatchNextSettingsDialog(onDismissRequest = { showWatchNextSettings = false })
    } else if (showInputsSettings) {
        InputsSettingsDialog(onDismissRequest = { showInputsSettings = false })
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

                    // Channel Card Size
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_channel_card_size, channelCardSize),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row {
                            SmallButton(onClick = {
                                settingsRepository.setChannelCardSize(
                                    (channelCardSize - 10).coerceAtLeast(50)
                                )
                            }) {
                                Text("-")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallButton(onClick = {
                                settingsRepository.setChannelCardSize(
                                    (channelCardSize + 10).coerceAtMost(200)
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

                    // Enable Animations Toggle
                    CompactSettingsItem(
                        title = stringResource(R.string.settings_enable_animations),
                        description = stringResource(R.string.settings_enable_animations_description),
                        onClick = { settingsRepository.toggleEnableAnimations() },
                        trailingContent = {
                            Switch(
                                checked = enableAnimations,
                                onCheckedChange = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.channel_watch_next),
                        onClick = { showWatchNextSettings = true }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_inputs),
                        onClick = { showInputsSettings = true }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.backup),
                        onClick = {
                            scope.launch {
                                try {
                                    backupRepository.createBackup()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.backup_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.backup_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.restore),
                        onClick = {
                            scope.launch {
                                try {
                                    if (!backupRepository.backupExists()) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.restore_not_found),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    }
                                    backupRepository.restoreBackup()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.restore_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: CancellationException) {
                                    // Ignore
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.restore_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_reset),
                        description = stringResource(R.string.settings_reset_description),
                        onClick = {
                            scope.launch {
                                settingsRepository.resetSettings()
                                channelRepository.clearWatchNextBlacklist()
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.settings_reset_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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
