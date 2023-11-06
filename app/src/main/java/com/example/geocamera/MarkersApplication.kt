package com.example.geocamera

import android.app.Application
import com.example.geocamera.Model.MarkerDatabase
import com.example.geocamera.Model.MarkerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MarkersApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { MarkerDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { MarkerRepository(database.markerDao()) }
}