package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    ClientEntity::class,
    MeasurementEntity::class,
    FoodEntity::class,
    ExerciseEntity::class,
    TrainingProgramEntity::class,
    TrainingDayEntity::class,
    TrainingDayExerciseEntity::class,
    WorkoutLogEntity::class,
    NutritionPlanEntity::class,
    MealEntity::class,
    MealFoodEntity::class,
    DailyNutritionLogEntity::class,
    CheckInEntity::class,
    MonthlyReportEntity::class,
    MessageEntity::class,
    TemplateEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class RushWayDatabase : RoomDatabase() {

  abstract fun clientDao(): ClientDao
  abstract fun measurementDao(): MeasurementDao
  abstract fun foodDao(): FoodDao
  abstract fun exerciseDao(): ExerciseDao
  abstract fun trainingDao(): TrainingDao
  abstract fun workoutLogDao(): WorkoutLogDao
  abstract fun nutritionDao(): NutritionDao
  abstract fun dailyNutritionLogDao(): DailyNutritionLogDao
  abstract fun checkInDao(): CheckInDao
  abstract fun monthlyReportDao(): MonthlyReportDao
  abstract fun messageDao(): MessageDao
  abstract fun templateDao(): TemplateDao

  companion object {
    @Volatile
    private var INSTANCE: RushWayDatabase? = null

    fun getDatabase(context: Context): RushWayDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          RushWayDatabase::class.java,
          "rush_way_database"
        )
          .addCallback(DatabaseCallback())
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          CoroutineScope(Dispatchers.IO).launch {
            populateFoundationalLibraries(database)
          }
        }
      }

      suspend fun populateFoundationalLibraries(database: RushWayDatabase) {
        // Populate standard authentic foundational foods (USDA-verified standard values per 100g or serving)
        val initialFoods = listOf(
          FoodEntity(name = "Chicken Breast (Cooked, Skinless)", category = "Protein", servingSize = "100g", calories = 165.0, proteinG = 31.0, carbsG = 0.0, fatG = 3.6, fiberG = 0.0),
          FoodEntity(name = "Egg (Large, Whole)", category = "Protein", servingSize = "1 large (50g)", calories = 72.0, proteinG = 6.3, carbsG = 0.4, fatG = 4.8, fiberG = 0.0),
          FoodEntity(name = "Egg Whites (Liquid/Cooked)", category = "Protein", servingSize = "100g", calories = 52.0, proteinG = 11.0, carbsG = 0.7, fatG = 0.2, fiberG = 0.0),
          FoodEntity(name = "Whey Protein Isolate (Standard 90%)", category = "Supplements", servingSize = "1 scoop (30g)", calories = 110.0, proteinG = 25.0, carbsG = 1.0, fatG = 0.5, fiberG = 0.0),
          FoodEntity(name = "Lean Ground Beef (93/7)", category = "Protein", servingSize = "100g", calories = 172.0, proteinG = 24.0, carbsG = 0.0, fatG = 7.0, fiberG = 0.0),
          FoodEntity(name = "Salmon Fillet (Atlantic, Raw)", category = "Protein", servingSize = "100g", calories = 208.0, proteinG = 20.4, carbsG = 0.0, fatG = 13.4, fiberG = 0.0),
          FoodEntity(name = "Tuna (Canned in Water, Drained)", category = "Protein", servingSize = "100g", calories = 116.0, proteinG = 26.0, carbsG = 0.0, fatG = 0.8, fiberG = 0.0),
          FoodEntity(name = "White Jasmine Rice (Dry)", category = "Carbohydrates", servingSize = "100g", calories = 365.0, proteinG = 7.1, carbsG = 80.0, fatG = 0.7, fiberG = 1.3),
          FoodEntity(name = "Rolled Oats (Dry)", category = "Carbohydrates", servingSize = "100g", calories = 379.0, proteinG = 13.2, carbsG = 67.7, fatG = 6.5, fiberG = 10.1),
          FoodEntity(name = "Sweet Potato (Raw)", category = "Carbohydrates", servingSize = "100g", calories = 86.0, proteinG = 1.6, carbsG = 20.1, fatG = 0.1, fiberG = 3.0),
          FoodEntity(name = "White Potato (Raw)", category = "Carbohydrates", servingSize = "100g", calories = 77.0, proteinG = 2.0, carbsG = 17.5, fatG = 0.1, fiberG = 2.2),
          FoodEntity(name = "Extra Virgin Olive Oil", category = "Fats", servingSize = "1 tbsp (14g)", calories = 119.0, proteinG = 0.0, carbsG = 0.0, fatG = 13.5, fiberG = 0.0),
          FoodEntity(name = "Almonds (Raw, Whole)", category = "Fats", servingSize = "28g (1 oz)", calories = 164.0, proteinG = 6.0, carbsG = 6.1, fatG = 14.2, fiberG = 3.5),
          FoodEntity(name = "Broccoli (Raw)", category = "Vegetables", servingSize = "100g", calories = 34.0, proteinG = 2.8, carbsG = 6.6, fatG = 0.4, fiberG = 2.6),
          FoodEntity(name = "Baby Spinach (Fresh)", category = "Vegetables", servingSize = "100g", calories = 23.0, proteinG = 2.9, carbsG = 3.6, fatG = 0.4, fiberG = 2.2),
          FoodEntity(name = "Banana (Medium)", category = "Fruits", servingSize = "1 medium (118g)", calories = 105.0, proteinG = 1.3, carbsG = 27.0, fatG = 0.3, fiberG = 3.1),
          FoodEntity(name = "Greek Yogurt (Nonfat, Plain)", category = "Dairy", servingSize = "100g", calories = 59.0, proteinG = 10.2, carbsG = 3.6, fatG = 0.4, fiberG = 0.0)
        )
        database.foodDao().insertAll(initialFoods)

        // Populate standard foundational exercises with biomechanical descriptions and coaching cues
        val initialExercises = listOf(
          ExerciseEntity(
            name = "Barbell Bench Press",
            targetMuscle = "Chest",
            secondaryMuscles = "Triceps, Anterior Deltoid",
            equipment = "Barbell",
            movementPattern = "Horizontal Push",
            instructions = "Lie flat on the bench with eyes under the bar. Plant feet firmly. Grip bar slightly wider than shoulder width. Retract scapulae. Unrack bar and lower with control to the mid-sternum, then press upward maintaining shoulder tightness.",
            commonMistakes = "Bouncing bar off ribs; flared elbows past 90 degrees; unstable foot placement; losing scapular retraction at top.",
            coachingCues = "Bend the bar in half; drive through heels; tuck elbows 45 degrees; control the descent 3 seconds."
          ),
          ExerciseEntity(
            name = "Incline Dumbbell Press",
            targetMuscle = "Chest",
            secondaryMuscles = "Anterior Deltoid, Triceps",
            equipment = "Dumbbell",
            movementPattern = "Horizontal Push",
            instructions = "Set bench to 30 degrees. Clean dumbbells to thighs, lie back and press overhead. Lower dumbbells along a natural arc until a deep chest stretch is felt, then press together without clacking.",
            commonMistakes = "Bench set too steep (>45 deg) shifting load to deltoids; losing arch in upper back; rushing the eccentric.",
            coachingCues = "Lead with upper chest; squeeze chest at contraction; imagine pushing floor away."
          ),
          ExerciseEntity(
            name = "Barbell Back Squat",
            targetMuscle = "Quads",
            secondaryMuscles = "Glutes, Adductors, Hamstrings, Spinal Erectors",
            equipment = "Barbell",
            movementPattern = "Squat",
            instructions = "Position bar across mid-traps or rear delts. Step back with a balanced stance shoulder-width apart. Brace core with Valsalva maneuver. Break at hips and knees simultaneously to hit depth below parallel, drive up forcefully.",
            commonMistakes = "Knees caving inward; heels rising; chest collapsing forward; shallow depth.",
            coachingCues = "Screw feet into floor; spread the floor apart; chest up like a silverback gorilla; drive hips up."
          ),
          ExerciseEntity(
            name = "Romanian Deadlift (RDL)",
            targetMuscle = "Hamstrings",
            secondaryMuscles = "Glutes, Lower Back, Forearms",
            equipment = "Barbell",
            movementPattern = "Hip Hinge",
            instructions = "Stand tall holding barbell with overhand grip. Initiate movement by pushing hips backward while keeping a soft knee bend and neutral spine. Lower bar to mid-shin level until maximum hamstring tension, then drive hips forward.",
            commonMistakes = "Rounding thoracic or lumbar spine; bending knees too much turning it into a squat; hyperextending at lockout.",
            coachingCues = "Push your hips into the wall behind you; shave your shins with the bar; feel the hamstrings stretch."
          ),
          ExerciseEntity(
            name = "Barbell Bent-Over Row",
            targetMuscle = "Back",
            secondaryMuscles = "Biceps, Posterior Deltoid, Core",
            equipment = "Barbell",
            movementPattern = "Horizontal Pull",
            instructions = "Hinge forward at roughly 45 degrees with neutral spine. Pull barbell towards lower abdomen/belly button, driving elbows back. Squeeze lats and rhomboids at peak contraction, lower under control.",
            commonMistakes = "Using excessive momentum/torso swing; pulling too high to chest; rounded lower back.",
            coachingCues = "Pull with your elbows, not your hands; crush an orange between your shoulder blades."
          ),
          ExerciseEntity(
            name = "Standing Overhead Barbell Press",
            targetMuscle = "Shoulders",
            secondaryMuscles = "Triceps, Upper Chest, Core",
            equipment = "Barbell",
            movementPattern = "Vertical Push",
            instructions = "Grip barbell at shoulder width in front rack. Brace glutes and abs. Press bar straight up, tilting head back slightly to clear chin, then push head through 'window' at top lockout.",
            commonMistakes = "Excessive lumbar hyperextension; incomplete lockout; pressing bar in front of center of gravity.",
            coachingCues = "Squeeze glutes tight; punch the ceiling; push head through the window at lockout."
          ),
          ExerciseEntity(
            name = "Lat Pulldown (Neutral/Pronated)",
            targetMuscle = "Back",
            secondaryMuscles = "Biceps, Brachialis, Rear Deltoids",
            equipment = "Cable",
            movementPattern = "Vertical Pull",
            instructions = "Secure thighs under pads. Grasp bar slightly wider than shoulders. Lean back approximately 10-15 degrees. Pull bar down toward upper chest while depressing and retracting shoulder blades.",
            commonMistakes = "Swinging body backward violently; pulling behind the neck; not letting lats fully stretch at top.",
            coachingCues = "Drive elbows down into your back pockets; hold 1-second pause at chest; 3-second controlled stretch up."
          ),
          ExerciseEntity(
            name = "Dumbbell Lateral Raise",
            targetMuscle = "Shoulders",
            secondaryMuscles = "Upper Trapezius",
            equipment = "Dumbbell",
            movementPattern = "Isolation",
            instructions = "Stand or sit with slight forward lean. Raise dumbbells out to sides along scapular plane (30 deg forward) until arms are parallel to floor with palms facing down. Lower slowly.",
            commonMistakes = "Shrugging traps to move weight; swinging body; using weights too heavy for strict abduction.",
            coachingCues = "Pour out two pitchers of water; push dumbbells toward the walls, not up."
          ),
          ExerciseEntity(
            name = "Incline Dumbbell Bicep Curl",
            targetMuscle = "Biceps",
            secondaryMuscles = "Brachialis, Forearms",
            equipment = "Dumbbell",
            movementPattern = "Isolation",
            instructions = "Sit on a 60-degree incline bench with arms hanging fully extended. Curl dumbbells while supinating wrists, keeping elbows pinned back for maximum long-head stretch.",
            commonMistakes = "Elbows drifting forward; curling quickly without full extension.",
            coachingCues = "Lock elbows in place; emphasize the full stretch at bottom; turn pinky outward at top."
          ),
          ExerciseEntity(
            name = "Cable Tricep Pushdown",
            targetMuscle = "Triceps",
            secondaryMuscles = "None",
            equipment = "Cable",
            movementPattern = "Isolation",
            instructions = "Stand with slight hip hinge holding straight or V-bar. Keep elbows pinned to sides. Extend forearms downward until full tricep contraction. Control return to 90 degrees.",
            commonMistakes = "Flaring elbows; letting shoulders rise; partial lockouts.",
            coachingCues = "Pin upper arms to ribcage; snap lockouts down; resist the weight on the way up."
          )
        )
        database.exerciseDao().insertAll(initialExercises)
      }
    }
  }
}
