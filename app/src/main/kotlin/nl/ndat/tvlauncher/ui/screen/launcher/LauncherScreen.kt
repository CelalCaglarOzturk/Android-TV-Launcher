package nl.ndat.tvlauncher.ui.screen.launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.ndat.tvlauncher.data.Destinations
import nl.ndat.tvlauncher.ui.tab.apps.AppsTab
import nl.ndat.tvlauncher.ui.tab.home.HomeTab
import nl.ndat.tvlauncher.ui.toolbar.Toolbar
import nl.ndat.tvlauncher.util.composition.LocalBackStack
import nl.ndat.tvlauncher.util.composition.ProvideNavigation
import nl.ndat.tvlauncher.util.modifier.autoFocus

// String keys for SaveableStateHolder (must be Bundle-compatible types)
private const val KEY_HOME_TAB = "home_tab"
private const val KEY_APPS_TAB = "apps_tab"

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
                val currentDestination by remember {
                    derivedStateOf {
                        backStack.lastOrNull() ?: Destinations.Home
                    }
                }

                // Use SaveableStateHolder to cache tab states (scroll position, etc.)
                // while only rendering one tab at a time to avoid focus conflicts
                val saveableStateHolder = rememberSaveableStateHolder()

                // Determine which tab key to use
                val currentTabKey = remember(currentDestination) {
                    when (currentDestination) {
                        Destinations.Apps -> KEY_APPS_TAB
                        else -> KEY_HOME_TAB
                    }
                }

                // Use key() to help Compose understand tab identity and reduce recomposition
                // This ensures stable identity for each tab content
                key(currentTabKey) {
                    saveableStateHolder.SaveableStateProvider(key = currentTabKey) {
                        when (currentDestination) {
                            Destinations.Apps -> {
                                AppsTab(modifier = Modifier.fillMaxSize())
                            }

                            else -> {
                                HomeTab(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}
