package com.arcseason.app.domain

data class DefaultBlock(val type: ScheduleBlockType, val label: String, val start: String, val end: String)

/** One-time seed content for each preset's timeline — used by ScheduleRepository on first run. */
object DefaultSchedule {
    fun blocksFor(preset: SchedulePresetType): List<DefaultBlock> = when (preset) {
        SchedulePresetType.SCHOOL_DAY -> listOf(
            DefaultBlock(ScheduleBlockType.WAKE, "Wake up", "06:15", "06:30"),
            DefaultBlock(ScheduleBlockType.MEAL, "Breakfast — eggs & oats", "06:30", "06:50"),
            DefaultBlock(ScheduleBlockType.SCHOOL, "School", "07:30", "14:30"),
            DefaultBlock(ScheduleBlockType.WORKOUT, "Gym — push day", "16:00", "17:00"),
            DefaultBlock(ScheduleBlockType.MEAL, "Dinner", "18:30", "19:00"),
            DefaultBlock(ScheduleBlockType.STUDY, "Homework", "19:15", "20:15"),
            DefaultBlock(ScheduleBlockType.WIND_DOWN, "Wind down, no screens", "21:30", "22:00"),
            DefaultBlock(ScheduleBlockType.SLEEP, "Sleep", "22:00", "06:15")
        )
        SchedulePresetType.NO_SCHOOL -> listOf(
            DefaultBlock(ScheduleBlockType.WAKE, "Wake up", "08:00", "08:15"),
            DefaultBlock(ScheduleBlockType.MEAL, "Breakfast", "08:15", "08:45"),
            DefaultBlock(ScheduleBlockType.RIDE, "Bike ride", "10:00", "11:00"),
            DefaultBlock(ScheduleBlockType.MEAL, "Lunch", "12:30", "13:00"),
            DefaultBlock(ScheduleBlockType.WORKOUT, "Gym — legs", "16:00", "17:00"),
            DefaultBlock(ScheduleBlockType.MEAL, "Dinner", "18:30", "19:00"),
            DefaultBlock(ScheduleBlockType.WIND_DOWN, "Wind down", "21:30", "22:00"),
            DefaultBlock(ScheduleBlockType.SLEEP, "Sleep", "22:00", "08:00")
        )
        SchedulePresetType.WEEKEND -> listOf(
            DefaultBlock(ScheduleBlockType.WAKE, "Wake up", "08:30", "08:45"),
            DefaultBlock(ScheduleBlockType.MEAL, "Breakfast", "08:45", "09:15"),
            DefaultBlock(ScheduleBlockType.RIDE, "Long bike ride", "10:00", "11:30"),
            DefaultBlock(ScheduleBlockType.MEAL, "Lunch", "13:00", "13:30"),
            DefaultBlock(ScheduleBlockType.MEAL, "Dinner", "18:30", "19:00"),
            DefaultBlock(ScheduleBlockType.WIND_DOWN, "Wind down", "22:00", "22:30"),
            DefaultBlock(ScheduleBlockType.SLEEP, "Sleep", "22:30", "08:30")
        )
    }
}
