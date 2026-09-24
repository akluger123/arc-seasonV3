package com.arcseason.app.domain

/** What kind of block a schedule entry represents — drives both seed data and UI color/icon. */
enum class ScheduleBlockType(val displayName: String) {
    WAKE("Wake Up"),
    MEAL("Meal"),
    WORKOUT("Gym"),
    RIDE("Bike Ride"),
    SCHOOL("School"),
    STUDY("Study"),
    WIND_DOWN("Wind Down"),
    SLEEP("Sleep")
}

/** The 3 schedule modes the Arc Coach's `updateDailySchedulePreset` tool switches between. */
enum class SchedulePresetType(val displayName: String) {
    SCHOOL_DAY("School Day"),
    NO_SCHOOL("No School"),
    WEEKEND("Weekend")
}
