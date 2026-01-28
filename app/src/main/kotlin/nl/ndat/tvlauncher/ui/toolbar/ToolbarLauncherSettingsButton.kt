package nl.ndat.tvlauncher.ui.toolbar

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import nl.ndat.tvlauncher.R

@Composable
fun ToolbarLauncherSettingsButton(
    onClick: () -> Unit,
) = Box {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(id = R.string.settings_launcher),
        )
    }
}
