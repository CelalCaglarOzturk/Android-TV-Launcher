package nl.ndat.tvlauncher.ui.tab.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    // Use lifecycle-aware collection for better performance
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val allAppChannels by viewModel.allAppChannels.collectAsStateWithLifecycle()
    val watchNextPrograms by viewModel.watchNextPrograms.collectAsStateWithLifecycle()

    // Use derivedStateOf for filtered lists to avoid unnecessary recompositions
    val enabledChannels by remember(channels) {
        derivedStateOf {
            channels.filter { it.type == ChannelType.PREVIEW && it.enabled }
        }
    }

    val disabledChannels by remember(allAppChannels) {
        derivedStateOf {
            allAppChannels.filter { !it.enabled && it.type == ChannelType.PREVIEW }
        }
    }

    val hasWatchNext by remember(watchNextPrograms) {
        derivedStateOf { watchNextPrograms.isNotEmpty() }
    }

    val hasDisabledChannels by remember(disabledChannels) {
        derivedStateOf { disabledChannels.isNotEmpty() }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item(key = "apps") {
            AppCardRow(apps = apps)
        }

        if (hasWatchNext) {
            item(key = ChannelResolver.CHANNEL_ID_WATCH_NEXT) {
                ChannelProgramCardRow(
                    title = stringResource(R.string.channel_watch_next),
                    programs = watchNextPrograms,
                    app = null,
                )
            }
        }

        itemsIndexed(
            items = enabledChannels,
            key = { _, channel -> channel.id }
        ) { index, channel ->
            val app = remember(channel.packageName, apps) {
                apps.firstOrNull { app -> app.packageName == channel.packageName }
            }
            val programs by viewModel.channelPrograms(channel).collectAsStateWithLifecycle(initialValue = emptyList())

            if (app != null && programs.isNotEmpty()) {
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
        if (hasDisabledChannels) {
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
