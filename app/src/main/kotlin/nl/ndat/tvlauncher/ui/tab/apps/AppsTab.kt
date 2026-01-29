package nl.ndat.tvlauncher.ui.tab.apps

import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.ui.component.card.AppCard
import nl.ndat.tvlauncher.ui.component.card.MoveableAppCard
import nl.ndat.tvlauncher.util.FocusController
import nl.ndat.tvlauncher.util.MoveDirection
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppsTab(
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    val viewModel = koinViewModel<AppsTabViewModel>()
    val focusController = koinInject<FocusController>()

    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val hiddenApps by viewModel.hiddenApps.collectAsStateWithLifecycle()
    val appCardSize by viewModel.appCardSize.collectAsStateWithLifecycle()

    val hasApps by remember { derivedStateOf { apps.isNotEmpty() } }
    val hasHiddenApps by remember { derivedStateOf { hiddenApps.isNotEmpty() } }

    var moveAppId by remember { mutableStateOf<String?>(null) }
    var focusedAppId by remember { mutableStateOf<String?>(null) }

    val firstItemFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyGridState()

    // Stable map of focus requesters
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    // Restore focus logic
    LaunchedEffect(focusedAppId, apps, hiddenApps) {
        focusedAppId?.let { id ->
            val index = apps.indexOfFirst { it.id == id }
            if (index != -1) {
                listState.scrollToItem(index)
            } else {
                val hiddenIndex = hiddenApps.indexOfFirst { it.id == id }
                if (hiddenIndex != -1) {
                    listState.scrollToItem(apps.size + 1 + hiddenIndex)
                }
            }
        }
    }

    // Reset focus on tab activation
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

    // Cleanup focus requesters
    LaunchedEffect(apps, hiddenApps) {
        val currentAppIds = (apps + hiddenApps).map { it.id }.toSet()
        focusRequesters.keys.retainAll(currentAppIds)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current

        val itemMinWidth = remember(appCardSize) { appCardSize.dp * (16f / 9f) }
        val spacing = remember { 14.dp }
        val horizontalPadding = remember { 96.dp }

        val columnCount = remember(maxWidth, itemMinWidth, spacing, horizontalPadding) {
            val availableWidth = maxWidth - horizontalPadding
            val itemWidthPx = with(density) { itemMinWidth.toPx() }
            val spacingPx = with(density) { spacing.toPx() }
            val availableWidthPx = with(density) { availableWidth.toPx() }

            ((availableWidthPx + spacingPx) / (itemWidthPx + spacingPx)).toInt().coerceAtLeast(1)
        }

        val gridCells = remember(columnCount) { GridCells.Fixed(columnCount) }
        val gridWidth = remember(columnCount, itemMinWidth, spacing, horizontalPadding) {
            (itemMinWidth * columnCount) + (spacing * (columnCount - 1)) + horizontalPadding
        }

        LazyVerticalGrid(
            state = listState,
            contentPadding = PaddingValues(
                vertical = 4.dp,
                horizontal = 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            columns = gridCells,
            modifier = Modifier
                .width(gridWidth)
                .fillMaxHeight()
                .focusProperties {
                    onEnter = {
                        if (requestedFocusDirection == FocusDirection.Down) {
                            firstItemFocusRequester
                        } else {
                            FocusRequester.Default
                        }
                    }
                }
                .focusRestorer(firstItemFocusRequester)
        ) {
            // Visible Apps
            if (hasApps) {
                itemsIndexed(
                    items = apps,
                    key = { _, app -> app.id },
                    contentType = { _, _ -> "app_card" }
                ) { index, app ->
                    val isInMoveMode = moveAppId == app.id
                    val isFavorite = remember(app.favoriteOrder) { app.favoriteOrder != null }

                    val appFocusRequester = remember(app.id) {
                        focusRequesters.getOrPut(app.id) { FocusRequester() }
                    }

                    LaunchedEffect(focusedAppId) {
                        if (focusedAppId == app.id) {
                            try {
                                appFocusRequester.requestFocus()
                                if (!isInMoveMode) focusedAppId = null
                            } catch (e: Exception) {
                            }
                        }
                    }

                    MoveableAppCard(
                        app = app,
                        baseHeight = appCardSize.dp,
                        modifier = Modifier
                            .animateItem()
                            .zIndex(if (isInMoveMode) 1f else 0f)
                            .focusRequester(appFocusRequester)
                            .then(
                                if (index == 0) Modifier.focusRequester(firstItemFocusRequester)
                                else Modifier
                            )
                            .focusGroup(),
                        isInMoveMode = isInMoveMode,
                        isFavorite = isFavorite,
                        onMoveModeChanged = { inMoveMode ->
                            moveAppId = if (inMoveMode) app.id else null
                            if (inMoveMode) focusedAppId = app.id
                        },
                        onMove = { direction ->
                            var targetIndex = -1
                            when (direction) {
                                MoveDirection.LEFT -> if (index > 0) targetIndex = index - 1
                                MoveDirection.RIGHT -> if (index < apps.size - 1) targetIndex = index + 1
                                MoveDirection.UP -> if (index >= columnCount) targetIndex = index - columnCount
                                MoveDirection.DOWN -> if (index + columnCount < apps.size) targetIndex =
                                    index + columnCount
                            }

                            if (targetIndex != -1) {
                                focusedAppId = app.id
                                val targetApp = apps[targetIndex]
                                viewModel.swapApps(app, targetApp)
                            }
                        },
                        onToggleFavorite = { favorite -> viewModel.favoriteApp(app, favorite) },
                        onToggleHidden = { _ -> viewModel.hideApp(app) },
                        onClick = null
                    )
                }
            }

            // Skeletons
            if (!hasApps && !hasHiddenApps) {
                items(15) {
                    SkeletonAppCard(appCardSize.dp)
                }
            }

            // Hidden Apps Header
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
                    val isFavorite = remember(app.favoriteOrder) { app.favoriteOrder != null }

                    val appFocusRequester = remember(app.id) {
                        focusRequesters.getOrPut(app.id) { FocusRequester() }
                    }

                    LaunchedEffect(focusedAppId) {
                        if (focusedAppId == app.id) {
                            try {
                                appFocusRequester.requestFocus()
                                focusedAppId = null
                            } catch (e: Exception) {
                            }
                        }
                    }

                    AppCard(
                        app = app,
                        baseHeight = appCardSize.dp,
                        modifier = Modifier
                            .animateItem()
                            .focusRequester(appFocusRequester)
                            .then(
                                if (!hasApps && index == 0) Modifier.focusRequester(firstItemFocusRequester)
                                else Modifier
                            )
                            .focusGroup(),
                        popupContent = {
                            HiddenAppPopup(
                                isFavorite = isFavorite,
                                onToggleFavorite = { favorite -> viewModel.favoriteApp(app, favorite) },
                                onUnhide = {
                                    focusedAppId = app.id
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

@Composable
fun SkeletonAppCard(baseHeight: Dp) {
    Box(
        modifier = Modifier
            .height(baseHeight)
            .width(baseHeight * (16f / 9f))
            .graphicsLayer { alpha = 0.99f }
            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
    )
}

@Composable
private fun HiddenAppPopup(
    isFavorite: Boolean,
    onToggleFavorite: (Boolean) -> Unit,
    onUnhide: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = stringResource(R.string.app_options),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        KeyDownButton(
            onClick = { onToggleFavorite(!isFavorite) },
            icon = if (isFavorite) Icons.Default.Clear else Icons.Default.Home,
            text = if (isFavorite) stringResource(R.string.app_remove_from_home) else stringResource(R.string.app_add_to_home)
        )

        KeyDownButton(
            onClick = onUnhide,
            icon = Icons.Default.Add,
            text = stringResource(R.string.app_unhide)
        )
    }
}

@Composable
private fun KeyDownButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    var hasTriggered by remember { mutableStateOf(false) }

    Button(
        onClick = { },
        modifier = modifier.onPreviewKeyEvent { event ->
            when {
                event.type == KeyEventType.KeyDown &&
                        (event.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                event.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER) -> {
                    if (!hasTriggered && event.nativeKeyEvent.repeatCount == 0) {
                        hasTriggered = true
                        onClick()
                    }
                    true
                }

                event.type == KeyEventType.KeyUp &&
                        (event.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                event.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER) -> {
                    hasTriggered = false
                    true
                }

                else -> false
            }
        },
        colors = ButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            focusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
            focusedContentColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text)
        }
    }
}
