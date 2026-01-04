package nl.ndat.tvlauncher.ui.tab.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.sqldelight.App
import nl.ndat.tvlauncher.data.sqldelight.Channel

@Composable
fun DisabledChannelsSection(
    modifier: Modifier = Modifier,
    disabledChannels: List<Channel>,
    apps: List<App>,
    onEnableChannel: (Channel) -> Unit
) {
    if (disabledChannels.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.disabled_channels),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                vertical = 4.dp,
                horizontal = 48.dp,
            )
        )

        LazyRow(
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 48.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = disabledChannels,
                key = { channel -> "disabled_${channel.id}" }
            ) { channel ->
                val app = remember(channel.packageName, apps) {
                    apps.firstOrNull { app -> app.packageName == channel.packageName }
                }

                DisabledChannelCard(
                    channel = channel,
                    appName = app?.displayName ?: channel.packageName,
                    onEnable = { onEnableChannel(channel) }
                )
            }
        }
    }
}

@Composable
private fun DisabledChannelCard(
    channel: Channel,
    appName: String,
    onEnable: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardCardContainer(
        modifier = modifier.width(200.dp),
        imageCard = { interactionSource ->
            androidx.tv.material3.Card(
                onClick = onEnable,
                interactionSource = interactionSource,
                colors = CardDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = channel.displayName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = appName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.channel_enable),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.channel_enable),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        title = { }
    )
}
