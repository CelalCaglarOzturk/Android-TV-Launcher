package nl.ndat.tvlauncher.util.composition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import nl.ndat.tvlauncher.data.DefaultDestination

val LocalBackStack = staticCompositionLocalOf<SnapshotStateList<Any>> {
    error("LocalBackStack not provided")
}

@Composable
fun ProvideNavigation(
    backStack: SnapshotStateList<Any> = remember { mutableStateListOf<Any>(DefaultDestination) },
    content: @Composable () -> Unit,
) = CompositionLocalProvider(
    LocalBackStack provides backStack,
    content = content,
)
