package com.example.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(
  clients: List<ClientEntity>,
  plans: List<NutritionPlanEntity>,
  activePlanId: Long?,
  onSelectPlan: (Long?) -> Unit,
  onCreatePlan: (Long?, String, Int, Int, Int, Int, List<String>) -> Unit,
  onDeletePlan: (NutritionPlanEntity) -> Unit,
  planMeals: List<MealEntity>,
  getFoodsForMeal: (Long) -> Flow<List<MealFoodEntity>>,
  onAddFoodToMeal: (Long, FoodEntity, Double) -> Unit,
  onDeleteMealFood: (MealFoodEntity) -> Unit,
  foods: List<FoodEntity>,
  onAddFood: (FoodEntity) -> Unit,
  onDeleteFood: (FoodEntity) -> Unit,
  onExportPdf: (Long) -> Unit
) {
  var topTab by remember { mutableIntStateOf(0) } // 0: Plan Builder, 1: Food Database
  var isCreatePlanOpen by remember { mutableStateOf(false) }
  var isAddCustomFoodOpen by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(RushDarkNavy)
  ) {
    // Header & Tab Bar
    Card(
      shape = RoundedCornerShape(0.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "NUTRITION & MACRO ARCHITECTURE",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                color = RushTextWhite,
                letterSpacing = 0.5.sp
              )
            )
            Text(
              text = "Scientific Bodybuilding Fuel & Meal Design",
              style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
            )
          }
          if (topTab == 0) {
            Button(
              onClick = { isCreatePlanOpen = true },
              colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .testTag("create_nutrition_plan_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("+ Plan", fontWeight = FontWeight.Bold)
            }
          } else {
            Button(
              onClick = { isAddCustomFoodOpen = true },
              colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .testTag("add_custom_food_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("+ Custom Food", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    TabRow(
      selectedTabIndex = topTab,
      containerColor = RushNavySurface,
      contentColor = RushPurpleAccent
    ) {
      Tab(
        selected = topTab == 0,
        onClick = { topTab = 0 },
        text = { Text("Meal Plan Builder (${plans.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
      )
      Tab(
        selected = topTab == 1,
        onClick = { topTab = 1 },
        text = { Text("Food Database (${foods.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
      )
    }

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      if (topTab == 0) {
        NutritionPlanBuilderView(
          plans = plans,
          activePlanId = activePlanId,
          onSelectPlan = onSelectPlan,
          planMeals = planMeals,
          getFoodsForMeal = getFoodsForMeal,
          onAddFoodToMeal = onAddFoodToMeal,
          onDeleteMealFood = onDeleteMealFood,
          foods = foods,
          onExportPdf = onExportPdf,
          onDeletePlan = onDeletePlan
        )
      } else {
        FoodDatabaseView(
          foods = foods,
          onAddCustomFood = { isAddCustomFoodOpen = true },
          onDeleteFood = onDeleteFood,
          onAddFood = onAddFood
        )
      }
    }
  }

  // Create Plan Dialog
  if (isCreatePlanOpen) {
    CreateNutritionPlanDialog(
      clients = clients,
      onDismiss = { isCreatePlanOpen = false },
      onCreate = { clientId, name, cals, p, c, f, mealNames ->
        onCreatePlan(clientId, name, cals, p, c, f, mealNames)
        isCreatePlanOpen = false
      }
    )
  }

  // Add Custom Food Dialog
  if (isAddCustomFoodOpen) {
    AddCustomFoodDialog(
      onDismiss = { isAddCustomFoodOpen = false },
      onSave = { food ->
        onAddFood(food)
        isAddCustomFoodOpen = false
      }
    )
  }
}

@Composable
fun NutritionPlanBuilderView(
  plans: List<NutritionPlanEntity>,
  activePlanId: Long?,
  onSelectPlan: (Long?) -> Unit,
  planMeals: List<MealEntity>,
  getFoodsForMeal: (Long) -> Flow<List<MealFoodEntity>>,
  onAddFoodToMeal: (Long, FoodEntity, Double) -> Unit,
  onDeleteMealFood: (MealFoodEntity) -> Unit,
  foods: List<FoodEntity>,
  onExportPdf: (Long) -> Unit,
  onDeletePlan: (NutritionPlanEntity) -> Unit
) {
  var selectedMealForFoodAddition by remember { mutableStateOf<MealEntity?>(null) }

  val activePlan = plans.find { it.id == activePlanId } ?: plans.firstOrNull()

  if (plans.isEmpty()) {
    EmptyStateView(
      icon = Icons.Default.Restaurant,
      title = "No Nutrition Plans Built",
      message = "Construct comprehensive daily meal plans matching target calories, protein, carbohydrates, and healthy fats."
    )
  } else {
    Column(modifier = Modifier.fillMaxSize()) {
      // Plan Selector Row
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(plans) { plan ->
          val isSelected = (activePlan?.id == plan.id)
          FilterChip(
            selected = isSelected,
            onClick = { onSelectPlan(plan.id) },
            label = { Text(plan.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = RushPurpleContainer,
              selectedLabelColor = RushPurpleLight
            ),
            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (activePlan != null) {
        // Active Plan Targets Banner
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = activePlan.name,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RushTextWhite)
                )
                Text(
                  text = "Target: ${activePlan.targetCalories} kcal • P: ${activePlan.targetProteinG}g • C: ${activePlan.targetCarbsG}g • F: ${activePlan.targetFatG}g",
                  style = MaterialTheme.typography.bodySmall.copy(color = RushSuccess)
                )
              }
              Row {
                Button(
                  onClick = { onExportPdf(activePlan.id) },
                  colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier
                    .defaultMinSize(minHeight = 40.dp)
                    .testTag("export_nutrition_pdf_button")
                ) {
                  Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { onDeletePlan(activePlan) }, modifier = Modifier.size(48.dp)) {
                  Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = RushDanger)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Meals List
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(bottom = 60.dp)
        ) {
          items(planMeals) { meal ->
            val mealFoods by getFoodsForMeal(meal.id).collectAsState(initial = emptyList())
            val mealCals = mealFoods.sumOf { it.calories }.roundToInt()
            val mealP = mealFoods.sumOf { it.proteinG }.roundToInt()
            val mealC = mealFoods.sumOf { it.carbsG }.roundToInt()
            val mealF = mealFoods.sumOf { it.fatG }.roundToInt()

            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = RushNavySurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = meal.mealName.uppercase(),
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RushTextWhite)
                    )
                    Text(
                      text = "${meal.timing ?: "Anytime"} • $mealCals kcal (P: ${mealP}g, C: ${mealC}g, F: ${mealF}g)",
                      style = MaterialTheme.typography.bodySmall.copy(color = RushPurpleLight)
                    )
                  }
                  Button(
                    onClick = { selectedMealForFoodAddition = meal },
                    colors = ButtonDefaults.buttonColors(containerColor = RushNavyElevated),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                  ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = RushPurpleLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Food", fontSize = 11.sp, color = RushTextWhite)
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (mealFoods.isEmpty()) {
                  Text(
                    text = "No foods added yet. Click '+ Food' to assign items.",
                    style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
                  )
                } else {
                  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    mealFoods.forEach { foodItem ->
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .background(RushDarkNavy, RoundedCornerShape(6.dp))
                          .border(1.dp, RushNavyBorder, RoundedCornerShape(6.dp))
                          .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = foodItem.foodName,
                            style = MaterialTheme.typography.bodyMedium.copy(color = RushTextWhite, fontWeight = FontWeight.Medium)
                          )
                          Text(
                            text = "${foodItem.quantity} • ${foodItem.calories.roundToInt()} kcal (P: ${foodItem.proteinG.roundToInt()}g, C: ${foodItem.carbsG.roundToInt()}g, F: ${foodItem.fatG.roundToInt()}g)",
                            style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray, fontSize = 11.sp)
                          )
                        }
                        IconButton(
                          onClick = { onDeleteMealFood(foodItem) },
                          modifier = Modifier.size(32.dp)
                        ) {
                          Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = RushTextGray, modifier = Modifier.size(16.dp))
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // Add Food to Meal Dialog
        if (selectedMealForFoodAddition != null) {
          AddFoodToMealDialog(
            foods = foods,
            onDismiss = { selectedMealForFoodAddition = null },
            onAdd = { food, multiplier ->
              onAddFoodToMeal(selectedMealForFoodAddition!!.id, food, multiplier)
              selectedMealForFoodAddition = null
            }
          )
        }
      }
    }
  }
}

@Composable
fun FoodDatabaseView(
  foods: List<FoodEntity>,
  onAddCustomFood: () -> Unit,
  onDeleteFood: (FoodEntity) -> Unit,
  onAddFood: (FoodEntity) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("All") }

  val categories = listOf("All", "Protein", "Carbohydrates", "Fats", "Vegetables", "Fruits", "Dairy", "Supplements")

  val filtered = foods.filter { f ->
    val matchesSearch = f.name.contains(searchQuery, ignoreCase = true)
    val matchesCat = if (selectedCategory == "All") true else f.category.equals(selectedCategory, ignoreCase = true)
    matchesSearch && matchesCat
  }

  Column(modifier = Modifier.fillMaxSize()) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search food or macro source...", color = RushTextMuted) },
      leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = RushTextGray) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("food_database_search")
    )

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      items(categories) { c ->
        FilterChip(
          selected = selectedCategory == c,
          onClick = { selectedCategory = c },
          label = { Text(c, fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RushPurpleContainer,
            selectedLabelColor = RushPurpleLight
          ),
          modifier = Modifier.defaultMinSize(minHeight = 40.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 60.dp)
    ) {
      items(filtered) { food ->
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = food.name,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RushTextWhite)
                )
                if (food.isCustom) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    color = RushCobalt.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = "CUSTOM",
                      style = MaterialTheme.typography.labelSmall.copy(color = RushCobalt, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${food.servingSize} • ${food.calories.roundToInt()} kcal",
                style = MaterialTheme.typography.bodySmall.copy(color = RushPurpleLight)
              )
              Text(
                text = "P: ${food.proteinG}g • C: ${food.carbsG}g • F: ${food.fatG}g ${if (food.fiberG != null) "• Fiber: ${food.fiberG}g" else ""}",
                style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
              )
            }

            Row {
              // Duplicate food action (Section 7)
              IconButton(
                onClick = {
                  onAddFood(food.copy(id = 0, name = "${food.name} (Copy)", isCustom = true))
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = RushTextGray, modifier = Modifier.size(16.dp))
              }
              if (food.isCustom) {
                IconButton(
                  onClick = { onDeleteFood(food) },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = RushDanger, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}

// Dialog to create Nutrition Plan
@Composable
fun CreateNutritionPlanDialog(
  clients: List<ClientEntity>,
  onDismiss: () -> Unit,
  onCreate: (Long?, String, Int, Int, Int, Int, List<String>) -> Unit
) {
  var planName by remember { mutableStateOf("Hypertrophy Nutrition Plan") }
  var caloriesStr by remember { mutableStateOf("2600") }
  var proteinStr by remember { mutableStateOf("190") }
  var carbsStr by remember { mutableStateOf("280") }
  var fatStr by remember { mutableStateOf("70") }
  var selectedClientId by remember { mutableStateOf<Long?>(null) }

  val defaultMealNames = listOf(
    "Meal 1: Breakfast / Pre-Workout",
    "Meal 2: Post-Workout Anabolic Window",
    "Meal 3: Mid-Day Balanced Meal",
    "Meal 4: Evening Sustained Protein"
  )

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("CREATE NUTRITION PLAN", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))

        OutlinedTextField(
          value = planName,
          onValueChange = { planName = it },
          label = { Text("Plan Title *") },
          modifier = Modifier.fillMaxWidth().testTag("nutrition_plan_name_input")
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = caloriesStr,
            onValueChange = { caloriesStr = it },
            label = { Text("Calories") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = proteinStr,
            onValueChange = { proteinStr = it },
            label = { Text("Protein (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = carbsStr,
            onValueChange = { carbsStr = it },
            label = { Text("Carbs (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = fatStr,
            onValueChange = { fatStr = it },
            label = { Text("Fat (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )
        }

        Text("Assign to Client (Optional)", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          item {
            FilterChip(
              selected = selectedClientId == null,
              onClick = { selectedClientId = null },
              label = { Text("Template (No Client)", fontSize = 11.sp) },
              modifier = Modifier.defaultMinSize(minHeight = 40.dp)
            )
          }
          items(clients) { c ->
            FilterChip(
              selected = selectedClientId == c.id,
              onClick = { selectedClientId = c.id },
              label = { Text(c.fullName, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RushPurpleContainer,
                selectedLabelColor = RushPurpleLight
              ),
              modifier = Modifier.defaultMinSize(minHeight = 40.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
          onClick = {
            if (planName.isNotBlank()) {
              val cals = caloriesStr.toIntOrNull() ?: 2500
              val p = proteinStr.toIntOrNull() ?: 180
              val c = carbsStr.toIntOrNull() ?: 260
              val f = fatStr.toIntOrNull() ?: 70
              onCreate(selectedClientId, planName, cals, p, c, f, defaultMealNames)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
          Text("Build Nutrition Plan", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}

// Dialog to add food item to meal
@Composable
fun AddFoodToMealDialog(
  foods: List<FoodEntity>,
  onDismiss: () -> Unit,
  onAdd: (FoodEntity, Double) -> Unit
) {
  var selectedFood by remember { mutableStateOf(foods.firstOrNull()) }
  var portionMultiplierStr by remember { mutableStateOf("1.0") } // e.g. 1.5x of 100g = 150g

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.82f)
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("SELECT FOOD FOR MEAL", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))

        Text("Select Food from Database", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(foods) { f ->
            val isSelected = (selectedFood?.id == f.id)
            Surface(
              color = if (isSelected) RushPurpleContainer else RushDarkNavy,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) RushPurpleAccent else RushNavyBorder
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedFood = f }
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = f.name, style = MaterialTheme.typography.bodyMedium.copy(color = RushTextWhite, fontWeight = FontWeight.Bold))
                  Text(text = "${f.servingSize} • ${f.calories.roundToInt()} kcal (P: ${f.proteinG}g, C: ${f.carbsG}g, F: ${f.fatG}g)", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
                }
                if (isSelected) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RushPurpleLight)
                }
              }
            }
          }
        }

        OutlinedTextField(
          value = portionMultiplierStr,
          onValueChange = { portionMultiplierStr = it },
          label = { Text("Portion Multiplier (1.0 = 100%, 1.5 = 150%, 0.5 = 50%)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth()
        )

        Button(
          onClick = {
            if (selectedFood != null) {
              val mult = portionMultiplierStr.toDoubleOrNull() ?: 1.0
              onAdd(selectedFood!!, mult)
            }
          },
          enabled = selectedFood != null,
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
          Text("Add to Meal", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}

// Dialog to add custom food
@Composable
fun AddCustomFoodDialog(
  onDismiss: () -> Unit,
  onSave: (FoodEntity) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Protein") }
  var servingSize by remember { mutableStateOf("100g") }
  var caloriesStr by remember { mutableStateOf("150") }
  var proteinStr by remember { mutableStateOf("25") }
  var carbsStr by remember { mutableStateOf("0") }
  var fatStr by remember { mutableStateOf("3") }
  var fiberStr by remember { mutableStateOf("0") }

  val categories = listOf("Protein", "Carbohydrates", "Fats", "Vegetables", "Fruits", "Dairy", "Supplements", "Other")

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("ADD CUSTOM FOOD", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Food Name *") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Food Category", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(categories) { c ->
            FilterChip(
              selected = category == c,
              onClick = { category = c },
              label = { Text(c, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RushPurpleContainer,
                selectedLabelColor = RushPurpleLight
              ),
              modifier = Modifier.defaultMinSize(minHeight = 40.dp)
            )
          }
        }

        OutlinedTextField(
          value = servingSize,
          onValueChange = { servingSize = it },
          label = { Text("Serving Size (e.g. 100g, 1 scoop)") },
          modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = caloriesStr,
            onValueChange = { caloriesStr = it },
            label = { Text("Calories") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = proteinStr,
            onValueChange = { proteinStr = it },
            label = { Text("Protein (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = carbsStr,
            onValueChange = { carbsStr = it },
            label = { Text("Carbs (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = fatStr,
            onValueChange = { fatStr = it },
            label = { Text("Fat (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
        }

        Button(
          onClick = {
            if (name.isNotBlank()) {
              onSave(
                FoodEntity(
                  name = name,
                  category = category,
                  servingSize = servingSize,
                  calories = caloriesStr.toDoubleOrNull() ?: 100.0,
                  proteinG = proteinStr.toDoubleOrNull() ?: 0.0,
                  carbsG = carbsStr.toDoubleOrNull() ?: 0.0,
                  fatG = fatStr.toDoubleOrNull() ?: 0.0,
                  fiberG = fiberStr.toDoubleOrNull(),
                  isCustom = true
                )
              )
            }
          },
          enabled = name.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
          Text("Save Food to Database", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}
