package nl.ndat.tvlauncher.ui.tab.inputs

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.InputRepository
import nl.ndat.tvlauncher.ui.component.card.InputCard
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputsTab(
    modifier: Modifier = Modifier
) {
    val inputRepository = koinInject<InputRepository>()
    val inputs by inputRepository.getInputs().collectAsState(initial = emptyList())
    val context = LocalContext.current

    if (inputs.isEmpty()) {
        // Show empty state
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_input),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = "No TV inputs available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyVerticalGrid(
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            columns = GridCells.Adaptive(160.dp),
            modifier = modifier
                .focusRestorer()
                .fillMaxSize()
        ) {
            items(
                items = inputs,
                key = { input -> input.id },
            ) { input ->
                Box(
                    modifier = Modifier.animateItem()
                ) {
                    InputCard(
                        input = input,
                        onClick = {
                            if (input.switchIntentUri != null) {
                                context.startActivity(Intent.parseUri(input.switchIntentUri, 0))
                            }
                        }
                    )
                }
            }
        }
    }
}
