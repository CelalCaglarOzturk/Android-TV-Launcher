package dev.mudrock.tiviyomitvlauncher.ui.onboarding

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.mudrock.tiviyomitvlauncher.R

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val pageCount = OnboardingPage.pages.size
    val isLastPage = currentPage == pageCount - 1

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(500.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.onboarding_welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OnboardingPageContent(page = OnboardingPage.pages[currentPage])

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(pageCount) { _ ->
                        Spacer(
                            modifier = Modifier
                                .width(8.dp)
                                .height(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                ) {
                    if (!isLastPage) {
                        OutlinedButton(
                            onClick = onDismiss
                        ) {
                            Text(stringResource(R.string.onboarding_skip))
                        }
                        Button(
                            onClick = { currentPage++ }
                        ) {
                            Text(stringResource(R.string.onboarding_next))
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(0.6f)
                        ) {
                            Text(stringResource(R.string.onboarding_get_started))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (page.iconRes != null) {
            Image(
                painter = painterResource(page.iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 12.dp)
            )
        }

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

data class OnboardingPage(
    val iconRes: Int?,
    val titleRes: Int,
    val descriptionRes: Int
) {
    companion object {
        val pages = listOf(
            OnboardingPage(
                iconRes = R.drawable.ic_launcher_foreground,
                titleRes = R.string.onboarding_page1_title,
                descriptionRes = R.string.onboarding_page1_description
            ),
            OnboardingPage(
                iconRes = R.drawable.ic_launcher_foreground,
                titleRes = R.string.onboarding_page2_title,
                descriptionRes = R.string.onboarding_page2_description
            ),
            OnboardingPage(
                iconRes = R.drawable.ic_launcher_foreground,
                titleRes = R.string.onboarding_page3_title,
                descriptionRes = R.string.onboarding_page3_description
            ),
            OnboardingPage(
                iconRes = R.drawable.ic_launcher_foreground,
                titleRes = R.string.onboarding_page4_title,
                descriptionRes = R.string.onboarding_page4_description
            )
        )
    }
}

class OnboardingManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "onboarding"
        private const val KEY_COMPLETED = "completed"
    }

    fun isCompleted(): Boolean {
        return prefs.getBoolean(KEY_COMPLETED, false)
    }

    fun markCompleted() {
        prefs.edit { putBoolean(KEY_COMPLETED, true) }
    }

    fun reset() {
        prefs.edit { remove(KEY_COMPLETED) }
    }
}