package com.example.myapplication.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.ui.theme.DarkPinkGradient
import com.example.myapplication.ui.theme.PinkGradient
import com.example.myapplication.ui.theme.PinkPrimary

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: UserPreferencesRepository,
    stepsToday: Int,
    stepGoal: Int,
    caloriesBurned: Int,
    workoutsCount: Int,
    waterToday: Int,
    weeklyStepAverage: Int,
    activityComparisonText: String,
    onAddActivityClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onWorkoutSuggestionsClick: () -> Unit,
    onNutrientsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWaterClick: () -> Unit,
    onBmiClick: () -> Unit,
    onCaloriesClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onWeightClick: () -> Unit
) {
    val username by repository.username.collectAsState(initial = "User")
    val profileImageUri by repository.profileImageUri.collectAsState(initial = null)

    val activity = LocalActivity.current
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val backgroundBrush = if (isDark) DarkPinkGradient else PinkGradient
    val primaryColor = MaterialTheme.colorScheme.primary

    // Calculate Health Score (0-100)
    val healthScore = remember(stepsToday, stepGoal, waterToday, workoutsCount) {
        val stepScore = if (stepGoal > 0) (stepsToday.toFloat() / stepGoal).coerceAtMost(1f) else 0f
        val waterScore = (waterToday.toFloat() / 2000f).coerceAtMost(1f) // Target 2L
        val activityScore = if (workoutsCount > 0) 1f else 0.5f
        ((stepScore * 0.4f + waterScore * 0.4f + activityScore * 0.2f) * 100).toInt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent, 
            topBar = {}
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- Top Profile Section ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hey, ${username ?: "User"}! 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Ready for a workout?",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    Surface(
                        onClick = onProfileClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        modifier = Modifier.size(60.dp).border(2.dp, primaryColor, CircleShape)
                    ) {
                        if (profileImageUri != null) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = primaryColor,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // --- Health Score Card ---
                HealthScoreCard(score = healthScore)

                // --- Daily Summary Report ---
                DailySummaryReport(
                    stepsToday = stepsToday,
                    stepGoal = stepGoal,
                    waterToday = waterToday
                )

                // --- Activity Comparison Card (NEW) ---
                ActivityCheckCard(
                    weeklyAverage = weeklyStepAverage,
                    comparisonText = activityComparisonText
                )

                // --- Hero Progress Card ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = primaryColor),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CircularProgressDashboard(
                            progress = if (stepGoal > 0) stepsToday.toFloat() / stepGoal else 0f,
                            currentSteps = stepsToday,
                            goalSteps = stepGoal,
                            size = 150.dp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatItem(
                                icon = Icons.Default.Whatshot,
                                value = "$caloriesBurned",
                                label = "kcal",
                                color = Color(0xFFFF5252)
                            )
                            StatItem(
                                icon = Icons.Default.Timer,
                                value = "$workoutsCount",
                                label = "Activities",
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                // --- Interactive Insight Card ---
                DailyInsightCard(stepsToday = stepsToday, stepGoal = stepGoal, waterToday = waterToday)

                // --- Features Section ---
                Text(
                    text = "Health Trackers",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                val features = listOf(
                    FeatureData("Hydration", Icons.Default.LocalDrink, Color(0xFF2196F3), onWaterClick),
                    FeatureData("BMI Calc", Icons.Default.Scale, Color(0xFF9C27B0), onBmiClick),
                    FeatureData("Meal Log", Icons.Default.Restaurant, Color(0xFFE91E63), onCaloriesClick),
                    FeatureData("Weights", Icons.Default.MonitorWeight, Color(0xFF673AB7), onWeightClick),
                    FeatureData("Analytics", Icons.Default.Timeline, Color(0xFFFF9800), onAnalyticsClick)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    features.chunked(2).forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { feature ->
                                FeatureCard(feature, Modifier.weight(1f))
                            }
                        }
                    }
                }

                // --- Bottom Action Area ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.horizontalGradient(listOf(primaryColor, MaterialTheme.colorScheme.secondary)))
                        .clickable { onAddActivityClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = if (isDark) Color.Black else Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("LOG NEW ACTIVITY", color = if (isDark) Color.Black else Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryActionBtn("HISTORY", Icons.Default.History, onViewLogsClick, Modifier.weight(1f))
                    SecondaryActionBtn("IDEAS", Icons.Default.Lightbulb, onWorkoutSuggestionsClick, Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun HealthScoreCard(score: Int) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else PinkPrimary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Daily Health Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = when {
                        score > 80 -> "Excellent progress!"
                        score > 50 -> "Keep it up!"
                        else -> "Start moving!"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "$score%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else PinkPrimary
            )
        }
    }
}

@Composable
fun ActivityCheckCard(weeklyAverage: Int, comparisonText: String) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QueryStats, contentDescription = null, tint = if (isDark) Color.White else Color.Gray)
                Spacer(Modifier.width(12.dp))
                Text("Weekly Activity Check", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Your weekly average: $weeklyAverage steps/day",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = comparisonText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PinkPrimary
            )
        }
    }
}

@Composable
fun DailySummaryReport(stepsToday: Int, stepGoal: Int, waterToday: Int) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val stepsLeft = (stepGoal - stepsToday).coerceAtLeast(0)
    val waterPercent = (waterToday.toFloat() / 2000f * 100).toInt().coerceAtMost(100)
    
    val summaryText = if (stepsLeft > 0) {
        "You\u0027re $stepsLeft steps away from your goal and have drunk $waterPercent% of your daily water!"
    } else {
        "Goal achieved! You\u0027ve crushed your steps and drunk $waterPercent% of your daily water!"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else PinkPrimary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Summarize, contentDescription = null, tint = if (isDark) Color.White else PinkPrimary)
            Spacer(Modifier.width(12.dp))
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DailyInsightCard(stepsToday: Int, stepGoal: Int, waterToday: Int) {
    val insight = remember(stepsToday, waterToday) {
        when {
            stepsToday >= stepGoal -> "\uD83C\uDFC6 Goal Reached! You\u0027re crushing it today!"
            stepsToday > stepGoal * 0.7f -> "\uD83D\uDD25 So close! Just a few more steps to hit your goal."
            waterToday < 1000 -> "\uD83D\uDCA7 Stay hydrated! Remember to drink some water."
            else -> "✨ Keep moving! Every step counts towards your health."
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(text = insight, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun FeatureCard(data: FeatureData, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(80.dp).clickable { data.onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = data.color.copy(alpha = 0.15f)
            ) {
                Icon(data.icon, contentDescription = null, tint = data.color, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(data.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SecondaryActionBtn(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier.height(60.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun CircularProgressDashboard(progress: Float, currentSteps: Int, goalSteps: Int, size: Dp) {
    val animProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(1500))
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.Gray.copy(alpha = 0.1f), style = Stroke(14.dp.toPx(), cap = StrokeCap.Round))
            drawArc(
                brush = Brush.sweepGradient(listOf(secondaryColor, primaryColor, secondaryColor)),
                startAngle = -90f,
                sweepAngle = 360 * animProgress,
                useCenter = false,
                style = Stroke(14.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = String.format("%,d", currentSteps), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "steps", style = MaterialTheme.typography.labelSmall, color = primaryColor)
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(11.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

data class FeatureData(val title: String, val icon: ImageVector, val color: Color, val onClick: () -> Unit)
