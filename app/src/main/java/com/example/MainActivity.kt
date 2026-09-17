package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.calculator.CalorieCalculatorScreen
import com.example.ui.checkin.CheckInsScreen
import com.example.ui.clients.ClientsScreen
import com.example.ui.components.AiAssistantDialog
import com.example.ui.components.RushWayTopBar
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.nutrition.NutritionScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.theme.*
import com.example.ui.training.TrainingScreen
import com.example.ui.viewmodel.RushWayViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
  object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
  object Clients : Screen("clients", "Athletes", Icons.Default.People)
  object Calculator : Screen("calculator", "Calorie Calc", Icons.Default.Calculate)
  object Training : Screen("training", "Training", Icons.Default.FitnessCenter)
  object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.Restaurant)
  object CheckIns : Screen("checkins", "Check-Ins", Icons.Default.FactCheck)
  object Reports : Screen("reports", "Reports", Icons.Default.Assessment)
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      TheRushWayTheme {
        val viewModel: RushWayViewModel = viewModel()
        MainAppContent(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun MainAppContent(viewModel: RushWayViewModel) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
  var isAiDialogOpen by remember { mutableStateOf(false) }

  // State collectors from ViewModel
  val isCoachMode by viewModel.isCoachMode.collectAsStateWithLifecycle()
  val clients by viewModel.clients.collectAsStateWithLifecycle()
  val selectedClientId by viewModel.selectedClientId.collectAsStateWithLifecycle()
  val selectedClient by viewModel.selectedClient.collectAsStateWithLifecycle()
  val measurements by viewModel.measurements.collectAsStateWithLifecycle()
  val checkIns by viewModel.allCheckIns.collectAsStateWithLifecycle()
  val exercises by viewModel.allExercises.collectAsStateWithLifecycle()
  val foods by viewModel.allFoods.collectAsStateWithLifecycle()
  val trainingPrograms by viewModel.allPrograms.collectAsStateWithLifecycle()
  val activeProgramId by viewModel.activeProgramId.collectAsStateWithLifecycle()
  val programDays by viewModel.activeProgramDays.collectAsStateWithLifecycle()
  val nutritionPlans by viewModel.allNutritionPlans.collectAsStateWithLifecycle()
  val activePlanId by viewModel.activeNutritionPlanId.collectAsStateWithLifecycle()
  val planMeals by viewModel.activePlanMeals.collectAsStateWithLifecycle()
  val monthlyReports by viewModel.allReports.collectAsStateWithLifecycle()

  val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
  val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

  val navItems = listOf(
    Screen.Dashboard,
    Screen.Clients,
    Screen.Calculator,
    Screen.Training,
    Screen.Nutrition,
    Screen.CheckIns,
    Screen.Reports
  )

  Scaffold(
    topBar = {
      RushWayTopBar(
        title = "THE RUSH WAY",
        isCoachMode = isCoachMode,
        onToggleMode = { viewModel.toggleAppMode() },
        onOpenAi = { isAiDialogOpen = true }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = RushNavySurface,
        contentColor = RushPurpleAccent,
        modifier = Modifier.fillMaxWidth().testTag("bottom_nav_bar")
      ) {
        navItems.forEach { screen ->
          val isSelected = currentScreen.route == screen.route
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentScreen = screen },
            icon = {
              Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = if (isSelected) RushPurpleLight else RushTextGray
              )
            },
            label = {
              Text(
                text = screen.title,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) RushPurpleLight else RushTextGray,
                maxLines = 1
              )
            },
            colors = NavigationBarItemDefaults.colors(
              indicatorColor = RushPurpleContainer
            ),
            modifier = Modifier.testTag("nav_item_${screen.route}")
          )
        }
      }
    },
    containerColor = RushDarkNavy,
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        Screen.Dashboard -> {
          DashboardScreen(
            clients = clients,
            checkIns = checkIns,
            reports = monthlyReports,
            onNavigateToClients = { currentScreen = Screen.Clients },
            onNavigateToTraining = { currentScreen = Screen.Training },
            onNavigateToNutrition = { currentScreen = Screen.Nutrition },
            onNavigateToCheckIns = { currentScreen = Screen.CheckIns },
            onNavigateToReports = { currentScreen = Screen.Reports },
            onNavigateToCalculator = { currentScreen = Screen.Calculator },
            onSelectClient = { cid ->
              viewModel.selectClient(cid)
              currentScreen = Screen.Clients
            },
            onOpenAddClient = { currentScreen = Screen.Clients }
          )
        }
        Screen.Clients -> {
          ClientsScreen(
            clients = clients,
            selectedClientId = selectedClientId,
            onSelectClient = { cid -> viewModel.selectClient(cid) },
            onAddClient = { client, weight, waist ->
              viewModel.addClient(client, weight, waist)
            },
            onUpdateClient = { client -> viewModel.updateClient(client) },
            onDeleteClient = { client -> viewModel.deleteClient(client) },
            onAddMeasurement = { m -> viewModel.addMeasurement(m) },
            measurements = measurements,
            trainingPrograms = trainingPrograms,
            nutritionPlans = nutritionPlans,
            checkIns = checkIns,
            onNavigateToCalculator = { currentScreen = Screen.Calculator }
          )
        }
        Screen.Calculator -> {
          CalorieCalculatorScreen(
            clients = clients,
            selectedClientId = selectedClientId,
            onApplyToClient = { cid, cals, p, c, f, formula ->
              viewModel.applyCalculationToClient(cid, cals, p, c, f, formula)
            }
          )
        }
        Screen.Training -> {
          TrainingScreen(
            clients = clients,
            programs = trainingPrograms,
            activeProgramId = activeProgramId,
            onSelectProgram = { pid -> viewModel.setActiveProgram(pid) },
            onCreateProgram = { cid, name, days, desc, dayNames ->
              viewModel.createTrainingProgram(cid, name, days, desc, dayNames)
            },
            onDeleteProgram = { prog ->
              viewModel.deleteTrainingProgram(prog)
            },
            programDays = programDays,
            getExercisesForDay = { dayId -> viewModel.getExercisesForDay(dayId) },
            onAddExerciseToDay = { dayId, ex, sets, reps, rest, method, notes ->
              viewModel.addExerciseToDay(dayId, ex, sets, reps, rest, method, notes)
            },
            onMoveDayExercise = { dayId, item, up ->
              viewModel.moveDayExercise(dayId, item, up)
            },
            onDeleteDayExercise = { item -> viewModel.deleteDayExercise(item) },
            exercises = exercises,
            onAddExercise = { ex -> viewModel.addExercise(ex) },
            onExportPdf = { pid ->
              coroutineScope.launch {
                val intent = viewModel.exportTrainingProgramPdf(context, pid)
                if (intent != null) {
                  try {
                    context.startActivity(intent)
                  } catch (e: Exception) {
                    Toast.makeText(context, "PDF saved to Documents", Toast.LENGTH_SHORT).show()
                  }
                } else {
                  Toast.makeText(context, "Failed to generate Training PDF", Toast.LENGTH_SHORT).show()
                }
              }
            }
          )
        }
        Screen.Nutrition -> {
          NutritionScreen(
            clients = clients,
            plans = nutritionPlans,
            activePlanId = activePlanId,
            onSelectPlan = { pid -> viewModel.setActiveNutritionPlan(pid) },
            onCreatePlan = { cid, name, cals, p, c, f, mealNames ->
              viewModel.createNutritionPlan(cid, name, cals, p, c, f, mealNames)
            },
            onDeletePlan = { plan ->
              viewModel.deleteNutritionPlan(plan)
            },
            planMeals = planMeals,
            getFoodsForMeal = { mealId -> viewModel.getFoodsForMeal(mealId) },
            onAddFoodToMeal = { mealId, food, mult ->
              viewModel.addFoodToMeal(mealId, food, mult)
            },
            onDeleteMealFood = { item -> viewModel.deleteMealFood(item) },
            foods = foods,
            onAddFood = { food -> viewModel.addFood(food) },
            onDeleteFood = { food -> viewModel.deleteFood(food) },
            onExportPdf = { pid ->
              coroutineScope.launch {
                val intent = viewModel.exportNutritionPlanPdf(context, pid)
                if (intent != null) {
                  try {
                    context.startActivity(intent)
                  } catch (e: Exception) {
                    Toast.makeText(context, "PDF saved to Documents", Toast.LENGTH_SHORT).show()
                  }
                } else {
                  Toast.makeText(context, "Failed to generate Nutrition PDF", Toast.LENGTH_SHORT).show()
                }
              }
            }
          )
        }
        Screen.CheckIns -> {
          CheckInsScreen(
            clients = clients,
            checkIns = checkIns,
            onReviewCheckIn = { ci, feedback, adj, cals, p, c, f, trAdj, nextDate ->
              viewModel.reviewCheckIn(ci, feedback, adj, cals, p, c, f, trAdj, nextDate)
            },
            onSubmitCheckIn = { cid, w, waist, trAdh, nutAdh, slp, nrg, strs, hngr, perf, cmts ->
              viewModel.submitCheckIn(cid, w, waist, trAdh, nutAdh, slp, nrg, strs, hngr, perf, cmts)
            }
          )
        }
        Screen.Reports -> {
          ReportsScreen(
            clients = clients,
            selectedClientId = selectedClientId,
            onSelectClient = { cid -> viewModel.selectClient(cid) },
            measurements = measurements,
            checkIns = checkIns,
            reports = monthlyReports,
            onGenerateReport = { cid, my, sw, cw, ms, ta, na, ss, obs, g, mac, tp ->
              viewModel.generateMonthlyReport(cid, my, sw, cw, ms, ta, na, ss, obs, g, mac, tp)
            },
            onExportReportPdf = { report ->
              coroutineScope.launch {
                val intent = viewModel.exportReportPdf(context, report)
                if (intent != null) {
                  try {
                    context.startActivity(intent)
                  } catch (e: Exception) {
                    Toast.makeText(context, "PDF saved to Documents", Toast.LENGTH_SHORT).show()
                  }
                } else {
                  Toast.makeText(context, "Failed to generate Report PDF", Toast.LENGTH_SHORT).show()
                }
              }
            }
          )
        }
      }
    }
  }

  // AI Assistant Dialog (Coach Co-Pilot)
  AiAssistantDialog(
    isOpen = isAiDialogOpen,
    onDismiss = { isAiDialogOpen = false },
    selectedClient = selectedClient,
    isLoading = isAiLoading,
    aiResponse = aiResponse,
    onAskAi = { prompt, ctx -> viewModel.askAi(prompt, ctx) },
    onClearResponse = { viewModel.clearAiResponse() }
  )
}
