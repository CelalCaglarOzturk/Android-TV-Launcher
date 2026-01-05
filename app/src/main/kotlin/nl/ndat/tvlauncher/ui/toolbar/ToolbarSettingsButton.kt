package nl.ndat.tvlauncher.ui.toolbar

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
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
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.BackupRepository
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import nl.ndat.tvlauncher.ui.settings.WatchNextSettingsDialog
import org.koin.compose.koinInject

@Composable
fun ToolbarSettingsButton() = Box {
    val context = LocalContext.current
    val settingsRepository = koinInject<SettingsRepository>()
    val backupRepository = koinInject<BackupRepository>()
    val appCardSize by settingsRepository.appCardSize.collectAsState()
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var showLauncherSettings by remember { mutableStateOf(false) }
    var showWatchNextSettings by remember { mutableStateOf(false) }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                scope.launch {
                    backupRepository.exportBackup(uri)
                }
            }
        }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                backupRepository.importBackup(uri)
            }
        }
    }

    IconButton(
        onClick = { showDialog = true }
    ) {
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = stringResource(id = R.string.settings),
        )
    }

    if (showDialog) {
        Dialog(onDismissRequest = {
            showDialog = false
            showLauncherSettings = false
        }) {
            Surface(shape = MaterialTheme.shapes.medium) {
                if (showLauncherSettings) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_launcher),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("App Card Size: $appCardSize dp")
                            Row {
                                Button(onClick = { settingsRepository.setAppCardSize((appCardSize - 10).coerceAtLeast(50)) }) {
                                    Text("-")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { settingsRepository.setAppCardSize((appCardSize + 10).coerceAtMost(200)) }) {
                                    Text("+")
                                }

                                if (showWatchNextSettings) {
                                    WatchNextSettingsDialog(onDismissRequest = { showWatchNextSettings = false })
                                }
                            }
                        }

                        ListItem(
                            selected = false,
                            onClick = { showWatchNextSettings = true },
                            headlineContent = { Text(stringResource(R.string.channel_watch_next)) }
                        )

                        ListItem(
                            selected = false,
                            onClick = { exportLauncher.launch("tv_launcher_backup.json") },
                            headlineContent = { Text("Export Backup") }
                        )

                        ListItem(
                            selected = false,
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            headlineContent = { Text("Import Backup") }
                        )
                    }
                } else {
                    Column {
                        ListItem(
                            selected = false,
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                showDialog = false
                            },
                            headlineContent = { Text(stringResource(R.string.settings_system)) }
                        )
                        ListItem(
                            selected = false,
                            onClick = { showLauncherSettings = true },
                            headlineContent = { Text(stringResource(R.string.settings_launcher)) }
                        )
                    }
                }
            }
        }
    }
}
