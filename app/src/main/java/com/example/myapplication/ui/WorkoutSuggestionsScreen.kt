package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.RetrofitInstance
import com.example.myapplication.data.WorkoutSuggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSuggestionsScreen(onBack: () -> Unit) {
    var suggestions by remember { mutableStateOf<List<WorkoutSuggestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // State for the detail view
    var selectedExercise by remember { mutableStateOf<WorkoutSuggestion?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.getWorkouts()
            // Less strict filter to ensure we show content. 
            // We handle missing images with a professional placeholder.
            val filtered = response.results.filter { 
                it.name.isNotBlank() && it.name != "Untitled Exercise" 
            }
            
            if (filtered.isEmpty()) {
                // If everything was filtered, let's at least show what we have
                suggestions = response.results.take(20)
            } else {
                suggestions = filtered
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load suggestions: ${e.message}"
            isLoading = false
        }
    }

    if (selectedExercise != null) {
        ExerciseDetailScreen(
            suggestion = selectedExercise!!,
            onBack = { selectedExercise = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Workout Ideas", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { 
                                isLoading = true
                                errorMessage = null
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                suggestions.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No workouts found. Try again later.")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(suggestions) { suggestion ->
                            WorkoutSuggestionItem(
                                suggestion = suggestion,
                                onDetailsClick = { selectedExercise = suggestion }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutSuggestionItem(
    suggestion: WorkoutSuggestion,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (suggestion.imageUrl != null) {
                    AsyncImage(
                        model = suggestion.imageUrl,
                        contentDescription = suggestion.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Professional Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No Preview Available",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (suggestion.name == "Untitled Exercise") "Daily Workout" else suggestion.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val cleanDescription = suggestion.description.replace(Regex("<[^>]*>"), "")
                Text(
                    text = if (cleanDescription.isBlank()) "Targeting: ${suggestion.muscleGroup}" else cleanDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "${suggestion.caloriesBurned} kcal",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    AssistChip(
                        onClick = onDetailsClick,
                        label = { Text("Exercise Details") }
                    )
                }
            }
        }
    }
}
