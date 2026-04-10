package dev.mudrock.TiViyomiTVLauncher.util

object LauncherConstants {
    object CardSize {
        const val DEFAULT_APP_CARD_SIZE = 90
        const val MIN_APP_CARD_SIZE = 50
        const val MAX_APP_CARD_SIZE = 200
        const val CARD_SIZE_STEP = 10
        
        const val DEFAULT_CHANNEL_CARD_SIZE = 90
    }
    
    object ChannelSettings {
        const val DEFAULT_CARDS_PER_ROW = 7
        const val MIN_CARDS_PER_ROW = 3
        const val MAX_CARDS_PER_ROW = 20
    }
    
    object Backup {
        const val CURRENT_VERSION = 2
        const val MAX_HISTORY_COUNT =5
        const val DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"
        const val BACKUP_FILE_NAME = "tv_launcher_backup.json"
        const val BACKUP_DIR_NAME = "TVLauncher"
    }
    
    object SystemLaunchers {
        val DEFAULT_PACKAGES = listOf(
            "com.google.android.tvlauncher",
            "com.android.tv.launcher",
            "com.google.android.apps.tv.launcherx",
            "com.android.launcher",
            "com.android.launcher2",
            "com.google.android.tv.launcher",
            "com.google.android.launcher",
            "com.android.tv.settings"
        )
        
        val CUSTOM_ROM_PACKAGES = listOf(
            "org.lineageos.tv.launcher",
            "com.cyanogenmod.tv.launcher",
            "com.aosp.tv.launcher"
        )
        
        fun getAllKnownPackages(): List<String> = DEFAULT_PACKAGES + CUSTOM_ROM_PACKAGES
        
        fun isSystemLauncher(packageName: String?): Boolean {
            if (packageName == null) return false
            return getAllKnownPackages().contains(packageName)
        }
    }
    
    object CrashRecovery {
        const val MAX_CRASH_COUNT = 3
        const val CRASH_COUNT_RESET_HOURS = 1
        const val CRASH_COUNT_PREFS = "crash_recovery"
        const val KEY_CRASH_COUNT = "crash_count"
        const val KEY_LAST_CRASH_TIME = "last_crash_time"
    }
    
    object Animations {
        const val DEFAULT_ENABLED = true
        const val ICON_MARQUEE_DURATION_MS = 3000
        const val SCALE_ANIMATION_DURATION_MS = 150
        const val MOVE_ANIMATION_DURATION_MS = 200
    }
    
    object Accessibility {
        const val NOTIFICATION_TIMEOUT_MS = 3000
        const val EVENT_DEBOUNCE_MS = 500
    }
    
    object Developer {
        const val TEST_CRASH_TRIGGER_COUNT = 5
        const val TEST_CRASH_KEY = 9
    }
    
    object Background {
        object Presets {
            const val BLACK = 0xFF000000L
            const val DARK_GRAY = 0xFF1A1A1AL
            const val DARK_BLUE = 0xFF0D1B2AL
            const val DARK_GREEN = 0xFF1B2D1BL
            const val DARK_PURPLE = 0xFF2D1B2DL
            const val DARK_RED = 0xFF2D1B1BL
        }
        
        const val DEFAULT_BACKGROUND_COLOR = 0xFF000000L
        
        val PRESET_BACKGROUNDS = listOf(
            Presets.BLACK,
            Presets.DARK_GRAY,
            Presets.DARK_BLUE,
            Presets.DARK_GREEN,
            Presets.DARK_PURPLE,
            Presets.DARK_RED
        )
    }
}