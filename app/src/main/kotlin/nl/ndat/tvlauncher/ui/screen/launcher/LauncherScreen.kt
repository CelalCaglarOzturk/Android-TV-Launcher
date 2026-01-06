package nl.ndat.tvlauncher.ui.screen.launcher

import androidx.compose.foundation.focusGroup
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import nl.ndat.tvlauncher.data.Destinations
import nl.ndat.tvlauncher.ui.tab.apps.AppsTab
import nl.ndat.tvlauncher.ui.tab.home.HomeTab
import nl.ndat.tvlauncher.ui.toolbar.Toolbar
import nl.ndat.tvlauncher.util.composition.LocalBackStack
import nl.ndat.tvlauncher.util.composition.ProvideNavigation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LauncherScreen() {
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
                    )
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
                        }
                        .focusGroup()
                        .focusProperties {
                            // Block all focus directions when not visible
                            if (!isHomeVisible) {
                                onEnter = { FocusRequester.Cancel }
                            }
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
                        }
                        .focusGroup()
                        .focusProperties {
                            // Block all focus directions when not visible
                            if (!isAppsVisible) {
                                onEnter = { FocusRequester.Cancel }
                            }
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
