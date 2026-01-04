package nl.ndat.tvlauncher.ui.tab.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.model.ChannelType
import nl.ndat.tvlauncher.data.resolver.ChannelResolver
import nl.ndat.tvlauncher.ui.tab.home.row.AppCardRow
import nl.ndat.tvlauncher.ui.tab.home.row.ChannelProgramCardRow
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeTab(
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<HomeTabViewModel>()
    val apps by viewModel.apps.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val allAppChannels by viewModel.allAppChannels.collectAsState()
    val watchNextPrograms by viewModel.watchNextPrograms.collectAsState()

    // Filter to get only enabled preview channels (not watch next)
    val enabledChannels = remember(channels) {
        channels.filter { it.type == ChannelType.PREVIEW && it.enabled }
    }

    // Filter to get disabled channels
    val disabledChannels = remember(allAppChannels) {
        allAppChannels.filter { !it.enabled && it.type == ChannelType.PREVIEW }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxSize()
    ) {
        item(key = "apps") {
            AppCardRow(
                apps = apps
            )
        }

        item(
            key = ChannelResolver.CHANNEL_ID_WATCH_NEXT
        ) {
            ChannelProgramCardRow(
                title = stringResource(R.string.channel_watch_next),
                programs = watchNextPrograms,
                app = null,
            )
        }

        itemsIndexed(
            items = enabledChannels,
            key = { _, channel -> channel.id }
        ) { index, channel ->
            val app = remember(channel.packageName, apps) {
                apps.firstOrNull { app -> app.packageName == channel.packageName }
            }
            val programs by viewModel.channelPrograms(channel).collectAsState(initial = emptyList())

            if (app != null) {
                val title = stringResource(R.string.channel_preview, app.displayName, channel.displayName)

                ChannelProgramCardRow(
                    title = title,
                    programs = programs,
                    app = app,
                    channel = channel,
                    isFirst = index == 0,
                    isLast = index == enabledChannels.size - 1,
                    onToggleEnabled = { enabled ->
                        viewModel.setChannelEnabled(channel, enabled)
                    },
                    onMoveUp = {
                        viewModel.moveChannelUp(channel)
                    },
                    onMoveDown = {
                        viewModel.moveChannelDown(channel)
                    }
                )
            }
        }

        // Show disabled channels section if there are any
        if (disabledChannels.isNotEmpty()) {
            item(key = "disabled_channels_header") {
                DisabledChannelsSection(
                    disabledChannels = disabledChannels,
                    apps = apps,
                    onEnableChannel = { channel ->
                        viewModel.enableChannel(channel)
                    }
                )
            }
        }
    }
}
