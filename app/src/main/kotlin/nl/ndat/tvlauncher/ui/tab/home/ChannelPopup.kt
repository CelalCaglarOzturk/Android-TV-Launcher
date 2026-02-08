package nl.ndat.tvlauncher.ui.tab.home

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

@Composable
fun ChannelPopup(
    channelName: String,
    isEnabled: Boolean,
    onToggleEnabled: (enabled: Boolean) -> Unit,
    onEnterMoveMode: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .padding(4.dp)
    ) {
        Text(
            text = channelName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Enable/Disable button
        KeyDownButton(
            onClick = {
                onToggleEnabled(!isEnabled)
                onDismiss()
            },
            icon = if (isEnabled) Icons.Default.Delete else Icons.Default.Check,
            text = if (isEnabled)
                stringResource(R.string.channel_disable)
            else
                stringResource(R.string.channel_enable),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        // Move button (only shown for enabled channels)
        if (isEnabled) {
            KeyDownButton(
                onClick = {
                    onEnterMoveMode()
                    onDismiss()
                },
                icon = Icons.Default.Menu,
                text = stringResource(R.string.channel_move),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Close button
        KeyDownButton(
            onClick = onDismiss,
            icon = Icons.Default.Clear,
            text = stringResource(R.string.close),
            modifier = Modifier.fillMaxWidth()
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
    enabled: Boolean = true,
) {
    var hasTriggered by remember { mutableStateOf(false) }

    Button(
        onClick = { /* Disabled - we handle click on key down */ },
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = modifier
            .onPreviewKeyEvent { event ->
                if (!enabled) return@onPreviewKeyEvent false

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
