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

    @Query("SELECT * FROM todo_place_table WHERE taskId = :key")
    fun get(key: Long): Place?

    @Query("DELETE FROM todo_place_table")
    fun clear()

    @Query("SELECT * FROM todo_place_table ORDER BY taskId DESC")
    fun getAllTasks(): LiveData<List<Place>>

    @Query("SELECT * FROM todo_place_table ORDER BY taskId DESC LIMIT 1")
    fun getLastTask(): Place?

    @Query("SELECT * FROM todo_place_table WHERE taskId = :key")
    fun getTaskWithId(key: Long): LiveData<Place>

    @Query("DELETE FROM todo_place_table WHERE taskId = :key")
    fun deleteWithId(key: Long) : Int
}
