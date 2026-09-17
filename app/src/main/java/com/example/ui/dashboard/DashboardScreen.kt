package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
  clients: List<ClientEntity>,
  checkIns: List<CheckInEntity>,
  reports: List<MonthlyReportEntity>,
  onNavigateToClients: () -> Unit,
  onNavigateToTraining: () -> Unit,
  onNavigateToNutrition: () -> Unit,
  onNavigateToCheckIns: () -> Unit,
  onNavigateToReports: () -> Unit,
  onNavigateToCalculator: () -> Unit,
  onSelectClient: (Long) -> Unit,
  onOpenAddClient: () -> Unit
) {
  val activeClients = clients.filter { it.status == "Active" }
  val followUpClients = clients.filter { it.status == "Requires Follow-up" }
  val pendingCheckIns = checkIns.filter { it.status == "Pending Review" }

  val avgTrainingAdherence = if (checkIns.isNotEmpty()) {
    checkIns.map { it.trainingAdherencePercent }.average().toInt()
  } else 0

  val avgNutritionAdherence = if (checkIns.isNotEmpty()) {
    checkIns.map { it.nutritionAdherencePercent }.average().toInt()
  } else 0

  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(RushDarkNavy)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Brand Tagline
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RushNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "COACHING COMMAND CENTER",
              style = MaterialTheme.typography.labelMedium.copy(
                color = RushPurpleLight,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "The Rush Way Operating System",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = RushTextWhite
              )
            )
            Text(
              text = "Systematic Hypertrophy & Nutrition Management",
              style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray)
            )
          }
          Surface(
            color = RushPurpleContainer,
            shape = CircleShape,
            modifier = Modifier.size(44.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = RushPurpleLight)
            }
          }
        }
      }
    }

    // Quick Action Buttons
    item {
      RushSectionHeader(title = "Quick Actions")
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          QuickActionButton(
            label = "+ Add Client",
            icon = Icons.Default.PersonAdd,
            accentColor = RushPurpleAccent,
            onClick = onOpenAddClient,
            testTag = "dashboard_action_add_client"
          )
        }
        item {
          QuickActionButton(
            label = "Training Builder",
            icon = Icons.Default.FitnessCenter,
            accentColor = RushCobalt,
            onClick = onNavigateToTraining,
            testTag = "dashboard_action_training"
          )
        }
        item {
          QuickActionButton(
            label = "Nutrition Plan",
            icon = Icons.Default.Restaurant,
            accentColor = RushSuccess,
            onClick = onNavigateToNutrition,
            testTag = "dashboard_action_nutrition"
          )
        }
        item {
          QuickActionButton(
            label = "Check-Ins",
            icon = Icons.Default.AssignmentLate,
            accentColor = RushWarning,
            onClick = onNavigateToCheckIns,
            testTag = "dashboard_action_checkins"
          )
        }
        item {
          QuickActionButton(
            label = "Calculator",
            icon = Icons.Default.Calculate,
            accentColor = RushPurpleLight,
            onClick = onNavigateToCalculator,
            testTag = "dashboard_action_calculator"
          )
        }
        item {
          QuickActionButton(
            label = "Reports",
            icon = Icons.Default.Assessment,
            accentColor = RushCobalt,
            onClick = onNavigateToReports,
            testTag = "dashboard_action_reports"
          )
        }
      }
    }

    // Key Performance Metrics Grid
    item {
      RushSectionHeader(title = "Business & Roster Overview")
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricStatCard(
          title = "Active Clients",
          value = "${activeClients.size}",
          subtitle = "${clients.size} Total Registered",
          icon = Icons.Default.People,
          accentColor = RushPurpleAccent,
          modifier = Modifier.weight(1f)
        )
        MetricStatCard(
          title = "Follow-ups Due",
          value = "${followUpClients.size}",
          subtitle = if (followUpClients.isNotEmpty()) "Action Required" else "Roster Clean",
          icon = Icons.Default.NotificationImportant,
          accentColor = if (followUpClients.isNotEmpty()) RushWarning else RushSuccess,
          modifier = Modifier.weight(1f)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricStatCard(
          title = "Training Adherence",
          value = if (checkIns.isNotEmpty()) "$avgTrainingAdherence%" else "No data",
          subtitle = "Average Compliance",
          icon = Icons.Default.Speed,
          accentColor = RushCobalt,
          modifier = Modifier.weight(1f)
        )
        MetricStatCard(
          title = "Nutrition Adherence",
          value = if (checkIns.isNotEmpty()) "$avgNutritionAdherence%" else "No data",
          subtitle = "Average Compliance",
          icon = Icons.Default.LocalDining,
          accentColor = RushSuccess,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Pending Check-Ins Section
    item {
      RushSectionHeader(
        title = "Check-Ins Requiring Review",
        subtitle = "${pendingCheckIns.size} Pending",
        actionText = "View All",
        onActionClick = onNavigateToCheckIns
      )
      if (pendingCheckIns.isEmpty()) {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RushSuccess, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "All check-ins reviewed. No pending client feedback.",
              style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray)
            )
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          pendingCheckIns.take(4).forEach { item ->
            val client = clients.find { it.id == item.clientId }
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = RushNavySurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, RushWarning.copy(alpha = 0.5f)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToCheckIns() }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(
                    text = client?.fullName ?: "Client #${item.clientId}",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = RushTextWhite
                    )
                  )
                  Text(
                    text = "Weight: ${item.weightKg} kg • Training: ${item.trainingAdherencePercent}% • Nutrition: ${item.nutritionAdherencePercent}%",
                    style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
                  )
                }
                Surface(
                  color = RushWarning.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = "NEEDS REVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = RushWarning,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // Recent Clients List
    item {
      RushSectionHeader(
        title = "Client Roster",
        actionText = "Manage All",
        onActionClick = onNavigateToClients
      )
      if (clients.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.PersonAdd,
          title = "No Clients Yet",
          message = "Add your first athlete or client to configure training programs, nutrition targets, and track weekly check-ins.",
          actionLabel = "Add Client",
          onAction = onOpenAddClient
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          clients.take(5).forEach { client ->
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = RushNavySurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectClient(client.id) }
                .testTag("dashboard_client_item_${client.id}")
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
                    modifier = Modifier.size(38.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = client.fullName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                          color = RushPurpleLight,
                          fontWeight = FontWeight.Bold
                        )
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = client.fullName,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RushTextWhite
                      )
                    )
                    Text(
                      text = "${client.weightKg} kg • ${client.goals}",
                      style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
                    )
                  }
                }
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
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun QuickActionButton(
  label: String,
  icon: ImageVector,
  accentColor: Color,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = RushNavySurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
    modifier = Modifier
      .defaultMinSize(minWidth = 110.dp, minHeight = 48.dp)
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          color = RushTextWhite,
          fontSize = 11.sp
        )
      )
    }
  }
}
