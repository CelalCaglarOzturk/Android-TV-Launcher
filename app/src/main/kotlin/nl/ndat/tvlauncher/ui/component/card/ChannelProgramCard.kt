package nl.ndat.tvlauncher.ui.component.card

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nl.ndat.tvlauncher.data.sqldelight.ChannelProgram

@Composable
fun ChannelProgramCard(
    program: ChannelProgram,
    modifier: Modifier = Modifier,
    baseHeight: Dp = 100.dp,
    overrideAspectRatio: Float? = null,
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .height(baseHeight)
            .aspectRatio(overrideAspectRatio ?: program.posterArtAspectRatio?.floatValue ?: (16f / 9f)),
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
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}
