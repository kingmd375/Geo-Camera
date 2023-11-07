package com.example.geocamera.MainActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.geocamera.Model.Marker
import com.example.geocamera.Model.MarkerRepository
import kotlinx.coroutines.launch

class MapViewModel(private val repository: MarkerRepository) : ViewModel() {
    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allMarkers: LiveData<List<Marker>> = repository.allMarkers.asLiveData()

    fun getMarker(id: Int): Marker {
        return repository.getMarker(id)
    }

    fun updateDesc(id: Int, newDesc: String) {
        viewModelScope.launch {
            repository.updateDesc(id, newDesc)
        }
    }

    fun add(marker: Marker) {
        viewModelScope.launch {
            repository.insert(marker)
        }
    }
}

class MapViewModelFactory(private val repository: MarkerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}