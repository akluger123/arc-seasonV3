package com.arcseason.app

import android.app.Application
import com.arcseason.app.data.local.ArcSeasonDatabase
import com.arcseason.app.data.repository.ArcRuleRepository
import com.arcseason.app.data.repository.NutritionRepository
import com.arcseason.app.data.repository.ScheduleRepository

class ArcSeasonApplication : Application() {
    // Simple manual DI (no Hilt) — one lazily-created database instance and
    // one repository per feature area, all read by ViewModels via
    // `(application as ArcSeasonApplication)`.
    val database: ArcSeasonDatabase by lazy { ArcSeasonDatabase.getInstance(this) }

    val arcRuleRepository: ArcRuleRepository by lazy { ArcRuleRepository(database.arcRuleDao()) }
    val nutritionRepository: NutritionRepository by lazy { NutritionRepository(database.nutritionDao()) }
    val scheduleRepository: ScheduleRepository by lazy { ScheduleRepository(database.scheduleDao()) }
}
