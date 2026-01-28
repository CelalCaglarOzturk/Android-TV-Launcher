package nl.ndat.tvlauncher.ui.screen.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import nl.ndat.tvlauncher.data.Destinations
import nl.ndat.tvlauncher.ui.settings.LauncherSettingsDialog
import nl.ndat.tvlauncher.ui.tab.apps.AppsTab
import nl.ndat.tvlauncher.ui.tab.home.HomeTab
import nl.ndat.tvlauncher.ui.toolbar.Toolbar
import nl.ndat.tvlauncher.util.FocusController
import nl.ndat.tvlauncher.util.composition.LocalBackStack
import nl.ndat.tvlauncher.util.composition.ProvideNavigation
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LauncherScreen() {
    val focusController = koinInject<FocusController>()
    var showLauncherSettings by remember { mutableStateOf(false) }

    if (showLauncherSettings) {
        LauncherSettingsDialog(onDismissRequest = { showLauncherSettings = false })
    }

    BackHandler {
        focusController.requestFocusReset()
    }

    ProvideNavigation {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Toolbar(
                modifier = Modifier
                    .padding(
                        vertical = 27.dp,
                        horizontal = 48.dp,
                    ),
                onOpenLauncherSettings = { showLauncherSettings = true }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val backStack = LocalBackStack.current
                val currentDestination by remember {
                    derivedStateOf {
                        backStack.lastOrNull() ?: Destinations.Home
                    }
                }

                val isHomeVisible = currentDestination == Destinations.Home
                val isAppsVisible = currentDestination == Destinations.Apps

                // Both tabs are always composed and kept in memory for instant switching.
                // We use graphicsLayer for visibility (more efficient than alpha modifier)
                // and focusProperties with FocusRequester.Cancel to block focus traversal
                // on the inactive tab completely.

                // Home Tab - always composed
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = if (isHomeVisible) 1f else 0f
                            translationX = if (isHomeVisible) 0f else 10000f
                        }
                ) {
                    HomeTab(
                        modifier = Modifier.fillMaxSize(),
                        isActive = isHomeVisible
                    )
                }

                // Apps Tab - always composed
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = if (isAppsVisible) 1f else 0f
                            translationX = if (isAppsVisible) 0f else 10000f
                        }
                ) {
                    AppsTab(
                        modifier = Modifier.fillMaxSize(),
                        isActive = isAppsVisible
                    )
                }
            }
        }
    }
}
