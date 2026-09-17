package com.example.data.repository

import com.example.data.RushWayDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class RushWayRepository(private val db: RushWayDatabase) {

  // Clients
  val allClients: Flow<List<ClientEntity>> = db.clientDao().getAllClients()

  fun getClientById(id: Long): Flow<ClientEntity?> = db.clientDao().getClientById(id)
  suspend fun getClientByIdDirect(id: Long): ClientEntity? = db.clientDao().getClientByIdDirect(id)
  suspend fun insertClient(client: ClientEntity): Long = db.clientDao().insertClient(client)
  suspend fun updateClient(client: ClientEntity) = db.clientDao().updateClient(client)
  suspend fun deleteClient(client: ClientEntity) = db.clientDao().deleteClient(client)

  // Measurements
  fun getMeasurementsForClient(clientId: Long): Flow<List<MeasurementEntity>> =
    db.measurementDao().getMeasurementsForClient(clientId)
  suspend fun getMeasurementsChronological(clientId: Long): List<MeasurementEntity> =
    db.measurementDao().getMeasurementsChronological(clientId)
  suspend fun insertMeasurement(measurement: MeasurementEntity): Long =
    db.measurementDao().insertMeasurement(measurement)
  suspend fun deleteMeasurement(measurement: MeasurementEntity) =
    db.measurementDao().deleteMeasurement(measurement)

  // Foods
  val allFoods: Flow<List<FoodEntity>> = db.foodDao().getAllFoods()
  fun searchFoods(query: String): Flow<List<FoodEntity>> = db.foodDao().searchFoods(query)
  suspend fun insertFood(food: FoodEntity): Long = db.foodDao().insertFood(food)
  suspend fun updateFood(food: FoodEntity) = db.foodDao().updateFood(food)
  suspend fun deleteFood(food: FoodEntity) = db.foodDao().deleteFood(food)

  // Exercises
  val allExercises: Flow<List<ExerciseEntity>> = db.exerciseDao().getAllExercises()
  fun searchExercises(query: String): Flow<List<ExerciseEntity>> = db.exerciseDao().searchExercises(query)
  suspend fun insertExercise(exercise: ExerciseEntity): Long = db.exerciseDao().insertExercise(exercise)
  suspend fun updateExercise(exercise: ExerciseEntity) = db.exerciseDao().updateExercise(exercise)
  suspend fun deleteExercise(exercise: ExerciseEntity) = db.exerciseDao().deleteExercise(exercise)

  // Training
  val allPrograms: Flow<List<TrainingProgramEntity>> = db.trainingDao().getAllPrograms()
  fun getProgramsForClient(clientId: Long): Flow<List<TrainingProgramEntity>> =
    db.trainingDao().getProgramsForClient(clientId)
  val trainingTemplates: Flow<List<TrainingProgramEntity>> = db.trainingDao().getTemplates()
  fun getProgramById(id: Long): Flow<TrainingProgramEntity?> = db.trainingDao().getProgramById(id)
  suspend fun insertProgram(program: TrainingProgramEntity): Long = db.trainingDao().insertProgram(program)
  suspend fun updateProgram(program: TrainingProgramEntity) = db.trainingDao().updateProgram(program)
  suspend fun deleteProgram(program: TrainingProgramEntity) = db.trainingDao().deleteProgram(program)

  fun getDaysForProgram(programId: Long): Flow<List<TrainingDayEntity>> =
    db.trainingDao().getDaysForProgram(programId)
  suspend fun getDaysForProgramDirect(programId: Long): List<TrainingDayEntity> =
    db.trainingDao().getDaysForProgramDirect(programId)
  suspend fun insertDay(day: TrainingDayEntity): Long = db.trainingDao().insertDay(day)
  suspend fun deleteDay(day: TrainingDayEntity) = db.trainingDao().deleteDay(day)

  fun getExercisesForDay(dayId: Long): Flow<List<TrainingDayExerciseEntity>> =
    db.trainingDao().getExercisesForDay(dayId)
  suspend fun getExercisesForDayDirect(dayId: Long): List<TrainingDayExerciseEntity> =
    db.trainingDao().getExercisesForDayDirect(dayId)
  suspend fun insertDayExercise(dayExercise: TrainingDayExerciseEntity): Long =
    db.trainingDao().insertDayExercise(dayExercise)
  suspend fun updateDayExercise(dayExercise: TrainingDayExerciseEntity) =
    db.trainingDao().updateDayExercise(dayExercise)
  suspend fun deleteDayExercise(dayExercise: TrainingDayExerciseEntity) =
    db.trainingDao().deleteDayExercise(dayExercise)

  // Workout Logs
  fun getLogsForClient(clientId: Long): Flow<List<WorkoutLogEntity>> =
    db.workoutLogDao().getLogsForClient(clientId)
  fun getLastLogForExercise(clientId: Long, exerciseName: String): Flow<WorkoutLogEntity?> =
    db.workoutLogDao().getLastLogForExercise(clientId, exerciseName)
  suspend fun getLastLogForExerciseDirect(clientId: Long, exerciseName: String): WorkoutLogEntity? =
    db.workoutLogDao().getLastLogForExerciseDirect(clientId, exerciseName)
  suspend fun insertWorkoutLog(log: WorkoutLogEntity): Long =
    db.workoutLogDao().insertWorkoutLog(log)
  suspend fun deleteWorkoutLog(log: WorkoutLogEntity) =
    db.workoutLogDao().deleteWorkoutLog(log)

  // Nutrition Plans
  val allNutritionPlans: Flow<List<NutritionPlanEntity>> = db.nutritionDao().getAllPlans()
  fun getNutritionPlansForClient(clientId: Long): Flow<List<NutritionPlanEntity>> =
    db.nutritionDao().getPlansForClient(clientId)
  val nutritionTemplates: Flow<List<NutritionPlanEntity>> = db.nutritionDao().getTemplates()
  fun getNutritionPlanById(id: Long): Flow<NutritionPlanEntity?> = db.nutritionDao().getPlanById(id)
  suspend fun insertNutritionPlan(plan: NutritionPlanEntity): Long = db.nutritionDao().insertPlan(plan)
  suspend fun updateNutritionPlan(plan: NutritionPlanEntity) = db.nutritionDao().updatePlan(plan)
  suspend fun deleteNutritionPlan(plan: NutritionPlanEntity) = db.nutritionDao().deletePlan(plan)

  fun getMealsForPlan(planId: Long): Flow<List<MealEntity>> = db.nutritionDao().getMealsForPlan(planId)
  suspend fun getMealsForPlanDirect(planId: Long): List<MealEntity> = db.nutritionDao().getMealsForPlanDirect(planId)
  suspend fun insertMeal(meal: MealEntity): Long = db.nutritionDao().insertMeal(meal)
  suspend fun deleteMeal(meal: MealEntity) = db.nutritionDao().deleteMeal(meal)

  fun getFoodsForMeal(mealId: Long): Flow<List<MealFoodEntity>> = db.nutritionDao().getFoodsForMeal(mealId)
  suspend fun getFoodsForMealDirect(mealId: Long): List<MealFoodEntity> = db.nutritionDao().getFoodsForMealDirect(mealId)
  suspend fun insertMealFood(food: MealFoodEntity): Long = db.nutritionDao().insertMealFood(food)
  suspend fun deleteMealFood(food: MealFoodEntity) = db.nutritionDao().deleteMealFood(food)

  // Daily Nutrition Logs
  fun getDailyLogsForDate(clientId: Long, startOfDay: Long, endOfDay: Long): Flow<List<DailyNutritionLogEntity>> =
    db.dailyNutritionLogDao().getLogsForDate(clientId, startOfDay, endOfDay)
  fun getDailyLogsForClient(clientId: Long): Flow<List<DailyNutritionLogEntity>> =
    db.dailyNutritionLogDao().getLogsForClient(clientId)
  suspend fun insertDailyNutritionLog(log: DailyNutritionLogEntity): Long =
    db.dailyNutritionLogDao().insertNutritionLog(log)
  suspend fun deleteDailyNutritionLog(log: DailyNutritionLogEntity) =
    db.dailyNutritionLogDao().deleteNutritionLog(log)

  // Check-Ins
  val allCheckIns: Flow<List<CheckInEntity>> = db.checkInDao().getAllCheckIns()
  fun getCheckInsForClient(clientId: Long): Flow<List<CheckInEntity>> =
    db.checkInDao().getCheckInsForClient(clientId)
  fun getCheckInById(id: Long): Flow<CheckInEntity?> = db.checkInDao().getCheckInById(id)
  suspend fun insertCheckIn(checkIn: CheckInEntity): Long = db.checkInDao().insertCheckIn(checkIn)
  suspend fun updateCheckIn(checkIn: CheckInEntity) = db.checkInDao().updateCheckIn(checkIn)
  suspend fun deleteCheckIn(checkIn: CheckInEntity) = db.checkInDao().deleteCheckIn(checkIn)

  // Monthly Reports
  val allReports: Flow<List<MonthlyReportEntity>> = db.monthlyReportDao().getAllReports()
  fun getReportsForClient(clientId: Long): Flow<List<MonthlyReportEntity>> =
    db.monthlyReportDao().getReportsForClient(clientId)
  fun getReportById(id: Long): Flow<MonthlyReportEntity?> = db.monthlyReportDao().getReportById(id)
  suspend fun insertReport(report: MonthlyReportEntity): Long = db.monthlyReportDao().insertReport(report)
  suspend fun deleteReport(report: MonthlyReportEntity) = db.monthlyReportDao().deleteReport(report)

  // Messages
  val allMessages: Flow<List<MessageEntity>> = db.messageDao().getAllMessages()
  fun getMessagesForClient(clientId: Long): Flow<List<MessageEntity>> =
    db.messageDao().getMessagesForClient(clientId)
  suspend fun insertMessage(message: MessageEntity): Long = db.messageDao().insertMessage(message)

  // Templates
  fun getTemplatesByType(type: String): Flow<List<TemplateEntity>> =
    db.templateDao().getTemplatesByType(type)
  suspend fun insertTemplate(template: TemplateEntity): Long = db.templateDao().insertTemplate(template)
  suspend fun deleteTemplate(template: TemplateEntity) = db.templateDao().deleteTemplate(template)
}
