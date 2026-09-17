package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.RushWayAiAssistant
import com.example.calculator.CalorieCalculationResult
import com.example.calculator.CalorieCalculator
import com.example.data.RushWayDatabase
import com.example.data.model.*
import com.example.data.repository.RushWayRepository
import com.example.pdf.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RushWayViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: RushWayRepository = RushWayRepository(RushWayDatabase.getDatabase(application))

  // App Mode: Coach vs Client
  private val _isCoachMode = MutableStateFlow(true)
  val isCoachMode: StateFlow<Boolean> = _isCoachMode.asStateFlow()

  fun toggleAppMode() {
    _isCoachMode.value = !_isCoachMode.value
  }

  fun setAppMode(isCoach: Boolean) {
    _isCoachMode.value = isCoach
  }

  // Selected Client (for detailed viewing, assigning, or Client Mode session)
  private val _selectedClientId = MutableStateFlow<Long?>(null)
  val selectedClientId: StateFlow<Long?> = _selectedClientId.asStateFlow()

  fun selectClient(clientId: Long?) {
    _selectedClientId.value = clientId
  }

  // Reactive Data Streams
  val clients: StateFlow<List<ClientEntity>> = repository.allClients
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val selectedClient: StateFlow<ClientEntity?> = _selectedClientId.flatMapLatest { id ->
    if (id != null) repository.getClientById(id) else flowOf(null)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val measurements: StateFlow<List<MeasurementEntity>> = _selectedClientId.flatMapLatest { id ->
    if (id != null) repository.getMeasurementsForClient(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allFoods: StateFlow<List<FoodEntity>> = repository.allFoods
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allExercises: StateFlow<List<ExerciseEntity>> = repository.allExercises
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allPrograms: StateFlow<List<TrainingProgramEntity>> = repository.allPrograms
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allNutritionPlans: StateFlow<List<NutritionPlanEntity>> = repository.allNutritionPlans
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allCheckIns: StateFlow<List<CheckInEntity>> = repository.allCheckIns
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allReports: StateFlow<List<MonthlyReportEntity>> = repository.allReports
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allMessages: StateFlow<List<MessageEntity>> = repository.allMessages
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Selected Program for Builder
  private val _activeProgramId = MutableStateFlow<Long?>(null)
  val activeProgramId: StateFlow<Long?> = _activeProgramId.asStateFlow()

  val activeProgram: StateFlow<TrainingProgramEntity?> = _activeProgramId.flatMapLatest { id ->
    if (id != null) repository.getProgramById(id) else flowOf(null)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val activeProgramDays: StateFlow<List<TrainingDayEntity>> = _activeProgramId.flatMapLatest { id ->
    if (id != null) repository.getDaysForProgram(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setActiveProgram(id: Long?) {
    _activeProgramId.value = id
  }

  fun getExercisesForDay(dayId: Long): Flow<List<TrainingDayExerciseEntity>> {
    return repository.getExercisesForDay(dayId)
  }

  // Selected Nutrition Plan for Builder
  private val _activeNutritionPlanId = MutableStateFlow<Long?>(null)
  val activeNutritionPlanId: StateFlow<Long?> = _activeNutritionPlanId.asStateFlow()

  val activeNutritionPlan: StateFlow<NutritionPlanEntity?> = _activeNutritionPlanId.flatMapLatest { id ->
    if (id != null) repository.getNutritionPlanById(id) else flowOf(null)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val activePlanMeals: StateFlow<List<MealEntity>> = _activeNutritionPlanId.flatMapLatest { id ->
    if (id != null) repository.getMealsForPlan(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setActiveNutritionPlan(id: Long?) {
    _activeNutritionPlanId.value = id
  }

  fun getFoodsForMeal(mealId: Long): Flow<List<MealFoodEntity>> {
    return repository.getFoodsForMeal(mealId)
  }

  // Client Workout Logs
  val clientWorkoutLogs: StateFlow<List<WorkoutLogEntity>> = _selectedClientId.flatMapLatest { id ->
    if (id != null) repository.getLogsForClient(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun getLastLogForExercise(exerciseName: String): Flow<WorkoutLogEntity?> {
    val clientId = _selectedClientId.value ?: 0L
    return repository.getLastLogForExercise(clientId, exerciseName)
  }

  // Client Daily Nutrition Logs
  val clientDailyNutritionLogs: StateFlow<List<DailyNutritionLogEntity>> = _selectedClientId.flatMapLatest { id ->
    if (id != null) repository.getDailyLogsForClient(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Calorie Calculator state
  private val _calculationResult = MutableStateFlow<CalorieCalculationResult?>(null)
  val calculationResult: StateFlow<CalorieCalculationResult?> = _calculationResult.asStateFlow()

  fun runCalorieCalculation(
    weightKg: Double,
    heightCm: Double,
    age: Int,
    gender: String,
    activityLevel: String,
    goal: String,
    method: String,
    bodyFatPercent: Double? = null,
    proteinPerKg: Double = 2.2,
    fatPercent: Double = 0.25
  ) {
    val result = CalorieCalculator.calculateAll(
      weightKg = weightKg,
      heightCm = heightCm,
      age = age,
      gender = gender,
      activityLevel = activityLevel,
      primaryGoal = goal,
      formula = method,
      bodyFatPercent = bodyFatPercent,
      proteinPerKg = proteinPerKg,
      fatPercentOfCalories = fatPercent
    )
    _calculationResult.value = result
  }

  fun applyCalculationToClient(
    clientId: Long,
    calories: Int,
    protein: Int,
    carbs: Int,
    fat: Int,
    method: String
  ) {
    viewModelScope.launch {
      val existing = repository.getClientByIdDirect(clientId) ?: return@launch
      val updated = existing.copy(
        targetCalories = calories,
        targetProteinG = protein,
        targetCarbsG = carbs,
        targetFatG = fat,
        calculationMethod = method
      )
      repository.updateClient(updated)
    }
  }

  // AI Assistant State
  private val _aiResponse = MutableStateFlow<String?>(null)
  val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

  private val _isAiLoading = MutableStateFlow(false)
  val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

  fun askAi(prompt: String, contextInfo: String? = null) {
    viewModelScope.launch {
      _isAiLoading.value = true
      _aiResponse.value = null
      val response = RushWayAiAssistant.askAssistant(prompt, contextInfo)
      _aiResponse.value = response
      _isAiLoading.value = false
    }
  }

  fun clearAiResponse() {
    _aiResponse.value = null
  }

  // Client CRUD
  fun addClient(client: ClientEntity, initialWeight: Double, initialWaist: Double? = null) {
    viewModelScope.launch {
      val clientId = repository.insertClient(client)
      // Record initial measurement
      repository.insertMeasurement(
        MeasurementEntity(
          clientId = clientId,
          weightKg = initialWeight,
          waistCm = initialWaist,
          notes = "Baseline measurement at intake"
        )
      )
      _selectedClientId.value = clientId
    }
  }

  fun updateClient(client: ClientEntity) {
    viewModelScope.launch {
      repository.updateClient(client)
    }
  }

  fun deleteClient(client: ClientEntity) {
    viewModelScope.launch {
      repository.deleteClient(client)
      if (_selectedClientId.value == client.id) {
        _selectedClientId.value = null
      }
    }
  }

  // Measurement CRUD
  fun addMeasurement(measurement: MeasurementEntity) {
    viewModelScope.launch {
      repository.insertMeasurement(measurement)
      // Also update client current weight
      val client = repository.getClientByIdDirect(measurement.clientId)
      if (client != null) {
        repository.updateClient(client.copy(weightKg = measurement.weightKg))
      }
    }
  }

  fun deleteMeasurement(measurement: MeasurementEntity) {
    viewModelScope.launch {
      repository.deleteMeasurement(measurement)
    }
  }

  // Food CRUD
  fun addFood(food: FoodEntity) {
    viewModelScope.launch {
      repository.insertFood(food)
    }
  }

  fun updateFood(food: FoodEntity) {
    viewModelScope.launch {
      repository.updateFood(food)
    }
  }

  fun deleteFood(food: FoodEntity) {
    viewModelScope.launch {
      repository.deleteFood(food)
    }
  }

  // Exercise CRUD
  fun addExercise(exercise: ExerciseEntity) {
    viewModelScope.launch {
      repository.insertExercise(exercise)
    }
  }

  fun updateExercise(exercise: ExerciseEntity) {
    viewModelScope.launch {
      repository.updateExercise(exercise)
    }
  }

  fun deleteExercise(exercise: ExerciseEntity) {
    viewModelScope.launch {
      repository.deleteExercise(exercise)
    }
  }

  // Training Program Creation & Management
  fun createTrainingProgram(
    clientId: Long?,
    name: String,
    daysPerWeek: Int,
    description: String,
    dayNames: List<String>
  ) {
    viewModelScope.launch {
      val progId = repository.insertProgram(
        TrainingProgramEntity(
          clientId = clientId,
          name = name,
          daysPerWeek = daysPerWeek,
          description = description,
          isTemplate = (clientId == null)
        )
      )
      dayNames.forEachIndexed { index, dayName ->
        repository.insertDay(
          TrainingDayEntity(
            programId = progId,
            dayOrder = index + 1,
            name = dayName,
            targetMuscles = when {
              dayName.contains("Push", ignoreCase = true) -> "Chest, Shoulders, Triceps"
              dayName.contains("Pull", ignoreCase = true) -> "Back, Biceps, Rear Delts"
              dayName.contains("Legs", ignoreCase = true) -> "Quads, Hamstrings, Calves"
              dayName.contains("Upper", ignoreCase = true) -> "Upper Body Compound"
              dayName.contains("Lower", ignoreCase = true) -> "Lower Body Compound"
              else -> "Full Body"
            }
          )
        )
      }
      _activeProgramId.value = progId
    }
  }

  fun addExerciseToDay(dayId: Long, exercise: ExerciseEntity, sets: Int, reps: String, rest: String, method: String, notes: String?) {
    viewModelScope.launch {
      val existing = repository.getExercisesForDayDirect(dayId)
      repository.insertDayExercise(
        TrainingDayExerciseEntity(
          dayId = dayId,
          exerciseId = exercise.id,
          exerciseName = exercise.name,
          orderIndex = existing.size,
          sets = sets,
          reps = reps,
          restTime = rest,
          trainingMethod = method,
          notes = notes
        )
      )
    }
  }

  fun moveDayExercise(dayId: Long, item: TrainingDayExerciseEntity, moveUp: Boolean) {
    viewModelScope.launch {
      val all = repository.getExercisesForDayDirect(dayId).toMutableList()
      val idx = all.indexOfFirst { it.id == item.id }
      if (idx == -1) return@launch

      val targetIdx = if (moveUp) idx - 1 else idx + 1
      if (targetIdx in all.indices) {
        val temp = all[idx]
        all[idx] = all[targetIdx]
        all[targetIdx] = temp

        all.forEachIndexed { i, ex ->
          repository.updateDayExercise(ex.copy(orderIndex = i))
        }
      }
    }
  }

  fun deleteDayExercise(item: TrainingDayExerciseEntity) {
    viewModelScope.launch {
      repository.deleteDayExercise(item)
    }
  }

  fun deleteTrainingProgram(program: TrainingProgramEntity) {
    viewModelScope.launch {
      repository.deleteProgram(program)
      if (_activeProgramId.value == program.id) {
        _activeProgramId.value = null
      }
    }
  }

  // Nutrition Plan Creation & Management
  fun createNutritionPlan(
    clientId: Long?,
    name: String,
    targetCalories: Int,
    proteinG: Int,
    carbsG: Int,
    fatG: Int,
    mealNames: List<String>
  ) {
    viewModelScope.launch {
      val planId = repository.insertNutritionPlan(
        NutritionPlanEntity(
          clientId = clientId,
          name = name,
          targetCalories = targetCalories,
          targetProteinG = proteinG,
          targetCarbsG = carbsG,
          targetFatG = fatG,
          isTemplate = (clientId == null)
        )
      )
      mealNames.forEachIndexed { index, mName ->
        repository.insertMeal(
          MealEntity(
            planId = planId,
            orderIndex = index + 1,
            mealName = mName,
            timing = when (index) {
              0 -> "08:00 AM (Breakfast)"
              1 -> "12:30 PM (Lunch)"
              2 -> "04:30 PM (Pre/Post-Workout)"
              3 -> "07:30 PM (Dinner)"
              else -> "Flexible"
            }
          )
        )
      }
      _activeNutritionPlanId.value = planId
    }
  }

  fun addFoodToMeal(mealId: Long, food: FoodEntity, portionMultiplier: Double = 1.0) {
    viewModelScope.launch {
      val cals = food.calories * portionMultiplier
      val p = food.proteinG * portionMultiplier
      val c = food.carbsG * portionMultiplier
      val f = food.fatG * portionMultiplier

      val servingStr = if (portionMultiplier == 1.0) food.servingSize else "${(portionMultiplier * 100).toInt()}% of ${food.servingSize}"

      repository.insertMealFood(
        MealFoodEntity(
          mealId = mealId,
          foodId = food.id,
          foodName = food.name,
          quantity = servingStr,
          calories = cals,
          proteinG = p,
          carbsG = c,
          fatG = f,
          fiberG = food.fiberG?.times(portionMultiplier)
        )
      )
    }
  }

  fun deleteMealFood(item: MealFoodEntity) {
    viewModelScope.launch {
      repository.deleteMealFood(item)
    }
  }

  fun deleteNutritionPlan(plan: NutritionPlanEntity) {
    viewModelScope.launch {
      repository.deleteNutritionPlan(plan)
      if (_activeNutritionPlanId.value == plan.id) {
        _activeNutritionPlanId.value = null
      }
    }
  }

  // Workout Logging (Client Mode progressive overload)
  fun logWorkout(
    exerciseName: String,
    weightKg: Double,
    reps: Int,
    sets: Int,
    rir: Int?,
    notes: String?
  ) {
    val clientId = _selectedClientId.value ?: return
    viewModelScope.launch {
      repository.insertWorkoutLog(
        WorkoutLogEntity(
          clientId = clientId,
          exerciseName = exerciseName,
          weightKg = weightKg,
          reps = reps,
          sets = sets,
          rir = rir,
          notes = notes
        )
      )
    }
  }

  // Daily Nutrition Logging (Client Mode)
  fun logDailyNutrition(
    mealName: String,
    foodName: String,
    quantity: String,
    calories: Double,
    proteinG: Double,
    carbsG: Double,
    fatG: Double,
    waterMl: Int = 0
  ) {
    val clientId = _selectedClientId.value ?: return
    viewModelScope.launch {
      repository.insertDailyNutritionLog(
        DailyNutritionLogEntity(
          clientId = clientId,
          mealName = mealName,
          foodName = foodName,
          quantity = quantity,
          calories = calories,
          proteinG = proteinG,
          carbsG = carbsG,
          fatG = fatG,
          waterMl = waterMl
        )
      )
    }
  }

  // Check-In Submission (Client) & Review (Coach)
  fun submitCheckIn(
    clientId: Long,
    weightKg: Double,
    waistCm: Double?,
    trainingAdherence: Int,
    nutritionAdherence: Int,
    sleep: Int,
    energy: Int,
    stress: Int,
    hunger: Int,
    performance: Int,
    comments: String,
    photoUri: String? = null
  ) {
    viewModelScope.launch {
      repository.insertCheckIn(
        CheckInEntity(
          clientId = clientId,
          weightKg = weightKg,
          waistCm = waistCm,
          trainingAdherencePercent = trainingAdherence,
          nutritionAdherencePercent = nutritionAdherence,
          sleepScore = sleep,
          energyScore = energy,
          stressScore = stress,
          hungerScore = hunger,
          performanceScore = performance,
          clientComments = comments,
          photoUri = photoUri,
          status = "Pending Review"
        )
      )

      // Update client's lastCheckInDate & current weight
      val client = repository.getClientByIdDirect(clientId)
      if (client != null) {
        repository.updateClient(
          client.copy(
            weightKg = weightKg,
            lastCheckInDate = System.currentTimeMillis()
          )
        )
      }
    }
  }

  fun reviewCheckIn(
    checkIn: CheckInEntity,
    feedback: String,
    adjustments: String,
    newCalories: Int?,
    newProtein: Int?,
    newCarbs: Int?,
    newFat: Int?,
    trainingAdj: String?,
    nextDateMillis: Long?
  ) {
    viewModelScope.launch {
      val updated = checkIn.copy(
        status = "Reviewed",
        coachFeedback = feedback,
        coachAdjustments = adjustments,
        newCalorieTarget = newCalories,
        newProteinTarget = newProtein,
        newCarbsTarget = newCarbs,
        newFatTarget = newFat,
        trainingAdjustments = trainingAdj,
        nextCheckInDateMillis = nextDateMillis,
        reviewedDateMillis = System.currentTimeMillis()
      )
      repository.updateCheckIn(updated)

      // Apply new macros to client if provided
      if (newCalories != null) {
        val client = repository.getClientByIdDirect(checkIn.clientId)
        if (client != null) {
          repository.updateClient(
            client.copy(
              targetCalories = newCalories,
              targetProteinG = newProtein,
              targetCarbsG = newCarbs,
              targetFatG = newFat
            )
          )
        }
      }
    }
  }

  // Monthly Report Generator
  fun generateMonthlyReport(
    clientId: Long,
    monthYear: String,
    startingWeightKg: Double,
    currentWeightKg: Double,
    measurementChanges: String,
    trainingAdherenceAvg: Int,
    nutritionAdherenceAvg: Int,
    strengthProgress: String,
    coachObservations: String,
    nextGoals: String,
    updatedMacros: String,
    updatedTrainingPlan: String
  ) {
    viewModelScope.launch {
      val delta = currentWeightKg - startingWeightKg
      repository.insertReport(
        MonthlyReportEntity(
          clientId = clientId,
          monthYear = monthYear,
          startingWeightKg = startingWeightKg,
          currentWeightKg = currentWeightKg,
          weightChangeKg = delta,
          measurementChangesSummary = measurementChanges,
          trainingAdherenceAvg = trainingAdherenceAvg,
          nutritionAdherenceAvg = nutritionAdherenceAvg,
          strengthProgressSummary = strengthProgress,
          coachObservations = coachObservations,
          nextMonthGoals = nextGoals,
          updatedCaloriesMacros = updatedMacros,
          updatedTrainingPlan = updatedTrainingPlan
        )
      )
    }
  }

  // PDF Export Triggers
  suspend fun exportTrainingProgramPdf(context: Context, programId: Long): Intent? = withContext(Dispatchers.IO) {
    val program = repository.allPrograms.first().find { it.id == programId } ?: return@withContext null
    val clientName = if (program.clientId != null) {
      repository.getClientByIdDirect(program.clientId)?.fullName ?: "Client"
    } else {
      "Template Reference"
    }
    val days = repository.getDaysForProgramDirect(programId)
    val daysWithExercises = days.map { day ->
      val exercises = repository.getExercisesForDayDirect(day.id)
      Pair(day, exercises)
    }

    val file = PdfExporter.exportTrainingProgramPdf(context, clientName, program, daysWithExercises)
    PdfExporter.createShareIntent(context, file)
  }

  suspend fun exportNutritionPlanPdf(context: Context, planId: Long): Intent? = withContext(Dispatchers.IO) {
    val plan = repository.allNutritionPlans.first().find { it.id == planId } ?: return@withContext null
    val clientName = if (plan.clientId != null) {
      repository.getClientByIdDirect(plan.clientId)?.fullName ?: "Client"
    } else {
      "Template Reference"
    }
    val meals = repository.getMealsForPlanDirect(planId)
    val mealsWithFoods = meals.map { meal ->
      val foods = repository.getFoodsForMealDirect(meal.id)
      Pair(meal, foods)
    }

    val file = PdfExporter.exportNutritionPlanPdf(context, clientName, plan, mealsWithFoods)
    PdfExporter.createShareIntent(context, file)
  }

  suspend fun exportReportPdf(context: Context, report: MonthlyReportEntity): Intent? = withContext(Dispatchers.IO) {
    val clientName = repository.getClientByIdDirect(report.clientId)?.fullName ?: "Client"
    val file = PdfExporter.exportMonthlyReportPdf(context, clientName, report)
    PdfExporter.createShareIntent(context, file)
  }

  // Messaging
  fun sendMessage(clientId: Long, content: String, category: String = "General", language: String = "en", sender: String = "COACH") {
    viewModelScope.launch {
      repository.insertMessage(
        MessageEntity(
          clientId = clientId,
          sender = sender,
          category = category,
          language = language,
          content = content
        )
      )
    }
  }
}
