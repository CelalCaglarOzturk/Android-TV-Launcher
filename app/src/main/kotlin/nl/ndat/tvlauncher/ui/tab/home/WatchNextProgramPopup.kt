package nl.ndat.tvlauncher.ui.tab.home

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R

@Composable
fun WatchNextProgramPopup(
    programName: String,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
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
            text = programName,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Remove from Watch Next button
        KeyDownButton(
            onClick = onRemove,
            icon = Icons.Default.Delete,
            text = stringResource(R.string.watch_next_remove),
            modifier = Modifier.focusRequester(focusRequester)
        )

        // Close button
        KeyDownButton(
            onClick = onDismiss,
            icon = Icons.Default.Clear,
            text = stringResource(R.string.close)
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
