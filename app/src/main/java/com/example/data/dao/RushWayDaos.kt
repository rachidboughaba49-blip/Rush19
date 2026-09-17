package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
  @Query("SELECT * FROM clients ORDER BY fullName ASC")
  fun getAllClients(): Flow<List<ClientEntity>>

  @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
  fun getClientById(id: Long): Flow<ClientEntity?>

  @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
  suspend fun getClientByIdDirect(id: Long): ClientEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClient(client: ClientEntity): Long

  @Update
  suspend fun updateClient(client: ClientEntity)

  @Delete
  suspend fun deleteClient(client: ClientEntity)
}

@Dao
interface MeasurementDao {
  @Query("SELECT * FROM measurements WHERE clientId = :clientId ORDER BY dateMillis DESC")
  fun getMeasurementsForClient(clientId: Long): Flow<List<MeasurementEntity>>

  @Query("SELECT * FROM measurements WHERE clientId = :clientId ORDER BY dateMillis ASC")
  suspend fun getMeasurementsChronological(clientId: Long): List<MeasurementEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMeasurement(measurement: MeasurementEntity): Long

  @Delete
  suspend fun deleteMeasurement(measurement: MeasurementEntity)
}

@Dao
interface FoodDao {
  @Query("SELECT * FROM foods ORDER BY name ASC")
  fun getAllFoods(): Flow<List<FoodEntity>>

  @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
  fun searchFoods(query: String): Flow<List<FoodEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFood(food: FoodEntity): Long

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(foods: List<FoodEntity>)

  @Update
  suspend fun updateFood(food: FoodEntity)

  @Delete
  suspend fun deleteFood(food: FoodEntity)
}

@Dao
interface ExerciseDao {
  @Query("SELECT * FROM exercises ORDER BY name ASC")
  fun getAllExercises(): Flow<List<ExerciseEntity>>

  @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' OR targetMuscle LIKE '%' || :query || '%' ORDER BY name ASC")
  fun searchExercises(query: String): Flow<List<ExerciseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExercise(exercise: ExerciseEntity): Long

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(exercises: List<ExerciseEntity>)

  @Update
  suspend fun updateExercise(exercise: ExerciseEntity)

  @Delete
  suspend fun deleteExercise(exercise: ExerciseEntity)
}

@Dao
interface TrainingDao {
  @Query("SELECT * FROM training_programs ORDER BY createdAt DESC")
  fun getAllPrograms(): Flow<List<TrainingProgramEntity>>

  @Query("SELECT * FROM training_programs WHERE clientId = :clientId ORDER BY createdAt DESC")
  fun getProgramsForClient(clientId: Long): Flow<List<TrainingProgramEntity>>

  @Query("SELECT * FROM training_programs WHERE isTemplate = 1 ORDER BY name ASC")
  fun getTemplates(): Flow<List<TrainingProgramEntity>>

  @Query("SELECT * FROM training_programs WHERE id = :id LIMIT 1")
  fun getProgramById(id: Long): Flow<TrainingProgramEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProgram(program: TrainingProgramEntity): Long

  @Update
  suspend fun updateProgram(program: TrainingProgramEntity)

  @Delete
  suspend fun deleteProgram(program: TrainingProgramEntity)

  // Training Days
  @Query("SELECT * FROM training_days WHERE programId = :programId ORDER BY dayOrder ASC")
  fun getDaysForProgram(programId: Long): Flow<List<TrainingDayEntity>>

  @Query("SELECT * FROM training_days WHERE programId = :programId ORDER BY dayOrder ASC")
  suspend fun getDaysForProgramDirect(programId: Long): List<TrainingDayEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDay(day: TrainingDayEntity): Long

  @Delete
  suspend fun deleteDay(day: TrainingDayEntity)

  // Day Exercises
  @Query("SELECT * FROM training_day_exercises WHERE dayId = :dayId ORDER BY orderIndex ASC")
  fun getExercisesForDay(dayId: Long): Flow<List<TrainingDayExerciseEntity>>

  @Query("SELECT * FROM training_day_exercises WHERE dayId = :dayId ORDER BY orderIndex ASC")
  suspend fun getExercisesForDayDirect(dayId: Long): List<TrainingDayExerciseEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDayExercise(dayExercise: TrainingDayExerciseEntity): Long

  @Update
  suspend fun updateDayExercise(dayExercise: TrainingDayExerciseEntity)

  @Delete
  suspend fun deleteDayExercise(dayExercise: TrainingDayExerciseEntity)

  @Query("DELETE FROM training_day_exercises WHERE dayId = :dayId")
  suspend fun deleteExercisesForDay(dayId: Long)
}

@Dao
interface WorkoutLogDao {
  @Query("SELECT * FROM workout_logs WHERE clientId = :clientId ORDER BY dateMillis DESC")
  fun getLogsForClient(clientId: Long): Flow<List<WorkoutLogEntity>>

  @Query("SELECT * FROM workout_logs WHERE clientId = :clientId AND exerciseName = :exerciseName ORDER BY dateMillis DESC LIMIT 1")
  fun getLastLogForExercise(clientId: Long, exerciseName: String): Flow<WorkoutLogEntity?>

  @Query("SELECT * FROM workout_logs WHERE clientId = :clientId AND exerciseName = :exerciseName ORDER BY dateMillis DESC LIMIT 1")
  suspend fun getLastLogForExerciseDirect(clientId: Long, exerciseName: String): WorkoutLogEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWorkoutLog(log: WorkoutLogEntity): Long

