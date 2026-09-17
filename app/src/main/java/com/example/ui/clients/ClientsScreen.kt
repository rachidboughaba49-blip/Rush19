package com.example.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClientsScreen(
  clients: List<ClientEntity>,
  selectedClientId: Long?,
  onSelectClient: (Long?) -> Unit,
  onAddClient: (ClientEntity, Double, Double?) -> Unit,
  onUpdateClient: (ClientEntity) -> Unit,
  onDeleteClient: (ClientEntity) -> Unit,
  onAddMeasurement: (MeasurementEntity) -> Unit,
  measurements: List<MeasurementEntity>,
  trainingPrograms: List<TrainingProgramEntity>,
  nutritionPlans: List<NutritionPlanEntity>,
  checkIns: List<CheckInEntity>,
  onNavigateToCalculator: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedStatusFilter by remember { mutableStateOf("All") }
  var isAddClientOpen by remember { mutableStateOf(false) }
  var isAddMeasurementOpen by remember { mutableStateOf(false) }

  val selectedClient = clients.find { it.id == selectedClientId }

  val filteredClients = clients.filter { client ->
    val matchesSearch = client.fullName.contains(searchQuery, ignoreCase = true) ||
        client.goals.contains(searchQuery, ignoreCase = true)
    val matchesStatus = if (selectedStatusFilter == "All") true else client.status == selectedStatusFilter
    matchesSearch && matchesStatus
  }

  if (selectedClient != null) {
    // Client Profile Detail View
    ClientProfileDetailView(
      client = selectedClient,
      onBack = { onSelectClient(null) },
      onUpdateClient = onUpdateClient,
      onDeleteClient = {
        onDeleteClient(selectedClient)
        onSelectClient(null)
      },
      measurements = measurements,
      onOpenAddMeasurement = { isAddMeasurementOpen = true },
      trainingPrograms = trainingPrograms.filter { it.clientId == selectedClient.id },
      nutritionPlans = nutritionPlans.filter { it.clientId == selectedClient.id },
      checkIns = checkIns.filter { it.clientId == selectedClient.id },
      onNavigateToCalculator = onNavigateToCalculator
    )
  } else {
    // Client List View
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(RushDarkNavy)
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Search & Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "ATHLETES & CLIENTS (${clients.size})",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Black,
            color = RushTextWhite,
            letterSpacing = 0.5.sp
          )
        )
        Button(
          onClick = { isAddClientOpen = true },
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .defaultMinSize(minHeight = 44.dp)
            .testTag("add_client_button")
        ) {
          Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "+ New Client", fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Search Field
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search client name or fitness goal...", color = RushTextMuted) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = RushTextGray) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = RushTextWhite,
          unfocusedTextColor = RushTextWhite,
          focusedBorderColor = RushPurpleAccent,
          unfocusedBorderColor = RushNavyBorder,
          focusedContainerColor = RushNavySurface,
          unfocusedContainerColor = RushNavySurface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("client_search_input")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        val filters = listOf("All", "Active", "Requires Follow-up", "Inactive")
        items(filters) { f ->
          FilterChip(
            selected = selectedStatusFilter == f,
            onClick = { selectedStatusFilter = f },
            label = { Text(f, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = RushPurpleContainer,
              selectedLabelColor = RushPurpleLight,
              containerColor = RushNavySurface,
              labelColor = RushTextGray
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = if (selectedStatusFilter == f) RushPurpleAccent else RushNavyBorder,
              enabled = true,
              selected = selectedStatusFilter == f
            ),
            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Client List
      if (filteredClients.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.PeopleOutline,
          title = if (clients.isEmpty()) "No Clients Enrolled" else "No Matching Clients",
          message = if (clients.isEmpty()) {
            "Enroll your first client with intake details, baseline measurements, and target goals."
          } else {
            "No client matches your current search or filter criteria."
          },
          actionLabel = if (clients.isEmpty()) "Add New Client" else null,
          onAction = if (clients.isEmpty()) { { isAddClientOpen = true } } else null
        )
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 80.dp)
        ) {
          items(filteredClients) { client ->
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = RushNavySurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectClient(client.id) }
                .testTag("client_roster_card_${client.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    color = RushPurpleContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = client.fullName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                          color = RushPurpleLight,
                          fontWeight = FontWeight.Black
                        )
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(14.dp))
                  Column {
                    Text(
                      text = client.fullName,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RushTextWhite
                      )
                    )
                    Text(
                      text = "${client.age} yrs • ${client.gender} • ${client.weightKg} kg • ${client.heightCm} cm",
                      style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "Goals: ${client.goals}",
                      style = MaterialTheme.typography.labelSmall.copy(color = RushPurpleLight)
                    )
                  }
                }
                Column(horizontalAlignment = Alignment.End) {
                  Surface(
                    color = when (client.status) {
                      "Active" -> RushSuccess.copy(alpha = 0.2f)
                      "Requires Follow-up" -> RushWarning.copy(alpha = 0.2f)
                      else -> RushNavyElevated
                    },
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = client.status.uppercase(),
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = when (client.status) {
                          "Active" -> RushSuccess
                          "Requires Follow-up" -> RushWarning
                          else -> RushTextGray
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                      ),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = RushTextGray)
                }
              }
            }
          }
        }
      }
    }
  }

  // Add Client Dialog
  if (isAddClientOpen) {
    AddClientDialog(
      onDismiss = { isAddClientOpen = false },
      onSave = { client, weight, waist ->
        onAddClient(client, weight, waist)
        isAddClientOpen = false
      }
    )
  }

  // Add Measurement Dialog
  if (isAddMeasurementOpen && selectedClient != null) {
    AddMeasurementDialog(
      clientId = selectedClient.id,
      onDismiss = { isAddMeasurementOpen = false },
      onSave = { m ->
        onAddMeasurement(m)
        isAddMeasurementOpen = false
      }
    )
  }
}

