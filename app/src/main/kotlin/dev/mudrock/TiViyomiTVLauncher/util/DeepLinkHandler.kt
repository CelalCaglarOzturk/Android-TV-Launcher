package dev.mudrock.TiViyomiTVLauncher.util

import android.net.Uri
import timber.log.Timber

data class DeepLink(
    val action: String,
    val params: Map<String, String>
) {
    companion object {
        const val ACTION_SETTINGS = "settings"
        const val ACTION_APPS = "apps"
        const val ACTION_BACKUP = "backup"
        const val ACTION_RESTORE = "restore"
        
        const val PARAM_TAB = "tab"
        const val PARAM_SECTION = "section"
        
        val TAB_HOME = "home"
        val TAB_APPS = "apps"
        
        val SECTION_ANIMATIONS = "animations"
        val SECTION_CHANNELS = "channels"
        val SECTION_WATCH_NEXT = "watch_next"
        val SECTION_INPUTS = "inputs"
        val SECTION_TOOLBAR = "toolbar"
    }
    
    fun toUri(): Uri {
        val builder = Uri.Builder()
            .scheme("tvlauncher")
            .authority(action)
        params.forEach { (key, value) ->
            builder.appendQueryParameter(key, value)
        }
        return builder.build()
    }
}

object DeepLinkHandler {
    private const val SCHEME = "tvlauncher"
    
    fun parse(uri: Uri): DeepLink? {
        if (uri.scheme != SCHEME) {
            Timber.d("DeepLink: Invalid scheme ${uri.scheme}")
            return null
        }
        
        val action = uri.host ?: uri.authority ?: return null
        val params = uri.queryParameterNames.associateWith { uri.getQueryParameter(it) ?: "" }
        
        Timber.d("DeepLink: Parsed action=$action, params=$params")
        return DeepLink(action, params)
    }
    
    fun createSettingsUri(section: String? = null): Uri {
        val builder = Uri.Builder()
            .scheme(SCHEME)
            .authority(DeepLink.ACTION_SETTINGS)
        section?.let { builder.appendQueryParameter(DeepLink.PARAM_SECTION, it) }
        return builder.build()
    }
    
    fun createAppsUri(): Uri {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(DeepLink.ACTION_APPS)
            .build()
    }
    
    fun createBackupUri(): Uri {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(DeepLink.ACTION_BACKUP)
            .build()
    }
    
    fun createRestoreUri(): Uri {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(DeepLink.ACTION_RESTORE)
            .build()
    }
}