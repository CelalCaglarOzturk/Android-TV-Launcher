package nl.ndat.tvlauncher.ui.toolbar

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.repository.InputRepository
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import org.koin.compose.koinInject

@Composable
fun ToolbarInputsButton() {
    val inputRepository = koinInject<InputRepository>()
    val settingsRepository = koinInject<SettingsRepository>()
    val allInputs by inputRepository.getInputs().collectAsStateWithLifecycle(initialValue = emptyList())
    val hiddenInputs by settingsRepository.hiddenInputs.collectAsStateWithLifecycle()

    val inputs by remember(allInputs, hiddenInputs) {
        derivedStateOf {
            allInputs.filter { !hiddenInputs.contains(it.id) }
        }
    }

    val context = LocalContext.current

    var expand by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expand = true },
        enabled = inputs.isNotEmpty()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_input),
            contentDescription = stringResource(id = R.string.input_switch),
        )
    }

    if (expand) {
        Dialog(onDismissRequest = { expand = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    for (input in inputs) {
                        ListItem(
                            selected = false,
                            headlineContent = { Text(input.displayName) },
                            onClick = {
                                if (input.switchIntentUri != null) {
                                    context.startActivity(Intent.parseUri(input.switchIntentUri, 0))
                                }

                                expand = false
                            },
                        )
                    }
                }
            }
        }
    }
}
