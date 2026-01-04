package nl.ndat.tvlauncher.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.ndat.tvlauncher.data.DatabaseContainer
import nl.ndat.tvlauncher.data.executeAsListFlow
import nl.ndat.tvlauncher.data.resolver.AppResolver
import nl.ndat.tvlauncher.data.sqldelight.App
import timber.log.Timber

class AppRepository(
    private val context: Context,
    private val appResolver: AppResolver,
    private val database: DatabaseContainer,
) {
    private suspend fun commitApps(apps: Collection<App>) = withContext(Dispatchers.IO) {
        Timber.d("AppRepository: commitApps called with ${apps.size} apps")

        try {
            // Remove apps found in database but not in committed list
            val existingIds = database.apps.getAll().executeAsList().map { it.id }
            Timber.d("AppRepository: Existing IDs in DB: ${existingIds.size}")

            val newIds = apps.map { it.id }.toSet()
            val idsToRemove = existingIds.subtract(newIds)

            Timber.d("AppRepository: IDs to remove: ${idsToRemove.size}")

            idsToRemove.forEach { id ->
                database.apps.removeById(id)
            }

            // Upsert all found
            apps.forEach { app ->
                Timber.d("AppRepository: Upserting app: ${app.displayName} (${app.id})")
                commitApp(app)
            }

            Timber.d("AppRepository: commitApps completed")
        } catch (e: Exception) {
            Timber.e(e, "AppRepository: Error in commitApps")
            throw e
        }
    }

    private fun commitApp(app: App) {
        try {
            database.apps.upsert(
                displayName = app.displayName,
                packageName = app.packageName,
                launchIntentUriDefault = app.launchIntentUriDefault,
                launchIntentUriLeanback = app.launchIntentUriLeanback,
                id = app.id
            )
        } catch (e: Exception) {
            Timber.e(e, "AppRepository: Error upserting app ${app.packageName}")
            throw e
        }
    }

    suspend fun refreshAllApplications() = withContext(Dispatchers.IO) {
        Timber.d("AppRepository: refreshAllApplications called")
        try {
            val apps = appResolver.getApplications(context)
            Timber.d("AppRepository: Resolver returned ${apps.size} apps")

            if (apps.isNotEmpty()) {
                commitApps(apps)
                Timber.d("AppRepository: Apps committed to database")

                // Verify what's in the database
                val dbApps = database.apps.getAll().executeAsList()
                Timber.d("AppRepository: Database now contains ${dbApps.size} apps")
                dbApps.forEach { app ->
                    Timber.d("AppRepository: DB App - ${app.displayName} (${app.packageName})")
                }
            } else {
                Timber.w("AppRepository: No apps returned from resolver")
            }
        } catch (e: Exception) {
            Timber.e(e, "AppRepository: Error in refreshAllApplications")
            throw e
        }
    }

    suspend fun refreshApplication(packageName: String) = withContext(Dispatchers.IO) {
        val app = appResolver.getApplication(context, packageName)

        if (app == null) {
            database.apps.removeByPackageName(packageName)
        } else {
            commitApp(app)
        }
    }

    fun getApps() = database.apps.getAll().executeAsListFlow()

    fun getFavoriteApps() = database.apps.getAllFavorites().executeAsListFlow()

    suspend fun getByPackageName(packageName: String) =
        withContext(Dispatchers.IO) { database.apps.getByPackageName(packageName).executeAsOneOrNull() }

    suspend fun favorite(id: String) = withContext(Dispatchers.IO) {
        database.apps.updateFavoriteAdd(id)
    }

    suspend fun unfavorite(id: String) = withContext(Dispatchers.IO) {
        database.apps.updateFavoriteRemove(id)
    }

    suspend fun updateFavoriteOrder(id: String, order: Int) =
        withContext(Dispatchers.IO) { database.apps.updateFavoriteOrder(id, order.toLong()) }
}
