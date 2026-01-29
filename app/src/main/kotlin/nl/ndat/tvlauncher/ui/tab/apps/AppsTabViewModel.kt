package nl.ndat.tvlauncher.ui.tab.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.BuildConfig
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import nl.ndat.tvlauncher.data.sqldelight.App

class AppsTabViewModel(
    private val appRepository: AppRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        // Use WhileSubscribed with a 20 second timeout to keep data cached during tab switches
        // This prevents reloading when quickly switching tabs while still allowing cleanup
        // when the screen is completely left
        private val SHARING_STARTED = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = 20000L,
            replayExpirationMillis = Long.MAX_VALUE // Never expire the replay cache
        )
    }

    val showMobileApps = settingsRepository.showMobileApps
        .stateIn(viewModelScope, SHARING_STARTED, false)

    val appCardSize = settingsRepository.appCardSize
        .stateIn(viewModelScope, SHARING_STARTED, SettingsRepository.DEFAULT_APP_CARD_SIZE)

    val apps = combine(
        appRepository.getApps(),
        settingsRepository.showMobileApps
    ) { apps, showMobile ->
        apps
            .filterNot { app -> app.packageName == BuildConfig.APPLICATION_ID }
            .filter { app ->
                // Always show Launcher Settings regardless of filter
                if (app.packageName == "nl.ndat.tvlauncher.settings") return@filter true
                // If showMobile is true, show all apps
                // If showMobile is false, only show apps with leanback intent
                showMobile || app.launchIntentUriLeanback != null
            }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SHARING_STARTED, emptyList())

    val hiddenApps = combine(
        appRepository.getHiddenApps(),
        settingsRepository.showMobileApps
    ) { apps, showMobile ->
        apps
            .filterNot { app -> app.packageName == BuildConfig.APPLICATION_ID }
            .filter { app ->
                showMobile || app.launchIntentUriLeanback != null
            }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SHARING_STARTED, emptyList())

    fun favoriteApp(app: App, favorite: Boolean) = viewModelScope.launch {
        if (favorite) appRepository.favorite(app.id)
        else appRepository.unfavorite(app.id)
    }

    fun hideApp(app: App) = viewModelScope.launch {
        appRepository.hideApp(app.id)
    }

    fun unhideApp(app: App) = viewModelScope.launch {
        appRepository.unhideApp(app.id)
    }

    fun swapApps(app1: App, app2: App) = viewModelScope.launch {
        appRepository.swapAppOrder(app1.id, app2.id)
    }
}
