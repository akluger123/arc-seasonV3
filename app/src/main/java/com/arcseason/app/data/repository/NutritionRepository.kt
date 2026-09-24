package com.arcseason.app.data.repository

import com.arcseason.app.data.local.dao.NutritionDao
import com.arcseason.app.data.local.entity.NutritionLogEntity
import com.arcseason.app.data.local.entity.UserProfileEntity
import com.arcseason.app.domain.BmrCalculator
import com.arcseason.app.domain.DateUtils
import kotlinx.coroutines.flow.Flow

class NutritionRepository(private val dao: NutritionDao) {

    fun observeLogsForDate(date: String = DateUtils.today()): Flow<List<NutritionLogEntity>> =
        dao.observeLogsForDate(date)

    suspend fun logFood(
        foodName: String,
        calories: Int,
        proteinGrams: Int,
        carbsGrams: Int,
        fatGrams: Int,
        isJunkFood: Boolean,
        source: String = "MANUAL",
        date: String = DateUtils.today()
    ) {
        dao.insertLog(
            NutritionLogEntity(
                date = date,
                timestampMillis = System.currentTimeMillis(),
                foodName = foodName,
                calories = calories,
                proteinGrams = proteinGrams,
                carbsGrams = carbsGrams,
                fatGrams = fatGrams,
                source = source,
                isJunkFood = isJunkFood
            )
        )
    }

    suspend fun deleteLog(entry: NutritionLogEntity) = dao.deleteLog(entry)

    fun observeUserProfile(): Flow<UserProfileEntity?> = dao.observeUserProfile()

    /**
     * Runs [BmrCalculator] on the given inputs and saves the result as the
     * user's profile — this is what the Phase 3 onboarding flow (and its
     * "Edit profile" re-entry point) calls. Phase 2's approach of silently
     * seeding a hardcoded default profile is gone; a profile now only
     * exists once someone has actually gone through onboarding.
     */
    suspend fun saveProfile(inputs: BmrCalculator.Inputs, guardianAcknowledged: Boolean) {
        val result = BmrCalculator.calculate(inputs)
        dao.upsertUserProfile(
            UserProfileEntity(
                ageYears = inputs.ageYears,
                sex = inputs.sex,
                heightCm = inputs.heightCm,
                weightKg = inputs.weightKg,
                activityLevel = inputs.activityLevel,
                goal = inputs.goal,
                bmrKcal = result.bmrKcal,
                tdeeKcal = result.tdeeKcal,
                calorieTargetKcal = result.calorieTargetKcal,
                proteinTargetGrams = result.proteinTargetGrams,
                carbsTargetGrams = result.carbsTargetGrams,
                fatTargetGrams = result.fatTargetGrams,
                guardianAwareOfGoal = guardianAcknowledged
            )
        )
    }
}
