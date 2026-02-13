package nl.ndat.tvlauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import org.koin.compose.koinInject

@Composable
fun AnimationsSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val settingsRepository = koinInject<SettingsRepository>()
    val enableAnimations by settingsRepository.enableAnimations.collectAsStateWithLifecycle()
    val animAppIcon by settingsRepository.animAppIcon.collectAsStateWithLifecycle()
    val animChannelRow by settingsRepository.animChannelRow.collectAsStateWithLifecycle()
    val animChannelMove by settingsRepository.animChannelMove.collectAsStateWithLifecycle()
    val animAppMove by settingsRepository.animAppMove.collectAsStateWithLifecycle()
    val animTransition by settingsRepository.animTransition.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_animations),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Master Switch
                CompactSettingsItem(
                    title = stringResource(R.string.settings_enable_animations),
                    description = stringResource(R.string.settings_enable_animations_description),
                    onClick = { settingsRepository.toggleEnableAnimations() },
                    trailingContent = {
                        Switch(
                            checked = enableAnimations,
                            onCheckedChange = null,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                )

                // Sub Settings - only active if master is on
                if (enableAnimations) {
                    CompactSettingsItem(
                        title = stringResource(R.string.settings_anim_app_icon),
                        description = stringResource(R.string.settings_anim_app_icon_desc),
                        onClick = { settingsRepository.setAnimAppIcon(!animAppIcon) },
                        trailingContent = {
                            Switch(
                                checked = animAppIcon,
                                onCheckedChange = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_anim_channel_row),
                        description = stringResource(R.string.settings_anim_channel_row_desc),
                        onClick = { settingsRepository.setAnimChannelRow(!animChannelRow) },
                        trailingContent = {
                            Switch(
                                checked = animChannelRow,
                                onCheckedChange = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_anim_channel_move),
                        description = stringResource(R.string.settings_anim_channel_move_desc),
                        onClick = { settingsRepository.setAnimChannelMove(!animChannelMove) },
                        trailingContent = {
                            Switch(
                                checked = animChannelMove,
                                onCheckedChange = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_anim_app_move),
                        description = stringResource(R.string.settings_anim_app_move_desc),
                        onClick = { settingsRepository.setAnimAppMove(!animAppMove) },
                        trailingContent = {
                            Switch(
                                checked = animAppMove,
                                onCheckedChange = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )

                    CompactSettingsItem(
                        title = stringResource(R.string.settings_anim_transition),
                        description = stringResource(R.string.settings_anim_transition_desc),
                        onClick = { settingsRepository.setAnimTransition(!animTransition) },
                        trailingContent = {
                            Switch(
                                checked = animTransition,
                                onCheckedChange = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

// Reusing the compact item composable style
@Composable
private fun CompactSettingsItem(
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        onClick = onClick,
        shape = CardDefaults.shape(shape = MaterialTheme.shapes.small),
        colors = CardDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            pressedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        ),
        scale = CardDefaults.scale(focusedScale = 1.0f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            if (trailingContent != null) {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    trailingContent()
                }
            }
        }
    }
}
