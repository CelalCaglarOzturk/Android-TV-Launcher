package nl.ndat.tvlauncher.data

import android.content.Context
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import nl.ndat.tvlauncher.data.sqldelight.Channel
import nl.ndat.tvlauncher.data.sqldelight.ChannelProgram
import nl.ndat.tvlauncher.data.sqldelight.Database
import nl.ndat.tvlauncher.data.sqldelight.Input
import timber.log.Timber
import java.io.File

class DatabaseContainer(
    private val context: Context
) {
    companion object {
        const val DB_FILE = "data.db"
        
        fun deleteDatabase(ctx: Context): Boolean {
            return try {
                val dbFile = ctx.getDatabasePath(DB_FILE)
                if (dbFile.exists()) {
                    dbFile.delete()
                    Timber.d("Database deleted: ${dbFile.absolutePath}")
                }
                val shmFile = File(dbFile.parent, "$DB_FILE-shm")
                if (shmFile.exists()) shmFile.delete()
                val walFile = File(dbFile.parent, "$DB_FILE-wal")
                if (walFile.exists()) walFile.delete()
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete database")
                false
            }
        }
    }

    private var driver: AndroidSqliteDriver? = null
    private var _database: Database? = null
    
    val database: Database
        get() = _database ?: createDatabase()
    
    val apps get() = database.appQueries
    val inputs get() = database.inputQueries
    val channels get() = database.channelQueries
    val channelPrograms get() = database.channelProgramQueries
    val watchNextBlacklist get() = database.watchNextBlacklistQueries
    
    init {
        try {
            _database = createDatabase()
        } catch (e: Exception) {
            Timber.e(e, "DatabaseContainer: Failed to create database, attempting recovery")
            recoverDatabase()
            _database = createDatabase()
        }
    }
    
    private fun createDatabase(): Database {
        val drv = driver ?: AndroidSqliteDriver(Database.Schema, context, DB_FILE).also { driver = it }
        return Database(
            driver = drv,
            ChannelAdapter = Channel.Adapter(
                typeAdapter = EnumColumnAdapter(),
            ),
            ChannelProgramAdapter = ChannelProgram.Adapter(
                weightAdapter = IntColumnAdapter(),
                typeAdapter = EnumColumnAdapter(),
                posterArtAspectRatioAdapter = EnumColumnAdapter(),
                lastPlaybackPositionMillisAdapter = IntColumnAdapter(),
                durationMillisAdapter = IntColumnAdapter(),
                itemCountAdapter = IntColumnAdapter(),
                interactionTypeAdapter = EnumColumnAdapter(),
            ),
            InputAdapter = Input.Adapter(
                typeAdapter = EnumColumnAdapter()
            ),
        )
    }
    
    private fun recoverDatabase() {
        Timber.w("DatabaseContainer: Performing database recovery")
        try {
            deleteDatabase(context)
            Timber.i("DatabaseContainer: Recovery completed successfully")
        } catch (e: Exception) {
            Timber.e(e, "DatabaseContainer: Recovery failed")
            throw e
        }
    }
}

class IntColumnAdapter : ColumnAdapter<Int, Long> {
    override fun decode(databaseValue: Long): Int = databaseValue.toInt()
    override fun encode(value: Int): Long = value.toLong()
}
