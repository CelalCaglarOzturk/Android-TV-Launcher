package nl.ndat.tvlauncher.ui.tab.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import android.view.KeyEvent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import nl.ndat.tvlauncher.ui.component.card.AppCard
import nl.ndat.tvlauncher.ui.component.card.MoveableAppCard
import nl.ndat.tvlauncher.ui.component.card.MoveDirection
import nl.ndat.tvlauncher.ui.settings.LauncherSettingsDialog
import nl.ndat.tvlauncher.util.modifier.ifElse
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppsTab(
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<AppsTabViewModel>()

    // Use collectAsStateWithLifecycle for better lifecycle handling and performance
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val hiddenApps by viewModel.hiddenApps.collectAsStateWithLifecycle()
    val showMobileApps by viewModel.showMobileApps.collectAsStateWithLifecycle()
    val mobileOnlyAppsCount by viewModel.mobileOnlyAppsCount.collectAsStateWithLifecycle()
    val appCardSize by viewModel.appCardSize.collectAsStateWithLifecycle()

    // Use derivedStateOf to avoid unnecessary recompositions
    val hasApps by remember { derivedStateOf { apps.isNotEmpty() } }
    val hasHiddenApps by remember { derivedStateOf { hiddenApps.isNotEmpty() } }
    val hasMobileApps by remember { derivedStateOf { mobileOnlyAppsCount > 0 } }

    var showSettings by remember { mutableStateOf(false) }
    var moveAppId by remember { mutableStateOf<String?>(null) }
    val firstItemFocusRequester = remember { FocusRequester() }

    if (showSettings) {
        LauncherSettingsDialog(onDismissRequest = { showSettings = false })
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val itemMinWidth = appCardSize.dp * (16f / 9f)
        val spacing = 14.dp
        val horizontalPadding = 96.dp // 48.dp * 2

        val columnCount = remember(maxWidth, itemMinWidth) {
            val availableWidth = maxWidth - horizontalPadding
            val itemWidthPx = with(density) { itemMinWidth.toPx() }
            val spacingPx = with(density) { spacing.toPx() }
            val availableWidthPx = with(density) { availableWidth.toPx() }

            ((availableWidthPx + spacingPx) / (itemWidthPx + spacingPx)).toInt().coerceAtLeast(1)
        }

        LazyVerticalGrid(
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            columns = GridCells.Adaptive(appCardSize.dp * (16f / 9f)),
            modifier = Modifier.fillMaxSize()
        ) {
            // Mobile Apps Toggle (only show if there are mobile-only apps)
            if (hasMobileApps) {
                item(
                    key = "mobile_toggle",
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "mobile_toggle"
                ) {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleShowMobileApps() },
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.apps_show_mobile),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.apps_mobile_available, mobileOnlyAppsCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = showMobileApps,
                                onCheckedChange = null
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .onPreviewKeyEvent {
                                if (it.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_DOWN && it.type == KeyEventType.KeyDown) {
                                    firstItemFocusRequester.requestFocus()
                                    true
                                } else {
                                    false
                                }
                            }
                    )
                }
            }

            // Visible Apps Section
            if (hasApps) {
                item(
                    key = "all_apps_header",
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "section_header"
                ) {
                    Text(
                        text = "All Apps",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                itemsIndexed(
                    items = apps,
                    key = { _, app -> app.id },
                    contentType = { _, _ -> "app_card" }
                ) { index, app ->
                    val isSettings = app.packageName == "nl.ndat.tvlauncher.settings"
                    val isInMoveMode = moveAppId == app.id

                    Box {
                        MoveableAppCard(
                            app = app,
                            baseHeight = appCardSize.dp,
                            modifier = Modifier
                                .ifElse(
                                    condition = index == 0,
                                    positiveModifier = Modifier.focusRequester(firstItemFocusRequester)
                                )
                                .ifElse(
                                    condition = isSettings,
                                    positiveModifier = Modifier.onPreviewKeyEvent { event ->
                                        if (!isInMoveMode &&
                                            (event.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                                    event.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER)
                                        ) {
                                            if (event.type == KeyEventType.KeyUp) {
                                                showSettings = true
                                                true
                                            } else {
                                                false
                                            }
                                        } else {
                                            false
                                        }
                                    }
                                ),
                            isInMoveMode = isInMoveMode,
                            isFavorite = app.favoriteOrder != null,
                            onMoveModeChanged = { inMoveMode ->
                                moveAppId = if (inMoveMode) app.id else null
                            },
                            onMove = { direction ->
                                when (direction) {
                                    MoveDirection.LEFT -> {
                                        if (index > 0) {
                                            val targetApp = apps[index - 1]
                                            val targetOrder = targetApp.allAppsOrder?.toInt() ?: (index - 1)
                                            viewModel.moveApp(app, targetOrder)
                                        }
                                    }

                                    MoveDirection.RIGHT -> {
                                        if (index < apps.size - 1) {
                                            val targetApp = apps[index + 1]
                                            val targetOrder = targetApp.allAppsOrder?.toInt() ?: (index + 1)
                                            viewModel.moveApp(app, targetOrder)
                                        }
                                    }

                                    MoveDirection.UP -> {
                                        if (index >= columnCount) {
                                            val targetApp = apps[index - columnCount]
                                            val targetOrder = targetApp.allAppsOrder?.toInt() ?: (index - columnCount)
                                            viewModel.moveApp(app, targetOrder)
                                        }
                                    }

                                    MoveDirection.DOWN -> {
                                        if (index + columnCount < apps.size) {
                                            val targetApp = apps[index + columnCount]
                                            val targetOrder = targetApp.allAppsOrder?.toInt() ?: (index + columnCount)
                                            viewModel.moveApp(app, targetOrder)
                                        }
                                    }
                                }
                            },
                            onToggleFavorite = if (!isSettings) {
                                { favorite -> viewModel.favoriteApp(app, favorite) }
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

                items(
                    items = hiddenApps,
                    key = { app -> "hidden_${app.id}" },
                    contentType = { "app_card" }
                ) { app ->
                    Box {
                        AppCard(
                            app = app,
                            baseHeight = appCardSize.dp,
                            popupContent = {
                                AppPopup(
                                    isFavorite = app.favoriteOrder != null,
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
