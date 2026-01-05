package nl.ndat.tvlauncher.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.repository.ChannelRepository
import org.koin.compose.koinInject

@Composable
fun WatchNextSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val appRepository = koinInject<AppRepository>()
    val channelRepository = koinInject<ChannelRepository>()
    val scope = rememberCoroutineScope()

    val apps by appRepository.getApps().collectAsState(initial = emptyList())
    val blacklist by channelRepository.getWatchNextBlacklist().collectAsState(initial = emptyList())

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.channel_watch_next),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(apps) { app ->
                        val isBlacklisted = blacklist.contains(app.packageName)
                        val isEnabled = !isBlacklisted

                        ListItem(
                            selected = false,
                            onClick = {
                                scope.launch {
                                    if (isEnabled) {
                                        channelRepository.addToWatchNextBlacklist(app.packageName)
                                    } else {
                                        channelRepository.removeFromWatchNextBlacklist(app.packageName)
                                    }
                                }
                            },
                            headlineContent = { Text(app.displayName) },
                            trailingContent = {
                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = null // Handled by ListItem onClick
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
