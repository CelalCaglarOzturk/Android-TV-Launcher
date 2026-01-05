package nl.ndat.tvlauncher.data.resolver

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.CancellationSignal
import androidx.core.content.ContentResolverCompat
import androidx.tvprovider.media.tv.PreviewChannel
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import androidx.tvprovider.media.tv.WatchNextProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.ndat.tvlauncher.data.model.ChannelProgramAspectRatio
import nl.ndat.tvlauncher.data.model.ChannelProgramInteractionType
import nl.ndat.tvlauncher.data.model.ChannelProgramType
import nl.ndat.tvlauncher.data.model.ChannelType
import nl.ndat.tvlauncher.data.sqldelight.Channel
import nl.ndat.tvlauncher.data.sqldelight.ChannelProgram
import timber.log.Timber

@SuppressLint("RestrictedApi")
class ChannelResolver {
    companion object {
        const val CHANNEL_ID_PREFIX = "channel:"
        const val CHANNEL_ID_WATCH_NEXT = "$CHANNEL_ID_PREFIX@watch_next"
        const val CHANNEL_PROGRAM_ID_PREFIX = "channel_program:"
    }

    /**
     * Safely query content resolver with proper resource management.
     * Returns null if the query fails or the URI is invalid.
     */
    private fun ContentResolver.tryQuery(
        uri: Uri,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null,
        cancellationSignal: CancellationSignal? = null
    ): Cursor? = try {
        ContentResolverCompat.query(
            this,
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder,
            cancellationSignal
        )
    } catch (err: IllegalArgumentException) {
        // Invalid URI - likely that this platform is not Android TV
        Timber.w(err, "Failed to query content resolver - invalid URI")
        null
    } catch (err: SecurityException) {
        // Permission denied
        Timber.w(err, "Failed to query content resolver - permission denied")
        null
    }

    /**
     * Process a cursor safely, ensuring it's closed even if an exception occurs.
     * Uses the cursor's use() extension to guarantee closure.
     */
    private inline fun <T> Cursor?.processRows(block: (Cursor) -> T?): List<T> {
        if (this == null) return emptyList()

        return this.use { cursor ->
            if (cursor.count == 0) return@use emptyList()

            buildList {
                if (!cursor.moveToFirst()) {
                    Timber.w("Unable to move cursor to first row")
                    return@buildList
                }

                do {
                    try {
                        val result = block(cursor)
                        if (result != null) {
                            add(result)
                        }
                    } catch (err: NullPointerException) {
                        Timber.e(err, "Unable to parse row - null pointer")
                    } catch (err: CursorIndexOutOfBoundsException) {
                        Timber.e(err, "Unable to parse row - cursor index out of bounds")
                    } catch (err: Exception) {
                        Timber.e(err, "Unable to parse row - unexpected error")
                    }
                } while (cursor.moveToNext())
            }
        }
    }

    suspend fun getPreviewChannels(context: Context): List<Channel> = withContext(Dispatchers.IO) {
        context.contentResolver.tryQuery(
            TvContractCompat.Channels.CONTENT_URI,
            PreviewChannel.Columns.PROJECTION,
        ).processRows { cursor ->
            val appLinkIntentUri = cursor.getString(PreviewChannel.Columns.COL_APP_LINK_INTENT_URI)
            val displayName = cursor.getString(PreviewChannel.Columns.COL_DISPLAY_NAME)
            val packageName = cursor.getString(PreviewChannel.Columns.COL_PACKAGE_NAME)

            when {
                appLinkIntentUri.isNullOrEmpty() -> {
                    Timber.d("Ignoring channel $packageName due to missing intent uri")
                    null
                }

                displayName.isNullOrEmpty() -> {
                    Timber.d("Ignoring channel $packageName due to missing display name")
                    null
                }

                else -> {
                    val channel = PreviewChannel.fromCursor(cursor)?.toChannel()
                    if (channel == null) {
                        Timber.d("Ignoring channel $packageName due to failing to parse")
                    }
                    channel
                }
            }
        }
    }

