package com.example.geocamera.Model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Marker::class), version = 1, exportSchema = false)

abstract class MarkerDatabase : RoomDatabase() {
    abstract fun markerDao(): MarkerDao

    private class HomeMaintenanceDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    val taskDao = database.markerDao()
                    // Delete all content here.
                    taskDao.deleteAll()
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MarkerDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): MarkerDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarkerDatabase::class.java,
                    "task_database"
                )
                    .addCallback(HomeMaintenanceDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}