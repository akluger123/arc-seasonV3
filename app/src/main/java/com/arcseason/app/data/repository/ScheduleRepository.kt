package com.arcseason.app.data.repository

import com.arcseason.app.data.local.dao.ScheduleDao
import com.arcseason.app.data.local.entity.ActiveScheduleDayEntity
import com.arcseason.app.data.local.entity.ScheduleBlockEntity
import com.arcseason.app.data.local.entity.SchedulePresetEntity
import com.arcseason.app.domain.DateUtils
import com.arcseason.app.domain.DefaultSchedule
import com.arcseason.app.domain.ScheduleBlockType
import com.arcseason.app.domain.SchedulePresetType
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val dao: ScheduleDao) {

    fun observeBlocksForPreset(presetType: String): Flow<List<ScheduleBlockEntity>> =
        dao.observeBlocksForPreset(presetType)

    suspend fun addBlock(presetType: String, blockType: String, label: String, start: String, end: String) {
        dao.upsertBlocks(
            listOf(ScheduleBlockEntity(presetType = presetType, blockType = blockType, label = label, startTime = start, endTime = end))
        )
    }

    suspend fun deleteBlock(block: ScheduleBlockEntity) = dao.deleteBlock(block)

    suspend fun setActivePresetForToday(presetType: String) {
        dao.setActiveScheduleForDay(ActiveScheduleDayEntity(date = DateUtils.today(), presetType = presetType))
    }

    suspend fun getActivePresetForToday(): String? = dao.getActiveScheduleForDay(DateUtils.today())?.presetType

    /** Seeds each of the 3 presets with a sensible default timeline the first time it's needed. */
    suspend fun ensureDefaultPresetsSeeded() {
        SchedulePresetType.entries.forEach { preset ->
            if (dao.getPreset(preset.name) == null) {
                val blocks = DefaultSchedule.blocksFor(preset)
                val wakeTime = blocks.first { it.type == ScheduleBlockType.WAKE }.start
                val sleepTime = blocks.first { it.type == ScheduleBlockType.SLEEP }.start
                dao.upsertPreset(SchedulePresetEntity(presetType = preset.name, wakeUpTime = wakeTime, sleepTime = sleepTime))
                dao.upsertBlocks(
                    blocks.map { b ->
                        ScheduleBlockEntity(
                            presetType = preset.name,
                            blockType = b.type.name,
                            label = b.label,
                            startTime = b.start,
                            endTime = b.end
                        )
                    }
                )
            }
        }
    }
}
