package nl.ndat.tvlauncher.ui.tab.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
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
    val appCardSize by viewModel.appCardSize.collectAsStateWithLifecycle()

    // Use derivedStateOf for filtered lists to avoid unnecessary recompositions
    val enabledChannels by remember(channels) {
        derivedStateOf {
            channels.filter { it.enabled }
        }
    }

    val disabledChannels by remember(allAppChannels) {
        derivedStateOf {
            allAppChannels.filter { !it.enabled }
        }
    }

    val hasDisabledChannels by remember(disabledChannels) {
        derivedStateOf { disabledChannels.isNotEmpty() }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item(
            key = "apps",
            contentType = "apps_row"
        ) {
            AppCardRow(
                apps = apps,
                baseHeight = appCardSize.dp
            )
        }

        itemsIndexed(
            items = enabledChannels,
            key = { _, channel -> channel.id },
            contentType = { _, _ -> "channel_row" }
        ) { index, channel ->
            val app = remember(channel.packageName, apps) {
                apps.firstOrNull { app -> app.packageName == channel.packageName }
            }
            val programs by viewModel.channelPrograms(channel).collectAsStateWithLifecycle(initialValue = emptyList())
            val isWatchNext = channel.type == ChannelType.WATCH_NEXT

            if ((isWatchNext || app != null) && programs.isNotEmpty()) {
                val title = if (isWatchNext) {
                    stringResource(R.string.channel_watch_next)
                } else {
                    stringResource(R.string.channel_preview, app!!.displayName, channel.displayName)
                }

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
                        // Calculate index in LazyColumn: Apps (1) + index
                        val baseIndex = 1
                        if (index > 0) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(baseIndex + index - 1)
                            }
                        }
                    },
                    onMoveDown = {
                        viewModel.moveChannelDown(channel)
                        // Calculate index in LazyColumn: Apps (1) + index
                        val baseIndex = 1
                        if (index < enabledChannels.size - 1) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(baseIndex + index + 1)
                            }
                        }
                    }
                )
            }
        }

        // Show disabled channels section if there are any
        if (hasDisabledChannels) {
            item(
                key = "disabled_channels_header",
                contentType = "disabled_channels_section"
            ) {
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
