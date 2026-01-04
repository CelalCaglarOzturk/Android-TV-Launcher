package nl.ndat.tvlauncher.ui.tab.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun AppPopup(
    isFavorite: Boolean,
    onToggleFavorite: (favorite: Boolean) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = if (isFavorite) "Remove from Home" else "Add to Home",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Button(
            onClick = { onToggleFavorite(!isFavorite) },
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFavorite) "Remove" else "Add"
                )
            }
        }
    }
}
