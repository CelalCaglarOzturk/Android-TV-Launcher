package nl.ndat.tvlauncher.ui.tab.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.repository.ChannelRepository
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import nl.ndat.tvlauncher.data.sqldelight.App
import nl.ndat.tvlauncher.data.sqldelight.Channel

class HomeTabViewModel(
    private val appRepository: AppRepository,
    private val channelRepository: ChannelRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        // Keep the flow active lazily to avoid reloading when switching tabs
        private val SHARING_STARTED = SharingStarted.Lazily
    }

    val apps = appRepository.getFavoriteApps()
        .map { apps ->
            // Filter out Launcher Settings from Home tab
            apps.filterNot { it.packageName == "nl.ndat.tvlauncher.settings" }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SHARING_STARTED, emptyList())

    val channels = channelRepository.getEnabledChannels()
        .stateIn(viewModelScope, SHARING_STARTED, emptyList())

    val allAppChannels = channelRepository.getChannels()
        .stateIn(viewModelScope, SHARING_STARTED, emptyList())

    val watchNextPrograms = channelRepository.getWatchNextPrograms()
        .stateIn(viewModelScope, SHARING_STARTED, emptyList())

    val watchNextBlacklist = channelRepository.getWatchNextBlacklist()
        .stateIn(viewModelScope, SHARING_STARTED, emptyList())

    val appCardSize = settingsRepository.appCardSize
        .stateIn(viewModelScope, SHARING_STARTED, SettingsRepository.DEFAULT_APP_CARD_SIZE)

    fun channelPrograms(channel: Channel) = channelRepository.getProgramsByChannel(channel)

    fun favoriteApp(app: App, favorite: Boolean) = viewModelScope.launch {
        // Return if state is already satisfied
        if ((app.favoriteOrder != null) == favorite) return@launch

        if (favorite) appRepository.favorite(app.id)
        else appRepository.unfavorite(app.id)
    }

    fun setFavoriteOrder(app: App, order: Int) = viewModelScope.launch {
        // Make sure app is favorite first
        if (app.favoriteOrder == null) appRepository.favorite(app.id)
        appRepository.updateFavoriteOrder(app.id, order)
    }

    fun hideApp(app: App) = viewModelScope.launch {
        appRepository.hideApp(app.id)
    }

    fun toggleWatchNextBlacklist(packageName: String, blacklisted: Boolean) = viewModelScope.launch {
        if (blacklisted) {
            channelRepository.addToWatchNextBlacklist(packageName)
        } else {
            channelRepository.removeFromWatchNextBlacklist(packageName)
        }
    }

    fun removeWatchNextProgram(programId: String) = viewModelScope.launch {
        channelRepository.removeWatchNextProgram(programId)
    }

    // Channel management functions
    fun enableChannel(channel: Channel) = viewModelScope.launch {
        channelRepository.enableChannel(channel.id)
    }

    fun disableChannel(channel: Channel) = viewModelScope.launch {
        channelRepository.disableChannel(channel.id)
    }

    fun setChannelEnabled(channel: Channel, enabled: Boolean) = viewModelScope.launch {
        channelRepository.setChannelEnabled(channel.id, enabled)
    }

    fun moveChannelUp(channel: Channel) = viewModelScope.launch {
        channelRepository.moveChannelUp(channel.id)
    }

    fun moveChannelDown(channel: Channel) = viewModelScope.launch {
        channelRepository.moveChannelDown(channel.id)
    }

    fun setChannelOrder(channel: Channel, order: Long) = viewModelScope.launch {
        channelRepository.updateChannelOrder(channel.id, order)
    }
}
