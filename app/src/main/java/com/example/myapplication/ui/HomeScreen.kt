package com.example.myapplication.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.Nutrient
import com.example.myapplication.data.RetrofitInstance

@Composable
fun HomeScreen(
    stepsToday: Int,
    stepGoal: Int,
    caloriesBurned: Int,
    workoutsCount: Int,
    onAddActivityClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onWorkoutSuggestionsClick: () -> Unit,
    onNutrientsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWaterClick: () -> Unit,
    onBmiClick: () -> Unit,
    onAnalyticsClick: () -> Unit
) {
    var nutrients by remember { mutableStateOf<List<Nutrient>>(emptyList()) }
    var isLoadingNutrients by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.getNutrients()
            nutrients = response.results.take(10)
            isLoadingNutrients = false
        } catch (e: Exception) {
            isLoadingNutrients = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Keep it up!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Main Progress Card
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onAnalyticsClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CircularProgressDashboard(
                    progress = if (stepGoal > 0) stepsToday.toFloat() / stepGoal else 0f,
                    currentSteps = stepsToday,
                    goalSteps = stepGoal
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatItem(
                        icon = Icons.Default.Whatshot,
                        value = "$caloriesBurned",
                        label = "kcal",
                        color = Color(0xFFF44336)
                    )
                    StatItem(
                        icon = Icons.Default.FitnessCenter,
                        value = "$workoutsCount",
                        label = "Workouts",
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // New Pro Features Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                title = "Hydration",
                icon = Icons.Default.LocalDrink,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f),
                onClick = onWaterClick
            )
            FeatureCard(
                title = "BMI Calc",
                icon = Icons.Default.Calculate,
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f),
                onClick = onBmiClick
            )
            FeatureCard(
                title = "Analytics",
                icon = Icons.Default.BarChart,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f),
                onClick = onAnalyticsClick
            )
        }

        // Nutrients Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onNutrientsClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nutrition Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "See All",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (isLoadingNutrients) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(nutrients) { nutrient ->
                        NutrientCard(nutrient, onClick = onNutrientsClick)
                    }
                }
            }
        }

        Text(
            text = "Quick Actions",
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        DashboardButton(
            text = "Add Activity",
            icon = Icons.Default.Add,
            onClick = onAddActivityClick,
            containerColor = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardButton(
                text = "Logs",
                icon = Icons.Default.History,
                onClick = onViewLogsClick,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
            DashboardButton(
                text = "Ideas",
                icon = Icons.Default.Lightbulb,
                onClick = onWorkoutSuggestionsClick,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun FeatureCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(100.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun NutrientCard(nutrient: Nutrient, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            if (nutrient.imageUrl != null) {
                AsyncImage(
                    model = nutrient.imageUrl,
                    contentDescription = nutrient.name,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = nutrient.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = nutrient.description.replace(Regex("<[^>]*>"), ""), style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}

@Composable
fun CircularProgressDashboard(progress: Float, currentSteps: Int, goalSteps: Int, size: Dp = 140.dp, strokeWidth: Dp = 12.dp) {
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(1000), label = "progress")
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.LightGray.copy(alpha = 0.3f), style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round))
            drawArc(color = Color(0xFF6200EE), startAngle = -90f, sweepAngle = 360 * animatedProgress, useCenter = false, style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = currentSteps.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = "of $goalSteps", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = color)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun DashboardButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, containerColor: Color = MaterialTheme.colorScheme.surface, contentColor: Color = contentColorFor(containerColor)) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor), shape = MaterialTheme.shapes.large, elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
