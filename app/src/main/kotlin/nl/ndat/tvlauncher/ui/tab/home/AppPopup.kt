package nl.ndat.tvlauncher.ui.tab.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun AppPopup(
    isFirst: Boolean,
    isLast: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: (favorite: Boolean) -> Unit,
    onMove: (relativePosition: Int) -> Unit,
    onHide: (() -> Unit)? = null,
    onDismiss: () -> Unit = {},
) {
    var isMoveMode by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = if (isMoveMode) "Move App" else "App Options",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        if (isMoveMode) {
            // Move Mode UI
            Text(
                text = "Use arrows to move, press OK when done",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    enabled = !isFirst,
                    modifier = Modifier.size(IconButtonDefaults.MediumButtonSize),
                    onClick = { onMove(-1) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                        contentDescription = "Move Left",
                        modifier = Modifier.size(IconButtonDefaults.MediumIconSize)
                    )
                }

                IconButton(
                    enabled = !isLast,
                    modifier = Modifier.size(IconButtonDefaults.MediumButtonSize),
                    onClick = { onMove(+1) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                        contentDescription = "Move Right",
                        modifier = Modifier.size(IconButtonDefaults.MediumIconSize)
                    )
                }
            }

            Button(
                onClick = {
                    isMoveMode = false
                    onDismiss()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = "Done")
            }
        } else {
            // Normal Options UI

            // Reorder Button
            Button(
                onClick = { isMoveMode = true },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Reorder")
                }
            }

            // Remove from Home Button
            Button(
                onClick = { onToggleFavorite(!isFavorite) },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Clear else Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFavorite) "Remove from Home" else "Add to Home"
                    )
                }
            }

            // Hide App Button
            if (onHide != null) {
                Button(
                    onClick = { onHide() },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Hide App")
                    }
                }
            }
        }
    }
}