@Composable
fun ClientProfileDetailView(
  client: ClientEntity,
  onBack: () -> Unit,
  onUpdateClient: (ClientEntity) -> Unit,
  onDeleteClient: () -> Unit,
  measurements: List<MeasurementEntity>,
  onOpenAddMeasurement: () -> Unit,
  trainingPrograms: List<TrainingProgramEntity>,
  nutritionPlans: List<NutritionPlanEntity>,
  checkIns: List<CheckInEntity>,
  onNavigateToCalculator: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Overview", "Measurements", "Plans", "Check-Ins")
  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(RushDarkNavy)
  ) {
    // Profile Header Banner
    Card(
      shape = RoundedCornerShape(0.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = RushTextWhite)
          }
          Row {
            // Status Toggle
            OutlinedButton(
              onClick = {
                val nextStatus = when (client.status) {
                  "Active" -> "Requires Follow-up"
                  "Requires Follow-up" -> "Inactive"
                  else -> "Active"
                }
                onUpdateClient(client.copy(status = nextStatus))
              },
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.defaultMinSize(minHeight = 44.dp)
            ) {
              Text("Status: ${client.status}", color = RushPurpleLight, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onDeleteClient, modifier = Modifier.size(48.dp)) {
              Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete Client", tint = RushDanger)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = RushPurpleContainer,
            shape = CircleShape,
            modifier = Modifier.size(54.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = client.fullName.take(2).uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(
                  color = RushPurpleLight,
                  fontWeight = FontWeight.Black
                )
              )
            }
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = client.fullName,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = RushTextWhite
              )
            )
            Text(
              text = "${client.age} years • ${client.gender} • ${client.occupation.ifBlank { "Client" }}",
              style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray)
            )
            Text(
              text = "Goals: ${client.goals}",
              style = MaterialTheme.typography.bodySmall.copy(color = RushCobalt)
            )
          }
        }
      }
    }

    // Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = RushNavySurface,
      contentColor = RushPurpleAccent
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              color = if (selectedTab == index) RushPurpleLight else RushTextGray,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
              fontSize = 12.sp
            )
          },
          modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        )
      }
    }

    // Tab Contents
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      when (selectedTab) {
        0 -> ClientOverviewTab(client = client, onNavigateToCalculator = onNavigateToCalculator)
        1 -> ClientMeasurementsTab(
          measurements = measurements,
          onOpenAddMeasurement = onOpenAddMeasurement
        )
        2 -> ClientPlansTab(
          trainingPrograms = trainingPrograms,
          nutritionPlans = nutritionPlans,
          client = client
        )
        3 -> ClientCheckInsTab(checkIns = checkIns)
      }
    }
  }
}

