package com.example.ui.training

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

@Composable
fun TrainingScreen(
  clients: List<ClientEntity>,
  programs: List<TrainingProgramEntity>,
  activeProgramId: Long?,
  onSelectProgram: (Long?) -> Unit,
  onCreateProgram: (Long?, String, Int, String, List<String>) -> Unit,
  onDeleteProgram: (TrainingProgramEntity) -> Unit,
  programDays: List<TrainingDayEntity>,
  getExercisesForDay: (Long) -> Flow<List<TrainingDayExerciseEntity>>,
  onAddExerciseToDay: (Long, ExerciseEntity, Int, String, String, String, String?) -> Unit,
  onMoveDayExercise: (Long, TrainingDayExerciseEntity, Boolean) -> Unit,
  onDeleteDayExercise: (TrainingDayExerciseEntity) -> Unit,
  exercises: List<ExerciseEntity>,
  onAddExercise: (ExerciseEntity) -> Unit,
  onExportPdf: (Long) -> Unit
) {
  var topTab by remember { mutableIntStateOf(0) } // 0: Programs & Builder, 1: Exercise Library
  var isCreateProgramOpen by remember { mutableStateOf(false) }
  var isAddCustomExerciseOpen by remember { mutableStateOf(false) }

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
              text = "TRAINING SYSTEMS & BUILDER",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                color = RushTextWhite,
                letterSpacing = 0.5.sp
              )
            )
            Text(
              text = "Periodized Hypertrophy & Overload Architect",
              style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
            )
          }
          if (topTab == 0) {
            Button(
              onClick = { isCreateProgramOpen = true },
              colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .testTag("create_program_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("+ Program", fontWeight = FontWeight.Bold)
            }
          } else {
            Button(
              onClick = { isAddCustomExerciseOpen = true },
              colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .testTag("add_custom_exercise_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("+ Custom Exercise", fontWeight = FontWeight.Bold)
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
        text = { Text("Program Builder (${programs.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
      )
      Tab(
        selected = topTab == 1,
        onClick = { topTab = 1 },
        text = { Text("Exercise Library (${exercises.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
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
        ProgramBuilderView(
          programs = programs,
          activeProgramId = activeProgramId,
          onSelectProgram = onSelectProgram,
          programDays = programDays,
          getExercisesForDay = getExercisesForDay,
          onAddExerciseToDay = onAddExerciseToDay,
          onMoveDayExercise = onMoveDayExercise,
          onDeleteDayExercise = onDeleteDayExercise,
          exercises = exercises,
          onExportPdf = onExportPdf,
          onDeleteProgram = onDeleteProgram
        )
      } else {
        ExerciseLibraryView(
          exercises = exercises,
          onAddCustomExercise = { isAddCustomExerciseOpen = true }
        )
      }
    }
  }

  // Create Program Dialog
  if (isCreateProgramOpen) {
    CreateProgramDialog(
      clients = clients,
      onDismiss = { isCreateProgramOpen = false },
      onCreate = { clientId, name, days, desc, dayNames ->
        onCreateProgram(clientId, name, days, desc, dayNames)
        isCreateProgramOpen = false
      }
    )
  }

  // Add Custom Exercise Dialog
  if (isAddCustomExerciseOpen) {
    AddCustomExerciseDialog(
      onDismiss = { isAddCustomExerciseOpen = false },
      onSave = { ex ->
        onAddExercise(ex)
        isAddCustomExerciseOpen = false
      }
    )
  }
}

@Composable
fun ProgramBuilderView(
  programs: List<TrainingProgramEntity>,
  activeProgramId: Long?,
  onSelectProgram: (Long?) -> Unit,
  programDays: List<TrainingDayEntity>,
  getExercisesForDay: (Long) -> Flow<List<TrainingDayExerciseEntity>>,
  onAddExerciseToDay: (Long, ExerciseEntity, Int, String, String, String, String?) -> Unit,
  onMoveDayExercise: (Long, TrainingDayExerciseEntity, Boolean) -> Unit,
  onDeleteDayExercise: (TrainingDayExerciseEntity) -> Unit,
  exercises: List<ExerciseEntity>,
  onExportPdf: (Long) -> Unit,
  onDeleteProgram: (TrainingProgramEntity) -> Unit
) {
  var selectedDayId by remember { mutableStateOf<Long?>(null) }
  var isAddExerciseToDayOpen by remember { mutableStateOf(false) }

  val activeProgram = programs.find { it.id == activeProgramId } ?: programs.firstOrNull()

  LaunchedEffect(activeProgram, programDays) {
    if (activeProgram != null && (selectedDayId == null || programDays.none { it.id == selectedDayId })) {
      selectedDayId = programDays.firstOrNull()?.id
    }
  }

  if (programs.isEmpty()) {
    EmptyStateView(
      icon = Icons.Default.FitnessCenter,
      title = "No Programs Created",
      message = "Build 3, 4, 5, or 6-day periodized programs with sets, reps, tempo, RIR, and bodybuilding intensifiers (Myo-reps, Drop sets, Supersets)."
    )
  } else {
    Column(modifier = Modifier.fillMaxSize()) {
      // Program Selector Row
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(programs) { prog ->
          val isSelected = (activeProgram?.id == prog.id)
          FilterChip(
            selected = isSelected,
            onClick = { onSelectProgram(prog.id) },
            label = { Text(prog.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = RushPurpleContainer,
              selectedLabelColor = RushPurpleLight
            ),
            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (activeProgram != null) {
        // Active Program Header & Actions
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = activeProgram.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RushTextWhite)
              )
              Text(
                text = "${activeProgram.daysPerWeek} Days/Week • ${activeProgram.description.ifBlank { "Progressive overload routine" }}",
                style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
              )
            }
            Row {
              Button(
                onClick = { onExportPdf(activeProgram.id) },
                colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .defaultMinSize(minHeight = 40.dp)
                  .testTag("export_training_pdf_button")
              ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              IconButton(onClick = { onDeleteProgram(activeProgram) }, modifier = Modifier.size(48.dp)) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = RushDanger)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Days Selector
        Text("TRAINING DAYS", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          items(programDays) { day ->
            val isDaySelected = (selectedDayId == day.id)
            Surface(
              color = if (isDaySelected) RushPurpleAccent else RushNavySurface,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDaySelected) RushPurpleAccent else RushNavyBorder
              ),
              modifier = Modifier
                .clickable { selectedDayId = day.id }
                .defaultMinSize(minHeight = 44.dp)
            ) {
              Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(
                  text = day.name,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isDaySelected) RushTextWhite else RushTextGray
                  )
                )
                if (day.targetMuscles.isNotBlank()) {
                  Text(
                    text = day.targetMuscles,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 9.sp,
                      color = if (isDaySelected) RushTextWhite.copy(alpha = 0.8f) else RushTextMuted
                    )
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Exercises in Selected Day
        val currentDay = programDays.find { it.id == selectedDayId }
        if (currentDay != null) {
          val dayExercises by getExercisesForDay(currentDay.id).collectAsState(initial = emptyList())

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "PRESCRIBED EXERCISES (${dayExercises.size})",
              style = MaterialTheme.typography.labelLarge.copy(color = RushTextWhite, fontWeight = FontWeight.Bold)
            )
            Button(
              onClick = { isAddExerciseToDayOpen = true },
              colors = ButtonDefaults.buttonColors(containerColor = RushCobalt),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.defaultMinSize(minHeight = 38.dp)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("+ Add Exercise", fontSize = 11.sp, color = RushBlack, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (dayExercises.isEmpty()) {
            EmptyStateView(
              icon = Icons.Default.AddCircleOutline,
              title = "No Exercises in ${currentDay.name}",
              message = "Add exercises with sets, reps, tempo, and training method to populate this training day.",
              actionLabel = "Add Exercise to Day",
              onAction = { isAddExerciseToDayOpen = true }
            )
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              contentPadding = PaddingValues(bottom = 60.dp)
            ) {
              items(dayExercises) { item ->
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
                          text = item.exerciseName,
                          style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RushTextWhite
                          )
                        )
                        if (item.trainingMethod != "Straight sets") {
                          Spacer(modifier = Modifier.width(6.dp))
                          Surface(
                            color = RushPurpleContainer,
                            shape = RoundedCornerShape(4.dp)
                          ) {
                            Text(
                              text = item.trainingMethod.uppercase(),
                              style = MaterialTheme.typography.labelSmall.copy(
                                color = RushPurpleLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                              ),
                              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                          }
                        }
                      }
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = "${item.sets} sets × ${item.reps} • Rest: ${item.restTime} • ${item.rirRpe ?: "RIR 1-2"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
                      )
                      if (item.notes?.isNotBlank() == true) {
                        Text(
                          text = "Cues: ${item.notes}",
                          style = MaterialTheme.typography.bodySmall.copy(color = RushPurpleLight, fontSize = 11.sp)
                        )
                      }
                    }

                    // Reorder & Delete controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      IconButton(
                        onClick = { onMoveDayExercise(currentDay.id, item, true) },
                        modifier = Modifier.size(36.dp)
                      ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = RushTextGray, modifier = Modifier.size(18.dp))
                      }
                      IconButton(
                        onClick = { onMoveDayExercise(currentDay.id, item, false) },
                        modifier = Modifier.size(36.dp)
                      ) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = RushTextGray, modifier = Modifier.size(18.dp))
                      }
                      IconButton(
                        onClick = { onDeleteDayExercise(item) },
                        modifier = Modifier.size(36.dp)
                      ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = RushDanger, modifier = Modifier.size(18.dp))
                      }
                    }
                  }
                }
              }
            }
          }

          // Add Exercise to Day Dialog
          if (isAddExerciseToDayOpen) {
            AddExerciseToDayDialog(
              exercises = exercises,
              onDismiss = { isAddExerciseToDayOpen = false },
              onAdd = { ex, sets, reps, rest, method, notes ->
                onAddExerciseToDay(currentDay.id, ex, sets, reps, rest, method, notes)
                isAddExerciseToDayOpen = false
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun ExerciseLibraryView(
  exercises: List<ExerciseEntity>,
  onAddCustomExercise: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedMuscle by remember { mutableStateOf("All") }

  val muscles = listOf("All", "Chest", "Back", "Quads", "Hamstrings", "Glutes", "Shoulders", "Biceps", "Triceps")

  val filtered = exercises.filter { ex ->
    val matchesSearch = ex.name.contains(searchQuery, ignoreCase = true) ||
        ex.movementPattern.contains(searchQuery, ignoreCase = true)
    val matchesMuscle = if (selectedMuscle == "All") true else ex.targetMuscle.equals(selectedMuscle, ignoreCase = true)
    matchesSearch && matchesMuscle
  }

  Column(modifier = Modifier.fillMaxSize()) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search exercise, pattern, or equipment...", color = RushTextMuted) },
      leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = RushTextGray) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("exercise_library_search")
    )

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      items(muscles) { m ->
        FilterChip(
          selected = selectedMuscle == m,
          onClick = { selectedMuscle = m },
          label = { Text(m, fontSize = 11.sp) },
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
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(bottom = 60.dp)
    ) {
      items(filtered) { ex ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = RushNavySurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, RushNavyBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = ex.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RushTextWhite)
              )
              Surface(
                color = RushPurpleContainer,
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = ex.targetMuscle.uppercase(),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = RushPurpleLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  ),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = "Equipment: ${ex.equipment} • Pattern: ${ex.movementPattern}",
              style = MaterialTheme.typography.bodySmall.copy(color = RushTextGray)
            )

            if (ex.instructions.isNotBlank()) {
              Text(
                text = "Biomechanical Execution:\n${ex.instructions}",
                style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite)
              )
            }

            if (ex.coachingCues.isNotBlank()) {
              Surface(
                color = RushDarkNavy,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Text(
                    text = "THE RUSH WAY COACHING CUES:",
                    style = MaterialTheme.typography.labelSmall.copy(color = RushPurpleLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                  )
                  Text(
                    text = ex.coachingCues,
                    style = MaterialTheme.typography.bodySmall.copy(color = RushTextWhite, fontSize = 11.sp)
                  )
                }
              }
            }

            if (ex.commonMistakes.isNotBlank()) {
              Text(
                text = "Common Faults: ${ex.commonMistakes}",
                style = MaterialTheme.typography.bodySmall.copy(color = RushWarning, fontSize = 11.sp)
              )
            }
          }
        }
      }
    }
  }
}

