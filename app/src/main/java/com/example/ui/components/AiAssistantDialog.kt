package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ClientEntity
import com.example.ui.theme.*

@Composable
fun AiAssistantDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  selectedClient: ClientEntity?,
  isLoading: Boolean,
  aiResponse: String?,
  onAskAi: (prompt: String, context: String?) -> Unit,
  onClearResponse: () -> Unit
) {
  if (!isOpen) return

  var customPrompt by remember { mutableStateOf("") }

  val defaultPrompts = listOf(
    "Explain BMR and TDEE calculation methodology",
    "Suggest 4-Day Upper/Lower Hypertrophy Program",
    "Draft weekly check-in feedback message (English)",
    "Draft message in French (Feedback d'entraînement)",
    "Draft message in Arabic (رسالة متابعة المدرب)",
    "Analyze progressive overload and volume landmarks"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.85f)
        .testTag("ai_assistant_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = RushPurpleAccent,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "THE RUSH WAY AI",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.5.sp,
                  color = RushTextWhite
                )
              )
              Text(
                text = "Elite Coaching & Analysis Assistant",
                style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
              )
            }
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = RushTextGray)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Client Context Banner
        Surface(
          color = RushDarkNavy,
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = RushPurpleLight, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (selectedClient != null) {
                "Active Context: ${selectedClient.fullName} (${selectedClient.weightKg} kg, ${selectedClient.goals})"
              } else {
                "No client selected (General Coaching Knowledge Mode)"
              },
              style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray, fontSize = 11.sp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Response or Quick Chips Area
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(RushDarkNavy, RoundedCornerShape(8.dp))
            .border(1.dp, RushNavyBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
        ) {
          if (isLoading) {
            Column(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(color = RushPurpleAccent)
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Consulting The Rush Way Knowledge Base…",
                style = MaterialTheme.typography.bodyMedium.copy(color = RushTextGray)
              )
            }
          } else if (aiResponse != null) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "ASSISTANT RECOMMENDATION",
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = RushPurpleLight,
                    fontWeight = FontWeight.Bold
                  )
                )
                TextButton(onClick = onClearResponse, modifier = Modifier.defaultMinSize(minHeight = 40.dp)) {
                  Text("Clear", color = RushTextGray, fontSize = 12.sp)
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = aiResponse,
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = RushTextWhite,
                  lineHeight = 22.sp
                )
              )
            }
          } else {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
            ) {
              Text(
                text = "COACHING PROMPTS",
                style = MaterialTheme.typography.labelMedium.copy(
                  color = RushTextGray,
                  fontWeight = FontWeight.Bold
                )
              )
              Spacer(modifier = Modifier.height(10.dp))
              defaultPrompts.forEach { p ->
                Surface(
                  color = RushNavySurface,
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                      val contextStr = selectedClient?.let {
                        "Client: ${it.fullName}, Gender: ${it.gender}, Age: ${it.age}, Weight: ${it.weightKg} kg, Height: ${it.heightCm} cm, Goal: ${it.goals}, Activity: ${it.activityLevel}, Target Cals: ${it.targetCalories ?: "None"}"
                      }
                      onAskAi(p, contextStr)
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.ChevronRight,
                      contentDescription = null,
                      tint = RushPurpleLight,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = p, style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite))
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Prompt Input Field
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            placeholder = { Text("Ask about nutrition, training splits, or drafting...", color = RushTextMuted, fontSize = 12.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = RushTextWhite,
              unfocusedTextColor = RushTextWhite,
              focusedBorderColor = RushPurpleAccent,
              unfocusedBorderColor = RushNavyBorder,
              focusedContainerColor = RushDarkNavy,
              unfocusedContainerColor = RushDarkNavy
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("ai_custom_prompt_input")
          )
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (customPrompt.isNotBlank()) {
                val contextStr = selectedClient?.let {
                  "Client: ${it.fullName}, Gender: ${it.gender}, Age: ${it.age}, Weight: ${it.weightKg} kg, Height: ${it.heightCm} cm, Goal: ${it.goals}, Activity: ${it.activityLevel}, Target Cals: ${it.targetCalories ?: "None"}"
                }
                onAskAi(customPrompt, contextStr)
                customPrompt = ""
              }
            },
            enabled = customPrompt.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .defaultMinSize(minHeight = 52.dp)
              .testTag("ai_send_prompt_button")
          ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = RushTextWhite)
          }
        }
      }
    }
  }
}
