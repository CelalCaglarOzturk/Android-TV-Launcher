package dev.mudrock.TiViyomiTVLauncher

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import dev.mudrock.TiViyomiTVLauncher.crash.CrashHandler
import dev.mudrock.TiViyomiTVLauncher.data.DatabaseContainer
import dev.mudrock.TiViyomiTVLauncher.data.repository.AppRepository
import dev.mudrock.TiViyomiTVLauncher.data.repository.BackupRepository
import dev.mudrock.TiViyomiTVLauncher.data.repository.ChannelRepository
import dev.mudrock.TiViyomiTVLauncher.data.repository.InputRepository
import dev.mudrock.TiViyomiTVLauncher.data.repository.SettingsRepository
import dev.mudrock.TiViyomiTVLauncher.data.resolver.AppResolver
import dev.mudrock.TiViyomiTVLauncher.data.resolver.ChannelResolver
import dev.mudrock.TiViyomiTVLauncher.data.resolver.InputResolver
import dev.mudrock.TiViyomiTVLauncher.image.AppIconFetcher
import dev.mudrock.TiViyomiTVLauncher.ui.tab.apps.AppsTabViewModel
import dev.mudrock.TiViyomiTVLauncher.ui.tab.home.HomeTabViewModel
import dev.mudrock.TiViyomiTVLauncher.util.DefaultLauncherHelper
import dev.mudrock.TiViyomiTVLauncher.util.FocusController
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import timber.log.Timber

private val launcherModule = module {
    single { DefaultLauncherHelper(get()) }
    single { FocusController() }

    single { AppRepository(get(), get(), get()) }
    single { AppResolver() }

    single { ChannelRepository(get(), get(), get()) }
    single { ChannelResolver() }

    single { InputRepository(get(), get(), get()) }
    single { InputResolver() }

    single { SettingsRepository(get()) }
    single { BackupRepository(get(), get(), get(), get()) }

    viewModel { HomeTabViewModel(get(), get(), get()) }
    viewModel { AppsTabViewModel(get(), get()) }
}

private val databaseModule = module {
    // Create database(s)
    single { DatabaseContainer(get()) }
}

class LauncherApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        CrashHandler.init(this)

        Timber.plant(Timber.DebugTree())

        startKoin {
            androidLogger(level = if (BuildConfig.DEBUG) Level.DEBUG else Level.INFO)
            androidContext(this@LauncherApplication)

            modules(launcherModule, databaseModule)
        }
    }

    override fun newImageLoader() = ImageLoader.Builder(this)
        .components {
            // Register keyer for proper cache key generation
            add(AppIconFetcher.AppKeyer())
            // Register fetcher for loading app icons
            add(AppIconFetcher.Factory(this@LauncherApplication))
        }
        // Only enable debug logging in debug builds
        .apply {
            if (BuildConfig.DEBUG) {
                logger(DebugLogger())
            }
        }
        // Enable memory and disk caching (enabled by default, but being explicit)
        .respectCacheHeaders(false) // App icons don't have HTTP cache headers
        .build()
}
