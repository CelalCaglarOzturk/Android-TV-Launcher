package nl.ndat.tvlauncher.ui.toolbar

import android.content.Intent
import android.provider.Settings
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
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import org.koin.compose.koinInject

@Composable
fun ToolbarSettingsButton() = Box {
    val context = LocalContext.current
    val settingsRepository = koinInject<SettingsRepository>()
    val appCardSize by settingsRepository.appCardSize.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showLauncherSettings by remember { mutableStateOf(false) }

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
                            }
                        }
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
