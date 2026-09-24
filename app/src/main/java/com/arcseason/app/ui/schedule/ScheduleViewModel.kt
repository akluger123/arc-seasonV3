package com.arcseason.app.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arcseason.app.ArcSeasonApplication
import com.arcseason.app.data.local.entity.ScheduleBlockEntity
import com.arcseason.app.domain.ScheduleBlockType
import com.arcseason.app.domain.SchedulePresetType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ArcSeasonApplication).scheduleRepository

    private val _selectedPreset = MutableStateFlow(SchedulePresetType.SCHOOL_DAY)
    val selectedPreset: StateFlow<SchedulePresetType> = _selectedPreset.asStateFlow()

    val blocks: StateFlow<List<ScheduleBlockEntity>> = _selectedPreset
        .flatMapLatest { preset -> repo.observeBlocksForPreset(preset.name) }
        .map { list -> list.sortedBy { it.startTime } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repo.ensureDefaultPresetsSeeded()
            repo.getActivePresetForToday()
                ?.let { activeName -> SchedulePresetType.entries.find { it.name == activeName } }
                ?.let { active -> _selectedPreset.value = active }
        }
    }

    fun selectPreset(preset: SchedulePresetType) {
        _selectedPreset.value = preset
        viewModelScope.launch { repo.setActivePresetForToday(preset.name) }
    }

    fun addBlock(type: ScheduleBlockType, label: String, start: String, end: String) {
        viewModelScope.launch {
            repo.addBlock(presetType = _selectedPreset.value.name, blockType = type.name, label = label, start = start, end = end)
        }
    }

    fun deleteBlock(block: ScheduleBlockEntity) {
        viewModelScope.launch { repo.deleteBlock(block) }
    }
}
