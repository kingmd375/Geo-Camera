package com.example.geocamera.Model

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class MarkerRepository(private val markerDao: MarkerDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allMarkers: Flow<List<Marker>> = markerDao.getAllMarkers()

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getMarkerLiveData(id:Int): Flow<Marker> {
        return markerDao.getMarker(id)
    }

    fun getMarker(id:Int):Marker{
        return markerDao.getMarkerNotLive(id)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(homeMaintenanceTask: Marker) {
        //Note that I am pretending this is a network call by adding
        //a 5 second sleep call here
        //If you don't run this in a scope that is still active
        //Then the call won't complete
        markerDao.insert(homeMaintenanceTask)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(homeMaintenanceTask: Marker) {
        markerDao.update(homeMaintenanceTask)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteTask(id: Int) {
        Log.d("Model","Deleting id: $id")
        markerDao.deleteMarker(id)
    }
}