package com.example.ui.calculator

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
import com.example.calculator.CalorieCalculationResult
import com.example.calculator.CalorieCalculator
import com.example.data.model.ClientEntity
import com.example.ui.components.RushSectionHeader
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun CalorieCalculatorScreen(
  clients: List<ClientEntity>,
  selectedClientId: Long?,
  onApplyToClient: (Long, Int, Int, Int, Int, String) -> Unit
) {
  var selectedClient by remember(selectedClientId, clients) {
    mutableStateOf(clients.find { it.id == selectedClientId } ?: clients.firstOrNull())
  }

  var weightStr by remember(selectedClient) {
    mutableStateOf(selectedClient?.weightKg?.toString() ?: "80.0")
  }
  var heightStr by remember(selectedClient) {
    mutableStateOf(selectedClient?.heightCm?.toString() ?: "178.0")
  }
  var ageStr by remember(selectedClient) {
    mutableStateOf(selectedClient?.age?.toString() ?: "26")
  }
  var gender by remember(selectedClient) {
    mutableStateOf(selectedClient?.gender ?: "Male")
  }
  var activityLevel by remember(selectedClient) {
    mutableStateOf(selectedClient?.activityLevel ?: "Moderately Active (1.55)")
  }
  var primaryGoal by remember(selectedClient) {
    mutableStateOf(
      if (selectedClient != null && selectedClient!!.goals.isNotBlank()) {
        selectedClient!!.goals.split(",").first().trim()
      } else "Muscle gain"
    )
  }

  var calculationMethod by remember { mutableStateOf("Mifflin-St Jeor") }
  var bodyFatStr by remember { mutableStateOf("15.0") }

  // Manual Override Fields
  var isManualOverride by remember { mutableStateOf(false) }
  var overrideCaloriesStr by remember { mutableStateOf("") }
  var overrideProteinStr by remember { mutableStateOf("") }
  var overrideCarbsStr by remember { mutableStateOf("") }
  var overrideFatStr by remember { mutableStateOf("") }

  var appliedConfirmation by remember { mutableStateOf(false) }

  val formulas = listOf("Mifflin-St Jeor", "Harris-Benedict", "Katch-McArdle")
  val goals = listOf("Fat loss", "Muscle gain", "Body recomposition", "Strength", "Maintenance")
  val activityLevels = listOf(
    "Sedentary (1.2)",
    "Lightly Active (1.375)",
    "Moderately Active (1.55)",
    "Very Active (1.725)",
    "Extremely Active (1.9)"
  )

  // Compute live calculation
  val calculatedResult = remember(
    weightStr, heightStr, ageStr, gender, activityLevel, primaryGoal, calculationMethod, bodyFatStr
  ) {
    val w = weightStr.toDoubleOrNull() ?: 80.0
    val h = heightStr.toDoubleOrNull() ?: 178.0
    val a = ageStr.toIntOrNull() ?: 26
    val bf = bodyFatStr.toDoubleOrNull()

    CalorieCalculator.calculateAll(
      weightKg = w,
      heightCm = h,
      age = a,
      gender = gender,
      activityLevel = activityLevel,
      primaryGoal = primaryGoal,
      formula = calculationMethod,
      bodyFatPercent = bf
    )
  }

  // Pre-fill override fields if not yet set
  LaunchedEffect(calculatedResult) {
    if (!isManualOverride) {
      overrideCaloriesStr = calculatedResult.targetCalories.toString()
      overrideProteinStr = calculatedResult.targetProteinG.toString()
      overrideCarbsStr = calculatedResult.targetCarbsG.toString()
      overrideFatStr = calculatedResult.targetFatG.toString()
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(RushDarkNavy)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    item {
      Text(
        text = "METABOLIC & MACRONUTRIENT CALCULATOR",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Black,
          color = RushTextWhite,
          letterSpacing = 0.5.sp
        )
      )
      Text(
        text = "Scientific Basal & Total Energy Expenditure Engine",
        style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray)
      )
    }

    // Client Selector Dropdown / Row
    if (clients.isNotEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "LOAD CLIENT METRICS",
              style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(clients) { c ->
                FilterChip(
                  selected = selectedClient?.id == c.id,
                  onClick = { selectedClient = c },
                  label = { Text(c.fullName, fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RushPurpleContainer,
                    selectedLabelColor = RushPurpleLight
                  ),
                  modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                )
              }
            }
          }
        }
      }
    }

    // Formula Selection
    item {
      Text(
        text = "Calculation Formula",
        style = MaterialTheme.typography.labelMedium.copy(color = RushTextGray, fontWeight = FontWeight.Bold)
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        formulas.forEach { f ->
          Surface(
            color = if (calculationMethod == f) RushPurpleContainer else RushNavySurface,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (calculationMethod == f) RushPurpleAccent else RushNavyBorder
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { calculationMethod = f }
              .defaultMinSize(minHeight = 48.dp)
          ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
              Text(
                text = f,
                style = MaterialTheme.typography.labelMedium.copy(
                  color = if (calculationMethod == f) RushTextWhite else RushTextGray,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              )
            }
          }
        }
      }
    }

    // Input Parameters
    item {
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = RushNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "PHYSIOLOGICAL INPUTS",
            style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontWeight = FontWeight.Bold)
          )
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = weightStr,
              onValueChange = { weightStr = it },
              label = { Text("Weight (kg)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.weight(1f).testTag("calc_weight_input")
            )
            OutlinedTextField(
              value = heightStr,
              onValueChange = { heightStr = it },
              label = { Text("Height (cm)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.weight(1f)
            )
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = ageStr,
              onValueChange = { ageStr = it },
              label = { Text("Age") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = gender,
              onValueChange = { gender = it },
              label = { Text("Gender (Male/Female)") },
              modifier = Modifier.weight(1f)
            )
          }

          if (calculationMethod == "Katch-McArdle") {
            OutlinedTextField(
              value = bodyFatStr,
              onValueChange = { bodyFatStr = it },
              label = { Text("Body Fat % (Required for Katch-McArdle)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.fillMaxWidth()
            )
          }

          Text("Activity Level", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(activityLevels) { lvl ->
              FilterChip(
                selected = activityLevel == lvl,
                onClick = { activityLevel = lvl },
                label = { Text(lvl, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = RushPurpleContainer,
                  selectedLabelColor = RushPurpleLight
                ),
                modifier = Modifier.defaultMinSize(minHeight = 40.dp)
              )
            }
          }

          Text("Primary Goal", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(goals) { g ->
              FilterChip(
                selected = primaryGoal == g,
                onClick = { primaryGoal = g },
                label = { Text(g, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = RushPurpleAccent,
                  selectedLabelColor = RushTextWhite
                ),
                modifier = Modifier.defaultMinSize(minHeight = 40.dp)
              )
            }
          }
        }
      }
    }

    // Step-by-Step Breakdown Card (Requirement: Clearly show BMR -> Activity Multiplier -> TDEE -> Calorie Adjustment -> Target Calories)
    item {
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = RushNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().testTag("calc_breakdown_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "METABOLIC PATHWAY BREAKDOWN",
              style = MaterialTheme.typography.labelMedium.copy(color = RushPurpleLight, fontWeight = FontWeight.Bold)
            )
            Surface(
              color = RushPurpleContainer,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "[CALCULATED: $calculationMethod]",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = RushPurpleLight,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Flow diagram: BMR -> TDEE -> Target
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            StepItem(label = "BMR", value = "${calculatedResult.bmr.roundToInt()}", unit = "kcal")
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = RushPurpleLight, modifier = Modifier.size(16.dp))
            StepItem(label = "TDEE", value = "${calculatedResult.maintenanceCalories}", unit = "kcal")
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = RushPurpleLight, modifier = Modifier.size(16.dp))
            StepItem(
              label = "GOAL DELTA",
              value = if (calculatedResult.calorieAdjustment >= 0) "+${calculatedResult.calorieAdjustment}" else "${calculatedResult.calorieAdjustment}",
              unit = "kcal"
            )
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = RushPurpleLight, modifier = Modifier.size(16.dp))
            StepItem(label = "TARGET", value = "${calculatedResult.targetCalories}", unit = "kcal", isHighlighted = true)
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = RushNavyBorder)
          Spacer(modifier = Modifier.height(12.dp))

          // Macro Distribution
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            MacroCol("PROTEIN", "${calculatedResult.targetProteinG}g", "4 kcal/g", RushPurpleLight)
            MacroCol("CARBS", "${calculatedResult.targetCarbsG}g", "4 kcal/g", RushSuccess)
            MacroCol("FATS", "${calculatedResult.targetFatG}g", "9 kcal/g", RushWarning)
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "* Note: These values are mathematical estimates based on empirical formulas. Coach must monitor weekly body composition to make metabolic adjustments.",
            style = MaterialTheme.typography.bodySmall.copy(color = RushTextMuted, fontSize = 11.sp)
          )
        }
      }
    }

    // Manual Coach Override Section
    item {
      Card(
        shape = RoundedCornerShape(10.dp),
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
                text = "MANUAL COACH OVERRIDE",
                style = MaterialTheme.typography.labelMedium.copy(color = RushTextWhite, fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Adjust targets based on coach clinical judgment",
                style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
              )
            }
            Switch(
              checked = isManualOverride,
              onCheckedChange = { isManualOverride = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = RushPurpleAccent,
                checkedTrackColor = RushPurpleContainer
              )
            )
          }

          if (isManualOverride) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = overrideCaloriesStr,
                onValueChange = { overrideCaloriesStr = it },
                label = { Text("Calories") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = overrideProteinStr,
                onValueChange = { overrideProteinStr = it },
                label = { Text("Protein (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = overrideCarbsStr,
                onValueChange = { overrideCarbsStr = it },
                label = { Text("Carbs (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = overrideFatStr,
                onValueChange = { overrideFatStr = it },
                label = { Text("Fat (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // Apply to Client Action
    item {
      val finalCals = if (isManualOverride) (overrideCaloriesStr.toIntOrNull() ?: calculatedResult.targetCalories) else calculatedResult.targetCalories
      val finalP = if (isManualOverride) (overrideProteinStr.toIntOrNull() ?: calculatedResult.targetProteinG) else calculatedResult.targetProteinG
      val finalC = if (isManualOverride) (overrideCarbsStr.toIntOrNull() ?: calculatedResult.targetCarbsG) else calculatedResult.targetCarbsG
      val finalF = if (isManualOverride) (overrideFatStr.toIntOrNull() ?: calculatedResult.targetFatG) else calculatedResult.targetFatG

      Button(
        onClick = {
          if (selectedClient != null) {
            onApplyToClient(selectedClient!!.id, finalCals, finalP, finalC, finalF, calculationMethod)
            appliedConfirmation = true
          }
        },
        enabled = selectedClient != null,
        colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 50.dp)
          .testTag("apply_nutrition_targets_button")
      ) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (selectedClient != null) "Apply Targets to ${selectedClient!!.fullName}" else "Select a Client to Apply",
          fontWeight = FontWeight.Bold,
          color = RushTextWhite
        )
      }

      if (appliedConfirmation) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "✓ Targets successfully assigned to ${selectedClient?.fullName}!",
          style = MaterialTheme.typography.bodySmall.copy(color = RushSuccess, fontWeight = FontWeight.Bold)
        )
      }
    }
  }
}

@Composable
fun StepItem(label: String, value: String, unit: String, isHighlighted: Boolean = false) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontSize = 9.sp))
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium.copy(
        color = if (isHighlighted) RushPurpleLight else RushTextWhite,
        fontWeight = FontWeight.Black
      )
    )
    Text(text = unit, style = MaterialTheme.typography.labelSmall.copy(color = RushTextMuted, fontSize = 9.sp))
  }
}

@Composable
fun MacroCol(label: String, grams: String, calsPerG: String, color: androidx.compose.ui.graphics.Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontSize = 10.sp))
    Text(text = grams, style = MaterialTheme.typography.titleLarge.copy(color = color, fontWeight = FontWeight.Black))
    Text(text = calsPerG, style = MaterialTheme.typography.labelSmall.copy(color = RushTextMuted, fontSize = 9.sp))
  }
}
