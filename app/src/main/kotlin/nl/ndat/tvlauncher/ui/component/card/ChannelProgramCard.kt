package nl.ndat.tvlauncher.ui.component.card

import android.content.Intent
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import coil.request.ImageRequest
import nl.ndat.tvlauncher.data.sqldelight.ChannelProgram
import nl.ndat.tvlauncher.util.modifier.ifElse

@Composable
fun ChannelProgramCard(
    program: ChannelProgram,
    modifier: Modifier = Modifier,
    baseHeight: Dp = 100.dp,
    overrideAspectRatio: Float? = null,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val aspectRatio = overrideAspectRatio ?: program.posterArtAspectRatio?.floatValue ?: (16f / 9f)
    val cardWidth = remember(baseHeight, aspectRatio) { baseHeight * aspectRatio }

    StandardCardContainer(
        modifier = modifier.width(cardWidth),
        interactionSource = interactionSource,
        title = {
            if (!program.title.isNullOrEmpty()) {
                Text(
                    text = program.title!!,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    softWrap = false,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .ifElse(
                            isFocused,
                            Modifier.basicMarquee(
                                iterations = Int.MAX_VALUE,
                                initialDelayMillis = 0,
                            ),
                        )
                        .padding(top = 6.dp),
                )
            }
        },
        imageCard = { _ ->
            Card(
                modifier = Modifier
                    .height(baseHeight)
                    .aspectRatio(aspectRatio),
                interactionSource = interactionSource,
                border = CardDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.border),
                    )
                ),
                onClick = {
                    if (program.intentUri != null) {
                        context.startActivity(Intent.parseUri(program.intentUri, 0))
                    }
                },
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(program.posterArtUri)
                        .allowHardware(false)
                        .crossfade(true)
                        .build(),
                    contentDescription = program.title,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    )
}
