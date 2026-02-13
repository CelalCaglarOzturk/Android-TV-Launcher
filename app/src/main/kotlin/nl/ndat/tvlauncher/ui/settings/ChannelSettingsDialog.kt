package nl.ndat.tvlauncher.ui.settings

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import org.koin.compose.koinInject

@Composable
fun ChannelSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val settingsRepository = koinInject<SettingsRepository>()
    val channelCardSize by settingsRepository.channelCardSize.collectAsStateWithLifecycle()
    val channelCardsPerRow by settingsRepository.channelCardsPerRow.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismissRequest) {
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
                    text = stringResource(R.string.settings_channel),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
                        Button(
                            onClick = {
                                settingsRepository.setChannelCardSize(
                                    (channelCardSize - 10).coerceAtLeast(50)
                                )
                            },
                            scale = ButtonDefaults.scale(focusedScale = 1.05f),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text("-")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                settingsRepository.setChannelCardSize(
                                    (channelCardSize + 10).coerceAtMost(200)
                                )
                            },
                            scale = ButtonDefaults.scale(focusedScale = 1.05f),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text("+")
                        }
                    }
                }

                // Channel Cards Per Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_channel_cards_per_row, channelCardsPerRow),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.settings_channel_cards_per_row_description),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Row {
                        Button(
                            onClick = {
                                settingsRepository.setChannelCardsPerRow(channelCardsPerRow - 1)
                            },
                            scale = ButtonDefaults.scale(focusedScale = 1.05f),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text("-")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                settingsRepository.setChannelCardsPerRow(channelCardsPerRow + 1)
                            },
                            scale = ButtonDefaults.scale(focusedScale = 1.05f),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text("+")
                        }
                    }
                }
            }
        }
    }
}
