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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.model.ChannelType
import androidx.compose.runtime.LaunchedEffect
import nl.ndat.tvlauncher.ui.tab.home.row.AppCardRow
import nl.ndat.tvlauncher.ui.tab.home.row.ChannelProgramCardRow
import nl.ndat.tvlauncher.util.FocusController
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun HomeTab(
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<HomeTabViewModel>()
    val focusController = koinInject<FocusController>()

    // Use lifecycle-aware collection for better performance
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val allAppChannels by viewModel.allAppChannels.collectAsStateWithLifecycle()
    val appCardSize by viewModel.appCardSize.collectAsStateWithLifecycle()
    val channelCardSize by viewModel.channelCardSize.collectAsStateWithLifecycle()

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
    val firstItemFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusController.focusReset.collect {
            listState.scrollToItem(0)
            try {
                firstItemFocusRequester.requestFocus()
            } catch (e: Exception) {
                // Ignore focus request failures
            }
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item(
            key = "apps",
            contentType = "apps_row"
        ) {
            AppCardRow(
                apps = apps,
                baseHeight = appCardSize.dp,
                firstItemFocusRequester = firstItemFocusRequester
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
                    baseHeight = channelCardSize.dp,
                    overrideAspectRatio = if (isWatchNext) 16f / 9f else null,
                    onToggleEnabled = { enabled ->
                        viewModel.setChannelEnabled(channel, enabled)
                    },
                    onMoveUp = {
                        viewModel.moveChannelUp(channel)
                    },
                    onMoveDown = {
                        viewModel.moveChannelDown(channel)
                    },
                    onRemoveProgram = if (isWatchNext) {
                        { program -> viewModel.removeWatchNextProgram(program.id) }
                    } else null
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
