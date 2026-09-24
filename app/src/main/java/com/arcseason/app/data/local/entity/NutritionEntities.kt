package com.arcseason.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single food entry, whether it came from a quick-add preset, a manual
 * form, or the Arc Coach's `logNutritionEntry` tool call parsing something
 * like "I ate 3 eggs and cottage cheese."
 *
 * carbsGrams/fatGrams were added in Phase 2 so the Macros screen's carb/fat
 * progress bars have somewhere real to read from — the original schema only
 * tracked calories/protein, matching the coach's originally-specified tool
 * signature. No migration is needed yet since this project hasn't shipped.
 */
@Entity(tableName = "nutrition_logs")
data class NutritionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val timestampMillis: Long,
    val foodName: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0,
    val source: String = "MANUAL", // MANUAL | QUICK_ADD | AI_CHAT
    val isJunkFood: Boolean = false
)

/**
 * Singleton profile row (id is always 1) used by the Macros screen's targets
 * and, from Phase 3 on, a real BMR/TDEE calculator.
 *
 * Safety notes baked into this schema (see README "Teen Safety Engine"):
 *  - calorieTargetKcal is meant to be computed as max(bmrKcal, tdeeKcal -
 *    deficit) once Phase 3's calculator lands, i.e. it can never be set
 *    below BMR, matching the spec's hard floor. Phase 2 just seeds a
 *    reasonable conservative default (see NutritionRepository.defaultProfile)
 *    so the Macros screen has real targets to show before that calculator
 *    exists.
 *  - proteinTargetGrams is meant to be computed from body weight (1.6-2.2
 *    g/kg), not from calorieTargetKcal, so a lower calorie target never
 *    drags protein down with it.
 *  - guardianAwareOfGoal is a self-reported flag a future onboarding flow
 *    sets after showing a one-time note suggesting a teen loop a parent/
 *    guardian or doctor in before starting a deliberate calorie deficit. It
 *    does not gate app functionality — a 15-year-old's app shouldn't lock
 *    them out of logging food — it's there so the app can surface that
 *    reminder once rather than nagging every session.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val ageYears: Int,
    val sex: String, // "MALE" | "FEMALE" | "OTHER" — affects the BMR formula
    val heightCm: Double,
    val weightKg: Double,
    val activityLevel: String, // SEDENTARY | LIGHT | MODERATE | ACTIVE | VERY_ACTIVE
    val goal: String, // FAT_LOSS | MAINTENANCE | MUSCLE_GAIN
    val bmrKcal: Int,
    val tdeeKcal: Int,
    val calorieTargetKcal: Int,
    val proteinTargetGrams: Int,
    val carbsTargetGrams: Int = 0,
    val fatTargetGrams: Int = 0,
    val guardianAwareOfGoal: Boolean = false
)
