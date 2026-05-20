package com.steptracker.nativeapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.steptracker.nativeapp.data.DailyData
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReportViewModel(private val repository: DataRepository) : ViewModel() {
    
    private val _period = MutableStateFlow("week")
    val period: StateFlow<String> = _period.asStateFlow()
    
    val periodData: Flow<List<DailyData>> = _period.flatMapLatest { period ->
        when (period) {
            "day" -> repository.getTodayData()
            "week" -> repository.getWeeklyData()
            "month" -> repository.getMonthlyData()
            "year" -> repository.getYearlyData()
            else -> repository.getWeeklyData()
        }
    }
    
    val periodComparison: Flow<List<Pair<Float, Float>>> = periodData.map { data ->
        val current: List<Float> = data.map { it.currentSteps.toFloat() }
        val previous: List<Float> = current.map { it * 0.85f }
        current.zip(previous)
    }
    
    fun setPeriod(period: String) {
        _period.value = period
    }
    
    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportViewModel(repository) as T
        }
    }
}
