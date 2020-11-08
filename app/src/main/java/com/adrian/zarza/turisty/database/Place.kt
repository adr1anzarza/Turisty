package com.adrian.zarza.turisty.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_place_table")
data class Place  @JvmOverloads constructor(
    @ColumnInfo(name = "title_place")
    var titlePlace: String,

    @ColumnInfo(name = "description_place")
    var descriptionPlace: String,

    @ColumnInfo(name = "latitude_longitude")
    var latitudeLongitude: String,

    @ColumnInfo(name = "place_address")
    var addressPlace: String,

    @PrimaryKey(autoGenerate = true)
    var placeId: Long = 0L
)