package com.arcseason.app.domain

/**
 * Mifflin-St Jeor BMR + activity-based TDEE, with the teen-safety
 * guardrails from the project brief:
 *  - calorieTarget can never drop below BMR, no matter what the goal or
 *    inputs say — the floor is applied after everything else, so it always
 *    wins.
 *  - Fat loss uses a conservative, fixed 300 kcal deficit (the cautious end
 *    of the 200-300 kcal range) rather than something aggressive.
 *  - Protein is derived from body weight (1.8 g/kg, the middle of the
 *    1.6-2.2 g/kg range), not from calorieTarget, so a lower calorie target
 *    never drags protein down with it.
 */
object BmrCalculator {

    data class Inputs(
        val ageYears: Int,
        val sex: String, // MALE | FEMALE | OTHER
        val heightCm: Double,
        val weightKg: Double,
        val activityLevel: String, // SEDENTARY | LIGHT | MODERATE | ACTIVE | VERY_ACTIVE
        val goal: String // FAT_LOSS | MAINTENANCE | MUSCLE_GAIN
    )

    data class Result(
        val bmrKcal: Int,
        val tdeeKcal: Int,
        val calorieTargetKcal: Int,
        val proteinTargetGrams: Int,
        val carbsTargetGrams: Int,
        val fatTargetGrams: Int
    )

    private const val FAT_LOSS_DEFICIT = 300
    private const val MUSCLE_GAIN_SURPLUS = 200
    private const val PROTEIN_GRAMS_PER_KG = 1.8
    private const val FAT_CALORIE_SHARE = 0.25

    private val activityMultipliers = mapOf(
        "SEDENTARY" to 1.2,
        "LIGHT" to 1.375,
        "MODERATE" to 1.55,
        "ACTIVE" to 1.725,
        "VERY_ACTIVE" to 1.9
    )

    fun calculate(inputs: Inputs): Result {
        val bmr = mifflinStJeor(inputs)
        val multiplier = activityMultipliers[inputs.activityLevel] ?: activityMultipliers.getValue("MODERATE")
        val tdee = (bmr * multiplier).toInt()

        val rawTarget = when (inputs.goal) {
            "FAT_LOSS" -> tdee - FAT_LOSS_DEFICIT
            "MUSCLE_GAIN" -> tdee + MUSCLE_GAIN_SURPLUS
            else -> tdee // MAINTENANCE
        }

        // Hard floor: the calorie target is never allowed below BMR.
        val calorieTarget = maxOf(rawTarget, bmr)

        val proteinGrams = (inputs.weightKg * PROTEIN_GRAMS_PER_KG).toInt()
        val proteinKcal = proteinGrams * 4

        val fatGrams = ((calorieTarget * FAT_CALORIE_SHARE) / 9).toInt()
        val fatKcal = fatGrams * 9

        // Carbs take whatever's left; coerced at 0 so a very low target (which
        // shouldn't happen given the BMR floor, but just in case) can't go negative.
        val carbsGrams = ((calorieTarget - proteinKcal - fatKcal) / 4).coerceAtLeast(0)

        return Result(
            bmrKcal = bmr,
            tdeeKcal = tdee,
            calorieTargetKcal = calorieTarget,
            proteinTargetGrams = proteinGrams,
            carbsTargetGrams = carbsGrams,
            fatTargetGrams = fatGrams
        )
    }

    private fun mifflinStJeor(inputs: Inputs): Int {
        // 10*weight(kg) + 6.25*height(cm) - 5*age(yr) + s
        // s = +5 (male) or -161 (female). For "OTHER" we use the midpoint
        // rather than defaulting to either sex-specific constant, since
        // guessing wrong in either direction isn't better than splitting it.
        val base = 10 * inputs.weightKg + 6.25 * inputs.heightCm - 5 * inputs.ageYears
        val sexConstant = when (inputs.sex) {
            "MALE" -> 5.0
            "FEMALE" -> -161.0
            else -> -78.0
        }
        return (base + sexConstant).toInt()
    }
}
