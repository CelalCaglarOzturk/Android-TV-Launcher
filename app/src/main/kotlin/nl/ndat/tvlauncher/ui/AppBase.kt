package nl.ndat.tvlauncher.ui

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Shapes
import androidx.tv.material3.Surface
import androidx.tv.material3.darkColorScheme
import nl.ndat.tvlauncher.ui.onboarding.OnboardingDialog
import nl.ndat.tvlauncher.ui.screen.launcher.LauncherScreen
import nl.ndat.tvlauncher.util.DeepLink

@Composable
fun AppBase(
	showOnboarding: Boolean = false,
	pendingDeepLink: DeepLink? = null,
	onOnboardingComplete: () -> Unit = {},
	onDeepLinkHandled: () -> Unit = {}
) {
	var onboardingShown by remember { mutableStateOf(showOnboarding) }
	var deepLink by remember { mutableStateOf(pendingDeepLink) }

	MaterialTheme(
		colorScheme = darkColorScheme(
			background = Color.Black,
			surface = Color.Black,
		),
		shapes = Shapes(
			small = RoundedCornerShape(4.dp),
			medium = RoundedCornerShape(4.dp),
			large = RoundedCornerShape(8.dp)
		),
	) {
		CompositionLocalProvider(
			LocalOverscrollFactory provides null
		) {
			Surface {
				if (onboardingShown) {
					OnboardingDialog(
						onDismiss = {
							onboardingShown = false
							onOnboardingComplete()
						}
					)
				}
				
				LauncherScreen(
					pendingDeepLink = deepLink,
					onDeepLinkHandled = {
						deepLink = null
						onDeepLinkHandled()
					}
				)
			}
		}
	}
}