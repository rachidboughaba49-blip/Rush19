package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RushWayTopBar(
  title: String,
  isCoachMode: Boolean,
  onToggleMode: () -> Unit,
  onOpenAi: () -> Unit,
  onOpenQuickAction: (() -> Unit)? = null
) {
  TopAppBar(
    title = {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "THE RUSH WAY",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp,
              color = RushTextWhite
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            color = if (isCoachMode) RushPurpleContainer else RushCobalt.copy(alpha = 0.2f),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isCoachMode) RushPurpleAccent else RushCobalt
            )
          ) {
            Text(
              text = if (isCoachMode) "COACH MODE" else "CLIENT MODE",
              style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCoachMode) RushPurpleLight else RushCobalt
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
        if (title != "THE RUSH WAY") {
          Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray)
          )
        }
      }
    },
    actions = {
      // Toggle Mode Button
      IconButton(
        onClick = onToggleMode,
        modifier = Modifier
          .testTag("toggle_mode_button")
          .size(48.dp)
      ) {
        Icon(
          imageVector = if (isCoachMode) Icons.Default.SwapHoriz else Icons.Default.AdminPanelSettings,
          contentDescription = "Switch between Coach and Client mode",
          tint = if (isCoachMode) RushPurpleLight else RushCobalt
        )
      }

      // AI Coaching Assistant Button
      IconButton(
        onClick = onOpenAi,
        modifier = Modifier
          .testTag("ai_assistant_button")
          .size(48.dp)
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Open Rush Way AI Assistant",
          tint = RushPurpleAccent
        )
      }

      if (onOpenQuickAction != null) {
        IconButton(
          onClick = onOpenQuickAction,
          modifier = Modifier
            .testTag("quick_action_button")
            .size(48.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = "Quick Actions",
            tint = RushTextWhite
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = RushDarkNavy,
      titleContentColor = RushTextWhite
    )
  )
}

@Composable
fun MetricStatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector? = null,
  accentColor: Color = RushPurpleAccent,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = RushNavySurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
    modifier = modifier.testTag("metric_card_${title.lowercase().replace(" ", "_")}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = title.uppercase(),
          style = MaterialTheme.typography.labelMedium.copy(
            color = RushTextGray,
            letterSpacing = 0.5.sp,
            fontSize = 11.sp
          )
        )
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          color = RushTextWhite
        )
      )
      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = accentColor,
            fontSize = 12.sp
          )
        )
      }
    }
  }
}

@Composable
fun EmptyStateView(
  icon: ImageVector,
  title: String,
  message: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Surface(
        color = RushNavySurface,
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
        modifier = Modifier.size(64.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RushPurpleAccent,
            modifier = Modifier.size(32.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          color = RushTextWhite
        ),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray),
        textAlign = TextAlign.Center
      )
      if (actionLabel != null && onAction != null) {
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onAction,
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("empty_state_action_button")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = actionLabel, color = RushTextWhite, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun RushSectionHeader(
  title: String,
  subtitle: String? = null,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = RushTextWhite,
          letterSpacing = 0.5.sp
        )
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray)
        )
      }
    }
    if (actionText != null && onActionClick != null) {
      TextButton(
        onClick = onActionClick,
        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
      ) {
        Text(
          text = actionText,
          style = MaterialTheme.typography.labelLarge.copy(color = RushPurpleLight)
        )
      }
    }
  }
}
