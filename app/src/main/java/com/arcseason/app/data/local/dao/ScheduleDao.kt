package com.arcseason.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.arcseason.app.data.local.entity.ActiveScheduleDayEntity
import com.arcseason.app.data.local.entity.SchedulePresetEntity
import com.arcseason.app.data.local.entity.ScheduleBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Upsert
    suspend fun upsertPreset(preset: SchedulePresetEntity)

    @Query("SELECT * FROM schedule_presets WHERE presetType = :presetType")
    suspend fun getPreset(presetType: String): SchedulePresetEntity?

    @Query("SELECT * FROM schedule_presets")
    fun observeAllPresets(): Flow<List<SchedulePresetEntity>>

    @Query("DELETE FROM schedule_blocks WHERE presetType = :presetType")
    suspend fun clearBlocksForPreset(presetType: String)

    @Upsert
    suspend fun upsertBlocks(blocks: List<ScheduleBlockEntity>)

    @Delete
    suspend fun deleteBlock(block: ScheduleBlockEntity)

    @Query("SELECT * FROM schedule_blocks WHERE presetType = :presetType ORDER BY startTime ASC")
    fun observeBlocksForPreset(presetType: String): Flow<List<ScheduleBlockEntity>>

    /**
     * Replaces every block for a preset in one call, so an
     * `updateDailySchedulePreset` tool execution from the Arc Coach (Phase 4)
     * can't leave the timeline half-updated if it's interrupted.
     */
    suspend fun replaceBlocksForPreset(presetType: String, blocks: List<ScheduleBlockEntity>) {
        clearBlocksForPreset(presetType)
        upsertBlocks(blocks)
    }

    @Upsert
    suspend fun setActiveScheduleForDay(day: ActiveScheduleDayEntity)

    @Query("SELECT * FROM active_schedule_days WHERE date = :date LIMIT 1")
    suspend fun getActiveScheduleForDay(date: String): ActiveScheduleDayEntity?
}
