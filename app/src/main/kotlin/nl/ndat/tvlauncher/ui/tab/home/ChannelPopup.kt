package nl.ndat.tvlauncher.ui.tab.home

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R

@Composable
fun ChannelPopup(
    channelName: String,
    isEnabled: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleEnabled: (enabled: Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    var isMoveMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = channelName,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        if (isMoveMode) {
            // Move mode UI
            Text(text = stringResource(R.string.channel_move_mode))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyDownButton(
                    onClick = { if (!isFirst) onMoveUp() },
                    icon = Icons.Default.KeyboardArrowUp,
                    text = stringResource(R.string.channel_move_up),
                    enabled = !isFirst,
                    modifier = Modifier.focusRequester(focusRequester)
                )

                KeyDownButton(
                    onClick = { if (!isLast) onMoveDown() },
                    icon = Icons.Default.KeyboardArrowDown,
                    text = stringResource(R.string.channel_move_down),
                    enabled = !isLast
                )
            }

            KeyDownButton(
                onClick = {
                    isMoveMode = false
                    onDismiss()
                },
                icon = Icons.Default.Check,
                text = stringResource(R.string.done)
            )
        } else {
            // Normal mode UI
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
                modifier = Modifier.focusRequester(focusRequester)
            )

            // Move button (only shown for enabled channels)
            if (isEnabled) {
                KeyDownButton(
                    onClick = { isMoveMode = true },
                    icon = Icons.Default.Menu,
                    text = stringResource(R.string.channel_move)
                )
            }

            // Close button
            KeyDownButton(
                onClick = onDismiss,
                icon = Icons.Default.Clear,
                text = stringResource(R.string.close)
            )
        }
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
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text)
    }
}
