package com.example.geocamera.Model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {
    //Get all markers
    @Query("SELECT * FROM marker_table")
    fun getAllMarkers(): Flow<List<Marker>>

    //Get a single marker with a given id as LiveData
    @Query("SELECT * FROM marker_table WHERE id=:id")
    fun getMarker(id:Int): Flow<Marker>

    //Get a single marker with a given id
    @Query("SELECT * FROM marker_table WHERE id=:id")
    fun getMarkerNotLive(id:Int): Marker

    //Insert a single marker
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(marker: Marker)

    //Delete all markers
    @Query("DELETE FROM marker_table")
    suspend fun deleteAll()

    //Update a single word
    @Update
    suspend fun update(marker: Marker):Int

    @Query("DELETE from marker_table WHERE id=:id")
    suspend fun deleteMarker(id: Int)
}