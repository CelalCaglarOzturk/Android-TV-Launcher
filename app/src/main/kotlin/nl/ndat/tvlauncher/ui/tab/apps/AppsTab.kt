package nl.ndat.tvlauncher.ui.tab.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.ui.component.card.AppCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppsTab(
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<AppsTabViewModel>()
    val apps by viewModel.apps.collectAsState()
    val hiddenApps by viewModel.hiddenApps.collectAsState()
    val showMobileApps by viewModel.showMobileApps.collectAsState()
    val mobileOnlyAppsCount by viewModel.mobileOnlyAppsCount.collectAsState()

    LazyVerticalGrid(
        contentPadding = PaddingValues(
            vertical = 16.dp,
            horizontal = 48.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        columns = GridCells.Adaptive(90.dp * (16f / 9f)),
        modifier = modifier
            .focusRestorer()
            .fillMaxSize()
    ) {
        // Mobile Apps Toggle (only show if there are mobile-only apps)
        if (mobileOnlyAppsCount > 0) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ListItem(
                    selected = false,
                    onClick = { viewModel.toggleShowMobileApps() },
                    headlineContent = {
                        Text(
                            text = "Show mobile apps",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "$mobileOnlyAppsCount mobile apps available",
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
                )
            }
        }

        // Visible Apps Section
        if (apps.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "All Apps",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(
                items = apps,
                key = { app -> app.id },
            ) { app ->
                Box(
                    modifier = Modifier
                        .animateItem()
                ) {
                    AppCard(
                        app = app,
                        popupContent = {
                            AppPopup(
                                isFavorite = app.favoriteOrder != null,
                                isHidden = false,
                                onToggleFavorite = { favorite ->
                                    viewModel.favoriteApp(app, favorite)
                                },
                                onToggleHidden = {
                                    viewModel.hideApp(app)
                                }
                            )
                        }
                    )
                }
            }
        }

        // Hidden Apps Section
        if (hiddenApps.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Hidden Apps",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            items(
                items = hiddenApps,
                key = { app -> "hidden_${app.id}" },
            ) { app ->
                Box(
                    modifier = Modifier
                        .animateItem()
                ) {
                    AppCard(
                        app = app,
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
