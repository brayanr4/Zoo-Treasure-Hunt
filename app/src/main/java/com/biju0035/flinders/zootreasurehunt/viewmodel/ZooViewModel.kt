package com.biju0035.flinders.zootreasurehunt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.biju0035.flinders.zootreasurehunt.Sighting
import com.biju0035.flinders.zootreasurehunt.data.SightingRepository
import com.biju0035.flinders.zootreasurehunt.worker.CongratulationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ZooViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SightingRepository(application)
    private val workManager = WorkManager.getInstance(application)

    private val _isSortByName = MutableStateFlow(true)
    val isSortByName: StateFlow<Boolean> = _isSortByName.asStateFlow()

    private val _sightings = MutableStateFlow<List<Sighting>>(emptyList())
    
    val sightings: StateFlow<List<Sighting>> = combine(_sightings, _isSortByName) { list, sort ->
        if (sort) {
            list.sortedBy { it.name }
        } else {
            // Sort by found status (not found first, then found) and ID (as a proxy for recency if IDs are sequential)
            // or we could use the list order if that represents recency.
            // Based on the prompt "Recency / Found Status", let's assume the original list order is recency.
            list.sortedBy { it.isFound }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            _sightings.value = repository.loadSightings()
        }
    }

    private fun saveData(newList: List<Sighting>) {
        _sightings.value = newList
        viewModelScope.launch {
            repository.saveSightings(newList)
        }
    }

    fun toggleSortOrder(sort: Boolean) {
        _isSortByName.value = sort
    }

    fun deleteSighting(sighting: Sighting) {
        val newList = _sightings.value.filter { it.id != sighting.id }
        saveData(newList)
    }

    fun updateSighting(updated: Sighting, wasPreviouslyFound: Boolean) {
        if (updated.isFound && !wasPreviouslyFound) {
            val workRequest = OneTimeWorkRequestBuilder<CongratulationWorker>()
                .setInputData(workDataOf("ANIMAL_NAME" to updated.name))
                .build()

            workManager.enqueue(workRequest)
        }

        val newList = _sightings.value.map {
            if (it.id == updated.id) updated else it
        }
        saveData(newList)
    }
}
