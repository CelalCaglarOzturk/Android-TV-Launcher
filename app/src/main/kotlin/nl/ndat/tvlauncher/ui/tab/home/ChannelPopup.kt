package nl.ndat.tvlauncher.ui.tab.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
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

    // Delay enabling clicks to prevent the key-up from long press from triggering a button
    var clicksEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300) // Wait for key-up event to pass
        clicksEnabled = true
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
                Button(
                    onClick = {
                        if (clicksEnabled && !isFirst) onMoveUp()
                    },
                    enabled = !isFirst,
                    modifier = Modifier.focusRequester(focusRequester)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.channel_move_up)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.channel_move_up))
                }

                Button(
                    onClick = {
                        if (clicksEnabled && !isLast) onMoveDown()
                    },
                    enabled = !isLast
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.channel_move_down)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.channel_move_down))
                }
            }

            Button(
                onClick = {
                    if (clicksEnabled) {
                        isMoveMode = false
                        onDismiss()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.done)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.done))
            }
        } else {
            // Normal mode UI
            // Enable/Disable button
            Button(
                onClick = {
                    if (clicksEnabled) {
                        onToggleEnabled(!isEnabled)
                        onDismiss()
                    }
                },
                modifier = Modifier.focusRequester(focusRequester)
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.Delete else Icons.Default.Check,
                    contentDescription = if (isEnabled)
                        stringResource(R.string.channel_disable)
                    else
                        stringResource(R.string.channel_enable)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isEnabled)
                        stringResource(R.string.channel_disable)
                    else
                        stringResource(R.string.channel_enable)
                )
            }

            // Move button (only shown for enabled channels)
            if (isEnabled) {
                Button(
                    onClick = { if (clicksEnabled) isMoveMode = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.channel_move)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.channel_move))
                }
            }

            // Close button
            Button(
                onClick = { if (clicksEnabled) onDismiss() }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.close)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.close))
            }
        }
    }
}
