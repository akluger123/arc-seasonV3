package com.arcseason.app.ui.macros

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arcseason.app.ArcSeasonApplication
import com.arcseason.app.data.local.entity.NutritionLogEntity
import com.arcseason.app.data.local.entity.UserProfileEntity
import com.arcseason.app.domain.BmrCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MacrosUiState(
    val profile: UserProfileEntity? = null,
    val caloriesTarget: Int = 0,
    val proteinTarget: Int = 0,
    val carbsTarget: Int = 0,
    val fatTarget: Int = 0,
    val logs: List<NutritionLogEntity> = emptyList()
) {
    val needsOnboarding: Boolean get() = profile == null
    val caloriesConsumed: Int get() = logs.sumOf { it.calories }
    val proteinConsumed: Int get() = logs.sumOf { it.proteinGrams }
    val carbsConsumed: Int get() = logs.sumOf { it.carbsGrams }
    val fatConsumed: Int get() = logs.sumOf { it.fatGrams }
}

class MacrosViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ArcSeasonApplication).nutritionRepository

    val uiState: StateFlow<MacrosUiState> = repo.observeUserProfile()
        .combine(repo.observeLogsForDate()) { profile, logs ->
            MacrosUiState(
                profile = profile,
                caloriesTarget = profile?.calorieTargetKcal ?: 0,
                proteinTarget = profile?.proteinTargetGrams ?: 0,
                carbsTarget = profile?.carbsTargetGrams ?: 0,
                fatTarget = profile?.fatTargetGrams ?: 0,
                logs = logs
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MacrosUiState())

    fun saveProfile(inputs: BmrCalculator.Inputs, guardianAcknowledged: Boolean) {
        viewModelScope.launch { repo.saveProfile(inputs, guardianAcknowledged) }
    }

    fun logFood(label: String, calories: Int, protein: Int, carbs: Int, fat: Int, isJunk: Boolean, source: String = "MANUAL") {
        viewModelScope.launch {
            repo.logFood(
                foodName = label,
                calories = calories,
                proteinGrams = protein,
                carbsGrams = carbs,
                fatGrams = fat,
                isJunkFood = isJunk,
                source = source
            )
        }
    }

    fun deleteFood(entry: NutritionLogEntity) {
        viewModelScope.launch { repo.deleteLog(entry) }
    }
}
