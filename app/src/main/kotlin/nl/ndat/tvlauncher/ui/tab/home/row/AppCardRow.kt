package nl.ndat.tvlauncher.ui.tab.home.row

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.data.sqldelight.App
import nl.ndat.tvlauncher.ui.component.card.MoveDirection
import nl.ndat.tvlauncher.ui.component.card.MoveableAppCard
import nl.ndat.tvlauncher.ui.tab.home.HomeTabViewModel
import nl.ndat.tvlauncher.util.modifier.ifElse
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppCardRow(
    apps: List<App>,
    modifier: Modifier = Modifier,
    baseHeight: Dp = 90.dp,
    firstItemFocusRequester: FocusRequester = remember { FocusRequester() },
) {
    val viewModel = koinViewModel<HomeTabViewModel>()

    // Track which app is in move mode (only one at a time)
    var moveAppId by remember { mutableStateOf<String?>(null) }

    // Track which app should receive focus after recomposition
    var focusedAppId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Create a map of focus requesters for each app
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    // Restore focus to the app that was being moved after list recomposes
    LaunchedEffect(apps, focusedAppId) {
        if (focusedAppId != null) {
            val focusRequester = focusRequesters[focusedAppId]
            if (focusRequester != null) {
                try {
                    focusRequester.requestFocus()
                } catch (e: Exception) {
                    // Ignore focus request failures
                }
            }
        }
    }

    CardRow(
        modifier = modifier,
        state = listState,
    ) { childFocusRequester ->
        itemsIndexed(
            items = apps,
            key = { _, app -> app.id },
        ) { index, app ->
            val isInMoveMode = moveAppId == app.id

            // Get or create a focus requester for this app
            val appFocusRequester = remember(app.id) {
                focusRequesters.getOrPut(app.id) { FocusRequester() }
            }

            Box {
                MoveableAppCard(
                    app = app,
                    baseHeight = baseHeight,
                    modifier = Modifier
                        .focusRequester(appFocusRequester)
                        .ifElse(
                            condition = index == 0,
                            positiveModifier = Modifier
                                .focusRequester(childFocusRequester)
                                .focusRequester(firstItemFocusRequester)
                        ),
                    isInMoveMode = isInMoveMode,
                    isFavorite = app.favoriteOrder != null,
                    onMoveModeChanged = { inMoveMode ->
                        moveAppId = if (inMoveMode) app.id else null
                        // Track focused app when entering move mode
                        if (inMoveMode) {
                            focusedAppId = app.id
                        }
                    },
                    onMove = { direction ->
                        when (direction) {
                            MoveDirection.LEFT -> {
                                if (index > 0) {
                                    // Track the app to restore focus
                                    focusedAppId = app.id
                                    viewModel.setFavoriteOrder(app, index - 1)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(maxOf(0, index - 1))
                                        // Request focus after scroll
                                        try {
                                            appFocusRequester.requestFocus()
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }
                                }
                            }

                            MoveDirection.RIGHT -> {
                                if (index < apps.size - 1) {
                                    // Track the app to restore focus
                                    focusedAppId = app.id
                                    viewModel.setFavoriteOrder(app, index + 1)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(minOf(apps.size - 1, index + 1))
                                        // Request focus after scroll
                                        try {
                                            appFocusRequester.requestFocus()
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }
                                }
                            }

                            MoveDirection.UP, MoveDirection.DOWN -> {
                                // UP and DOWN don't apply to horizontal row
                            }
                        }
                    },
                    onToggleFavorite = { favorite ->
                        viewModel.favoriteApp(app, favorite)
                    }
                )
            }
        }
    }

    // Clean up focus requesters for apps that are no longer in the list
    LaunchedEffect(apps) {
        val currentAppIds = apps.map { it.id }.toSet()
        focusRequesters.keys.removeAll { it !in currentAppIds }
    }
}