  @Delete
  suspend fun deleteWorkoutLog(log: WorkoutLogEntity)
}

@Dao
interface NutritionDao {
  @Query("SELECT * FROM nutrition_plans ORDER BY createdAt DESC")
  fun getAllPlans(): Flow<List<NutritionPlanEntity>>

  @Query("SELECT * FROM nutrition_plans WHERE clientId = :clientId ORDER BY createdAt DESC")
  fun getPlansForClient(clientId: Long): Flow<List<NutritionPlanEntity>>

  @Query("SELECT * FROM nutrition_plans WHERE isTemplate = 1 ORDER BY name ASC")
  fun getTemplates(): Flow<List<NutritionPlanEntity>>

  @Query("SELECT * FROM nutrition_plans WHERE id = :id LIMIT 1")
  fun getPlanById(id: Long): Flow<NutritionPlanEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlan(plan: NutritionPlanEntity): Long

  @Update
  suspend fun updatePlan(plan: NutritionPlanEntity)

  @Delete
  suspend fun deletePlan(plan: NutritionPlanEntity)

  // Meals
  @Query("SELECT * FROM meals WHERE planId = :planId ORDER BY orderIndex ASC")
  fun getMealsForPlan(planId: Long): Flow<List<MealEntity>>

  @Query("SELECT * FROM meals WHERE planId = :planId ORDER BY orderIndex ASC")
  suspend fun getMealsForPlanDirect(planId: Long): List<MealEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMeal(meal: MealEntity): Long

  @Delete
  suspend fun deleteMeal(meal: MealEntity)

  // Meal Foods
  @Query("SELECT * FROM meal_foods WHERE mealId = :mealId ORDER BY id ASC")
  fun getFoodsForMeal(mealId: Long): Flow<List<MealFoodEntity>>

  @Query("SELECT * FROM meal_foods WHERE mealId = :mealId ORDER BY id ASC")
  suspend fun getFoodsForMealDirect(mealId: Long): List<MealFoodEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMealFood(food: MealFoodEntity): Long

  @Delete
  suspend fun deleteMealFood(food: MealFoodEntity)

  @Query("DELETE FROM meal_foods WHERE mealId = :mealId")
  suspend fun deleteFoodsForMeal(mealId: Long)
}

@Dao
interface DailyNutritionLogDao {
  @Query("SELECT * FROM daily_nutrition_logs WHERE clientId = :clientId AND dateMillis >= :startOfDay AND dateMillis <= :endOfDay ORDER BY id ASC")
  fun getLogsForDate(clientId: Long, startOfDay: Long, endOfDay: Long): Flow<List<DailyNutritionLogEntity>>

  @Query("SELECT * FROM daily_nutrition_logs WHERE clientId = :clientId ORDER BY dateMillis DESC")
  fun getLogsForClient(clientId: Long): Flow<List<DailyNutritionLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNutritionLog(log: DailyNutritionLogEntity): Long

  @Delete
  suspend fun deleteNutritionLog(log: DailyNutritionLogEntity)
}

@Dao
interface CheckInDao {
  @Query("SELECT * FROM check_ins ORDER BY dateMillis DESC")
  fun getAllCheckIns(): Flow<List<CheckInEntity>>

  @Query("SELECT * FROM check_ins WHERE clientId = :clientId ORDER BY dateMillis DESC")
  fun getCheckInsForClient(clientId: Long): Flow<List<CheckInEntity>>

  @Query("SELECT * FROM check_ins WHERE id = :id LIMIT 1")
  fun getCheckInById(id: Long): Flow<CheckInEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCheckIn(checkIn: CheckInEntity): Long

  @Update
  suspend fun updateCheckIn(checkIn: CheckInEntity)

  @Delete
  suspend fun deleteCheckIn(checkIn: CheckInEntity)
}

@Dao
interface MonthlyReportDao {
  @Query("SELECT * FROM monthly_reports ORDER BY generatedDateMillis DESC")
  fun getAllReports(): Flow<List<MonthlyReportEntity>>

  @Query("SELECT * FROM monthly_reports WHERE clientId = :clientId ORDER BY generatedDateMillis DESC")
  fun getReportsForClient(clientId: Long): Flow<List<MonthlyReportEntity>>

  @Query("SELECT * FROM monthly_reports WHERE id = :id LIMIT 1")
  fun getReportById(id: Long): Flow<MonthlyReportEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReport(report: MonthlyReportEntity): Long

  @Delete
  suspend fun deleteReport(report: MonthlyReportEntity)
}

@Dao
interface MessageDao {
  @Query("SELECT * FROM messages WHERE clientId = :clientId ORDER BY timestamp ASC")
  fun getMessagesForClient(clientId: Long): Flow<List<MessageEntity>>

  @Query("SELECT * FROM messages ORDER BY timestamp DESC")
  fun getAllMessages(): Flow<List<MessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: MessageEntity): Long
}

@Dao
interface TemplateDao {
  @Query("SELECT * FROM templates WHERE type = :type ORDER BY title ASC")
  fun getTemplatesByType(type: String): Flow<List<TemplateEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTemplate(template: TemplateEntity): Long

  @Delete
  suspend fun deleteTemplate(template: TemplateEntity)
}
