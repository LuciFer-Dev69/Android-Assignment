package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ActivityEntity
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.ui.theme.PinkPrimary
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    repository: UserPreferencesRepository,
    activityId: Int? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var name by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var targetSteps by remember { mutableStateOf("") }
    var workoutType by remember { mutableStateOf("Walking") }
    var calories by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var isTask by remember { mutableStateOf(false) }
    var existingUserId by remember { mutableIntStateOf(0) }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Logic for Automatic Calorie Calculation
    LaunchedEffect(steps, workoutType) {
        val s = steps.toIntOrNull() ?: 0
        if (s > 0) {
            val multiplier = when (workoutType) {
                "Walking" -> 0.04
                "Running" -> 0.06
                "Cycling" -> 0.05
                "Gym" -> 0.05
                "Yoga" -> 0.03
                else -> 0.04
            }
            calories = (s * multiplier).toInt().toString()
        }
    }

    LaunchedEffect(activityId) {
        if (activityId != null) {
            val activity = db.activityDao().getActivityById(activityId)
            if (activity != null) {
                name = activity.name
                steps = activity.steps.toString()
                targetSteps = if (activity.targetSteps > 0) activity.targetSteps.toString() else ""
                isTask = activity.targetSteps > 0
                workoutType = activity.workoutType
                calories = activity.calories.toString()
                date = activity.date
                existingUserId = activity.userId
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (activityId == null) "Log Activity" else "Edit Activity",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Activity Title") },
                placeholder = { Text("e.g. Morning Walk") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                shape = MaterialTheme.shapes.large
            )

            val workoutTypes = listOf("Walking", "Running", "Cycling", "Gym", "Yoga", "Other")
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = workoutType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Workout Type") },
                    leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    workoutTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                workoutType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- Task Toggle ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = PinkPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set as Goal/Task", fontWeight = FontWeight.Bold)
                }
                Switch(checked = isTask, onCheckedChange = { isTask = it }, colors = SwitchDefaults.colors(checkedThumbColor = PinkPrimary))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = steps,
                    onValueChange = { if (it.all { char -> char.isDigit() }) steps = it },
                    label = { Text("Steps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.DirectionsRun, contentDescription = null) },
                    shape = MaterialTheme.shapes.large
                )

                if (isTask) {
                    OutlinedTextField(
                        value = targetSteps,
                        onValueChange = { if (it.all { char -> char.isDigit() }) targetSteps = it },
                        label = { Text("Target") },
                        placeholder = { Text("Goal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                        shape = MaterialTheme.shapes.large
                    )
                } else {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { if (it.all { char -> char.isDigit() }) calories = it },
                        label = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Whatshot, contentDescription = null) },
                        shape = MaterialTheme.shapes.large,
                        supportingText = { Text("Auto-calculated from steps") }
                    )
                }
            }

            OutlinedTextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { 
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    }
                },
                shape = MaterialTheme.shapes.large
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val userId = if (activityId == null) {
                            repository.userId.firstOrNull() ?: 0
                        } else {
                            existingUserId
                        }
                        
                        val s = steps.toIntOrNull() ?: 0
                        val ts = if (isTask) targetSteps.toIntOrNull() ?: 0 else 0
                        val activity = ActivityEntity(
                            id = activityId ?: 0,
                            userId = userId,
                            name = name,
                            steps = s,
                            targetSteps = ts,
                            workoutType = workoutType,
                            calories = calories.toIntOrNull() ?: 0,
                            date = date,
                            isCompleted = isTask && ts > 0 && s >= ts
                        )
                        if (activityId == null) {
                            db.activityDao().insertActivity(activity)
                        } else {
                            db.activityDao().updateActivity(activity)
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotBlank() && workoutType.isNotBlank(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) {
                Text(
                    if (activityId == null) "Log Activity" else "Update Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
