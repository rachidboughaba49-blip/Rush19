package com.example.ui.reports

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
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricStatCard
import com.example.ui.components.RushSectionHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(
  clients: List<ClientEntity>,
  selectedClientId: Long?,
  onSelectClient: (Long?) -> Unit,
  measurements: List<MeasurementEntity>,
  checkIns: List<CheckInEntity>,
  reports: List<MonthlyReportEntity>,
  onGenerateReport: (Long, String, Double, Double, String, Int, Int, String, String, String, String, String) -> Unit,
  onExportReportPdf: (MonthlyReportEntity) -> Unit
) {
  var topTab by remember { mutableIntStateOf(0) } // 0: Visual Progress, 1: Monthly PDF Reports
  var timeFilter by remember { mutableStateOf("30 Days") }
  var isCreateReportOpen by remember { mutableStateOf(false) }

  val activeClient = clients.find { it.id == selectedClientId } ?: clients.firstOrNull()
  val clientReports = if (activeClient != null) reports.filter { it.clientId == activeClient.id } else reports
  val clientMeasurements = if (activeClient != null) measurements.filter { it.clientId == activeClient.id } else measurements
  val clientCheckIns = if (activeClient != null) checkIns.filter { it.clientId == activeClient.id } else checkIns

  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(RushDarkNavy)
  ) {
    // Header Banner
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
            text = "PROGRESS TRACKING & REPORTS",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Black,
              color = RushTextWhite,
              letterSpacing = 0.5.sp
            )
          )
          Text(
            text = "Longitudinal Overload, Anthropometry & Reports",
            style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
          )
        }
        if (topTab == 1) {
          Button(
            onClick = { isCreateReportOpen = true },
            colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .defaultMinSize(minHeight = 44.dp)
              .testTag("generate_report_button")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("+ New Report", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Client Selector Bar
    if (clients.isNotEmpty()) {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .background(RushDarkNavy)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        items(clients) { c ->
          FilterChip(
            selected = activeClient?.id == c.id,
            onClick = { onSelectClient(c.id) },
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

    TabRow(
      selectedTabIndex = topTab,
      containerColor = RushNavySurface,
      contentColor = RushPurpleAccent
    ) {
      Tab(
        selected = topTab == 0,
        onClick = { topTab = 0 },
        text = { Text("Progress Visuals", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
      )
      Tab(
        selected = topTab == 1,
        onClick = { topTab = 1 },
        text = { Text("Monthly Reports (${clientReports.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
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
        ProgressVisualsView(
          client = activeClient,
          measurements = clientMeasurements,
          checkIns = clientCheckIns,
          timeFilter = timeFilter,
          onTimeFilterChange = { timeFilter = it }
        )
      } else {
        MonthlyReportsListView(
          reports = clientReports,
          clientName = activeClient?.fullName ?: "All Clients",
          onExportPdf = onExportReportPdf,
          onOpenCreate = { isCreateReportOpen = true }
        )
      }
    }
  }

  // Create Monthly Report Dialog
  if (isCreateReportOpen && activeClient != null) {
    val initialWeight = clientMeasurements.lastOrNull()?.weightKg ?: activeClient.weightKg
    val currentWeight = clientMeasurements.firstOrNull()?.weightKg ?: activeClient.weightKg

    CreateMonthlyReportDialog(
      client = activeClient,
      initialWeight = initialWeight,
      currentWeight = currentWeight,
      onDismiss = { isCreateReportOpen = false },
      onSave = { monthYear, startW, currW, measSummary, trAdh, nutAdh, strSumm, obs, goals, macros, trainPlan ->
        onGenerateReport(
          activeClient.id,
          monthYear,
          startW,
          currW,
          measSummary,
          trAdh,
          nutAdh,
          strSumm,
          obs,
          goals,
          macros,
          trainPlan
        )
        isCreateReportOpen = false
      }
    )
  }
}

@Composable
fun ProgressVisualsView(
  client: ClientEntity?,
  measurements: List<MeasurementEntity>,
  checkIns: List<CheckInEntity>,
  timeFilter: String,
  onTimeFilterChange: (String) -> Unit
) {
  val filters = listOf("7 Days", "30 Days", "90 Days", "All Time")
  val dateFormat = SimpleDateFormat("MMM dd", Locale.US)

  if (client == null) {
    EmptyStateView(
      icon = Icons.Default.Timeline,
      title = "No Client Selected",
      message = "Select a client above to inspect longitudinal metrics and adherence curves."
    )
    return
  }

  val startWeight = measurements.lastOrNull()?.weightKg ?: client.weightKg
  val latestWeight = measurements.firstOrNull()?.weightKg ?: client.weightKg
  val delta = latestWeight - startWeight

  val avgTrainAdh = if (checkIns.isNotEmpty()) checkIns.map { it.trainingAdherencePercent }.average().toInt() else 0
  val avgNutrAdh = if (checkIns.isNotEmpty()) checkIns.map { it.nutritionAdherencePercent }.average().toInt() else 0

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(bottom = 60.dp)
  ) {
    // Time filter bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        filters.forEach { f ->
          FilterChip(
            selected = timeFilter == f,
            onClick = { onTimeFilterChange(f) },
            label = { Text(f, fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = RushPurpleContainer,
              selectedLabelColor = RushPurpleLight
            ),
            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
          )
        }
      }
    }

    // Delta Stats Grid
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricStatCard(
          title = "Net Weight Delta",
          value = "${if (delta >= 0) "+" else ""}${"%.1f".format(delta)} kg",
          subtitle = "Baseline: $startWeight kg → Now: $latestWeight kg",
          icon = Icons.Default.TrendingUp,
          accentColor = RushPurpleAccent,
          modifier = Modifier.weight(1f)
        )
        MetricStatCard(
          title = "Avg Compliance",
          value = "$avgTrainAdh% / $avgNutrAdh%",
          subtitle = "Training / Nutrition",
          icon = Icons.Default.CheckCircleOutline,
          accentColor = RushCobalt,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Measurement Progression History Table
    item {
      RushSectionHeader(title = "Historical Body Measurements Table")
      if (measurements.isEmpty()) {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No measurements recorded for this client yet.",
            style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray),
            modifier = Modifier.padding(16.dp)
          )
        }
      } else {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            // Table Header
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(RushDarkNavy, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("DATE", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontWeight = FontWeight.Bold))
              Text("WEIGHT", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontWeight = FontWeight.Bold))
              Text("WAIST", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontWeight = FontWeight.Bold))
              Text("CHEST", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontWeight = FontWeight.Bold))
              Text("ARMS", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(4.dp))

            measurements.take(8).forEach { m ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(dateFormat.format(Date(m.dateMillis)), style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite))
                Text("${m.weightKg} kg", style = MaterialTheme.typography.bodySmall.copy(color = RushPurpleLight, fontWeight = FontWeight.Bold))
                Text(m.waistCm?.let { "${it}cm" } ?: "-", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
                Text(m.chestCm?.let { "${it}cm" } ?: "-", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
                Text(m.armsCm?.let { "${it}cm" } ?: "-", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
              }
              HorizontalDivider(color = RushNavyBorder)
            }
          }
        }
      }
    }
  }
}

