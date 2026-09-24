package com.arcseason.app.data.local.dao

import androidx.room.Delete
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.arcseason.app.data.local.entity.NutritionLogEntity
import com.arcseason.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {

    @Insert
    suspend fun insertLog(entry: NutritionLogEntity): Long

    @Update
    suspend fun updateLog(entry: NutritionLogEntity)

    @Delete
    suspend fun deleteLog(entry: NutritionLogEntity)

    @Query("SELECT * FROM nutrition_logs WHERE date = :date ORDER BY timestampMillis ASC")
    fun observeLogsForDate(date: String): Flow<List<NutritionLogEntity>>

    @Query("SELECT COALESCE(SUM(calories), 0) FROM nutrition_logs WHERE date = :date")
    fun observeTotalCaloriesForDate(date: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(proteinGrams), 0) FROM nutrition_logs WHERE date = :date")
    fun observeTotalProteinForDate(date: String): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM nutrition_logs WHERE date = :date AND isJunkFood = 1)")
    fun observeHasJunkFoodForDate(date: String): Flow<Boolean>

    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeUserProfile(): Flow<UserProfileEntity?>
}