@Composable
fun ClientOverviewTab(
  client: ClientEntity,
  onNavigateToCalculator: () -> Unit
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(bottom = 60.dp)
  ) {
    // Current Targets Card
    item {
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = RushNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "CURRENT NUTRITION TARGETS",
              style = MaterialTheme.typography.labelMedium.copy(color = RushPurpleLight, fontWeight = FontWeight.Bold)
            )
            Button(
              onClick = onNavigateToCalculator,
              colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.defaultMinSize(minHeight = 36.dp)
            ) {
              Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Recalculate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            MacroPill("CALORIES", "${client.targetCalories ?: 2400} kcal", RushCobalt)
            MacroPill("PROTEIN", "${client.targetProteinG ?: 180}g", RushPurpleLight)
            MacroPill("CARBS", "${client.targetCarbsG ?: 250}g", RushSuccess)
            MacroPill("FAT", "${client.targetFatG ?: 65}g", RushWarning)
          }
          if (client.calculationMethod != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Formula: ${client.calculationMethod} (Coach verified)",
              style = MaterialTheme.typography.bodySmall.copy(color = RushTextMuted, fontSize = 11.sp)
            )
          }
        }
      }
    }

    // Physical & Lifestyle Profile
    item {
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = RushNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "LIFESTYLE & HEALTH INTAKE",
            style = MaterialTheme.typography.labelMedium.copy(color = RushTextGray, fontWeight = FontWeight.Bold)
          )
          ProfileRow("Current Weight", "${client.weightKg} kg")
          ProfileRow("Height", "${client.heightCm} cm")
          ProfileRow("Activity Multiplier", client.activityLevel)
          ProfileRow("Sleep Duration", "${client.sleepDurationHours} hours / night")
          if (client.dailySchedule.isNotBlank()) ProfileRow("Daily Schedule", client.dailySchedule)
          if (client.budget.isNotBlank()) ProfileRow("Nutrition Budget", client.budget)
          if (client.notes.isNotBlank()) ProfileRow("Intake Notes", client.notes)
        }
      }
    }
  }
}

@Composable
fun MacroPill(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontSize = 9.sp))
    Text(text = value, style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.Black))
  }
}

@Composable
fun ProfileRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray))
    Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(color = RushTextWhite, fontWeight = FontWeight.Medium))
  }
}

