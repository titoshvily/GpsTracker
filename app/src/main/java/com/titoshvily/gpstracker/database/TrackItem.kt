package com.titoshvily.gpstracker.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Track")
data class TrackItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,

    @ColumnInfo(name = "time")
    val time: String,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "distance")
    val distance: String,

    @ColumnInfo(name = "velocity")
    val velocity: String,

    @ColumnInfo(name = "geo_points")
    val geoPoints: String

)
