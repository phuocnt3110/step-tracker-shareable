package com.steptracker.nativeapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.steptracker.nativeapp.data.ActivityRecord
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ActivityViewModel(private val repository: DataRepository) : ViewModel() {
    
    val recentActivities: Flow<List<ActivityRecord>> = repository.getActivities()
    
    fun getActivitiesByDateRange(start: LocalDate, end: LocalDate): Flow<List<ActivityRecord>> {
        return repository.getActivitiesByRange(start, end)
    }
    
    suspend fun startActivity(): Long {
        return repository.startActivity()
    }
    
    suspend fun saveActivity(
        steps: Int,
        kcal: Int,
        km: Double,
        durationMinutes: Int,
        maxSpeed: Double,
        avgSpeed: Double,
        coordinates: List<Pair<Double, Double>>
    ) {
        val activityId = repository.startActivity()
        repository.endActivity(
            id = activityId,
            steps = steps,
            kcal = kcal,
            km = km,
            coordinates = coordinates,
            maxSpeed = maxSpeed,
            avgSpeed = avgSpeed
        )
    }
    
    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityViewModel(repository) as T
        }
    }
}
