package com.arcseason.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.arcseason.app.data.local.entity.ActivitySessionEntity
import com.arcseason.app.data.local.entity.ChallengeLogEntity
import com.arcseason.app.data.local.entity.DailyRuleLogEntity
import com.arcseason.app.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcRuleDao {

    // --- Simple daily rules (water, no-junk check-in, sleep flag, phone-free
    // meals, outside time) ---

    @Upsert
    suspend fun upsertRuleLog(log: DailyRuleLogEntity)

    @Query("SELECT * FROM daily_rule_logs WHERE date = :date")
    fun observeRuleLogsForDate(date: String): Flow<List<DailyRuleLogEntity>>

    @Query(
        "SELECT * FROM daily_rule_logs WHERE ruleType = :ruleType " +
            "AND date BETWEEN :startDate AND :endDate ORDER BY date ASC"
    )
    fun observeRuleLogsInRange(
        ruleType: String,
        startDate: String,
        endDate: String
    ): Flow<List<DailyRuleLogEntity>>

    /** All rule types at once, for streak/today-state calculations done in one pass. */
    @Query("SELECT * FROM daily_rule_logs WHERE date BETWEEN :startDate AND :endDate")
    fun observeAllRuleLogsInRange(startDate: String, endDate: String): Flow<List<DailyRuleLogEntity>>

    // --- Gym / cardio weekly-count rings ---

    @Upsert
    suspend fun upsertActivitySession(session: ActivitySessionEntity)

    @Delete
    suspend fun deleteActivitySession(session: ActivitySessionEntity)

    @Query(
        "SELECT * FROM activity_sessions WHERE activityType = :type " +
            "AND date BETWEEN :startDate AND :endDate ORDER BY date ASC"
    )
    fun observeActivitySessions(
        type: String,
        startDate: String,
        endDate: String
    ): Flow<List<ActivitySessionEntity>>

    @Query(
        "SELECT COUNT(*) FROM activity_sessions WHERE activityType = :type " +
            "AND date BETWEEN :startDate AND :endDate"
    )
    fun observeActivityCountInRange(
        type: String,
        startDate: String,
        endDate: String
    ): Flow<Int>

    // --- Gratitude / Wins journals (5 entries each) ---

    @Upsert
    suspend fun upsertJournalEntry(entry: JournalEntryEntity)

    @Query("SELECT * FROM journal_entries WHERE date = :date AND journalType = :journalType LIMIT 1")
    suspend fun getJournalEntry(date: String, journalType: String): JournalEntryEntity?

    /** Same lookup as [getJournalEntry] but observable, for driving Compose state directly. */
    @Query("SELECT * FROM journal_entries WHERE date = :date AND journalType = :journalType LIMIT 1")
    fun observeJournalEntryForDate(date: String, journalType: String): Flow<JournalEntryEntity?>

    /** Just the dates a journal type has an entry for, used to compute a streak. */
    @Query(
        "SELECT date FROM journal_entries WHERE journalType = :journalType " +
            "AND date BETWEEN :startDate AND :endDate"
    )
    fun observeJournalDatesInRange(journalType: String, startDate: String, endDate: String): Flow<List<String>>

    @Query("SELECT * FROM journal_entries WHERE journalType = :journalType ORDER BY date DESC LIMIT :limit")
    fun observeRecentJournalEntries(journalType: String, limit: Int): Flow<List<JournalEntryEntity>>

    // --- Plank / push-up challenge history ---

    @Insert
    suspend fun insertChallengeLog(log: ChallengeLogEntity)

    @Query("SELECT * FROM challenge_logs WHERE challengeType = :type ORDER BY date DESC, id DESC")
    fun observeChallengeHistory(type: String): Flow<List<ChallengeLogEntity>>

    /** Most recent attempt for a type on one specific date (there can be more than one per day). */
    @Query(
        "SELECT * FROM challenge_logs WHERE challengeType = :type AND date = :date " +
            "ORDER BY id DESC LIMIT 1"
    )
    fun observeTodayChallenge(type: String, date: String): Flow<ChallengeLogEntity?>

    @Query("SELECT MAX(secondsHeld) FROM challenge_logs WHERE challengeType = 'PLANK'")
    fun observePlankPersonalBest(): Flow<Int?>

    @Query("SELECT MAX(repsCompleted) FROM challenge_logs WHERE challengeType = 'PUSHUPS'")
    fun observePushupPersonalBest(): Flow<Int?>

    @Query(
        "SELECT COUNT(DISTINCT date) FROM challenge_logs WHERE challengeType = 'PLANK' " +
            "AND secondsHeld >= 60 AND date BETWEEN :startDate AND :endDate"
    )
    fun observePlankDaysHitInRange(startDate: String, endDate: String): Flow<Int>
}
