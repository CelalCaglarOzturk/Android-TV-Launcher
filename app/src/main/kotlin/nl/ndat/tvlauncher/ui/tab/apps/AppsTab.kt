package nl.ndat.tvlauncher.ui.tab.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.ui.component.card.AppCard
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

    // Use derivedStateOf to avoid unnecessary recompositions
    val hasApps by remember { derivedStateOf { apps.isNotEmpty() } }
    val hasHiddenApps by remember { derivedStateOf { hiddenApps.isNotEmpty() } }
    val hasMobileApps by remember { derivedStateOf { mobileOnlyAppsCount > 0 } }

    LazyVerticalGrid(
        contentPadding = PaddingValues(
            vertical = 16.dp,
            horizontal = 48.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        columns = GridCells.Adaptive(90.dp * (16f / 9f)),
        modifier = modifier.fillMaxSize()
    ) {
        // Mobile Apps Toggle (only show if there are mobile-only apps)
        if (hasMobileApps) {
            item(
                key = "mobile_toggle",
                span = { GridItemSpan(maxLineSpan) }
            ) {
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
        if (hasApps) {
            item(
                key = "all_apps_header",
                span = { GridItemSpan(maxLineSpan) }
            ) {
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
                Box {
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
        if (hasHiddenApps) {
            item(
                key = "hidden_apps_header",
                span = { GridItemSpan(maxLineSpan) }
            ) {
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
                Box {
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