@Composable
fun MonthlyReportsListView(
  reports: List<MonthlyReportEntity>,
  clientName: String,
  onExportPdf: (MonthlyReportEntity) -> Unit,
  onOpenCreate: () -> Unit
) {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

  if (reports.isEmpty()) {
    EmptyStateView(
      icon = Icons.Default.Assessment,
      title = "No Monthly Reports Generated",
      message = "Synthesize comprehensive executive coaching reports summarizing anthropometrics, adherence, strength landmarks, and export as a PDF.",
      actionLabel = "Generate First Report",
      onAction = onOpenCreate
    )
  } else {
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(bottom = 60.dp)
    ) {
      items(reports) { r ->
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
                  text = r.monthYear.uppercase(),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite)
                )
                Text(
                  text = "Generated: ${dateFormat.format(Date(r.generatedDateMillis))}",
                  style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
                )
              }
              Button(
                onClick = { onExportPdf(r) },
                colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .defaultMinSize(minHeight = 38.dp)
                  .testTag("export_report_pdf_button_${r.id}")
              ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "Start: ${r.startingWeightKg}kg", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
              Text(text = "Current: ${r.currentWeightKg}kg", style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite, fontWeight = FontWeight.Bold))
              Text(
                text = "Delta: ${if (r.weightChangeKg >= 0) "+" else ""}${"%.1f".format(r.weightChangeKg)}kg",
                style = MaterialTheme.typography.bodySmall.copy(color = RushCobalt, fontWeight = FontWeight.Bold)
              )
              Text(text = "Adherence: ${r.trainingAdherenceAvg}% / ${r.nutritionAdherenceAvg}%", style = MaterialTheme.typography.bodySmall.copy(color = RushSuccess))
            }

            if (r.coachObservations.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Coach Note: \"${r.coachObservations}\"",
                style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite)
              )
            }
          }
        }
      }
    }
  }
}

