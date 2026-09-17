package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.CalorieCalculator
import com.example.data.RushWayDatabase
import com.example.data.model.*
import com.example.data.repository.RushWayRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TheRushWayRobolectricTest {

  private lateinit var db: RushWayDatabase
  private lateinit var repository: RushWayRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, RushWayDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = RushWayRepository(db)
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun testMifflinStJeorCalculation() {
    // 80kg, 180cm, 28 years old Male
    // BMR = 10 * 80 + 6.25 * 180 - 5 * 28 + 5 = 800 + 1125 - 140 + 5 = 1790
    val result = CalorieCalculator.calculateAll(
      weightKg = 80.0,
      heightCm = 180.0,
      age = 28,
      gender = "Male",
      activityLevel = "Moderately Active (1.55)",
      primaryGoal = "Muscle gain",
      formula = "Mifflin-St Jeor"
    )

    assertEquals(1790.0, result.bmr, 1.0)
    val expectedTdee = Math.round(1790.0 * 1.55).toInt()
    assertEquals(expectedTdee, result.maintenanceCalories)
    assertEquals(expectedTdee + 350, result.targetCalories)
    assertTrue("Protein should be adequate for hypertrophy", result.targetProteinG >= 160)
    assertTrue("Carbs should be positive", result.targetCarbsG > 0)
    assertTrue("Fat should be positive", result.targetFatG > 0)
  }

  @Test
  fun testHarrisBenedictCalculation() {
    val result = CalorieCalculator.calculateAll(
      weightKg = 70.0,
      heightCm = 168.0,
      age = 26,
      gender = "Female",
      activityLevel = "Sedentary (1.2)",
      primaryGoal = "Fat loss",
      formula = "Harris-Benedict"
    )

    assertTrue(result.bmr > 1200)
    assertTrue("Target calories for fat loss must be below maintenance", result.targetCalories < result.maintenanceCalories)
    assertEquals(-450, result.calorieAdjustment)
  }

  @Test
  fun testKatchMcArdleCalculation() {
    // 80kg, 15% body fat -> LBM = 80 * 0.85 = 68kg
    // BMR = 370 + (21.6 * 68) = 370 + 1468.8 = 1838.8
    val result = CalorieCalculator.calculateAll(
      weightKg = 80.0,
      heightCm = 180.0,
      age = 30,
      gender = "Male",
      activityLevel = "Moderately Active (1.55)",
      primaryGoal = "Maintenance",
      formula = "Katch-McArdle",
      bodyFatPercent = 15.0
    )

    assertEquals(1838.8, result.bmr, 1.0)
    assertEquals(0, result.calorieAdjustment)
    assertEquals(result.maintenanceCalories, result.targetCalories)
  }

  @Test
  fun testClientLifecycleAndMeasurementSync() = runBlocking {
    // 1. Insert Client
    val client = ClientEntity(
      fullName = "Marcus Vance",
      age = 32,
      gender = "Male",
      heightCm = 183.0,
      weightKg = 92.5,
      occupation = "Executive",
      goals = "Body recomposition, Muscle gain"
    )
    val clientId = repository.insertClient(client)
    assertTrue(clientId > 0)

    val fetched = repository.getClientByIdDirect(clientId)
    assertNotNull(fetched)
    assertEquals("Marcus Vance", fetched!!.fullName)
    assertEquals(92.5, fetched.weightKg, 0.01)

    // 2. Add Measurement
    val measurement = MeasurementEntity(
      clientId = clientId,
      weightKg = 91.2,
      waistCm = 88.0,
      chestCm = 112.0,
      notes = "Week 1 Check-In"
    )
    repository.insertMeasurement(measurement)

    val clientMeasurements = repository.getMeasurementsForClient(clientId).first()
    assertEquals(1, clientMeasurements.size)
    assertEquals(91.2, clientMeasurements[0].weightKg, 0.01)
  }

  @Test
  fun testTrainingProgramAndDays() = runBlocking {
    val progId = repository.insertProgram(
      TrainingProgramEntity(
        name = "4-Day Rush Hypertrophy",
        daysPerWeek = 4,
        description = "Upper/Lower Progressive Overload"
      )
    )
    assertTrue(progId > 0)

    val dayId = repository.insertDay(
      TrainingDayEntity(
        programId = progId,
        dayOrder = 1,
        name = "Day 1: Upper Hypertrophy",
        targetMuscles = "Chest, Back, Shoulders"
      )
    )
    assertTrue(dayId > 0)

    repository.insertDayExercise(
      TrainingDayExerciseEntity(
        dayId = dayId,
        exerciseId = 1L,
        exerciseName = "Incline Dumbbell Press",
        orderIndex = 0,
        sets = 4,
        reps = "8-10",
        restTime = "2-3 min",
        trainingMethod = "Straight sets",
        notes = "3-second eccentric focus"
      )
    )

    val exercises = repository.getExercisesForDay(dayId).first()
    assertEquals(1, exercises.size)
    assertEquals("Incline Dumbbell Press", exercises[0].exerciseName)
    assertEquals(4, exercises[0].sets)
  }

  @Test
  fun testCheckInAndCoachReviewFlow() = runBlocking {
    val clientId = repository.insertClient(
      ClientEntity(
        fullName = "Elena Rostova",
        age = 27,
        gender = "Female",
        heightCm = 165.0,
        weightKg = 62.0,
        targetCalories = 2100
      )
    )

    val checkInId = repository.insertCheckIn(
      CheckInEntity(
        clientId = clientId,
        weightKg = 61.4,
        waistCm = 69.5,
        trainingAdherencePercent = 100,
        nutritionAdherencePercent = 95,
        sleepScore = 8,
        energyScore = 9,
        stressScore = 3,
        hungerScore = 4,
        performanceScore = 9,
        clientComments = "Felt energized throughout the week. Strength climbed on RDLs."
      )
    )

    val allCheckIns = repository.allCheckIns.first()
    assertEquals(1, allCheckIns.size)
    val checkIn = allCheckIns[0]
    assertEquals("Pending Review", checkIn.status)

    // Coach reviews
    val reviewed = checkIn.copy(
      status = "Reviewed",
      coachFeedback = "Outstanding progress. Keep calorie intake stable for one more week.",
      newCalorieTarget = 2150
    )
    repository.updateCheckIn(reviewed)

    val updatedCheckIns = repository.allCheckIns.first()
    assertEquals("Reviewed", updatedCheckIns[0].status)
    assertEquals(2150, updatedCheckIns[0].newCalorieTarget)
  }
}
