package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val fullName: String,
  val age: Int,
  val gender: String, // "Male", "Female", "Other"
  val heightCm: Double,
  val weightKg: Double,
  val dateOfBirth: String = "",
  val occupation: String = "",
  val activityLevel: String = "Moderately Active (1.55)",
  val sleepDurationHours: Double = 7.5,
  val dailySchedule: String = "",
  val budget: String = "",
  val notes: String = "",
  val goals: String = "Muscle gain", // Comma-separated
  val status: String = "Active", // "Active", "Inactive", "Requires Follow-up"
  val startDate: Long = System.currentTimeMillis(),
  val lastCheckInDate: Long? = null,
  val targetCalories: Int? = null,
  val targetProteinG: Int? = null,
  val targetCarbsG: Int? = null,
  val targetFatG: Int? = null,
  val calculationMethod: String? = "Mifflin-St Jeor"
)

@Entity(tableName = "measurements")
data class MeasurementEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val clientId: Long,
  val dateMillis: Long = System.currentTimeMillis(),
  val weightKg: Double,
  val waistCm: Double? = null,
  val neckCm: Double? = null,
  val chestCm: Double? = null,
  val shouldersCm: Double? = null,
  val armsCm: Double? = null,
  val forearmsCm: Double? = null,
  val hipsCm: Double? = null,
  val thighsCm: Double? = null,
  val calvesCm: Double? = null,
  val customMeasurements: String? = null, // JSON or formatted key-value string
  val notes: String? = null
)

@Entity(tableName = "foods")
data class FoodEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val category: String, // Protein, Carbohydrates, Fats, Vegetables, Fruits, Dairy, Supplements, Other
  val servingSize: String, // e.g. "100g", "1 scoop (30g)"
  val calories: Double,
  val proteinG: Double,
  val carbsG: Double,
  val fatG: Double,
  val fiberG: Double? = null,
  val isCustom: Boolean = false
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val targetMuscle: String, // Chest, Back, Quads, Hamstrings, Glutes, Shoulders, Biceps, Triceps, Abs, Calves, Full Body
  val secondaryMuscles: String = "",
  val equipment: String = "Barbell", // Barbell, Dumbbell, Cable, Machine, Bodyweight, Kettlebell, Smith Machine
  val movementPattern: String = "Compound", // Horizontal Push, Vertical Push, Horizontal Pull, Vertical Pull, Hip Hinge, Squat, Lunge, Isolation
  val instructions: String = "",
  val commonMistakes: String = "",
  val coachingCues: String = "",
  val mediaUri: String? = null,
  val isCustom: Boolean = false
)

@Entity(tableName = "training_programs")
data class TrainingProgramEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val clientId: Long? = null, // null if general template
  val name: String,
  val description: String = "",
  val daysPerWeek: Int = 4, // 3, 4, 5, 6
  val isTemplate: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "training_days")
data class TrainingDayEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val programId: Long,
  val dayOrder: Int, // 1, 2, 3...
  val name: String, // e.g. "Day 1: Upper Body Push", "Day 2: Lower Body"
  val targetMuscles: String = ""
)

@Entity(tableName = "training_day_exercises")
data class TrainingDayExerciseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val dayId: Long,
  val exerciseId: Long? = null,
  val exerciseName: String,
  val orderIndex: Int = 0,
  val sets: Int = 3,
  val reps: String = "8-10",
  val targetWeightKg: Double? = null,
  val restTime: String = "2-3 min",
  val tempo: String? = "3-0-1-0",
  val rirRpe: String? = "RIR 1-2",
  val trainingMethod: String = "Straight sets", // Straight sets, Superset, Drop set, Rest-pause, Myo-reps, Giant set, Failure set, AMRAP, Tempo training
  val notes: String? = null
)

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val clientId: Long,
  val exerciseId: Long? = null,
  val exerciseName: String,
  val dateMillis: Long = System.currentTimeMillis(),
  val weightKg: Double,
  val reps: Int,
  val sets: Int,
  val rir: Int? = null,
  val notes: String? = null
)

@Entity(tableName = "nutrition_plans")
data class NutritionPlanEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val clientId: Long? = null,
  val name: String,
  val targetCalories: Int,
  val targetProteinG: Int,
  val targetCarbsG: Int,
  val targetFatG: Int,
  val isTemplate: Boolean = false,
  val notes: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "meals")
data class MealEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val planId: Long,
  val orderIndex: Int,
  val mealName: String, // e.g. "Meal 1: Breakfast / Pre-Workout"
  val timing: String? = "08:00 AM",
  val notes: String? = null
)

@Entity(tableName = "meal_foods")
data class MealFoodEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val mealId: Long,
  val foodId: Long? = null,
  val foodName: String,
  val quantity: String = "100g",
  val calories: Double,
  val proteinG: Double,
  val carbsG: Double,
  val fatG: Double,
  val fiberG: Double? = null
)

@Entity(tableName = "daily_nutrition_logs")
data class DailyNutritionLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val clientId: Long,
  val dateMillis: Long = System.currentTimeMillis(),
  val mealName: String,
  val foodName: String,
  val quantity: String,
  val calories: Double,
  val proteinG: Double,
  val carbsG: Double,
  val fatG: Double,
  val waterMl: Int = 0
)

@Entity(tableName = "check_ins")
data class CheckInEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val clientId: Long,
  val dateMillis: Long = System.currentTimeMillis(),
  val weightKg: Double,
  val waistCm: Double? = null,
  val trainingAdherencePercent: Int = 90,
  val nutritionAdherencePercent: Int = 90,
  val sleepScore: Int = 8, // 1-10
  val energyScore: Int = 8, // 1-10
  val stressScore: Int = 4, // 1-10
  val hungerScore: Int = 5, // 1-10
  val performanceScore: Int = 8, // 1-10
  val clientComments: String = "",
  val photoUri: String? = null,
  val status: String = "Pending Review", // "Pending Review", "Reviewed"
  // Coach feedback & adjustments
  val coachFeedback: String? = null,
  val coachAdjustments: String? = null,
  val newCalorieTarget: Int? = null,
  val newProteinTarget: Int? = null,
  val newCarbsTarget: Int? = null,
  val newFatTarget: Int? = null,
  val trainingAdjustments: String? = null,
  val nextCheckInDateMillis: Long? = null,
  val reviewedDateMillis: Long? = null
)

@Entity(tableName = "monthly_reports")
data class MonthlyReportEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val clientId: Long,
  val monthYear: String, // e.g. "September 2026"
  val generatedDateMillis: Long = System.currentTimeMillis(),
  val startingWeightKg: Double,
  val currentWeightKg: Double,
  val weightChangeKg: Double,
  val measurementChangesSummary: String = "",
  val trainingAdherenceAvg: Int = 90,
  val nutritionAdherenceAvg: Int = 90,
  val strengthProgressSummary: String = "",
  val coachObservations: String = "",
  val nextMonthGoals: String = "",
  val updatedCaloriesMacros: String = "",
  val updatedTrainingPlan: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val clientId: Long,
  val sender: String = "COACH", // "COACH", "CLIENT"
  val category: String = "General", // "Check-in Feedback", "Training Instructions", "Nutrition Instructions", "Reminder", "General"
  val language: String = "en", // "en", "fr", "ar"
  val content: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "templates")
data class TemplateEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val type: String, // "TRAINING", "NUTRITION", "CHECKIN", "REPORT", "MESSAGE"
  val title: String,
  val contentJson: String = "" // Blank editable structure
)
