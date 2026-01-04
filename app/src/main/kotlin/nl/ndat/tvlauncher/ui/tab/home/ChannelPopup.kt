package nl.ndat.tvlauncher.ui.tab.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
                Button(
                    onClick = {
                        if (!isFirst) onMoveUp()
                    },
                    enabled = !isFirst,
                    modifier = Modifier.focusRequester(focusRequester)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = stringResource(R.string.channel_move_up)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.channel_move_up))
                }

                Button(
                    onClick = {
                        if (!isLast) onMoveDown()
                    },
                    enabled = !isLast
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = stringResource(R.string.channel_move_down)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.channel_move_down))
                }
            }

            Button(
                onClick = {
                    isMoveMode = false
                    onDismiss()
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
                    onToggleEnabled(!isEnabled)
                    onDismiss()
                },
                modifier = Modifier.focusRequester(focusRequester)
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
                    onClick = { isMoveMode = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = stringResource(R.string.channel_move)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.channel_move))
                }
            }

            // Close button
            Button(
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.close))
            }
        }
    }
}
