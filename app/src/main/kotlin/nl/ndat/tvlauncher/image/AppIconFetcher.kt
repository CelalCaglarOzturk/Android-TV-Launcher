package nl.ndat.tvlauncher.image

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.ndat.tvlauncher.R
import nl.ndat.tvlauncher.data.sqldelight.App

class AppIconFetcher(
    private val app: App,
    private val context: Context
) : Fetcher {

    override suspend fun fetch(): FetchResult = withContext(Dispatchers.IO) {
        if (app.packageName == "nl.ndat.tvlauncher.settings") {
            return@withContext DrawableResult(
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher)!!,
                isSampled = false,
                dataSource = DataSource.MEMORY
            )
        }

        val packageManager = context.packageManager
        val intent = Intent.parseUri(app.launchIntentUriLeanback ?: app.launchIntentUriDefault, 0)

        val drawable = try {
            packageManager.getActivityBanner(intent) ?: packageManager.getActivityIcon(intent)
        } catch (err: PackageManager.NameNotFoundException) {
            packageManager.defaultActivityIcon
        }

        DrawableResult(
            drawable = drawable,
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    class Factory(private val context: Context) : Fetcher.Factory<App> {
        override fun create(data: App, options: Options, imageLoader: ImageLoader): Fetcher {
            return AppIconFetcher(data, context)
        }
    }
}
