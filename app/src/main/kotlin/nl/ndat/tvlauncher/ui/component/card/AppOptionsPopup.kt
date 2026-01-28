package nl.ndat.tvlauncher.ui.component.card

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.sqldelight.App

@Composable
fun AppOptionsPopup(
    app: App,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onMove: () -> Unit,
    onToggleFavorite: (favorite: Boolean) -> Unit,
    onToggleHidden: ((hidden: Boolean) -> Unit)?,
    onInfo: () -> Unit,
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

        // Open
        KeyDownButton(
            onClick = onOpen,
            icon = Icons.Default.PlayArrow,
            text = stringResource(R.string.app_open)
        )

        // Move
        KeyDownButton(
            onClick = onMove,
            icon = Icons.Default.Menu,
            text = stringResource(R.string.app_move)
        )

        // Add to Home / Remove from Home
        KeyDownButton(
            onClick = { onToggleFavorite(!isFavorite) },
            icon = if (isFavorite) Icons.Default.Clear else Icons.Default.Home,
            text = if (isFavorite) stringResource(R.string.app_remove_from_home) else stringResource(R.string.app_add_to_home)
        )

        // Hide
        if (onToggleHidden != null) {
            KeyDownButton(
                onClick = { onToggleHidden(true) },
                icon = Icons.Default.Delete,
                text = stringResource(R.string.app_hide)
            )
        }

        // Info
        KeyDownButton(
            onClick = onInfo,
            icon = Icons.Default.Info,
            text = stringResource(R.string.app_info)
        )
    }
}

/**
 * A button that triggers onClick on key DOWN instead of key UP.
 * This prevents accidental clicks when releasing a long press.
 */
@Composable
private fun KeyDownButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    var hasTriggered by remember { mutableStateOf(false) }

    Button(
        onClick = { /* Disabled - we handle click on key down */ },
        modifier = modifier
            .onPreviewKeyEvent { event ->
                when {
                    event.type == KeyEventType.KeyDown &&
                            (event.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                    event.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER) -> {
                        if (!hasTriggered) {
                            if (event.nativeKeyEvent.repeatCount == 0) {
                                hasTriggered = true
                                onClick()
                            }
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
            pressedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            pressedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        scale = ButtonDefaults.scale(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}