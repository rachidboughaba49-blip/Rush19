package com.example.ui.checkin

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
import com.example.data.model.CheckInEntity
import com.example.data.model.ClientEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RushSectionHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CheckInsScreen(
  clients: List<ClientEntity>,
  checkIns: List<CheckInEntity>,
  onReviewCheckIn: (CheckInEntity, String, String, Int?, Int?, Int?, Int?, String?, Long?) -> Unit,
  onSubmitCheckIn: (Long, Double, Double?, Int, Int, Int, Int, Int, Int, Int, String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending Review, 1: Reviewed, 2: Record New
  var reviewingCheckIn by remember { mutableStateOf<CheckInEntity?>(null) }
  var isRecordCheckInOpen by remember { mutableStateOf(false) }

  val pendingCheckIns = checkIns.filter { it.status == "Pending Review" }
  val reviewedCheckIns = checkIns.filter { it.status == "Reviewed" }
  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(RushDarkNavy)
  ) {
    // Top Bar
    Card(
      shape = RoundedCornerShape(0.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "CHECK-IN REVIEW & ADHERENCE",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Black,
              color = RushTextWhite,
              letterSpacing = 0.5.sp
            )
          )
          Text(
            text = "Weekly Metabolic & Subjective Readiness Audits",
            style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
          )
        }
        Button(
          onClick = { isRecordCheckInOpen = true },
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.defaultMinSize(minHeight = 44.dp)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("+ Record Check-In", fontWeight = FontWeight.Bold)
        }
      }
    }

    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = RushNavySurface,
      contentColor = RushPurpleAccent
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Pending Review (${pendingCheckIns.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Reviewed (${reviewedCheckIns.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
      )
    }

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      val displayList = if (selectedTab == 0) pendingCheckIns else reviewedCheckIns

      if (displayList.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.FactCheck,
          title = if (selectedTab == 0) "No Pending Check-Ins" else "No Reviewed Check-Ins",
          message = if (selectedTab == 0) "All client check-ins have been reviewed and answered by coach." else "Reviewed client check-in history will show here."
        )
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 60.dp)
        ) {
          items(displayList) { item ->
            val client = clients.find { it.id == item.clientId }

            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = RushNavySurface),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (item.status == "Pending Review") RushWarning.copy(alpha = 0.5f) else RushNavyBorder
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { reviewingCheckIn = item }
                .testTag("check_in_item_${item.id}")
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = client?.fullName ?: "Client #${item.clientId}",
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RushTextWhite)
                    )
                    Text(
                      text = "Logged: ${dateFormat.format(Date(item.dateMillis))}",
                      style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
                    )
                  }
                  Surface(
                    color = if (item.status == "Reviewed") RushSuccess.copy(alpha = 0.2f) else RushWarning.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = item.status.uppercase(),
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = if (item.status == "Reviewed") RushSuccess else RushWarning,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                      ),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(text = "Weight: ${item.weightKg} kg", style = MaterialTheme.typography.bodyMedium.copy(color = RushTextWhite, fontWeight = FontWeight.Bold))
                  if (item.waistCm != null) Text(text = "Waist: ${item.waistCm} cm", style = MaterialTheme.typography.bodyMedium.copy(color = RushTextWhite))
                  Text(text = "Training: ${item.trainingAdherencePercent}%", style = MaterialTheme.typography.bodyMedium.copy(color = RushCobalt))
                  Text(text = "Nutrition: ${item.nutritionAdherencePercent}%", style = MaterialTheme.typography.bodyMedium.copy(color = RushSuccess))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = "Readiness: Sleep ${item.sleepScore}/10 • Energy ${item.energyScore}/10 • Stress ${item.stressScore}/10 • Hunger ${item.hungerScore}/10",
                  style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
                )

                if (item.clientComments.isNotBlank()) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "Client: \"${item.clientComments}\"",
                    style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite)
                  )
                }

                if (item.coachFeedback?.isNotBlank() == true) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Surface(
                    color = RushDarkNavy,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                      Text(text = "Coach Feedback:", style = MaterialTheme.typography.labelSmall.copy(color = RushPurpleLight, fontWeight = FontWeight.Bold))
                      Text(text = item.coachFeedback, style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite))
                      if (item.newCalorieTarget != null) {
                        Text(
                          text = "Calorie Adjustment: ${item.newCalorieTarget} kcal (P: ${item.newProteinTarget}g, C: ${item.newCarbsTarget}g, F: ${item.newFatTarget}g)",
                          style = MaterialTheme.typography.bodySmall.copy(color = RushSuccess, fontSize = 11.sp)
                        )
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = { reviewingCheckIn = item },
                  colors = ButtonDefaults.buttonColors(containerColor = RushNavyElevated),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 38.dp)
                ) {
                  Text(
                    text = if (item.status == "Reviewed") "Edit Coach Review" else "Open Coach Review Protocol",
                    fontSize = 11.sp,
                    color = RushPurpleLight,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Coach Review Dialog
  if (reviewingCheckIn != null) {
    val client = clients.find { it.id == reviewingCheckIn!!.clientId }
    CoachReviewDialog(
      checkIn = reviewingCheckIn!!,
      clientName = client?.fullName ?: "Client",
      currentCalories = client?.targetCalories ?: 2400,
      onDismiss = { reviewingCheckIn = null },
      onSave = { feedback, adj, cals, p, c, f, trainAdj, nextDate ->
        onReviewCheckIn(reviewingCheckIn!!, feedback, adj, cals, p, c, f, trainAdj, nextDate)
        reviewingCheckIn = null
      }
    )
  }

  // Record Check-In Dialog (Manual Entry by Coach)
  if (isRecordCheckInOpen) {
    RecordCheckInDialog(
      clients = clients,
      onDismiss = { isRecordCheckInOpen = false },
      onSubmit = { clientId, weight, waist, trainAdh, nutrAdh, sleep, energy, stress, hunger, perf, comments ->
        onSubmitCheckIn(clientId, weight, waist, trainAdh, nutrAdh, sleep, energy, stress, hunger, perf, comments)
        isRecordCheckInOpen = false
      }
    )
  }
}

@Composable
fun CoachReviewDialog(
  checkIn: CheckInEntity,
  clientName: String,
  currentCalories: Int,
  onDismiss: () -> Unit,
  onSave: (String, String, Int?, Int?, Int?, Int?, String?, Long?) -> Unit
) {
  var feedback by remember { mutableStateOf(checkIn.coachFeedback ?: "Solid adherence this week. Your biofeedback metrics indicate strong recovery.") }
  var adjustments by remember { mutableStateOf(checkIn.coachAdjustments ?: "Maintain current targets for 7 more days.") }
  var newCaloriesStr by remember { mutableStateOf(checkIn.newCalorieTarget?.toString() ?: currentCalories.toString()) }
  var newProteinStr by remember { mutableStateOf(checkIn.newProteinTarget?.toString() ?: "180") }
  var newCarbsStr by remember { mutableStateOf(checkIn.newCarbsTarget?.toString() ?: "260") }
  var newFatStr by remember { mutableStateOf(checkIn.newFatTarget?.toString() ?: "65") }
  var trainingAdjustments by remember { mutableStateOf(checkIn.trainingAdjustments ?: "Add 1 set to compound lifts.") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .padding(16.dp)
        .testTag("coach_review_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("COACH REVIEW PROTOCOL", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))
          IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = RushTextGray)
          }
        }

        Text(
          text = "Athlete: $clientName • Logged Weight: ${checkIn.weightKg} kg (Training: ${checkIn.trainingAdherencePercent}%, Nutrition: ${checkIn.nutritionAdherencePercent}%)",
          style = MaterialTheme.typography.bodySmall.copy(color = RushPurpleLight)
        )

        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            OutlinedTextField(
              value = feedback,
              onValueChange = { feedback = it },
              label = { Text("Coach Feedback to Athlete *") },
              modifier = Modifier.fillMaxWidth().testTag("coach_feedback_input")
            )
          }
          item {
            OutlinedTextField(
              value = adjustments,
              onValueChange = { adjustments = it },
              label = { Text("Metabolic Adjustments & Observations") },
              modifier = Modifier.fillMaxWidth()
            )
          }
          item {
            Text("Adjust Macro Targets (Optional)", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              OutlinedTextField(
                value = newCaloriesStr,
                onValueChange = { newCaloriesStr = it },
                label = { Text("Calories") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = newProteinStr,
                onValueChange = { newProteinStr = it },
                label = { Text("Protein (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
            }
          }
          item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              OutlinedTextField(
                value = newCarbsStr,
                onValueChange = { newCarbsStr = it },
                label = { Text("Carbs (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = newFatStr,
                onValueChange = { newFatStr = it },
                label = { Text("Fat (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
            }
          }
          item {
            OutlinedTextField(
              value = trainingAdjustments,
              onValueChange = { trainingAdjustments = it },
              label = { Text("Training Volume / Intensity Tweaks") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Button(
          onClick = {
            val cals = newCaloriesStr.toIntOrNull()
            val p = newProteinStr.toIntOrNull()
            val c = newCarbsStr.toIntOrNull()
            val f = newFatStr.toIntOrNull()
            val nextDate = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000) // +7 days
            onSave(feedback, adjustments, cals, p, c, f, trainingAdjustments, nextDate)
          },
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
          Text("Submit Review & Update Targets", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}

// Dialog to record Check-in
@Composable
fun RecordCheckInDialog(
  clients: List<ClientEntity>,
  onDismiss: () -> Unit,
  onSubmit: (Long, Double, Double?, Int, Int, Int, Int, Int, Int, Int, String) -> Unit
) {
  var selectedClientId by remember { mutableStateOf(clients.firstOrNull()?.id) }
  var weightStr by remember { mutableStateOf("81.5") }
  var waistStr by remember { mutableStateOf("83.5") }
  var trainingAdh by remember { mutableIntStateOf(95) }
  var nutritionAdh by remember { mutableIntStateOf(90) }
  var sleep by remember { mutableIntStateOf(8) }
  var energy by remember { mutableIntStateOf(8) }
  var stress by remember { mutableIntStateOf(4) }
  var hunger by remember { mutableIntStateOf(5) }
  var performance by remember { mutableIntStateOf(8) }
  var comments by remember { mutableStateOf("Hit all target numbers. Energy felt high on leg day.") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("RECORD WEEKLY CHECK-IN", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))

        Text("Select Athlete", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it },
                label = { Text("Weight (kg) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = waistStr,
                onValueChange = { waistStr = it },
                label = { Text("Waist (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
              )
            }
          }
          item {
            Text("Training Adherence: $trainingAdh%", style = MaterialTheme.typography.bodySmall.copy(color = RushCobalt))
            Slider(
              value = trainingAdh.toFloat(),
              onValueChange = { trainingAdh = it.toInt() },
              valueRange = 0f..100f,
              steps = 20
            )
          }
          item {
            Text("Nutrition Adherence: $nutritionAdh%", style = MaterialTheme.typography.bodySmall.copy(color = RushSuccess))
            Slider(
              value = nutritionAdh.toFloat(),
              onValueChange = { nutritionAdh = it.toInt() },
              valueRange = 0f..100f,
              steps = 20
            )
          }
          item {
            Text("Subjective Readiness (1-10 Scale)", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Sleep: $sleep/10", style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite))
              Text("Energy: $energy/10", style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite))
              Text("Stress: $stress/10", style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite))
              Text("Hunger: $hunger/10", style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite))
            }
          }
          item {
            OutlinedTextField(
              value = comments,
              onValueChange = { comments = it },
              label = { Text("Athlete Comments & Feedback") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Button(
          onClick = {
            val cid = selectedClientId
            val w = weightStr.toDoubleOrNull()
            if (cid != null && w != null) {
              onSubmit(cid, w, waistStr.toDoubleOrNull(), trainingAdh, nutritionAdh, sleep, energy, stress, hunger, performance, comments)
            }
          },
          enabled = selectedClientId != null && weightStr.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
          Text("Submit Check-In", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}
