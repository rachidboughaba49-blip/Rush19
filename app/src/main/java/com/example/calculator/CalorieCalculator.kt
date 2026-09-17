package com.example.calculator

import kotlin.math.roundToInt

data class CalorieCalculationResult(
  val formulaName: String,
  val bmr: Double,
  val activityMultiplier: Double,
  val tdee: Double,
  val maintenanceCalories: Int,
  val calorieAdjustment: Int,
  val targetCalories: Int,
  val targetProteinG: Int,
  val targetCarbsG: Int,
  val targetFatG: Int,
  val calculationExplanation: String
)

object CalorieCalculator {

  fun calculateBmrMifflinStJeor(
    weightKg: Double,
    heightCm: Double,
    age: Int,
    isMale: Boolean
  ): Double {
    return if (isMale) {
      (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0
    } else {
      (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0
    }
  }

  fun calculateBmrHarrisBenedict(
    weightKg: Double,
    heightCm: Double,
    age: Int,
    isMale: Boolean
  ): Double {
    return if (isMale) {
      88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age)
    } else {
      447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age)
    }
  }

  fun calculateBmrKatchMcArdle(
    weightKg: Double,
    bodyFatPercent: Double
  ): Double {
    val leanBodyMassKg = weightKg * (1.0 - (bodyFatPercent / 100.0))
    return 370.0 + (21.6 * leanBodyMassKg)
  }

  fun getActivityMultiplier(level: String): Double {
    return when {
      level.startsWith("Sedentary") -> 1.2
      level.startsWith("Lightly") -> 1.375
      level.startsWith("Moderately") -> 1.55
      level.startsWith("Very") -> 1.725
      level.startsWith("Extremely") -> 1.9
      else -> 1.55
    }
  }

  fun getGoalCalorieAdjustment(goal: String): Int {
    return when {
      goal.contains("Fat loss", ignoreCase = true) -> -450
      goal.contains("Muscle gain", ignoreCase = true) -> 350
      goal.contains("Body recomposition", ignoreCase = true) -> 0
      goal.contains("Strength", ignoreCase = true) -> 200
      goal.contains("Maintenance", ignoreCase = true) -> 0
      else -> 0
    }
  }

  fun calculateAll(
    weightKg: Double,
    heightCm: Double,
    age: Int,
    gender: String,
    activityLevel: String,
    primaryGoal: String,
    formula: String = "Mifflin-St Jeor",
    bodyFatPercent: Double? = null,
    proteinPerKg: Double = 2.2, // standard bodybuilding coach benchmark: 2.2g/kg (1g/lb)
    fatPercentOfCalories: Double = 0.25 // standard: 25% of calories from healthy fats
  ): CalorieCalculationResult {
    val isMale = !gender.equals("Female", ignoreCase = true)

    val bmr = when (formula) {
      "Harris-Benedict" -> calculateBmrHarrisBenedict(weightKg, heightCm, age, isMale)
      "Katch-McArdle" -> {
        if (bodyFatPercent != null && bodyFatPercent > 0) {
          calculateBmrKatchMcArdle(weightKg, bodyFatPercent)
        } else {
          calculateBmrMifflinStJeor(weightKg, heightCm, age, isMale)
        }
      }
      else -> calculateBmrMifflinStJeor(weightKg, heightCm, age, isMale)
    }

    val multiplier = getActivityMultiplier(activityLevel)
    val tdee = bmr * multiplier
    val maintenance = tdee.roundToInt()
    val adjustment = getGoalCalorieAdjustment(primaryGoal)
    val targetCalories = (maintenance + adjustment).coerceAtLeast(1200)

    // Macro breakdown
    val proteinG = (weightKg * proteinPerKg).roundToInt()
    val proteinCals = proteinG * 4

    val fatCals = (targetCalories * fatPercentOfCalories).roundToInt()
    val fatG = (fatCals / 9.0).roundToInt()

    val remainingCalsForCarbs = (targetCalories - proteinCals - (fatG * 9)).coerceAtLeast(0)
    val carbsG = (remainingCalsForCarbs / 4.0).roundToInt()

    val explanation = "Calculated using $formula formula:\n" +
        "• BMR: ${bmr.roundToInt()} kcal\n" +
        "• Activity Multiplier: ×$multiplier ($activityLevel)\n" +
        "• Estimated TDEE: $maintenance kcal\n" +
        "• Goal Adjustment ($primaryGoal): ${if (adjustment >= 0) "+$adjustment" else "$adjustment"} kcal\n" +
        "• Calculated Target: $targetCalories kcal (P: ${proteinG}g, C: ${carbsG}g, F: ${fatG}g)\n" +
        "*(Note: Estimates for coaching guidance only; verify with metabolic feedback)*"

    return CalorieCalculationResult(
      formulaName = formula,
      bmr = bmr,
      activityMultiplier = multiplier,
      tdee = tdee,
      maintenanceCalories = maintenance,
      calorieAdjustment = adjustment,
      targetCalories = targetCalories,
      targetProteinG = proteinG,
      targetCarbsG = carbsG,
      targetFatG = fatG,
      calculationExplanation = explanation
    )
  }
}