// Dialog to create a new program
@Composable
fun CreateProgramDialog(
  clients: List<ClientEntity>,
  onDismiss: () -> Unit,
  onCreate: (Long?, String, Int, String, List<String>) -> Unit
) {
  var programName by remember { mutableStateOf("Hypertrophy Block A") }
  var daysPerWeek by remember { mutableIntStateOf(4) }
  var description by remember { mutableStateOf("4-Day Upper/Lower Progressive Overload Regimen") }
  var selectedClientId by remember { mutableStateOf<Long?>(null) }

  val defaultDayNames = when (daysPerWeek) {
    3 -> listOf("Day 1: Full Body A", "Day 2: Full Body B", "Day 3: Full Body C")
    4 -> listOf("Day 1: Upper Strength", "Day 2: Lower Hypertrophy", "Day 3: Upper Hypertrophy", "Day 4: Lower Posterior")
    5 -> listOf("Day 1: Push Focus", "Day 2: Pull Focus", "Day 3: Legs Heavy", "Day 4: Upper Body", "Day 5: Lower Body")
    else -> listOf("Day 1: Push A", "Day 2: Pull A", "Day 3: Legs A", "Day 4: Push B", "Day 5: Pull B", "Day 6: Legs B")
  }

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
        Text("CREATE TRAINING PROGRAM", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))

        OutlinedTextField(
          value = programName,
          onValueChange = { programName = it },
          label = { Text("Program Title *") },
          modifier = Modifier.fillMaxWidth().testTag("program_name_input")
        )

        Text("Days Per Week Frequency", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(3, 4, 5, 6).forEach { d ->
            FilterChip(
              selected = daysPerWeek == d,
              onClick = { daysPerWeek = d },
              label = { Text("$d Days", fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RushPurpleContainer,
                selectedLabelColor = RushPurpleLight
              ),
              modifier = Modifier.defaultMinSize(minHeight = 40.dp)
            )
          }
        }

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Program Description & Mesocycle Focus") },
          modifier = Modifier.fillMaxWidth()
        )

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
            if (programName.isNotBlank()) {
              onCreate(selectedClientId, programName, daysPerWeek, description, defaultDayNames)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
          Text("Build Program & Generate Days", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}

// Dialog to add exercise to day
@Composable
fun AddExerciseToDayDialog(
  exercises: List<ExerciseEntity>,
  onDismiss: () -> Unit,
  onAdd: (ExerciseEntity, Int, String, String, String, String?) -> Unit
) {
  var selectedExercise by remember { mutableStateOf(exercises.firstOrNull()) }
  var setsStr by remember { mutableStateOf("3") }
  var repsStr by remember { mutableStateOf("8-10") }
  var restStr by remember { mutableStateOf("2-3 min") }
  var trainingMethod by remember { mutableStateOf("Straight sets") }
  var notes by remember { mutableStateOf("") }

  val methods = listOf(
    "Straight sets",
    "Superset",
    "Drop set",
    "Rest-pause",
    "Myo-reps",
    "Giant set",
    "Failure set",
    "AMRAP",
    "Tempo training"
  )

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = RushNavySurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, RushPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("PRESCRIBE EXERCISE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))

        Text("Select Exercise from Library", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(exercises) { ex ->
            FilterChip(
              selected = selectedExercise?.id == ex.id,
              onClick = { selectedExercise = ex },
              label = { Text(ex.name, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RushPurpleContainer,
                selectedLabelColor = RushPurpleLight
              ),
              modifier = Modifier.defaultMinSize(minHeight = 40.dp)
            )
          }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = setsStr,
            onValueChange = { setsStr = it },
            label = { Text("Sets") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = repsStr,
            onValueChange = { repsStr = it },
            label = { Text("Reps (e.g. 8-10)") },
            modifier = Modifier.weight(1f)
          )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = restStr,
            onValueChange = { restStr = it },
            label = { Text("Rest Period") },
            modifier = Modifier.weight(1f)
          )
        }

        Text("Bodybuilding Intensifier / Method", style = MaterialTheme.typography.labelSmall.copy(color = RushTextGray))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(methods) { m ->
            FilterChip(
              selected = trainingMethod == m,
              onClick = { trainingMethod = m },
              label = { Text(m, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RushPurpleAccent,
                selectedLabelColor = RushTextWhite
              ),
              modifier = Modifier.defaultMinSize(minHeight = 40.dp)
            )
          }
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Execution Cues (e.g. 3-sec eccentric)") },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
          onClick = {
            if (selectedExercise != null) {
              onAdd(
                selectedExercise!!,
                setsStr.toIntOrNull() ?: 3,
                repsStr,
                restStr,
                trainingMethod,
                notes
              )
            }
          },
          enabled = selectedExercise != null,
          colors = ButtonDefaults.buttonColors(containerColor = RushPurpleAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
          Text("Add to Program Day", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}

// Dialog to add custom exercise
@Composable
fun AddCustomExerciseDialog(
  onDismiss: () -> Unit,
  onSave: (ExerciseEntity) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var muscle by remember { mutableStateOf("Chest") }
  var equipment by remember { mutableStateOf("Barbell") }
  var pattern by remember { mutableStateOf("Horizontal Push") }
  var instructions by remember { mutableStateOf("") }
  var cues by remember { mutableStateOf("") }
  var mistakes by remember { mutableStateOf("") }

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
        Text("ADD CUSTOM EXERCISE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = RushTextWhite))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Exercise Name *") },
          modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = muscle,
            onValueChange = { muscle = it },
            label = { Text("Target Muscle") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = equipment,
            onValueChange = { equipment = it },
            label = { Text("Equipment") },
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = pattern,
          onValueChange = { pattern = it },
          label = { Text("Movement Pattern") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = cues,
          onValueChange = { cues = it },
          label = { Text("The Rush Way Coaching Cues") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = instructions,
          onValueChange = { instructions = it },
          label = { Text("Execution Instructions") },
          modifier = Modifier.fillMaxWidth()
        )

        Button(
          onClick = {
            if (name.isNotBlank()) {
              onSave(
                ExerciseEntity(
                  name = name,
                  targetMuscle = muscle,
                  equipment = equipment,
                  movementPattern = pattern,
                  coachingCues = cues,
                  instructions = instructions,
                  commonMistakes = mistakes,
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
          Text("Save to Exercise Library", fontWeight = FontWeight.Bold, color = RushTextWhite)
        }
      }
    }
  }
}
