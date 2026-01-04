package nl.ndat.tvlauncher.ui.tab.home.row

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
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
    onToggleEnabled: ((enabled: Boolean) -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
) {
    var focusedProgram by remember { mutableStateOf<ChannelProgram?>(null) }
    var popupVisible by remember { mutableStateOf(false) }
    val titleInteractionSource = remember { MutableInteractionSource() }
    val titleFocused by titleInteractionSource.collectIsFocusedAsState()

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
                                            .focusable(interactionSource = titleInteractionSource)
                                            .combinedClickable(
                                                interactionSource = titleInteractionSource,
                                                indication = null,
                                                onClick = { },
                                                onLongClick = { popupVisible = true }
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
                            vertical = 16.dp,
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
                            Box(
                                modifier = Modifier.animateItem()
                            ) {
                                ChannelProgramCard(
                                    program = program,
                                    modifier = Modifier
                                        .ifElse(
                                            condition = index == 0,
                                            positiveModifier = Modifier.focusRequester(childFocusRequester)
                                        )
                                        .onFocusChanged { state ->
                                            if (state.hasFocus && focusedProgram != program) focusedProgram = program
                                            else if (!state.hasFocus && focusedProgram == program) focusedProgram = null
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
                        isFirst = isFirst,
                        isLast = isLast,
                        onToggleEnabled = onToggleEnabled,
                        onMoveUp = onMoveUp ?: { },
                        onMoveDown = onMoveDown ?: { },
                        onDismiss = { popupVisible = false }
                    )
                }
            }
        )

        AnimatedContent(
            targetState = focusedProgram,
            label = "ChannelProgramCardRow",
            contentKey = { program -> program?.id },
            transitionSpec = {
                if (initialState == null && targetState != null) slideInVertically() togetherWith fadeOut()
                else if (initialState != null && targetState == null) fadeIn() togetherWith slideOutVertically()
                else fadeIn() togetherWith fadeOut()
            }
        ) { program ->
            if (program != null) {
                ChannelProgramCardDetails(program, app)
            }
        }
    }
}