    suspend fun getChannelPrograms(context: Context, channelId: Long): List<ChannelProgram> =
        withContext(Dispatchers.IO) {
            context.contentResolver.tryQuery(
                TvContractCompat.buildPreviewProgramsUriForChannel(channelId),
                PreviewProgram.PROJECTION,
            ).processRows { cursor ->
                PreviewProgram.fromCursor(cursor).toChannelProgram()
            }
        }

    suspend fun getWatchNextPrograms(context: Context): List<ChannelProgram> = withContext(Dispatchers.IO) {
        context.contentResolver.tryQuery(
            TvContractCompat.WatchNextPrograms.CONTENT_URI,
            WatchNextProgram.PROJECTION,
        ).processRows { cursor ->
            WatchNextProgram.fromCursor(cursor).toChannelProgram()
        }
    }

    suspend fun removeWatchNextProgram(context: Context, programId: Long) = withContext(Dispatchers.IO) {
        try {
            val uri = TvContractCompat.buildWatchNextProgramUri(programId)
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove watch next program: $programId")
        }
    }

    private fun PreviewChannel.toChannel() = Channel(
        id = "$CHANNEL_ID_PREFIX$id",
        type = ChannelType.PREVIEW,
        channelId = id,
        displayName = displayName.toString(),
        description = description?.toString(),
        packageName = packageName,
        appLinkIntentUri = appLinkIntentUri.toString(),
        enabled = true,
        displayOrder = null,
    )

    @Suppress("USELESS_ELVIS")
    private fun PreviewProgram.toChannelProgram() = ChannelProgram(
        id = "$CHANNEL_PROGRAM_ID_PREFIX$id",
        channelId = "$CHANNEL_ID_PREFIX$channelId",
        packageName = packageName,
        weight = weight,
        posterArtUri = posterArtUri?.toString(),
        posterArtAspectRatio = when (posterArtAspectRatio) {
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_16_9 -> ChannelProgramAspectRatio.AR16_9
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_3_2 -> ChannelProgramAspectRatio.AR3_2
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_4_3 -> ChannelProgramAspectRatio.AR4_3
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_1_1 -> ChannelProgramAspectRatio.AR1_1
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_2_3 -> ChannelProgramAspectRatio.AR2_3
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_MOVIE_POSTER -> ChannelProgramAspectRatio.AR1000_1441
            else -> null
        },
        lastPlaybackPositionMillis = lastPlaybackPositionMillis,
        durationMillis = durationMillis,
        type = when (type) {
            TvContractCompat.PreviewPrograms.TYPE_MOVIE -> ChannelProgramType.MOVIE
            TvContractCompat.PreviewPrograms.TYPE_TV_SERIES -> ChannelProgramType.TV_SERIES
            TvContractCompat.PreviewPrograms.TYPE_TV_SEASON -> ChannelProgramType.TV_SEASON
            TvContractCompat.PreviewPrograms.TYPE_TV_EPISODE -> ChannelProgramType.TV_EPISODE
            TvContractCompat.PreviewPrograms.TYPE_CLIP -> ChannelProgramType.CLIP
            TvContractCompat.PreviewPrograms.TYPE_EVENT -> ChannelProgramType.EVENT
            TvContractCompat.PreviewPrograms.TYPE_CHANNEL -> ChannelProgramType.CHANNEL
            TvContractCompat.PreviewPrograms.TYPE_TRACK -> ChannelProgramType.TRACK
            TvContractCompat.PreviewPrograms.TYPE_ALBUM -> ChannelProgramType.ALBUM
            TvContractCompat.PreviewPrograms.TYPE_ARTIST -> ChannelProgramType.ARTIST
            TvContractCompat.PreviewPrograms.TYPE_PLAYLIST -> ChannelProgramType.PLAYLIST
            TvContractCompat.PreviewPrograms.TYPE_STATION -> ChannelProgramType.STATION
            TvContractCompat.PreviewPrograms.TYPE_GAME -> ChannelProgramType.GAME
            else -> null
        },
        releaseDate = releaseDate,
        itemCount = itemCount,
        live = isLive ?: false,
        interactionType = when (interactionType) {
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_LISTENS -> ChannelProgramInteractionType.LISTENS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_FOLLOWERS -> ChannelProgramInteractionType.FOLLOWERS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_FANS -> ChannelProgramInteractionType.FANS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_LIKES -> ChannelProgramInteractionType.LIKES
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_THUMBS -> ChannelProgramInteractionType.THUMBS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_VIEWS -> ChannelProgramInteractionType.VIEWS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_VIEWERS -> ChannelProgramInteractionType.VIEWERS
            else -> null
        },
        interactionCount = interactionCount,
        author = author,
        genre = genre,
        startTimeUtcMillis = startTimeUtcMillis,
        endTimeUtcMillis = endTimeUtcMillis,
        title = title,
        episodeTitle = episodeTitle,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        description = description,
        intentUri = intentUri?.toString()
    )

