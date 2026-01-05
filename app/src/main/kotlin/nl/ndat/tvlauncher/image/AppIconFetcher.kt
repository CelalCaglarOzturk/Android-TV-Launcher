package nl.ndat.tvlauncher.image

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.key.Keyer
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.sqldelight.App

/**
 * Custom Coil fetcher for loading app icons efficiently.
 * Implements proper cache keying based on app ID to prevent redundant loads.
 */
class AppIconFetcher(
    private val app: App,
    private val context: Context
) : Fetcher {

    override suspend fun fetch(): FetchResult = withContext(Dispatchers.IO) {
        val drawable = loadAppIcon()

        DrawableResult(
            drawable = drawable,
            isSampled = false,
            // Report MEMORY for settings icon (already in resources)
            // Report DISK for app icons loaded from package manager (cached by system)
            dataSource = if (app.packageName == SETTINGS_PACKAGE_NAME) {
                DataSource.MEMORY
            } else {
                DataSource.DISK
            }
        )
    }

    private fun loadAppIcon(): Drawable {
        // Special case for settings - use launcher icon
        if (app.packageName == SETTINGS_PACKAGE_NAME) {
            return ContextCompat.getDrawable(context, R.drawable.ic_launcher)!!
        }

        val packageManager = context.packageManager

        // Try to get the app icon/banner from the launch intent
        val intentUri = app.launchIntentUriLeanback ?: app.launchIntentUriDefault
        if (intentUri != null) {
            try {
                val intent = Intent.parseUri(intentUri, 0)

                // First try to get the banner (preferred for TV)
                val banner = try {
                    packageManager.getActivityBanner(intent)
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }

                if (banner != null) {
                    return banner
                }

                // Fall back to activity icon
                val icon = try {
                    packageManager.getActivityIcon(intent)
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }

                if (icon != null) {
                    return icon
                }
            } catch (e: Exception) {
                // Ignore parse errors and fall through to default
            }
        }

        // Final fallback to default activity icon
        return packageManager.defaultActivityIcon
    }

    /**
     * Keyer implementation for proper Coil caching.
     * Uses the app ID as the cache key since that uniquely identifies the app.
     */
    class AppKeyer : Keyer<App> {
        override fun key(data: App, options: Options): String {
            // Use app ID as cache key - this is stable and unique per app
            // Include a version suffix if we ever need to invalidate cache
            return "app_icon:${data.id}"
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<App> {
        override fun create(data: App, options: Options, imageLoader: ImageLoader): Fetcher {
            return AppIconFetcher(data, context)
        }
    }

    companion object {
        private const val SETTINGS_PACKAGE_NAME = "nl.ndat.tvlauncher.settings"
    }
}
