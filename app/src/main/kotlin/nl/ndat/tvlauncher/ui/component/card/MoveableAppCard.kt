package nl.ndat.tvlauncher.ui.component.card

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.sqldelight.App
import nl.ndat.tvlauncher.ui.component.PopupContainer
import nl.ndat.tvlauncher.util.createDrawable
import nl.ndat.tvlauncher.util.modifier.ifElse

@Composable
fun MoveableAppCard(
    app: App,
    modifier: Modifier = Modifier,
    baseHeight: Dp = 90.dp,
    isInMoveMode: Boolean = false,
    isFavorite: Boolean = false,
    onMoveModeChanged: ((Boolean) -> Unit)? = null,
    onMove: ((direction: MoveDirection) -> Unit)? = null,
    onToggleFavorite: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current
    val image = remember(app.id) { app.createDrawable(context) }
    var imagePrimaryColor by remember { mutableStateOf<Color?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val launchIntentUri = remember(app.id) {
        app.launchIntentUriLeanback ?: app.launchIntentUriDefault
    }

    var menuVisible by remember { mutableStateOf(false) }

    // Border color based on state
    val borderColor = when {
        isInMoveMode -> Color.White
        imagePrimaryColor != null -> imagePrimaryColor!!
        else -> MaterialTheme.colorScheme.border
    }

    val borderWidth = if (isInMoveMode) 3.dp else 2.dp

    PopupContainer(
        visible = menuVisible && !isInMoveMode,
        onDismiss = { menuVisible = false },
        content = {
            StandardCardContainer(
                modifier = modifier
                    .width(baseHeight * (16f / 9f))
                    .onKeyEvent { event ->
                        // Handle move mode key events
                        if (isInMoveMode && event.type == KeyEventType.KeyDown) {
                            when (event.key.nativeKeyCode) {
                                KeyEvent.KEYCODE_DPAD_LEFT -> {
                                    onMove?.invoke(MoveDirection.LEFT)
                                    return@onKeyEvent true
                                }

                                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                    onMove?.invoke(MoveDirection.RIGHT)
                                    return@onKeyEvent true
                                }

                                KeyEvent.KEYCODE_DPAD_UP -> {
                                    onMove?.invoke(MoveDirection.UP)
                                    return@onKeyEvent true
                                }

                                KeyEvent.KEYCODE_DPAD_DOWN -> {
                                    onMove?.invoke(MoveDirection.DOWN)
                                    return@onKeyEvent true
                                }

                                KeyEvent.KEYCODE_DPAD_CENTER,
                                KeyEvent.KEYCODE_ENTER -> {
                                    onMoveModeChanged?.invoke(false)
                                    return@onKeyEvent true
                                }

                                KeyEvent.KEYCODE_BACK -> {
                                    onMoveModeChanged?.invoke(false)
                                    return@onKeyEvent true
                                }
                            }
                        }
                        false
                    },
                interactionSource = interactionSource,
                title = {
                    Text(
                        text = app.displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        softWrap = false,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .ifElse(
                                focused && !isInMoveMode,
                                Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    initialDelayMillis = 0,
                                ),
                            )
                            .padding(top = 6.dp),
                    )
                },
                imageCard = { _ ->
                    Card(
                        modifier = Modifier
                            .height(baseHeight)
                            .aspectRatio(16f / 9f),
                        interactionSource = interactionSource,
                        border = CardDefaults.border(
                            focusedBorder = Border(
                                border = BorderStroke(borderWidth, borderColor),
                            )
                        ),
                        onClick = {
                            if (!isInMoveMode && launchIntentUri != null) {
                                context.startActivity(Intent.parseUri(launchIntentUri, 0))
                            }
                        },
                        onLongClick = {
                            if (!isInMoveMode) {
                                menuVisible = true
                            }
                        }
                    ) {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = image,
                            contentDescription = app.displayName,
                            onSuccess = { result ->
                                if (imagePrimaryColor == null) {
                                    val palette = Palette.from(result.result.drawable.toBitmap()).generate()
                                    imagePrimaryColor = palette.mutedSwatch?.rgb?.let(::Color)
                                }
                            }
                        )
                    }
                }
            )
        },
        popupContent = {
            AppOptionsPopup(
                app = app,
                isFavorite = isFavorite,
                onOpen = {
                    menuVisible = false
                    if (launchIntentUri != null) {
                        context.startActivity(Intent.parseUri(launchIntentUri, 0))
                    }
                },
                onMove = {
                    menuVisible = false
                    onMoveModeChanged?.invoke(true)
                },
                onToggleFavorite = {
                    menuVisible = false
                    onToggleFavorite?.invoke(!isFavorite)
                },
                onInfo = {
                    menuVisible = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        }
    )
}

@Composable
private fun AppOptionsPopup(
    app: App,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onMove: () -> Unit,
    onToggleFavorite: () -> Unit,
    onInfo: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = app.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 8.dp)
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
            onClick = onToggleFavorite,
            icon = if (isFavorite) Icons.Default.Clear else Icons.Default.Home,
            text = if (isFavorite) stringResource(R.string.app_remove_from_home) else stringResource(R.string.app_add_to_home)
        )

        // Info
        KeyDownButton(
            onClick = onInfo,
            icon = Icons.Default.Settings,
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
            .width(200.dp)
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
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text)
        }
    }
}

enum class MoveDirection {
    LEFT, RIGHT, UP, DOWN
}
