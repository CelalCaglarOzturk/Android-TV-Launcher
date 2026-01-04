package nl.ndat.tvlauncher.ui.tab.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.repository.ChannelRepository
import nl.ndat.tvlauncher.data.sqldelight.App
import nl.ndat.tvlauncher.data.sqldelight.Channel

class HomeTabViewModel(
    private val appRepository: AppRepository,
    private val channelRepository: ChannelRepository,
) : ViewModel() {
    val apps = appRepository.getFavoriteApps()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val channels = channelRepository.getFavoriteAppChannels()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allAppChannels = channelRepository.getAllAppChannels()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val watchNextPrograms = channelRepository.getWatchNextPrograms()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

    fun setChannelOrder(channel: Channel, order: Int) = viewModelScope.launch {
        channelRepository.updateChannelOrder(channel.id, order)
    }
}