@Composable
fun ClientMeasurementsTab(
  measurements: List<MeasurementEntity>,
  onOpenAddMeasurement: () -> Unit
) {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "ANTHROPOMETRIC HISTORY (${measurements.size})",
        style = MaterialTheme.typography.labelLarge.copy(color = RushTextWhite, fontWeight = FontWeight.Bold)
      )
      Button(
        onClick = onOpenAddMeasurement,
        colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.defaultMinSize(minHeight = 40.dp)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("+ Log Measurement", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (measurements.isEmpty()) {
      EmptyStateView(
        icon = Icons.Default.Straighten,
        title = "No Measurements Logged",
        message = "Log baseline and weekly measurements (weight, waist, chest, arms, thighs) to track structural body recomposition.",
        actionLabel = "Log First Measurement",
        onAction = onOpenAddMeasurement
      )
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
      ) {
        items(measurements) { m ->
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = RushNavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = dateFormat.format(Date(m.dateMillis)),
                  style = MaterialTheme.typography.titleMedium.copy(color = RushPurpleLight, fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "${m.weightKg} kg",
                  style = MaterialTheme.typography.headlineSmall.copy(color = RushTextWhite, fontWeight = FontWeight.Black)
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              // Measurements Grid
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (m.waistCm != null) Text("Waist: ${m.waistCm}cm", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
                if (m.chestCm != null) Text("Chest: ${m.chestCm}cm", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
                if (m.armsCm != null) Text("Arms: ${m.armsCm}cm", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
                if (m.thighsCm != null) Text("Thighs: ${m.thighsCm}cm", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
              }
              if (m.notes != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Note: ${m.notes}", style = MaterialTheme.typography.bodySmall.copy(color = RushTextMuted))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ClientPlansTab(
  trainingPrograms: List<TrainingProgramEntity>,
  nutritionPlans: List<NutritionPlanEntity>,
  client: ClientEntity
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(bottom = 60.dp)
  ) {
    item {
      RushSectionHeader(title = "Assigned Training Programs")
      if (trainingPrograms.isEmpty()) {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No custom training program assigned to this client yet. Create one in Training Builder.",
            style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray),
            modifier = Modifier.padding(16.dp)
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          trainingPrograms.forEach { prog ->
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = RushNavySurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(text = prog.name, style = MaterialTheme.typography.titleMedium.copy(color = RushTextWhite, fontWeight = FontWeight.Bold))
                Text(text = "${prog.daysPerWeek} Days / Week • ${prog.description}", style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray))
              }
            }
          }
        }
      }
    }

    item {
      RushSectionHeader(title = "Assigned Nutrition Plans")
      if (nutritionPlans.isEmpty()) {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No custom nutrition plan assigned yet. Create one in Nutrition Builder.",
            style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray),
            modifier = Modifier.padding(16.dp)
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          nutritionPlans.forEach { plan ->
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = RushNavySurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, RushSuccess.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(text = plan.name, style = MaterialTheme.typography.titleMedium.copy(color = RushTextWhite, fontWeight = FontWeight.Bold))
                Text(
                  text = "${plan.targetCalories} kcal • P: ${plan.targetProteinG}g • C: ${plan.targetCarbsG}g • F: ${plan.targetFatG}g",
                  style = MaterialTheme.typography.bodySmall.copy(color = RushSuccess)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ClientCheckInsTab(checkIns: List<CheckInEntity>) {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

  if (checkIns.isEmpty()) {
    EmptyStateView(
      icon = Icons.Default.Assignment,
      title = "No Check-Ins Submitted",
      message = "Check-ins submitted by the client (or recorded manually by the coach) will appear here with adherence metrics and photos."
    )
  } else {
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(bottom = 60.dp)
    ) {
      items(checkIns) { ci ->
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = dateFormat.format(Date(ci.dateMillis)),
                style = MaterialTheme.typography.titleMedium.copy(color = RushPurpleLight, fontWeight = FontWeight.Bold)
              )
              Surface(
                color = if (ci.status == "Reviewed") RushSuccess.copy(alpha = 0.2f) else RushWarning.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = ci.status.uppercase(),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (ci.status == "Reviewed") RushSuccess else RushWarning,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  ),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Weight: ${ci.weightKg} kg • Training: ${ci.trainingAdherencePercent}% • Nutrition: ${ci.nutritionAdherencePercent}%",
              style = MaterialTheme.typography.bodyMedium.copy(color = RushTextWhite)
            )
            Text(
              text = "Sleep: ${ci.sleepScore}/10 • Energy: ${ci.energyScore}/10 • Stress: ${ci.stressScore}/10 • Hunger: ${ci.hungerScore}/10",
              style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
            )
            if (ci.clientComments.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = "Comments: \"${ci.clientComments}\"", style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite))
            }
            if (ci.coachFeedback?.isNotBlank() == true) {
              Spacer(modifier = Modifier.height(6.dp))
              Surface(
                color = RushDarkNavy,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(text = "Coach Feedback:", style = MaterialTheme.typography.labelSmall.copy(color = RushPurpleLight, fontWeight = FontWeight.Bold))
                  Text(text = ci.coachFeedback, style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite))
                }
              }
            }
          }
        }
      }
    }
  }
}

