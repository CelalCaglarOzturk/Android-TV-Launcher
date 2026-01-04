package nl.ndat.tvlauncher.data.resolver

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import nl.ndat.tvlauncher.data.sqldelight.App
import timber.log.Timber

class AppResolver {
    companion object {
        private val launcherCategories = arrayOf(
            Intent.CATEGORY_LEANBACK_LAUNCHER,
            Intent.CATEGORY_LAUNCHER,
        )

        const val APP_ID_PREFIX = "app:"
    }

    fun getApplication(context: Context, packageId: String): App? {
        val packageManager = context.packageManager

        var leanbackIntent: String? = null
        var defaultIntent: String? = null
        var displayName: String? = null

        try {
            val intent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                .setPackage(packageId)
            val activities = queryIntentActivitiesSafe(packageManager, intent)
            if (activities.isNotEmpty()) {
                val info = activities.first()
                leanbackIntent = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                    .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    .toUri(0)
                displayName = info.loadLabel(packageManager).toString()
            }
        } catch (e: Exception) {
            Timber.e(e, "AppResolver: Error querying leanback for $packageId")
        }

        try {
            val intent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setPackage(packageId)
            val activities = queryIntentActivitiesSafe(packageManager, intent)
            if (activities.isNotEmpty()) {
                val info = activities.first()
                defaultIntent = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    .toUri(0)
                if (displayName == null) {
                    displayName = info.loadLabel(packageManager).toString()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "AppResolver: Error querying launcher for $packageId")
        }

        if (leanbackIntent == null && defaultIntent == null) return null

        return App(
            id = "$APP_ID_PREFIX$packageId",
            displayName = displayName ?: packageId,
            packageName = packageId,
            launchIntentUriDefault = defaultIntent,
            launchIntentUriLeanback = leanbackIntent,
            favoriteOrder = null,
            hidden = 0,
        )
    }

    fun getApplications(context: Context): List<App> {
        val packageManager = context.packageManager

        Timber.d("AppResolver: Starting to query applications")
        Timber.d("AppResolver: Android SDK version: ${Build.VERSION.SDK_INT}")

        // First, try the standard approach with launcher categories
        val categoryApps = getCategoryApps(packageManager)

        // If that fails, try getting all installed apps as fallback
        if (categoryApps.isEmpty()) {
            Timber.w("AppResolver: No apps found via category queries, trying fallback method")
            return getInstalledAppsWithLaunchIntent(packageManager)
        }

        return categoryApps
    }

    private fun getCategoryApps(packageManager: PackageManager): List<App> {
        val leanbackIntents = mutableMapOf<String, String>()
        val defaultIntents = mutableMapOf<String, String>()
        val displayNames = mutableMapOf<String, String>()

        fun processActivities(activities: List<ResolveInfo>, isLeanback: Boolean) {
            activities.forEach { info ->
                val packageName = info.activityInfo.packageName
                val className = info.activityInfo.name

                val intent = Intent(Intent.ACTION_MAIN)
                    .addCategory(if (isLeanback) Intent.CATEGORY_LEANBACK_LAUNCHER else Intent.CATEGORY_LAUNCHER)
                    .setClassName(packageName, className)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

                val uri = intent.toUri(0)

                if (isLeanback) {
                    leanbackIntents[packageName] = uri
                } else {
                    defaultIntents[packageName] = uri
                }

                if (!displayNames.containsKey(packageName)) {
                    try {
                        displayNames[packageName] = info.loadLabel(packageManager).toString()
                    } catch (e: Exception) {
                        displayNames[packageName] = packageName
                    }
                }
            }
        }

        try {
            val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            val activities = queryIntentActivitiesSafe(packageManager, intent)
            processActivities(activities, true)
        } catch (e: Exception) {
            Timber.e(e, "AppResolver: Error querying leanback category")
        }

        try {
            val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val activities = queryIntentActivitiesSafe(packageManager, intent)
            processActivities(activities, false)
        } catch (e: Exception) {
            Timber.e(e, "AppResolver: Error querying launcher category")
        }

        val allPackages = leanbackIntents.keys + defaultIntents.keys

        val apps = allPackages.map { packageName ->
            App(
                id = "$APP_ID_PREFIX$packageName",
                displayName = displayNames[packageName] ?: packageName,
                packageName = packageName,
                launchIntentUriDefault = defaultIntents[packageName],
                launchIntentUriLeanback = leanbackIntents[packageName],
                favoriteOrder = null,
                hidden = 0,
            )
        }

        Timber.d("AppResolver: Total unique apps found via categories: ${apps.size}")
        return apps
    }

    /**
     * Fallback method: Get all installed applications that have a launch intent
     */
    private fun getInstalledAppsWithLaunchIntent(packageManager: PackageManager): List<App> {
        Timber.d("AppResolver: Using fallback method to get installed apps")

        val installedApps = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledApplications(
                    PackageManager.ApplicationInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledApplications(0)
            }
        } catch (e: Exception) {
            Timber.e(e, "AppResolver: Error getting installed applications")
            emptyList()
        }

        Timber.d("AppResolver: Found ${installedApps.size} installed applications")

        val apps = installedApps
            .filter { appInfo ->
                // Filter to only user-visible apps (not system apps without a launcher)
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                // Include if it's not a system app, or if it's an updated system app
                !isSystemApp || isUpdatedSystemApp || hasLaunchIntent(packageManager, appInfo.packageName)
            }
            .mapNotNull { appInfo ->
                try {
                    applicationInfoToApp(packageManager, appInfo)
                } catch (e: Exception) {
                    Timber.e(e, "AppResolver: Error converting ApplicationInfo to App for ${appInfo.packageName}")
                    null
                }
            }
            .filter { app ->
                // Only include apps that have a launch intent
                app.launchIntentUriDefault != null || app.launchIntentUriLeanback != null
            }

        Timber.d("AppResolver: Total launchable apps found via fallback: ${apps.size}")
        apps.forEach { app ->
            Timber.d("AppResolver: Fallback App - ${app.displayName} (${app.packageName})")
        }

        return apps
    }

    private fun hasLaunchIntent(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            packageManager.getLaunchIntentForPackage(packageName) != null ||
                    packageManager.getLeanbackLaunchIntentForPackage(packageName) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun applicationInfoToApp(packageManager: PackageManager, appInfo: ApplicationInfo): App? {
        val packageName = appInfo.packageName

        val launchIntent = try {
            packageManager.getLaunchIntentForPackage(packageName)
        } catch (e: Exception) {
            null
        }

        val leanbackIntent = try {
            packageManager.getLeanbackLaunchIntentForPackage(packageName)
        } catch (e: Exception) {
            null
        }

        // Skip if no launch intent at all
        if (launchIntent == null && leanbackIntent == null) {
            return null
        }

        val displayName = try {
            appInfo.loadLabel(packageManager).toString()
        } catch (e: Exception) {
            packageName
        }

        return App(
            id = "$APP_ID_PREFIX$packageName",
            displayName = displayName,
            packageName = packageName,
            launchIntentUriDefault = launchIntent?.toUri(0),
            launchIntentUriLeanback = leanbackIntent?.toUri(0),
            favoriteOrder = null,
            hidden = 0,
        )
    }

    private fun queryIntentActivitiesSafe(
        packageManager: PackageManager,
        intent: Intent
    ): List<ResolveInfo> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            }
        } catch (e: Exception) {
            Timber.e(e, "AppResolver: Error in queryIntentActivities")
            emptyList()
        }
    }


}
