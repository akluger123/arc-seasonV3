package com.arcseason.app.data.repository

import com.arcseason.app.data.local.dao.ArcRuleDao
import com.arcseason.app.data.local.entity.ActivitySessionEntity
import com.arcseason.app.data.local.entity.ChallengeLogEntity
import com.arcseason.app.data.local.entity.DailyRuleLogEntity
import com.arcseason.app.data.local.entity.JournalEntryEntity
import com.arcseason.app.domain.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Everything the Rules and Workout screens need, on top of the raw DAO. */
class ArcRuleRepository(private val dao: ArcRuleDao) {

    // --- Simple daily rules (water, mindful eating, sleep, phone-free meals, outside time) ---

    fun observeAllRuleLogsInRange(startDate: String = DateUtils.daysAgo(60), endDate: String = DateUtils.today()) =
        dao.observeAllRuleLogsInRange(startDate, endDate)

    suspend fun setRuleCompletion(ruleType: String, isCompleted: Boolean, numericValue: Double? = null, date: String = DateUtils.today()) {
        dao.upsertRuleLog(
            DailyRuleLogEntity(date = date, ruleType = ruleType, isCompleted = isCompleted, numericValue = numericValue)
        )
    }

    // --- Gym / cardio sessions & weekly counts ---

    suspend fun logActivitySession(type: String, date: String = DateUtils.today(), durationMinutes: Int? = null) {
        dao.upsertActivitySession(ActivitySessionEntity(date = date, activityType = type, durationMinutes = durationMinutes))
    }

    suspend fun deleteActivitySession(session: ActivitySessionEntity) = dao.deleteActivitySession(session)

    fun observeWeeklyActivityCount(type: String): Flow<Int> {
        val (start, end) = DateUtils.currentWeekRange()
        return dao.observeActivityCountInRange(type, start, end)
    }

    fun observeWeeklyActivitySessions(type: String): Flow<List<ActivitySessionEntity>> {
        val (start, end) = DateUtils.currentWeekRange()
        return dao.observeActivitySessions(type, start, end)
    }

    // --- Gratitude / Wins journals ---

    suspend fun saveJournalEntry(journalType: String, entries: List<String>, date: String = DateUtils.today()) {
        dao.upsertJournalEntry(JournalEntryEntity(date = date, journalType = journalType, entries = entries))
    }

    fun observeJournalEntryForDate(journalType: String, date: String = DateUtils.today()): Flow<JournalEntryEntity?> =
        dao.observeJournalEntryForDate(date, journalType)

    fun observeJournalStreak(journalType: String): Flow<Int> {
        val start = DateUtils.daysAgo(60)
        val end = DateUtils.today()
        return dao.observeJournalDatesInRange(journalType, start, end)
            .map { dates -> DateUtils.computeStreak(dates.toSet()) }
    }

    fun observeRecentJournalEntries(journalType: String, limit: Int = 3) =
        dao.observeRecentJournalEntries(journalType, limit)

    // --- Plank / push-up challenges ---

    suspend fun insertChallengeLog(type: String, date: String = DateUtils.today(), secondsHeld: Int? = null, repsCompleted: Int? = null) {
        dao.insertChallengeLog(ChallengeLogEntity(date = date, challengeType = type, secondsHeld = secondsHeld, repsCompleted = repsCompleted))
    }

    fun observeTodayChallenge(type: String, date: String = DateUtils.today()): Flow<ChallengeLogEntity?> =
        dao.observeTodayChallenge(type, date)

    fun observePlankBest(): Flow<Int?> = dao.observePlankPersonalBest()
    fun observePushupBest(): Flow<Int?> = dao.observePushupPersonalBest()
    fun observeChallengeHistory(type: String): Flow<List<ChallengeLogEntity>> = dao.observeChallengeHistory(type)
}
