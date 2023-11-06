package com.example.geocamera.NewEditPicActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.geocamera.Model.Marker
import com.example.geocamera.Model.MarkerRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class NewEditPicViewModel(private val repository: MarkerRepository, private val id:Int) : ViewModel() {
    var curMarker: LiveData<Marker> = repository.getMarkerLiveData(id).asLiveData()

    fun updateId(id:Int){
        curMarker = repository.getMarkerLiveData(id).asLiveData()
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    suspend fun insert(marker: Marker){
        coroutineScope {
            repository.insert(marker)
        }
    }

    /**
     * Launching a new coroutine to Update the data in a non-blocking way
     */
    suspend fun update(marker: Marker) {
        coroutineScope {
            repository.update(marker)
        }
    }

    suspend fun deleteMarker(id: Int) {
        coroutineScope {
            Log.d("ViewModel","Deleting id: $id")
            repository.deleteTask(id)
        }

    }
}
