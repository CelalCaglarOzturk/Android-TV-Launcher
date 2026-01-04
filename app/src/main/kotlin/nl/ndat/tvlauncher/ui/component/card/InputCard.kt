package nl.ndat.tvlauncher.ui.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.model.InputType
import nl.ndat.tvlauncher.data.sqldelight.Input

@Composable
fun InputCard(
    input: Input,
    modifier: Modifier = Modifier,
    baseHeight: Dp = 100.dp,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(baseHeight),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            )
        ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = getInputIcon(input.type)),
                contentDescription = input.displayName,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = input.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun getInputIcon(type: InputType?): Int {
    return when (type) {
        InputType.HDMI -> R.drawable.ic_input
        InputType.TUNER -> R.drawable.ic_input
        InputType.COMPOSITE -> R.drawable.ic_input
        InputType.COMPONENT -> R.drawable.ic_input
        InputType.DISPLAY_PORT -> R.drawable.ic_input
        InputType.SCART -> R.drawable.ic_input
        InputType.DVI -> R.drawable.ic_input
        InputType.VGA -> R.drawable.ic_input
        InputType.SVIDEO -> R.drawable.ic_input
        InputType.OTHER -> R.drawable.ic_input
        null -> R.drawable.ic_input
    }
}
