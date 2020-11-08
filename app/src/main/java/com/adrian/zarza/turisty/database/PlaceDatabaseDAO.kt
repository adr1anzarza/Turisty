package com.adrian.zarza.turisty.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlaceDatabaseDAO{

    @Insert
    fun insert(task: Place)

    @Update
    fun update(task: Place)

    @Query("SELECT * FROM todo_place_table WHERE placeId = :key")
    fun get(key: Long): Place?

    @Query("DELETE FROM todo_place_table")
    fun clear()

    @Query("SELECT * FROM todo_place_table ORDER BY placeId DESC")
    fun getAllPlaces(): LiveData<List<Place>>

    @Query("SELECT * FROM todo_place_table ORDER BY placeId DESC LIMIT 1")
    fun getLastPlace(): Place?

    @Query("SELECT * FROM todo_place_table WHERE placeId = :key")
    fun getPlaceWithId(key: Long): LiveData<Place>

    @Query("DELETE FROM todo_place_table WHERE placeId = :key")
    fun deleteWithId(key: Long) : Int
}
