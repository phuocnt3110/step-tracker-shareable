package com.steptracker.nativeapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.steptracker.nativeapp.data.DailyData
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class StepsViewModel(private val repository: DataRepository) : ViewModel() {
    
    val todayData: Flow<List<DailyData>> = repository.getTodayData()
    val weeklyData: Flow<List<DailyData>> = repository.getWeeklyData()
    val monthlyData: Flow<List<DailyData>> = repository.getMonthlyData()
    
    val todaySteps = todayData.map { it.firstOrNull()?.currentSteps ?: 0 }
    val todayGoal = todayData.map { it.firstOrNull()?.dailyGoal ?: 8000 }
    val progress = todayData.map { list ->
        val data = list.firstOrNull()
        val steps = data?.currentSteps ?: 0
        val goal = data?.dailyGoal ?: 8000
        if (goal == 0) 0f else (steps.toFloat() / goal * 100).coerceIn(0f, 100f)
    }
    
    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StepsViewModel(repository) as T
        }
    }
}
