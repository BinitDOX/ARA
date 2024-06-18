package com.dox.ara.command.types

import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandHandlerFactory.CommandType.PLAY_MUSIC
import com.dox.ara.command.CommandResponse
import com.dox.ara.manager.MediaControllerManager
import com.dox.ara.manager.PermissionManager
import com.truecrm.rat.utility.stringSimilarity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class PlayMusicCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
    private val mediaControllerManager: MediaControllerManager,
) : CommandHandler(args) {
    override val numArgs = 2
    private lateinit var applicationName: MusicApplication
    private lateinit var songName: String

    enum class MusicApplication {
        YOUTUBE,
        LOCAL
    }

    override fun help(): String {
        val appNames = MusicApplication.entries.joinToString("|") { it.name.lowercase() }

        return "[${PLAY_MUSIC.name.lowercase()}(<${appNames}>,'song_name')]"
    }

    override fun parseArguments() {
        val applicationName = args[0].replace(" ", "_").uppercase().replace("'", "")
        val songName = args[1].replace("'", "")

        try {
            this.applicationName = MusicApplication.valueOf(applicationName)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Invalid application: $applicationName, " +
                        "must be one of ${MusicApplication.entries.joinToString { it.name.lowercase() }}",
            )
        }

        this.songName = songName
    }

    private fun handleLocal(): CommandResponse {
        if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionManager.checkReadMediaAudio())  ||
           (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && !permissionManager.checkReadExternalStorage())){
            return CommandResponse(
                false,
                "No read media audio permission",
                true
            )
        }

        val songs = getAllLocalMusicFiles()
        val confidence = 0.55

        val bestMatch = songs.maxByOrNull { stringSimilarity(it.first, songName) }
        if (bestMatch != null) {
            val matchScore = stringSimilarity(bestMatch.first, songName)
            Timber.d("[${::handleLocal.name}] Best song match: ${bestMatch.first}, score: $matchScore")
            if (matchScore > confidence) {
                val filePath = bestMatch.second
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(filePath)

                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                val artwork = retriever.embeddedPicture

                val mediaItem = MediaItem.Builder()
                    .setMediaId("Music-$songName")
                    .setUri(filePath)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                        .setTitle(title ?: bestMatch.first)
                        .setArtist(artist)
                        .setAlbumTitle(album)
                        .setArtworkData(artwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                        .build())
                    .build()

                CoroutineScope(Dispatchers.Main).launch {
                    mediaControllerManager.playMusic(mediaItem)
                }
                return CommandResponse(true, "Playing '${songName}'", false)
            } else {
                return CommandResponse(
                    false,
                    "No songs found matching: '$songName', with sufficient confidence",
                    true
                )
            }
        } else {
            return CommandResponse(
                false,
                "No songs found matching: '$songName'",
                true
            )
        }
    }

    private fun handleYoutube(): CommandResponse {
        // By accessibility
        // TODO: Implement
        return CommandResponse(
            true,
            "Not implemented yet",
            true
        )
    }

    override suspend fun execute(): CommandResponse {
       return when(applicationName){
           MusicApplication.YOUTUBE -> handleYoutube()
           MusicApplication.LOCAL -> handleLocal()
       }
    }


    private fun getAllLocalMusicFiles(): List<Pair<String, String>> {
        val projection = arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        val songs = mutableListOf<Pair<String, String>>()
        cursor?.use {
            val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (it.moveToNext()) {
                val title = it.getString(titleIndex)
                val data = it.getString(dataIndex)
                songs.add(title to data)
            }
        }
        return songs
    }
}