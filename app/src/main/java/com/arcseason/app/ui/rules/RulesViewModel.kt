package com.arcseason.app.ui.rules

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arcseason.app.ArcSeasonApplication
import com.arcseason.app.domain.ArcRuleType
import com.arcseason.app.domain.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SimpleRuleUiState(val type: ArcRuleType, val isDone: Boolean, val streak: Int)

/** Also doubles as the running accumulator while the view model's flows are combined below. */
data class RulesUiState(
    val simpleRules: List<SimpleRuleUiState> = emptyList(),
    val gymWeeklyCount: Int = 0,
    val cardioWeeklyCount: Int = 0,
    val plankTodaySeconds: Int? = null,
    val plankBestSeconds: Int = 0,
    val pushupTodayReps: Int? = null,
    val pushupBestReps: Int = 0,
    val gratitudeToday: List<String> = emptyList(),
    val gratitudeStreak: Int = 0,
    val winsToday: List<String> = emptyList(),
    val winsStreak: Int = 0
)

private val SIMPLE_RULE_TYPES = listOf(
    ArcRuleType.WATER,
    ArcRuleType.NO_JUNK,
    ArcRuleType.SLEEP,
    ArcRuleType.NO_PHONE_MEALS,
    ArcRuleType.OUTSIDE_TIME
)

class RulesViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ArcSeasonApplication).arcRuleRepository

    private val simpleRulesFlow = repo.observeAllRuleLogsInRange().map { logs ->
        val today = DateUtils.today()
        SIMPLE_RULE_TYPES.map { type ->
            val logsForType = logs.filter { it.ruleType == type.name }
            val isDoneToday = logsForType.any { it.date == today && it.isCompleted }
            val completedDates = logsForType.filter { it.isCompleted }.map { it.date }.toSet()
            SimpleRuleUiState(type, isDoneToday, DateUtils.computeStreak(completedDates))
        }
    }

    private val plankFlow = repo.observeTodayChallenge("PLANK")
        .combine(repo.observePlankBest()) { today, best -> today?.secondsHeld to (best ?: 0) }

    private val pushupFlow = repo.observeTodayChallenge("PUSHUPS")
        .combine(repo.observePushupBest()) { today, best -> today?.repsCompleted to (best ?: 0) }

    private val gratitudeFlow = repo.observeJournalEntryForDate("GRATITUDE")
        .combine(repo.observeJournalStreak("GRATITUDE")) { entry, streak -> (entry?.entries ?: emptyList()) to streak }

    private val winsFlow = repo.observeJournalEntryForDate("WINS")
        .combine(repo.observeJournalStreak("WINS")) { entry, streak -> (entry?.entries ?: emptyList()) to streak }

    val uiState: StateFlow<RulesUiState> = simpleRulesFlow
        .map { RulesUiState(simpleRules = it) }
        .combine(repo.observeWeeklyActivityCount("GYM")) { acc, gym -> acc.copy(gymWeeklyCount = gym) }
        .combine(repo.observeWeeklyActivityCount("CARDIO")) { acc, cardio -> acc.copy(cardioWeeklyCount = cardio) }
        .combine(plankFlow) { acc, plank -> acc.copy(plankTodaySeconds = plank.first, plankBestSeconds = plank.second) }
        .combine(pushupFlow) { acc, pushup -> acc.copy(pushupTodayReps = pushup.first, pushupBestReps = pushup.second) }
        .combine(gratitudeFlow) { acc, g -> acc.copy(gratitudeToday = g.first, gratitudeStreak = g.second) }
        .combine(winsFlow) { acc, w -> acc.copy(winsToday = w.first, winsStreak = w.second) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RulesUiState())

    fun toggleRule(type: ArcRuleType) {
        val current = uiState.value.simpleRules.find { it.type == type }?.isDone ?: false
        viewModelScope.launch { repo.setRuleCompletion(ruleType = type.name, isCompleted = !current) }
    }

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

    fun saveGratitude(entries: List<String>) {
        viewModelScope.launch { repo.saveJournalEntry("GRATITUDE", entries) }
    }

    fun saveWins(entries: List<String>) {
        viewModelScope.launch { repo.saveJournalEntry("WINS", entries) }
    }
}