// Dialog to generate Monthly Report
@Composable
fun CreateMonthlyReportDialog(
  client: ClientEntity,
  initialWeight: Double,
  currentWeight: Double,
  onDismiss: () -> Unit,
  onSave: (String, Double, Double, String, Int, Int, String, String, String, String, String) -> Unit
) {
  var monthYear by remember { mutableStateOf("September 2026") }
  var startingWeightStr by remember { mutableStateOf(initialWeight.toString()) }
  var currentWeightStr by remember { mutableStateOf(currentWeight.toString()) }
  var measurementsSummary by remember { mutableStateOf("Waist decreased -2.0cm; Chest increased +1.5cm.") }
  var trainAdhStr by remember { mutableStateOf("95") }
  var nutAdhStr by remember { mutableStateOf("92") }
  var strengthSummary by remember { mutableStateOf("Bench Press +5kg; Squat +7.5kg; RDL strict form maintained.") }
  var coachObservations by remember { mutableStateOf("Exceptional discipline shown across recovery protocols and training frequency.") }
  var nextGoals by remember { mutableStateOf("Initiate 4-week progressive hypertrophy block; increase protein floor.") }
  var updatedMacros by remember { mutableStateOf("${client.targetCalories ?: 2400} kcal (P: ${client.targetProteinG ?: 180}g, C: ${client.targetCarbsG ?: 250}g, F: ${client.targetFatG ?: 65}g)") }
  var updatedTrainingPlan by remember { mutableStateOf("4-Day Upper/Lower Split with Myo-Rep Intensifiers on deltoids.") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .padding(16.dp)
        .testTag("create_report_dialog")
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
          Text("GENERATE MONTHLY REPORT", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))
          IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = RushTextGray)
          }
        }

        Text(text = "Athlete: ${client.fullName}", style = MaterialTheme.typography.bodySmall.copy(color = RushPurpleLight))

        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            OutlinedTextField(
              value = monthYear,
              onValueChange = { monthYear = it },
              label = { Text("Report Month / Period *") },
              modifier = Modifier.fillMaxWidth()
            )
          }
          item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = startingWeightStr,
                onValueChange = { startingWeightStr = it },
                label = { Text("Starting Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = currentWeightStr,
                onValueChange = { currentWeightStr = it },
                label = { Text("Current Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
              )
            }
          }
          item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = trainAdhStr,
                onValueChange = { trainAdhStr = it },
                label = { Text("Training Adh (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = nutAdhStr,
                onValueChange = { nutAdhStr = it },
                label = { Text("Nutrition Adh (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
            }
          }
          item {
            OutlinedTextField(
              value = measurementsSummary,
              onValueChange = { measurementsSummary = it },
              label = { Text("Anthropometric & Waist Delta Summary") },
              modifier = Modifier.fillMaxWidth()
            )
          }
          item {
            OutlinedTextField(
              value = strengthSummary,
              onValueChange = { strengthSummary = it },
              label = { Text("Key Lift & Overload Progression") },
              modifier = Modifier.fillMaxWidth()
            )
          }
          item {
            OutlinedTextField(
              value = coachObservations,
              onValueChange = { coachObservations = it },
              label = { Text("Head Coach Clinical Observations") },
              modifier = Modifier.fillMaxWidth()
            )
          }
          item {
            OutlinedTextField(
              value = nextGoals,
              onValueChange = { nextGoals = it },
              label = { Text("Next Month's Primary Objectives") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Button(
          onClick = {
            val sw = startingWeightStr.toDoubleOrNull() ?: 80.0
            val cw = currentWeightStr.toDoubleOrNull() ?: 80.0
            val ta = trainAdhStr.toIntOrNull() ?: 90
            val na = nutAdhStr.toIntOrNull() ?: 90
            onSave(monthYear, sw, cw, measurementsSummary, ta, na, strengthSummary, coachObservations, nextGoals, updatedMacros, updatedTrainingPlan)
          },
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
          Text("Generate Report & Save", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}
