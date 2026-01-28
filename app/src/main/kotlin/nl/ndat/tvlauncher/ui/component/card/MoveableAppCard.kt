package nl.ndat.tvlauncher.ui.component.card

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import nl.ndat.tvlauncher.data.sqldelight.App
import nl.ndat.tvlauncher.ui.component.PopupContainer
import nl.ndat.tvlauncher.util.MoveDirection
import org.koin.compose.koinInject
import androidx.core.net.toUri

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
    onToggleHidden: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val settingsRepository = koinInject<SettingsRepository>()
    val enableAnimations by settingsRepository.enableAnimations.collectAsState(initial = true)

    // Stable interaction source - remember without keys since it's per-composition
    val interactionSource = remember { MutableInteractionSource() }

    // Cache and parse launch intent - only recompute when app changes
    val launchIntent = remember(app.id) {
        val intentUri = app.launchIntentUriLeanback ?: app.launchIntentUriDefault
        intentUri?.let { uri ->
            try {
                Intent.parseUri(uri, 0)
            } catch (e: Exception) {
                null
            }
        }
    }

    var menuVisible by remember { mutableStateOf(false) }
    var ignoreNextKeyUp by remember { mutableStateOf(false) }

    // Memoize border styling based on move mode
    val borderColor = remember(isInMoveMode) {
        if (isInMoveMode) Color.White else null
    }
    val borderWidth = remember(isInMoveMode) { if (isInMoveMode) 3.dp else 2.dp }

    // Memoize card width calculation
    val cardWidth = remember(baseHeight) { baseHeight * (16f / 9f) }
    val density = LocalDensity.current

    val requestWidth = remember(cardWidth, density) { with(density) { cardWidth.roundToPx() } }
    val requestHeight = remember(baseHeight, density) { with(density) { baseHeight.roundToPx() } }

    // Memoize the image request to prevent recreating on every recomposition
    val imageRequest = remember(app.id, context, enableAnimations, requestWidth, requestHeight) {
        ImageRequest.Builder(context)
            .data(app)
            .memoryCacheKey("app_icon:${app.id}")
            .diskCacheKey("app_icon:${app.id}")
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(enableAnimations)
            .size(requestWidth, requestHeight)
            .build()
    }

    // Memoize app info intent
    val appInfoIntent = remember(app.packageName) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    PopupContainer(
        visible = menuVisible && !isInMoveMode,
        onDismiss = { menuVisible = false },
        content = {
            StandardCardContainer(
                modifier = modifier
                    .width(cardWidth)
                    .onPreviewKeyEvent { event ->
                        if (ignoreNextKeyUp && event.type == KeyEventType.KeyUp) {
                            ignoreNextKeyUp = false
                            return@onPreviewKeyEvent true
                        }

                        // Handle move mode key events
                        if (isInMoveMode) {
                            if (event.type == KeyEventType.KeyDown) {
                                when (event.key.nativeKeyCode) {
                                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                                        onMove?.invoke(MoveDirection.LEFT)
                                        return@onPreviewKeyEvent true
                                    }

                                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                        onMove?.invoke(MoveDirection.RIGHT)
                                        return@onPreviewKeyEvent true
                                    }

                                    KeyEvent.KEYCODE_DPAD_UP -> {
                                        onMove?.invoke(MoveDirection.UP)
                                        return@onPreviewKeyEvent true
                                    }

                                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                                        onMove?.invoke(MoveDirection.DOWN)
                                        return@onPreviewKeyEvent true
                                    }

                                    KeyEvent.KEYCODE_DPAD_CENTER,
                                    KeyEvent.KEYCODE_ENTER -> {
                                        onMoveModeChanged?.invoke(false)
                                        ignoreNextKeyUp = true
                                        return@onPreviewKeyEvent true
                                    }

                                    KeyEvent.KEYCODE_BACK -> {
                                        onMoveModeChanged?.invoke(false)
                                        return@onPreviewKeyEvent true
                                    }
                                }
                            } else if (event.type == KeyEventType.KeyUp) {
                                // Consume KeyUp for Center/Enter to prevent click action
                                when (event.key.nativeKeyCode) {
                                    KeyEvent.KEYCODE_DPAD_CENTER,
                                    KeyEvent.KEYCODE_ENTER -> {
                                        return@onPreviewKeyEvent true
                                    }
                                }
                            }
                        }
                        false
                    },
                interactionSource = interactionSource,
                title = {
                    MoveableAppCardTitle(
                        title = app.displayName,
                        interactionSource = interactionSource,
                        enableAnimations = enableAnimations,
                        isInMoveMode = isInMoveMode
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
                                border = BorderStroke(
                                    borderWidth,
                                    borderColor ?: MaterialTheme.colorScheme.border
                                ),
                            )
                        ),
                        onClick = {
                            if (!isInMoveMode) {
                                if (onClick != null) {
                                    onClick()
                                } else {
                                    launchIntent?.let { intent ->
                                        context.startActivity(intent)
                                    }
                                }
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
                            model = imageRequest,
                            contentDescription = app.displayName,
                            contentScale = ContentScale.Fit,
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
                    if (onClick != null) {
                        onClick()
                    } else {
                        launchIntent?.let { intent ->
                            context.startActivity(intent)
                        }
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
                onToggleHidden = if (onToggleHidden != null) {
                    {
                        menuVisible = false
                        onToggleHidden(true)
                    }
                } else null,
                onInfo = {
                    menuVisible = false
                    context.startActivity(appInfoIntent)
                }
            )
        }
    )
}

@Composable
private fun MoveableAppCardTitle(
    title: String,
    interactionSource: InteractionSource,
    enableAnimations: Boolean,
    isInMoveMode: Boolean
) {
    val focused by interactionSource.collectIsFocusedAsState()

    Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        softWrap = false,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier
            .then(
                if (focused && !isInMoveMode && enableAnimations) {
                    Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 0,
                    )
                } else {
                    Modifier
                }
            )
            .padding(top = 6.dp),
    )
}
