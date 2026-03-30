package nl.ndat.tvlauncher.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import nl.ndat.tvlauncher.util.LauncherConstants
import org.koin.compose.koinInject
import timber.log.Timber

@Composable
fun BackgroundSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val settingsRepository = koinInject<SettingsRepository>()
    val backgroundColor by settingsRepository.backgroundColor.collectAsStateWithLifecycle()
    val backgroundImageUri by settingsRepository.backgroundImageUri.collectAsStateWithLifecycle()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                settingsRepository.setBackgroundImageUri(it.toString())
            } catch (e: Exception) {
                Timber.e(e, "Failed to persist URI permission")
            }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(450.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_background),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.settings_background_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Color Presets
                Text(
                    text = stringResource(R.string.settings_background_colors),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                LauncherConstants.Background.PRESET_BACKGROUNDS.chunked(3).forEach { colorChunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                    ) {
                        colorChunk.forEach { colorLong ->
                            ColorOption(
                                color = Color(colorLong),
                                isSelected = backgroundColor == colorLong && backgroundImageUri == null,
                                onClick = {
                                    settingsRepository.setBackgroundColor(colorLong)
                                    settingsRepository.setBackgroundImageUri(null)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Image Option
                Text(
                    text = stringResource(R.string.settings_background_image),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Current image preview (if set)
                backgroundImageUri?.let { uri ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(Color(backgroundColor))
                    ) {
                        AsyncImage(
                            model = uri.toUri(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { imagePicker.launch(arrayOf("image/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.settings_background_select_image))
                    }
                    if (backgroundImageUri != null) {
                        Button(
                            onClick = { settingsRepository.setBackgroundImageUri(null) },
                            colors = ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.settings_background_clear_image))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { settingsRepository.resetBackground() },
                        colors = ButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.settings_background_reset))
                    }
                    Button(onClick = onDismissRequest) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(MaterialTheme.shapes.small)
            .background(color)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(MaterialTheme.shapes.small)
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier.matchParentSize(),
                colors = ButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {}
        }
    }
}