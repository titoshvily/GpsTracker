package com.titoshvily.gpstracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface Dao {

    @Insert
    suspend fun insertTrack(trackItem: TrackItem)

    @Query("SELECT * FROM Track")
    fun getAllTracks(): Flow<List<TrackItem>>

    @Delete
    suspend fun deleteTrack(trackItem: TrackItem)
}