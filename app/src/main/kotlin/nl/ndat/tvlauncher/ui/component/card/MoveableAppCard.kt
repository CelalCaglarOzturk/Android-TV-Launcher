package nl.ndat.tvlauncher.ui.component.card

import android.content.Intent
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import nl.ndat.tvlauncher.data.sqldelight.App
import nl.ndat.tvlauncher.ui.component.PopupContainer
import nl.ndat.tvlauncher.util.createDrawable
import nl.ndat.tvlauncher.util.modifier.ifElse

private const val LONG_PRESS_THRESHOLD_MS = 500L
private const val MOVE_MODE_THRESHOLD_MS = 1200L

@Composable
fun MoveableAppCard(
    app: App,
    modifier: Modifier = Modifier,
    baseHeight: Dp = 90.dp,
    isInMoveMode: Boolean = false,
    onMoveModeChanged: ((Boolean) -> Unit)? = null,
    onMove: ((direction: MoveDirection) -> Unit)? = null,
    popupContent: (@Composable () -> Unit)? = null,
    onPopupVisibilityChanged: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current
    // Cache the drawable to avoid repeated lookups
    val image = remember(app.id) { app.createDrawable(context) }
    var imagePrimaryColor by remember { mutableStateOf<Color?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val launchIntentUri = app.launchIntentUriLeanback ?: app.launchIntentUriDefault

    var menuVisible by remember { mutableStateOf(false) }
    var selectPressStartTime by remember { mutableLongStateOf(0L) }

    // Border color based on state
    val borderColor = when {
        isInMoveMode -> Color.White
        imagePrimaryColor != null -> imagePrimaryColor!!
        else -> MaterialTheme.colorScheme.border
    }

    val borderWidth = if (isInMoveMode) 3.dp else 2.dp

    PopupContainer(
        visible = menuVisible && popupContent != null && !isInMoveMode,
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
                                    // Exit move mode on confirm
                                    onMoveModeChanged?.invoke(false)
                                    return@onKeyEvent true
                                }

                                KeyEvent.KEYCODE_BACK -> {
                                    // Exit move mode on back
                                    onMoveModeChanged?.invoke(false)
                                    return@onKeyEvent true
                                }
                            }
                        }

                        // Track select button press duration for long-press detection
                        if (event.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            event.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER
                        ) {
                            if (event.type == KeyEventType.KeyDown) {
                                if (selectPressStartTime == 0L) {
                                    selectPressStartTime = System.currentTimeMillis()
                                } else {
                                    // Check if held long enough for move mode
                                    val heldDuration = System.currentTimeMillis() - selectPressStartTime
                                    if (heldDuration >= MOVE_MODE_THRESHOLD_MS && !isInMoveMode) {
                                        onMoveModeChanged?.invoke(true)
                                        selectPressStartTime = 0L
                                        return@onKeyEvent true
                                    }
                                }
                            } else if (event.type == KeyEventType.KeyUp) {
                                val pressDuration = System.currentTimeMillis() - selectPressStartTime
                                selectPressStartTime = 0L

                                when {
                                    pressDuration >= MOVE_MODE_THRESHOLD_MS -> {
                                        // Already handled move mode
                                        return@onKeyEvent true
                                    }

                                    pressDuration >= LONG_PRESS_THRESHOLD_MS -> {
                                        // Show popup
                                        if (popupContent != null) {
                                            menuVisible = true
                                            onPopupVisibilityChanged?.invoke(true)
                                        }
                                        return@onKeyEvent true
                                    }
                                    // Short press - let it fall through to onClick
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
                            if (!isInMoveMode && popupContent != null) {
                                menuVisible = true
                                onPopupVisibilityChanged?.invoke(true)
                            }
                        }
                    ) {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = image,
                            contentDescription = app.displayName,
                            onSuccess = { result ->
                                // Only compute palette if we don't have a color yet
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
            if (popupContent != null) popupContent()
        }
    )
}

enum class MoveDirection {
    LEFT, RIGHT, UP, DOWN
}