    @Suppress("USELESS_ELVIS")
    private fun WatchNextProgram.toChannelProgram() = ChannelProgram(
        id = "$CHANNEL_PROGRAM_ID_PREFIX$id",
        channelId = CHANNEL_ID_WATCH_NEXT,
        packageName = packageName,
        weight = -1,
        posterArtUri = posterArtUri?.toString(),
        posterArtAspectRatio = when (posterArtAspectRatio) {
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_16_9 -> ChannelProgramAspectRatio.AR16_9
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_3_2 -> ChannelProgramAspectRatio.AR3_2
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_4_3 -> ChannelProgramAspectRatio.AR4_3
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_1_1 -> ChannelProgramAspectRatio.AR1_1
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_2_3 -> ChannelProgramAspectRatio.AR2_3
            TvContractCompat.PreviewPrograms.ASPECT_RATIO_MOVIE_POSTER -> ChannelProgramAspectRatio.AR1000_1441
            else -> null
        },
        lastPlaybackPositionMillis = lastPlaybackPositionMillis,
        durationMillis = durationMillis,
        type = when (type) {
            TvContractCompat.PreviewPrograms.TYPE_MOVIE -> ChannelProgramType.MOVIE
            TvContractCompat.PreviewPrograms.TYPE_TV_SERIES -> ChannelProgramType.TV_SERIES
            TvContractCompat.PreviewPrograms.TYPE_TV_SEASON -> ChannelProgramType.TV_SEASON
            TvContractCompat.PreviewPrograms.TYPE_TV_EPISODE -> ChannelProgramType.TV_EPISODE
            TvContractCompat.PreviewPrograms.TYPE_CLIP -> ChannelProgramType.CLIP
            TvContractCompat.PreviewPrograms.TYPE_EVENT -> ChannelProgramType.EVENT
            TvContractCompat.PreviewPrograms.TYPE_CHANNEL -> ChannelProgramType.CHANNEL
            TvContractCompat.PreviewPrograms.TYPE_TRACK -> ChannelProgramType.TRACK
            TvContractCompat.PreviewPrograms.TYPE_ALBUM -> ChannelProgramType.ALBUM
            TvContractCompat.PreviewPrograms.TYPE_ARTIST -> ChannelProgramType.ARTIST
            TvContractCompat.PreviewPrograms.TYPE_PLAYLIST -> ChannelProgramType.PLAYLIST
            TvContractCompat.PreviewPrograms.TYPE_STATION -> ChannelProgramType.STATION
            TvContractCompat.PreviewPrograms.TYPE_GAME -> ChannelProgramType.GAME
            else -> null
        },
        releaseDate = releaseDate,
        itemCount = itemCount,
        live = isLive ?: false,
        interactionType = when (interactionType) {
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_LISTENS -> ChannelProgramInteractionType.LISTENS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_FOLLOWERS -> ChannelProgramInteractionType.FOLLOWERS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_FANS -> ChannelProgramInteractionType.FANS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_LIKES -> ChannelProgramInteractionType.LIKES
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_THUMBS -> ChannelProgramInteractionType.THUMBS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_VIEWS -> ChannelProgramInteractionType.VIEWS
            TvContractCompat.PreviewPrograms.INTERACTION_TYPE_VIEWERS -> ChannelProgramInteractionType.VIEWERS
            else -> null
        },
        interactionCount = interactionCount,
        author = author,
        genre = genre,
        startTimeUtcMillis = startTimeUtcMillis,
        endTimeUtcMillis = endTimeUtcMillis,
        title = title,
        episodeTitle = episodeTitle,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        description = description,
        intentUri = intentUri?.toString()
    )
}
