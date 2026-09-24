package com.arcseason.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per preset (SCHOOL_DAY / NO_SCHOOL / WEEKEND). This is what the
 * Arc Coach's `updateDailySchedulePreset` tool call (Phase 4) writes to
 * when the user says things like "I don't have school today."
 */
@Entity(tableName = "schedule_presets")
data class SchedulePresetEntity(
    @PrimaryKey val presetType: String, // "SCHOOL_DAY" | "NO_SCHOOL" | "WEEKEND"
    val wakeUpTime: String, // HH:mm, 24h
    val sleepTime: String   // HH:mm, back-calculated to protect the 8h-sleep rule
)

/**
 * The actual blocks (wake, meals, workout, school, wind-down, sleep) that
 * make up a preset's timeline. Deleting a preset cascades to its blocks.
 */
@Entity(
    tableName = "schedule_blocks",
    foreignKeys = [
        ForeignKey(
            entity = SchedulePresetEntity::class,
            parentColumns = ["presetType"],
            childColumns = ["presetType"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("presetType")]
)
data class ScheduleBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetType: String,
    val blockType: String, // WAKE, MEAL, WORKOUT, SCHOOL, WIND_DOWN, SLEEP, FREE
    val label: String,
    val startTime: String, // HH:mm
    val endTime: String    // HH:mm
)

/** Which preset is/was active on a given calendar date. */
@Entity(tableName = "active_schedule_days")
data class ActiveScheduleDayEntity(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val presetType: String
)