// Add Client Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClientDialog(
  onDismiss: () -> Unit,
  onSave: (ClientEntity, Double, Double?) -> Unit
) {
  var fullName by remember { mutableStateOf("") }
  var ageStr by remember { mutableStateOf("28") }
  var gender by remember { mutableStateOf("Male") }
  var heightStr by remember { mutableStateOf("178") }
  var weightStr by remember { mutableStateOf("82") }
  var waistStr by remember { mutableStateOf("84") }
  var occupation by remember { mutableStateOf("") }
  var activityLevel by remember { mutableStateOf("Moderately Active (1.55)") }
  var sleepHoursStr by remember { mutableStateOf("7.5") }
  var dailySchedule by remember { mutableStateOf("") }
  var budget by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }
  var selectedGoals by remember { mutableStateOf(setOf("Muscle gain", "Fat loss")) }

  val allGoals = listOf("Fat loss", "Muscle gain", "Body recomposition", "Strength", "General fitness", "Performance", "Maintenance")
  val activityLevels = listOf(
    "Sedentary (1.2)",
    "Lightly Active (1.375)",
    "Moderately Active (1.55)",
    "Very Active (1.725)",
    "Extremely Active (1.9)"
  )

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .testTag("add_client_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ADD NEW CLIENT",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite)
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = RushTextGray)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("Full Name *") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("add_client_name_input")
            )
          }
          item {
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
                label = { Text("Gender") },
                modifier = Modifier.weight(1f)
              )
            }
          }
          item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it },
                label = { Text("Weight (kg) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f).testTag("add_client_weight_input")
              )
              OutlinedTextField(
                value = heightStr,
                onValueChange = { heightStr = it },
                label = { Text("Height (cm) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
              )
            }
          }
          item {
            OutlinedTextField(
              value = waistStr,
              onValueChange = { waistStr = it },
              label = { Text("Baseline Waist (cm - optional)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.fillMaxWidth()
            )
          }
          item {
            OutlinedTextField(
              value = occupation,
              onValueChange = { occupation = it },
              label = { Text("Occupation / Schedule") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
          item {
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
          }
          item {
            Text("Fitness Goals (Select Multiple)", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(allGoals) { g ->
                val isSelected = selectedGoals.contains(g)
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    selectedGoals = if (isSelected) selectedGoals - g else selectedGoals + g
                  },
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
          item {
            OutlinedTextField(
              value = notes,
              onValueChange = { notes = it },
              label = { Text("Intake Notes / Medical Conditions") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            if (fullName.isNotBlank()) {
              val w = weightStr.toDoubleOrNull() ?: 75.0
              val h = heightStr.toDoubleOrNull() ?: 175.0
              val a = ageStr.toIntOrNull() ?: 25
              val waist = waistStr.toDoubleOrNull()
              val client = ClientEntity(
                fullName = fullName,
                age = a,
                gender = gender,
                heightCm = h,
                weightKg = w,
                occupation = occupation,
                activityLevel = activityLevel,
                notes = notes,
                goals = selectedGoals.joinToString(", ")
              )
              onSave(client, w, waist)
            }
          },
          enabled = fullName.isNotBlank() && weightStr.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .testTag("save_client_button")
        ) {
          Text("Save Client & Intake Baseline", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}

// Add Measurement Dialog
@Composable
fun AddMeasurementDialog(
  clientId: Long,
  onDismiss: () -> Unit,
  onSave: (MeasurementEntity) -> Unit
) {
  var weightStr by remember { mutableStateOf("") }
  var waistStr by remember { mutableStateOf("") }
  var chestStr by remember { mutableStateOf("") }
  var armsStr by remember { mutableStateOf("") }
  var thighsStr by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

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
        Text("LOG BODY MEASUREMENTS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))
        OutlinedTextField(
          value = weightStr,
          onValueChange = { weightStr = it },
          label = { Text("Weight (kg) *") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth().testTag("measurement_weight_input")
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = waistStr,
            onValueChange = { waistStr = it },
            label = { Text("Waist (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = chestStr,
            onValueChange = { chestStr = it },
            label = { Text("Chest (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = armsStr,
            onValueChange = { armsStr = it },
            label = { Text("Arms (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = thighsStr,
            onValueChange = { thighsStr = it },
            label = { Text("Thighs (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
        }
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Condition / Notes (e.g. Fasted AM)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
          onClick = {
            val w = weightStr.toDoubleOrNull()
            if (w != null) {
              onSave(
                MeasurementEntity(
                  clientId = clientId,
                  weightKg = w,
                  waistCm = waistStr.toDoubleOrNull(),
                  chestCm = chestStr.toDoubleOrNull(),
                  armsCm = armsStr.toDoubleOrNull(),
                  thighsCm = thighsStr.toDoubleOrNull(),
                  notes = notes
                )
              )
            }
          },
          enabled = weightStr.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
          Text("Save Measurements", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}
