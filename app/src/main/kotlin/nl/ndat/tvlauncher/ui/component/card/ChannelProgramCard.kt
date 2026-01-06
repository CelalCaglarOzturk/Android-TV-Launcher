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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import nl.ndat.tvlauncher.data.sqldelight.ChannelProgram
import nl.ndat.tvlauncher.util.modifier.ifElse
import org.koin.compose.koinInject

@Composable
fun ChannelProgramCard(
    program: ChannelProgram,
    modifier: Modifier = Modifier,
    baseHeight: Dp = 100.dp,
    overrideAspectRatio: Float? = null,
) {
    val context = LocalContext.current
    val settingsRepository = koinInject<SettingsRepository>()
    val enableAnimations by settingsRepository.enableAnimations.collectAsState(initial = true)

    // Stable interaction source - remember without keys since it's per-composition
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Memoize aspect ratio calculation to avoid recalculating on every recomposition
    val aspectRatio = remember(overrideAspectRatio, program.posterArtAspectRatio) {
        overrideAspectRatio ?: program.posterArtAspectRatio?.floatValue ?: (16f / 9f)
    }

    // Memoize card width calculation
    val cardWidth = remember(baseHeight, aspectRatio) { baseHeight * aspectRatio }
    val density = LocalDensity.current

    val requestWidth = remember(cardWidth, density) { with(density) { cardWidth.roundToPx() } }
    val requestHeight = remember(baseHeight, density) { with(density) { baseHeight.roundToPx() } }

    // Memoize the image request to prevent recreating on every recomposition
    // Key on posterArtUri to ensure we reload if the image changes
    val imageRequest = remember(program.posterArtUri, context, enableAnimations, requestWidth, requestHeight) {
        ImageRequest.Builder(context)
            .data(program.posterArtUri)
            .memoryCacheKey("program:${program.id}")
            .diskCacheKey("program:${program.id}")
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .allowHardware(false)
            .crossfade(enableAnimations)
            .size(requestWidth, requestHeight)
            .build()
    }

    // Memoize the launch intent to avoid parsing on every click
    val launchIntent = remember(program.intentUri) {
        program.intentUri?.let { uri ->
            try {
                Intent.parseUri(uri, 0)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Memoize whether we have a title to display
    val hasTitle = remember(program.title) { !program.title.isNullOrEmpty() }

    StandardCardContainer(
        modifier = modifier.width(cardWidth),
        interactionSource = interactionSource,
        title = {
            if (hasTitle) {
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
                            isFocused && enableAnimations,
                            Modifier.basicMarquee(
                                iterations = Int.MAX_VALUE,
                                initialDelayMillis = 2000,
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
                    launchIntent?.let { intent ->
                        context.startActivity(intent)
                    }
                },
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = imageRequest,
                    contentDescription = program.title,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    )
}
