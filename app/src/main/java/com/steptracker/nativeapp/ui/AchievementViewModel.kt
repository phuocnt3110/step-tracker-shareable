package com.steptracker.nativeapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.steptracker.nativeapp.data.Achievement
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AchievementViewModel(private val repository: DataRepository) : ViewModel() {
    
    val achievements: Flow<List<Achievement>> = repository.getAchievements()
    
    fun getAchievementsByCategory(category: String): Flow<List<Achievement>> {
        return repository.getAchievementsByCategory(category)
    }
    
    fun checkAchievements() {
        viewModelScope.launch {
            // Trigger achievement check
        }
    }
    
    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AchievementViewModel(repository) as T
        }
    }
}
