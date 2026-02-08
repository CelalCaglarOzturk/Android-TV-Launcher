package nl.ndat.tvlauncher.ui.tab.home.row

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CardRow(
    modifier: Modifier = Modifier,
    title: String? = null,
    state: LazyListState = rememberLazyListState(),
    content: LazyListScope.(childFocusRequester: FocusRequester) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0.5f,
        animationSpec = tween(durationMillis = 300),
        label = "rowAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0.95f,
        animationSpec = tween(durationMillis = 300),
        label = "rowScale"
    )

    Column(
        modifier = modifier
            .onFocusChanged { isFocused = it.hasFocus }
            .alpha(alpha)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        if (title != null) {
            Text(
                text = title,
                fontSize = 18.sp,
                modifier = Modifier.padding(
                    vertical = 4.dp,
                    horizontal = 48.dp,
                )
            )
        }

        val childFocusRequester = remember { FocusRequester() }

        LazyRow(
            state = state,
            contentPadding = PaddingValues(
                vertical = 4.dp,
                horizontal = 48.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRestorer(childFocusRequester),
        ) {
            content(childFocusRequester)
        }
    }
}
