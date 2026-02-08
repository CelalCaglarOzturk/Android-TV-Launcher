package nl.ndat.tvlauncher.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Checkbox
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.data.repository.InputRepository
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import org.koin.compose.koinInject

@Composable
fun InputsSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val inputRepository = koinInject<InputRepository>()
    val settingsRepository = koinInject<SettingsRepository>()

    val inputs by inputRepository.getInputs().collectAsState(initial = emptyList())
    val hiddenInputs by settingsRepository.hiddenInputs.collectAsState()

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Inputs",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (inputs.isEmpty()) {
                    Text(
                        text = stringResource(nl.ndat.tvlauncher.R.string.settings_no_inputs),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn {
                        items(inputs) { input ->
                            val isHidden = hiddenInputs.contains(input.id)
                            ListItem(
                                selected = false,
                                onClick = { settingsRepository.toggleInputHidden(input.id) },
                                headlineContent = { Text(input.displayName) },
                                trailingContent = {
                                    Checkbox(
                                        checked = !isHidden,
                                        onCheckedChange = null
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
