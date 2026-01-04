package nl.ndat.tvlauncher.ui.tab.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.BuildConfig
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.sqldelight.App

class AppsTabViewModel(
    private val appRepository: AppRepository,
) : ViewModel() {

    companion object {
        // Stop collecting after 5 seconds when there are no subscribers
        // This helps reduce resource usage on low-end devices
        private val SHARING_STARTED = SharingStarted.WhileSubscribed(5000L)
    }

    // Toggle for showing mobile apps (apps without leanback intent)
    private val _showMobileApps = MutableStateFlow(false)
    val showMobileApps: StateFlow<Boolean> = _showMobileApps

    val apps = combine(
        appRepository.getApps(),
        _showMobileApps
    ) { apps, showMobile ->
        apps
            .filterNot { app -> app.packageName == BuildConfig.APPLICATION_ID }
            .filter { app ->
                // If showMobile is true, show all apps
                // If showMobile is false, only show apps with leanback intent
                showMobile || app.launchIntentUriLeanback != null
            }
    }.stateIn(viewModelScope, SHARING_STARTED, emptyList())

    val hiddenApps = combine(
        appRepository.getHiddenApps(),
        _showMobileApps
    ) { apps, showMobile ->
        apps
            .filterNot { app -> app.packageName == BuildConfig.APPLICATION_ID }
            .filter { app ->
                showMobile || app.launchIntentUriLeanback != null
            }
    }.stateIn(viewModelScope, SHARING_STARTED, emptyList())

    // Count of mobile-only apps (apps without leanback intent)
    val mobileOnlyAppsCount = appRepository.getApps()
        .map { apps ->
            apps.count { app ->
                app.packageName != BuildConfig.APPLICATION_ID &&
                        app.launchIntentUriLeanback == null &&
                        app.launchIntentUriDefault != null
            }
        }
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
