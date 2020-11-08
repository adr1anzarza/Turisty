package com.adrian.zarza.turisty.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Place::class], version = 1,  exportSchema = false)
abstract class PlaceDatabase : RoomDatabase() {

    abstract val placeDatabaseDao: PlaceDatabaseDAO

    companion object {

        @Volatile
        private var INSTANCE: PlaceDatabase? = null

        fun getInstance(context: Context): PlaceDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PlaceDatabase::class.java,
                        "task_database"
                    ).fallbackToDestructiveMigration().build()
                }

                INSTANCE = instance
                return instance
            }
        }
    }

}