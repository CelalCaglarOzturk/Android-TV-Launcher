package nl.ndat.tvlauncher.ui.tab.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.ui.component.card.AppCard
import nl.ndat.tvlauncher.ui.component.card.MoveableAppCard
import nl.ndat.tvlauncher.ui.component.card.MoveDirection
import nl.ndat.tvlauncher.ui.settings.LauncherSettingsDialog
import nl.ndat.tvlauncher.util.FocusController
import nl.ndat.tvlauncher.util.modifier.ifElse
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppsTab(
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    val viewModel = koinViewModel<AppsTabViewModel>()
    val focusController = koinInject<FocusController>()

    // Use collectAsStateWithLifecycle for better lifecycle handling and performance
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val hiddenApps by viewModel.hiddenApps.collectAsStateWithLifecycle()
    val appCardSize by viewModel.appCardSize.collectAsStateWithLifecycle()

    // Use derivedStateOf to avoid unnecessary recompositions - no keys needed
    // since derivedStateOf already tracks dependencies automatically
    val hasApps by remember { derivedStateOf { apps.isNotEmpty() } }
    val hasHiddenApps by remember { derivedStateOf { hiddenApps.isNotEmpty() } }

    var showSettings by remember { mutableStateOf(false) }
    var moveAppId by remember { mutableStateOf<String?>(null) }

    // Track which app should receive focus after recomposition
    var focusedAppId by remember { mutableStateOf<String?>(null) }

    val firstItemFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    // Create a stable map of focus requesters for each app
    // Use remember with no keys to make it stable across recompositions
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    // Restore focus to the app that was being moved after list recomposes
    // Use a more targeted effect that only runs when focusedAppId changes and tab is active
    LaunchedEffect(focusedAppId, isActive) {
        if (isActive && focusedAppId != null) {
            val focusRequester = focusRequesters[focusedAppId]
            if (focusRequester != null) {
                try {
                    focusRequester.requestFocus()
                    focusedAppId = null
                } catch (e: Exception) {
                    // Ignore focus request failures
                }
            }
        }
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            focusController.focusReset.collect {
                listState.scrollToItem(0)
                try {
                    firstItemFocusRequester.requestFocus()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        } else {
            focusedAppId = null
        }
    }

    // Clean up focus requesters for apps that are no longer in the list
    // This runs only when apps list changes
    LaunchedEffect(apps) {
        val currentAppIds = apps.map { it.id }.toSet()
        focusRequesters.keys.retainAll(currentAppIds)
    }

    if (showSettings) {
        LauncherSettingsDialog(onDismissRequest = { showSettings = false })
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current

        // Memoize dimension calculations
        val itemMinWidth = remember(appCardSize) { appCardSize.dp * (16f / 9f) }
        val spacing = remember { 14.dp }
        val horizontalPadding = remember { 96.dp } // 48.dp * 2

        // Memoize column count calculation to avoid recalculating on every frame
        val columnCount = remember(maxWidth, itemMinWidth, spacing, horizontalPadding) {
            val availableWidth = maxWidth - horizontalPadding
            val itemWidthPx = with(density) { itemMinWidth.toPx() }
            val spacingPx = with(density) { spacing.toPx() }
            val availableWidthPx = with(density) { availableWidth.toPx() }

            ((availableWidthPx + spacingPx) / (itemWidthPx + spacingPx)).toInt().coerceAtLeast(1)
        }

        // Memoize the grid cell configuration
        val gridCells = remember(appCardSize) { GridCells.Adaptive(appCardSize.dp * (16f / 9f)) }

        LazyVerticalGrid(
            state = listState,
            contentPadding = PaddingValues(
                vertical = 4.dp,
                horizontal = 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            columns = gridCells,
            modifier = Modifier
                .fillMaxSize()
                .focusProperties {
                    enter = {
                        if (it == FocusDirection.Down) {
                            firstItemFocusRequester
                        } else {
                            FocusRequester.Default
                        }
                    }
                }
                .focusRestorer(firstItemFocusRequester)
        ) {
            // Visible Apps Section
            if (hasApps) {
                itemsIndexed(
                    items = apps,
                    key = { _, app -> app.id },
                    contentType = { _, _ -> "app_card" }
                ) { index, app ->
                    // Memoize per-item calculations
                    val isSettings = remember(app.packageName) {
                        app.packageName == "nl.ndat.tvlauncher.settings"
                    }
                    val isInMoveMode = moveAppId == app.id
                    val isFavorite = remember(app.favoriteOrder) { app.favoriteOrder != null }

                    // Get or create a focus requester for this app - stable reference
                    val appFocusRequester = remember(app.id) {
                        focusRequesters.getOrPut(app.id) { FocusRequester() }
                    }

                    Box {
                        MoveableAppCard(
                            app = app,
                            baseHeight = appCardSize.dp,
                            modifier = Modifier
                                .focusRequester(appFocusRequester)
                                .ifElse(
                                    condition = index == 0,
                                    positiveModifier = Modifier.focusRequester(firstItemFocusRequester)
                                ),
                            isInMoveMode = isInMoveMode,
                            isFavorite = isFavorite,
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
                                            focusedAppId = app.id
                                            val targetApp = apps[index - 1]
                                            val targetOrder = targetApp.allAppsOrder?.toInt() ?: (index - 1)
                                            viewModel.moveApp(app, targetOrder)
                                            coroutineScope.launch {
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
                                            focusedAppId = app.id
                                            val targetApp = apps[index + 1]
                                            val targetOrder = targetApp.allAppsOrder?.toInt() ?: (index + 1)
                                            viewModel.moveApp(app, targetOrder)
                                            coroutineScope.launch {
                                                try {
                                                    appFocusRequester.requestFocus()
                                                } catch (e: Exception) {
                                                    // Ignore
                                                }
                                            }
                                        }
                                    }

                                    MoveDirection.UP -> {
                                        if (index >= columnCount) {
                                            focusedAppId = app.id
                                            val targetApp = apps[index - columnCount]
                                            val targetOrder = targetApp.allAppsOrder?.toInt() ?: (index - columnCount)
                                            viewModel.moveApp(app, targetOrder)
                                            coroutineScope.launch {
                                                try {
                                                    appFocusRequester.requestFocus()
                                                } catch (e: Exception) {
                                                    // Ignore
                                                }
                                            }
                                        }
                                    }

                                    MoveDirection.DOWN -> {
                                        if (index + columnCount < apps.size) {
                                            focusedAppId = app.id
                                            val targetApp = apps[index + columnCount]
                                            val targetOrder = targetApp.allAppsOrder?.toInt() ?: (index + columnCount)
                                            viewModel.moveApp(app, targetOrder)
                                            coroutineScope.launch {
                                                try {
                                                    appFocusRequester.requestFocus()
                                                } catch (e: Exception) {
                                                    // Ignore
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onToggleFavorite = if (!isSettings) {
                                { favorite -> viewModel.favoriteApp(app, favorite) }
                            } else null,
                            onToggleHidden = if (!isSettings) {
                                { _ -> viewModel.hideApp(app) }
                            } else null,
                            onClick = if (isSettings) {
                                { showSettings = true }
                            } else null
                        )
                    }
                }
            }

            // Hidden Apps Section
            if (hasHiddenApps) {
                item(
                    key = "hidden_apps_header",
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "section_header"
                ) {
                    Text(
                        text = stringResource(R.string.apps_hidden_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                }

                itemsIndexed(
                    items = hiddenApps,
                    key = { _, app -> "hidden_${app.id}" },
                    contentType = { _, _ -> "app_card" }
                ) { index, app ->
                    // Memoize favorite status
                    val isFavorite = remember(app.favoriteOrder) { app.favoriteOrder != null }

                    Box {
                        AppCard(
                            app = app,
                            baseHeight = appCardSize.dp,
                            modifier = Modifier.ifElse(
                                condition = !hasApps && index == 0,
                                positiveModifier = Modifier.focusRequester(firstItemFocusRequester)
                            ),
                            popupContent = {
                                AppPopup(
                                    isFavorite = isFavorite,
                                    isHidden = true,
                                    onToggleFavorite = { favorite ->
                                        viewModel.favoriteApp(app, favorite)
                                    },
                                    onToggleHidden = {
                                        viewModel.unhideApp(app)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
