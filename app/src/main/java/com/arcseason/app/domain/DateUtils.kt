package com.arcseason.app.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Small date/streak helpers used by the repository layer. Uses java.time
 * directly (no desugaring config needed) since minSdk is 26, where
 * java.time shipped natively on-device.
 */
object DateUtils {
    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(ISO)

    fun daysAgo(days: Int): String = LocalDate.now().minusDays(days.toLong()).format(ISO)

    /** Monday..Sunday range (inclusive) containing today, as ISO date strings. */
    fun currentWeekRange(): Pair<String, String> {
        val today = LocalDate.now()
        val monday = today.minusDays((today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
        val sunday = monday.plusDays(6)
        return monday.format(ISO) to sunday.format(ISO)
    }

    /**
     * Counts consecutive days of completion ending today, given the set of
     * dates (ISO strings) on which something counted as "done". If today
     * isn't in the set yet, we start counting from yesterday instead — an
     * incomplete "today" shouldn't zero out a streak that's still alive
     * until the day actually ends. Capped at 60 for cheapness; this project
     * only ever queries a 60-day window anyway.
     */
    fun computeStreak(completedDates: Set<String>): Int {
        var streak = 0
        var cursor = LocalDate.now()
        if (cursor.format(ISO) !in completedDates) {
            cursor = cursor.minusDays(1)
        }
        while (streak < 60 && completedDates.contains(cursor.format(ISO))) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
