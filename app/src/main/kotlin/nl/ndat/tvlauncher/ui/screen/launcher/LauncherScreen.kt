package nl.ndat.tvlauncher.ui.screen.launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.ndat.tvlauncher.data.Destinations
import nl.ndat.tvlauncher.ui.tab.apps.AppsTab
import nl.ndat.tvlauncher.ui.tab.home.HomeTab
import nl.ndat.tvlauncher.ui.toolbar.Toolbar
import nl.ndat.tvlauncher.util.composition.LocalBackStack
import nl.ndat.tvlauncher.util.composition.ProvideNavigation
import nl.ndat.tvlauncher.util.modifier.autoFocus

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
                    .autoFocus()
                    .fillMaxSize()
            ) {
                val backStack = LocalBackStack.current
                val currentDestination = remember(backStack.size) {
                    backStack.lastOrNull() ?: Destinations.Home
                }

                // Direct content rendering without animation
                when (currentDestination) {
                    Destinations.Home -> HomeTab(modifier = Modifier.fillMaxSize())
                    Destinations.Apps -> AppsTab(modifier = Modifier.fillMaxSize())
                    else -> HomeTab(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
