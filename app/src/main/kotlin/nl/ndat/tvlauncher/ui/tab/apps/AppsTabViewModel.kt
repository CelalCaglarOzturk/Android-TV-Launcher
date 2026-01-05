package nl.ndat.tvlauncher.ui.tab.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.BuildConfig
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import nl.ndat.tvlauncher.data.sqldelight.App

class AppsTabViewModel(
    private val appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        // Keep the flow active lazily to avoid reloading when switching tabs
        private val SHARING_STARTED = SharingStarted.Lazily
    }

    // Toggle for showing mobile apps (apps without leanback intent)
    private val _showMobileApps = MutableStateFlow(false)
    val showMobileApps: StateFlow<Boolean> = _showMobileApps

    val appCardSize = settingsRepository.appCardSize
        .stateIn(viewModelScope, SHARING_STARTED, SettingsRepository.DEFAULT_APP_CARD_SIZE)

    val apps = combine(
        appRepository.getApps(),
        _showMobileApps
    ) { apps, showMobile ->
        val settingsApp = App(
            id = "settings",
            displayName = "Launcher Settings",
            packageName = "nl.ndat.tvlauncher.settings",
            launchIntentUriDefault = null,
            launchIntentUriLeanback = null,
            favoriteOrder = null,
            hidden = 0L
        )

        val filteredApps = apps
            .filterNot { app -> app.packageName == BuildConfig.APPLICATION_ID }
            .filter { app ->
                // If showMobile is true, show all apps
                // If showMobile is false, only show apps with leanback intent
                showMobile || app.launchIntentUriLeanback != null
            }

        listOf(settingsApp) + filteredApps
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SHARING_STARTED, emptyList())

    val hiddenApps = combine(
        appRepository.getHiddenApps(),
        _showMobileApps
    ) { apps, showMobile ->
        apps
            .filterNot { app -> app.packageName == BuildConfig.APPLICATION_ID }
            .filter { app ->
                showMobile || app.launchIntentUriLeanback != null
            }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SHARING_STARTED, emptyList())

    // Count of mobile-only apps (apps without leanback intent)
    val mobileOnlyAppsCount = appRepository.getApps()
        .map { apps ->
            apps.count { app ->
                app.packageName != BuildConfig.APPLICATION_ID &&
                        app.launchIntentUriLeanback == null &&
                        app.launchIntentUriDefault != null
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SHARING_STARTED, 0)

    fun toggleShowMobileApps() {
        _showMobileApps.value = !_showMobileApps.value
    }

    fun setShowMobileApps(show: Boolean) {
        _showMobileApps.value = show
    }

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
}
