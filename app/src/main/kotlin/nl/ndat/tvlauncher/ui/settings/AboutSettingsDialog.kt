package nl.ndat.tvlauncher.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import nl.ndat.tvlauncher.R

@Composable
fun AboutSettingsDialog(
	onDismissRequest: () -> Unit
) {
	val context = LocalContext.current
	val packageManager = context.packageManager
	val packageName = context.packageName
	val packageInfo = try {
		packageManager.getPackageInfo(packageName, 0)
	} catch (e: PackageManager.NameNotFoundException) {
		null
	}

	val versionName = packageInfo?.versionName ?: "Unknown"
	val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
		packageInfo?.longVersionCode ?: 0L
	} else {
		packageInfo?.versionCode?.toLong() ?: 0L
	}

	val emailAddress = stringResource(R.string.about_email_address)
	val githubIssuesUrl = stringResource(R.string.about_github_issues_url)
	val websiteUrl = stringResource(R.string.about_website_url)
	val privacyPolicyUrl = stringResource(R.string.about_privacy_policy_url)

	Dialog(onDismissRequest = onDismissRequest) {
		Surface(
			shape = MaterialTheme.shapes.medium,
			modifier = Modifier
				.fillMaxWidth(0.8f)
				.fillMaxHeight(0.9f)
		) {
			Column(modifier = Modifier.padding(24.dp)) {
				Text(
					text = stringResource(R.string.settings_about),
					style = MaterialTheme.typography.titleLarge,
					modifier = Modifier.padding(bottom = 16.dp)
				)

				LazyColumn(
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					item {
						AboutSectionHeader(stringResource(R.string.about_app_info))
						AboutItem(
							title = stringResource(R.string.app_name),
							description = stringResource(R.string.about_version, versionName, versionCode),
							onClick = {}
						)
					}

					item {
						AboutSectionHeader(stringResource(R.string.about_bug_reporting))
						AboutItem(
							title = stringResource(R.string.about_email, emailAddress),
							onClick = {
								val intent = Intent(Intent.ACTION_SENDTO).apply {
									data = Uri.parse("mailto:$emailAddress")
									putExtra(Intent.EXTRA_SUBJECT, "Bug Report: TV Launcher")
								}
								try {
									context.startActivity(intent)
								} catch (e: Exception) {
									// Handle no email app
								}
							}
						)
						AboutItem(
							title = stringResource(R.string.about_github_issues),
							description = githubIssuesUrl,
							onClick = {
								val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubIssuesUrl))
								context.startActivity(intent)
							}
						)

						// QR Code placeholder
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 8.dp),
							horizontalArrangement = Arrangement.Center,
							verticalAlignment = Alignment.CenterVertically
						) {
							Column(horizontalAlignment = Alignment.CenterHorizontally) {
								Image(
									painter = painterResource(id = R.drawable.ic_launcher_foreground),
									contentDescription = stringResource(R.string.about_github_qr_desc),
									modifier = Modifier.size(120.dp)
								)

								Text(
									text = stringResource(R.string.about_github_qr_desc),
									style = MaterialTheme.typography.labelSmall,
									modifier = Modifier.padding(top = 4.dp)
								)
							}
						}
					}

					item {
						AboutSectionHeader(stringResource(R.string.about_license))
						AboutItem(
							title = "GNU General Public License v3.0",
							onClick = {
								val intent = Intent(
									Intent.ACTION_VIEW,
									Uri.parse("https://github.com/CelalCaglarOzturk/Android-TV-Launcher/blob/master/LICENSE")
								)
								context.startActivity(intent)
							}
						)
					}

					item {
						AboutSectionHeader(stringResource(R.string.about_privacy_policy))
						AboutItem(
							title = stringResource(R.string.about_privacy_policy),
							description = privacyPolicyUrl,
							onClick = {
								val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
								context.startActivity(intent)
							}
						)
					}

					item {
						AboutSectionHeader(stringResource(R.string.about_website))
						AboutItem(
							title = stringResource(R.string.about_website),
							description = websiteUrl,
							onClick = {
								val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
								context.startActivity(intent)
							}
						)
					}
				}
			}
		}
	}
}

@Composable
private fun AboutSectionHeader(title: String) {
	Text(
		text = title,
		style = MaterialTheme.typography.labelMedium,
		color = MaterialTheme.colorScheme.primary,
		modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 12.dp)
	)
}

@Composable
private fun AboutItem(
	title: String,
	description: String? = null,
	onClick: () -> Unit
) {
	Card(
		onClick = onClick,
		shape = CardDefaults.shape(shape = MaterialTheme.shapes.small),
		colors = CardDefaults.colors(
			containerColor = Color.Transparent,
			contentColor = MaterialTheme.colorScheme.onSurface,
			focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
			pressedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
		),
		scale = CardDefaults.scale(focusedScale = 1.02f),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier
				.padding(horizontal = 12.dp, vertical = 8.dp)
				.fillMaxWidth()
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge
			)
			if (description != null) {
				Text(
					text = description,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
			}
		}
	}
}