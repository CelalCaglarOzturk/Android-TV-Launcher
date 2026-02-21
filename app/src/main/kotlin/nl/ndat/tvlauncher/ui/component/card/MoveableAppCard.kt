package nl.ndat.tvlauncher.ui.component.card

import android.content.Intent
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import nl.ndat.tvlauncher.data.sqldelight.App
import nl.ndat.tvlauncher.ui.component.PopupContainer
import nl.ndat.tvlauncher.util.MoveDirection
import org.koin.compose.koinInject

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
    val animAppIcon by settingsRepository.animAppIcon.collectAsState(initial = true)
    val areAnimationsEnabled = enableAnimations && animAppIcon

    // Stable interaction source - remember without keys since it's per-composition
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

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
    val borderWidth = remember(isInMoveMode) { if (isInMoveMode) 3.dp else 3.dp }

    // Memoize card width calculation
    val cardWidth = remember(baseHeight) { baseHeight * (16f / 9f) }
    val density = LocalDensity.current

    val requestWidth = remember(cardWidth, density) { with(density) { cardWidth.roundToPx() } }
    val requestHeight = remember(baseHeight, density) { with(density) { baseHeight.roundToPx() } }

    // Memoize the image request to prevent recreating on every recomposition
    val imageRequest = remember(app.id, context, areAnimationsEnabled, requestWidth, requestHeight) {
        ImageRequest.Builder(context)
            .data(app)
            .memoryCacheKey("app_icon:${app.id}")
            .diskCacheKey("app_icon:${app.id}")
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(areAnimationsEnabled)
            .size(requestWidth, requestHeight)
            .build()
    }

    // Memoize app info intent
    val appInfoIntent = remember(app.packageName) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${app.packageName}".toUri()
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
                        enableAnimations = areAnimationsEnabled,
                        isInMoveMode = isInMoveMode,
                        showHint = isFocused && !isInMoveMode
                    )
                },
                imageCard = { _ ->
                    Card(
                        modifier = Modifier
                            .height(baseHeight)
                            .aspectRatio(16f / 9f)
                            .then(
                                if (isFocused && !isInMoveMode) {
                                    Modifier.drawBehind {
                                        drawRect(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.3f),
                                                    Color.White.copy(alpha = 0.0f)
                                                ),
                                                radius = size.maxDimension * 0.8f
                                            )
                                        )
                                    }
                                } else Modifier
                            ),
                        interactionSource = interactionSource,
                        border = CardDefaults.border(
                            focusedBorder = Border(
                                border = BorderStroke(
                                    borderWidth,
                                    borderColor ?: Color.White
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
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                modifier = Modifier.fillMaxSize(),
                                model = imageRequest,
                                contentDescription = app.displayName,
                                contentScale = ContentScale.Fit,
                            )
                            
                            if (isInMoveMode) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            drawRoundRect(
                                                color = Color.Black.copy(alpha = 0.7f),
                                                cornerRadius = CornerRadius(0.dp.toPx())
                                            )
                                        }
                                ) {
                                    Text(
                                        text = stringResource(R.string.app_moving_instructions),
                                        color = Color.White,
                                        fontSize = with(LocalDensity.current) { (baseHeight.value / 9).dp.toSp() },
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        popupContent = {
            AppOptionsPopup(
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
    isInMoveMode: Boolean,
    showHint: Boolean = false
) {
    val focused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = Modifier.padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.then(
                if (focused && !isInMoveMode && enableAnimations) {
                    Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 0,
                    )
                } else {
                    Modifier
                }
            )
        )
        
        if (showHint) {
            Text(
                text = stringResource(R.string.app_hint_options),
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
