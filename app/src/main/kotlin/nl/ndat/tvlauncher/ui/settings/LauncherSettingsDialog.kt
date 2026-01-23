package nl.ndat.tvlauncher.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ListItem
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
            Surface(shape = MaterialTheme.shapes.medium) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.settings_launcher),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // App Card Size
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.settings_app_card_size, appCardSize))
                        Row {
                            Button(onClick = { settingsRepository.setAppCardSize((appCardSize - 10).coerceAtLeast(50)) }) {
                                Text("-")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { settingsRepository.setAppCardSize((appCardSize + 10).coerceAtMost(200)) }) {
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
                            .padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.settings_channel_card_size, channelCardSize))
                        Row {
                            Button(onClick = {
                                settingsRepository.setChannelCardSize(
                                    (channelCardSize - 10).coerceAtLeast(
                                        50
                                    )
                                )
                            }) {
                                Text("-")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                settingsRepository.setChannelCardSize(
                                    (channelCardSize + 10).coerceAtMost(
                                        200
                                    )
                                )
                            }) {
                                Text("+")
                            }
                        }
                    }

                    // Show Mobile Apps Toggle
                    ListItem(
                        selected = false,
                        onClick = { settingsRepository.toggleShowMobileApps() },
                        headlineContent = {
                            Text(stringResource(R.string.apps_show_mobile))
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.settings_show_mobile_apps_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = showMobileApps,
                                onCheckedChange = null
                            )
                        }
                    )

                    ListItem(
                        selected = false,
                        onClick = { settingsRepository.toggleEnableAnimations() },
                        headlineContent = {
                            Text(stringResource(R.string.settings_enable_animations))
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.settings_enable_animations_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = enableAnimations,
                                onCheckedChange = null
                            )
                        }
                    )

                    ListItem(
                        selected = false,
                        onClick = { showWatchNextSettings = true },
                        headlineContent = { Text(stringResource(R.string.channel_watch_next)) }
                    )

                    ListItem(
                        selected = false,
                        onClick = { showInputsSettings = true },
                        headlineContent = { Text(stringResource(R.string.settings_inputs)) }
                    )

                    ListItem(
                        selected = false,
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
                        },
                        headlineContent = { Text(stringResource(R.string.backup)) }
                    )

                    ListItem(
                        selected = false,
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
                        },
                        headlineContent = { Text(stringResource(R.string.restore)) }
                    )

                    ListItem(
                        selected = false,
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
                        },
                        headlineContent = { Text(stringResource(R.string.settings_reset)) },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.settings_reset_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    )
                }
            }
        }
    }
}
