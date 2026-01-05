package nl.ndat.tvlauncher.ui.tab.home.row

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.data.sqldelight.App
import nl.ndat.tvlauncher.data.sqldelight.Channel
import nl.ndat.tvlauncher.data.sqldelight.ChannelProgram
import nl.ndat.tvlauncher.ui.component.PopupContainer
import nl.ndat.tvlauncher.ui.component.card.ChannelProgramCard
import nl.ndat.tvlauncher.ui.tab.home.ChannelPopup
import nl.ndat.tvlauncher.util.modifier.ifElse

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChannelProgramCardRow(
    modifier: Modifier = Modifier,
    title: String,
    programs: List<ChannelProgram>,
    app: App?,
    channel: Channel? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    baseHeight: Dp = 90.dp,
    overrideAspectRatio: Float? = null,
    onToggleEnabled: ((enabled: Boolean) -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onRemoveProgram: ((program: ChannelProgram) -> Unit)? = null,
) {
    var focusedProgram by remember { mutableStateOf<ChannelProgram?>(null) }
    var popupVisible by remember { mutableStateOf(false) }
    var isInMoveMode by remember { mutableStateOf(false) }
    var ignoreNextKeyUp by remember { mutableStateOf(false) }

    val titleInteractionSource = remember { MutableInteractionSource() }
    val titleFocused by titleInteractionSource.collectIsFocusedAsState()
    val titleFocusRequester = remember { FocusRequester() }

    if (programs.isNotEmpty()) {
        PopupContainer(
            visible = popupVisible && channel != null && onToggleEnabled != null,
            onDismiss = { popupVisible = false },
            content = {
                Column(modifier = modifier) {
                    // Row title with optional long-press for channel management
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 4.dp,
                                horizontal = 48.dp,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            color = if (titleFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .then(
                                    if (channel != null && onToggleEnabled != null) {
                                        Modifier
                                            .focusRequester(titleFocusRequester)
                                            .onPreviewKeyEvent { event ->
                                                if (ignoreNextKeyUp && event.type == KeyEventType.KeyUp) {
                                                    ignoreNextKeyUp = false
                                                    return@onPreviewKeyEvent true
                                                }

                                                if (isInMoveMode) {
                                                    if (event.type == KeyEventType.KeyDown) {
                                                        when (event.key.nativeKeyCode) {
                                                            KeyEvent.KEYCODE_DPAD_UP -> {
                                                                onMoveUp?.invoke()
                                                                return@onPreviewKeyEvent true
                                                            }

                                                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                                                onMoveDown?.invoke()
                                                                return@onPreviewKeyEvent true
                                                            }

                                                            KeyEvent.KEYCODE_DPAD_CENTER,
                                                            KeyEvent.KEYCODE_ENTER,
                                                            KeyEvent.KEYCODE_BACK -> {
                                                                isInMoveMode = false
                                                                ignoreNextKeyUp = true
                                                                titleFocusRequester.requestFocus()
                                                                return@onPreviewKeyEvent true
                                                            }
                                                        }
                                                    } else if (event.type == KeyEventType.KeyUp) {
                                                        when (event.key.nativeKeyCode) {
                                                            KeyEvent.KEYCODE_DPAD_CENTER,
                                                            KeyEvent.KEYCODE_ENTER -> {
                                                                return@onPreviewKeyEvent true
                                                            }
                                                        }
                                                    }
                                                }
                                                false
                                            }
                                            .focusable(interactionSource = titleInteractionSource)
                                            .combinedClickable(
                                                interactionSource = titleInteractionSource,
                                                indication = null,
                                                onClick = { },
                                                onLongClick = {
                                                    if (!isInMoveMode) popupVisible = true
                                                }
                                            )
                                            .ifElse(
                                                isInMoveMode,
                                                Modifier
                                                    .border(
                                                        BorderStroke(2.dp, Color.White),
                                                        shape = MaterialTheme.shapes.small
                                                    )
                                                    .padding(4.dp)
                                            )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }

                    val childFocusRequester = remember { FocusRequester() }

                    LazyRow(
                        contentPadding = PaddingValues(
                            vertical = 4.dp,
                            horizontal = 48.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRestorer(childFocusRequester),
                    ) {
                        itemsIndexed(
                            items = programs,
                            key = { _, program -> program.id },
                        ) { index, program ->
                            Box {
                                var isLongPress by remember { mutableStateOf(false) }

                                ChannelProgramCard(
                                    program = program,
                                    baseHeight = baseHeight,
                                    overrideAspectRatio = overrideAspectRatio,
                                    modifier = Modifier
                                        .ifElse(
                                            condition = index == 0,
                                            positiveModifier = Modifier.focusRequester(childFocusRequester)
                                        )
                                        .onFocusChanged { state ->
                                            if (state.hasFocus && focusedProgram != program) focusedProgram = program
                                            else if (!state.hasFocus && focusedProgram == program) focusedProgram = null
                                        }
                                        .onPreviewKeyEvent { event ->
                                            if (onRemoveProgram != null &&
                                                (event.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER || event.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER)
                                            ) {
                                                if (event.type == KeyEventType.KeyDown) {
                                                    if (event.nativeKeyEvent.repeatCount > 0) {
                                                        isLongPress = true
                                                    }
                                                } else if (event.type == KeyEventType.KeyUp) {
                                                    if (isLongPress) {
                                                        isLongPress = false
                                                        onRemoveProgram(program)
                                                        return@onPreviewKeyEvent true
                                                    }
                                                }
                                            }
                                            false
                                        },
                                )
                            }
                        }
                    }
                }
            },
            popupContent = {
                if (channel != null && onToggleEnabled != null) {
                    ChannelPopup(
                        channelName = channel.displayName,
                        isEnabled = channel.enabled,
                        onToggleEnabled = onToggleEnabled,
                        onEnterMoveMode = { isInMoveMode = true },
                        onDismiss = { popupVisible = false }
                    )
                }
            }
        )

        // Show focused program details without animation
        if (focusedProgram != null) {
            ChannelProgramCardDetails(focusedProgram!!, app)
        }
    }
}
