package com.arcseason.app.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arcseason.app.ArcSeasonApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkoutUiState(
    val gymWeeklyCount: Int = 0,
    val cardioWeeklyCount: Int = 0,
    val plankTodaySeconds: Int? = null,
    val plankBestSeconds: Int = 0,
    val pushupTodayReps: Int? = null,
    val pushupBestReps: Int = 0,
    val pushupHistory: List<Int> = emptyList()
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ArcSeasonApplication).arcRuleRepository

    private val plankFlow = repo.observeTodayChallenge("PLANK")
        .combine(repo.observePlankBest()) { today, best -> today?.secondsHeld to (best ?: 0) }

    private val pushupFlow = repo.observeTodayChallenge("PUSHUPS")
        .combine(repo.observePushupBest()) { today, best -> today?.repsCompleted to (best ?: 0) }

    private val pushupHistoryFlow = repo.observeChallengeHistory("PUSHUPS")
        .map { list -> list.take(10).mapNotNull { it.repsCompleted }.reversed() }

    val uiState: StateFlow<WorkoutUiState> = repo.observeWeeklyActivityCount("GYM")
        .map { WorkoutUiState(gymWeeklyCount = it) }
        .combine(repo.observeWeeklyActivityCount("CARDIO")) { acc, cardio -> acc.copy(cardioWeeklyCount = cardio) }
        .combine(plankFlow) { acc, plank -> acc.copy(plankTodaySeconds = plank.first, plankBestSeconds = plank.second) }
        .combine(pushupFlow) { acc, pushup -> acc.copy(pushupTodayReps = pushup.first, pushupBestReps = pushup.second) }
        .combine(pushupHistoryFlow) { acc, history -> acc.copy(pushupHistory = history) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutUiState())

    fun logGymSession() {
        viewModelScope.launch { repo.logActivitySession("GYM") }
    }

    fun logCardioSession() {
        viewModelScope.launch { repo.logActivitySession("CARDIO") }
    }

    fun savePlank(seconds: Int) {
        viewModelScope.launch { repo.insertChallengeLog(type = "PLANK", secondsHeld = seconds) }
    }

    fun savePushups(reps: Int) {
        viewModelScope.launch { repo.insertChallengeLog(type = "PUSHUPS", repsCompleted = reps) }
    }
}
