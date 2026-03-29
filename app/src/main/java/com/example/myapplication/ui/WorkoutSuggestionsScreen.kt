package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.ExerciseDbModel

// Curated list of high-quality Unsplash IDs for gym/fitness
private val GYM_IMAGE_IDS = listOf(
    "1534438327276-14e5300c3a48", "1583454110551-21f2fa20019a", "1540497077202-7c8a3999166f",
    "1571019614242-c5c5dee9f50b", "1517836357463-d25dfeac3438", "1594882645126-14020914d58d",
    "1534258936925-a5d1b1df3961", "1550345332-09e3ac987658", "1526506118085-60ce8714f875",
    "1599058917232-d750089b6e7c", "1574673402454-043593922c1d", "1605296867304-46d5465a13f1",
    "1517130038641-a774d04afb3c", "1571902943202-507ec2618e8f", "1591115765373-590d7f124671",
    "1597452485669-2c7bb5fef90d", "1434608519344-49d77a699e1d", "1534438202075-427f3c674291"
)

@Composable
fun ExerciseImage(exercise: ExerciseDbModel, isDetail: Boolean = false) {
    val size = if (isDetail) 800 else 300
    val unsplashUrl = remember(exercise.id) {
        val index = Math.abs(exercise.id.hashCode()) % GYM_IMAGE_IDS.size
        "https://images.unsplash.com/photo-${GYM_IMAGE_IDS[index]}?q=80&w=$size&auto=format&fit=crop"
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(exercise.gifUrl.ifBlank { unsplashUrl })
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = if (isDetail) {
            Modifier.fillMaxWidth().height(300.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
        } else {
            Modifier.size(100.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
        },
        contentScale = ContentScale.Crop,
    ) {
        val state = painter.state
        if (state is AsyncImagePainter.State.Error) {
            AsyncImage(
                model = unsplashUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            SubcomposeAsyncImageContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSuggestionsScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedExercise by remember { mutableStateOf<ExerciseDbModel?>(null) }
    var showTipDialog by remember { mutableStateOf(false) }
    var currentTip by remember { mutableStateOf("") }

    val tips = listOf(
        "Drink at least 500ml of water 30 minutes before your workout.",
        "Include protein in your post-workout meal to help muscle recovery.",
        "Don't skip the warm-up; it prepares your heart and muscles for action.",
        "Consistency is key. Even a 15-minute workout is better than none.",
        "Focus on your form rather than the weight to avoid injuries.",
        "Get 7-9 hours of sleep; muscles grow while you rest, not at the gym."
    )

    if (selectedExercise != null) {
        ExerciseDbDetailScreen(
            exercise = selectedExercise!!,
            onBack = { selectedExercise = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Visual Workout Guide", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                // Get Daily Tip Section
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Coach's Tip", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Tap for expert advice", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                        Button(
                            onClick = {
                                currentTip = tips.random()
                                showTipDialog = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.TipsAndUpdates, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Get Tip")
                        }
                    }
                }

                when (val state = uiState) {
                    is WorkoutUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is WorkoutUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Button(onClick = { viewModel.loadWorkouts() }) { Text("Retry") }
                            }
                        }
                    }
                    is WorkoutUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No exercises found.", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    is WorkoutUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.suggestions) { suggestion ->
                                ExerciseItem(suggestion) { selectedExercise = suggestion }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTipDialog) {
        AlertDialog(
            onDismissRequest = { showTipDialog = false },
            title = { Text("Fitness Tip") },
            text = { Text(currentTip) },
            confirmButton = {
                TextButton(onClick = { showTipDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
fun ExerciseItem(exercise: ExerciseDbModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ExerciseImage(exercise = exercise)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(exercise.name.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(exercise.bodyPart.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Text("Target: ${exercise.target}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDbDetailScreen(exercise: ExerciseDbModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExerciseImage(exercise = exercise, isDetail = true)
            Spacer(modifier = Modifier.height(24.dp))
            Text(exercise.name.uppercase(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            
            Row(modifier = Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(onClick = {}, label = { Text(exercise.bodyPart) })
                SuggestionChip(onClick = {}, label = { Text(exercise.equipment) })
                SuggestionChip(onClick = {}, label = { Text(exercise.target) })
            }

            Text("Instructions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            exercise.instructions.forEachIndexed { index, step ->
                Text("${index + 1}. $step", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Back to Exercises")
            }
        }
    }
}
