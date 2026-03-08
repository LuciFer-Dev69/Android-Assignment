package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.UserPreferencesRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: UserPreferencesRepository,
    onBack: () -> Unit
) {
    val isDarkMode by repository.isDarkMode.collectAsState(initial = false)
    val stepGoal by repository.stepGoal.collectAsState(initial = 5000)
    val scope = rememberCoroutineScope()
    
    var stepGoalInput by remember(stepGoal) { mutableStateOf(stepGoal.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Theme Setting
            Card(
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
                            text = "Dark Mode",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isDarkMode) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { 
                            scope.launch { repository.updateDarkMode(it) }
                        }
                    )
                }
            }

            // Step Goal Setting
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Daily Step Goal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = stepGoalInput,
                        onValueChange = { 
                            stepGoalInput = it.filter { char -> char.isDigit() }
                        },
                        label = { Text("Steps") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Button(
                        onClick = {
                            val newGoal = stepGoalInput.toIntOrNull() ?: 5000
                            scope.launch { repository.updateStepGoal(newGoal) }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save Goal")
                    }
                }
            }
        }
    }
}
