package com.adrian.zarza.turisty.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_place_table")
data class Place  @JvmOverloads constructor(
    @ColumnInfo(name = "title_place")
    var titleTask: String,

    @ColumnInfo(name = "description_place")
    var descriptionTask: String,

    @ColumnInfo(name = "latitude_longitude")
    var latitudeLongitude: String,

    @ColumnInfo(name = "place_address")
    var address: String,

    @PrimaryKey(autoGenerate = true)
    var taskId: Long = 0L
)