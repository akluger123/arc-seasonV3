package com.arcseason.app.data.local.entity

import androidx.room.Entity

/**
 * One row per (date, rule) pair for the simple daily rules — water, sleep
 * flag, phone-free-meals timer completion, outside time, and the "junk
 * food" check-in.
 *
 * Design note: the source spec described "No junk" as a rule that "fails
 * the day" if junk food is logged. We kept the tracking (isCompleted /
 * numericValue below) but deliberately did NOT bake a pass/fail "you
 * failed today" state into the schema or the copy anywhere in this
 * codebase. All-or-nothing framing around food is a well-documented risk
 * factor for unhealthy eating patterns in teens, and it only takes one
 * bad day to make a binary streak feel pointless. The UI in Phase 2 will
 * present this as a simple daily check-in, same as the others, not as a
 * pass/fail gate on the whole day.
 */
@Entity(tableName = "daily_rule_logs", primaryKeys = ["date", "ruleType"])
data class DailyRuleLogEntity(
    val date: String, // ISO-8601, yyyy-MM-dd
    val ruleType: String, // matches ArcRuleType.name in domain/RuleCatalog.kt
    val isCompleted: Boolean = false,
    val numericValue: Double? = null, // e.g. water in ml, outside-time in minutes
    val notes: String? = null
)

/**
 * A single gym or cardio session. Weekly progress rings (4x/week targets)
 * are computed by counting rows in a date range rather than storing a
 * running counter, so edits/deletes stay consistent automatically.
 */
@Entity(tableName = "activity_sessions")
data class ActivitySessionEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val activityType: String, // "GYM" or "CARDIO"
    val durationMinutes: Int? = null,
    val notes: String? = null
)

/**
 * Gratitude (5 things) and Wins (5 things) journals share a shape: a date,
 * a type, and up to 5 free-text entries. `entries` round-trips through
 * Converters.kt as JSON so Room can store it as a single TEXT column.
 */
@Entity(tableName = "journal_entries", primaryKeys = ["date", "journalType"])
data class JournalEntryEntity(
    val date: String,
    val journalType: String, // "GRATITUDE" or "WINS"
    val entries: List<String> // UI enforces exactly 5 slots; stored as-is here
)

/**
 * Plank (secondsHeld) and push-up (repsCompleted) challenge attempts.
 * Kept as an append-only log (one row per attempt) so history/PBs and
 * "5 days a week" plank adherence can both be derived from it.
 */
@Entity(tableName = "challenge_logs")
data class ChallengeLogEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val challengeType: String, // "PLANK" or "PUSHUPS"
    val secondsHeld: Int? = null,
    val repsCompleted: Int? = null
)
