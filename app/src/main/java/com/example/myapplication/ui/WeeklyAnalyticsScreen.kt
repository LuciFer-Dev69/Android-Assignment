package com.example.myapplication.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.*
import com.example.myapplication.ui.theme.DarkPinkGradient
import com.example.myapplication.ui.theme.PinkGradient
import com.example.myapplication.ui.theme.PinkPrimary
import com.example.myapplication.ui.theme.PinkSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyAnalyticsScreen(
    activities: List<ActivityEntity>,
    waterLogs: List<WaterLogEntity>,
    foodLogs: List<FoodLogEntity>,
    weightHistory: List<WeightLogEntity>,
    onBack: () -> Unit
) {
    var isWeeklyView by remember { mutableStateOf(false) }
    var selectedWeekOffset by remember { mutableIntStateOf(0) }
    
    val weekData = remember(activities, selectedWeekOffset) { getWeekData(activities, selectedWeekOffset) }
    val foodWeekData = remember(foodLogs, selectedWeekOffset) { getFoodWeekData(foodLogs, selectedWeekOffset) }
    val waterWeekData = remember(waterLogs, selectedWeekOffset) { getWaterWeekData(waterLogs, selectedWeekOffset) }
    val weightWeekData = remember(weightHistory, selectedWeekOffset) { getWeightWeekData(weightHistory, selectedWeekOffset) }
    val weekRangeText = remember(selectedWeekOffset) { getWeekRangeText(selectedWeekOffset) }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val backgroundBrush = if (isDark) DarkPinkGradient else PinkGradient

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Performance Tracker", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(if (isWeeklyView) weekRangeText else "Today's Insight", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Toggle Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(25.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (!isWeeklyView) PinkPrimary else Color.Transparent, RoundedCornerShape(25.dp))
                            .clickable { isWeeklyView = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Daily", fontWeight = FontWeight.Bold, color = if (!isWeeklyView) Color.White else Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isWeeklyView) PinkPrimary else Color.Transparent, RoundedCornerShape(25.dp))
                            .clickable { isWeeklyView = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Weekly", fontWeight = FontWeight.Bold, color = if (isWeeklyView) Color.White else Color.Gray)
                    }
                }

                if (isWeeklyView) {
                    WeeklyAnalyticsContent(weekData, foodWeekData, waterWeekData, weightWeekData, weekRangeText, { selectedWeekOffset++ }, { if (selectedWeekOffset > 0) selectedWeekOffset-- }, selectedWeekOffset > 0)
                } else {
                    DailyAnalyticsContent(activities, foodLogs)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DailyAnalyticsContent(activities: List<ActivityEntity>, foodLogs: List<FoodLogEntity>) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayActs = activities.filter { it.date == today }
    val todayFoods = foodLogs.filter { it.date == today }
    
    val burned = todayActs.sumOf { it.calories }
    val intake = todayFoods.sumOf { it.calories }
    val steps = todayActs.sumOf { it.steps }

    DailySummaryMainCard(steps = steps, burned = burned, intake = intake)

    AnalyticsSection(
        title = "Energy Balance",
        data = listOf("Burned" to burned.toFloat(), "Intake" to intake.toFloat()),
        unit = "kcal",
        color = PinkPrimary
    )

    if (todayFoods.isNotEmpty()) {
        val p = todayFoods.sumOf { it.protein }
        val c = todayFoods.sumOf { it.carbs }
        val f = todayFoods.sumOf { it.fats }
        MacroDistributionCard("Today's Macros", p.toFloat(), c.toFloat(), f.toFloat())
    }

    if (todayActs.isNotEmpty()) {
        WorkoutVarietySection(todayActs)
    }
}

@Composable
fun WeeklyAnalyticsContent(
    weekData: List<Triple<String, Int, Int>>,
    foodWeekData: List<Pair<String, Int>>,
    waterWeekData: List<Pair<String, Int>>,
    weightWeekData: List<Pair<String, Double>>,
    weekRangeText: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    canNext: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }
        Text(text = weekRangeText, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext, enabled = canNext) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
    }

    WeightTrendCard(weightWeekData)

    AnalyticsSection(
        title = "Steps Overview",
        data = weekData.map { it.first to it.second.toFloat() },
        unit = "steps",
        color = PinkPrimary
    )

    AnalyticsSection(
        title = "Calorie Balance (Intake vs Burned)",
        data = weekData.mapIndexed { i, triple -> triple.first to (foodWeekData[i].second - triple.third).toFloat() },
        unit = "net kcal",
        color = Color(0xFF4CAF50)
    )

    WallOfFameSection(weekData, waterWeekData)
}

@Composable
fun MacroDistributionCard(title: String, p: Float, c: Float, f: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            val total = (p + c + f).coerceAtLeast(1f)
            Row(modifier = Modifier.fillMaxWidth().height(20.dp).clip(CircleShape)) {
                Box(modifier = Modifier.weight(p/total).fillMaxHeight().background(Color(0xFF4CAF50)))
                Box(modifier = Modifier.weight(c/total).fillMaxHeight().background(Color(0xFFFF9800)))
                Box(modifier = Modifier.weight(f/total).fillMaxHeight().background(Color(0xFFF44336)))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                MacroLegend("Protein", "${p.toInt()}g", Color(0xFF4CAF50))
                MacroLegend("Carbs", "${c.toInt()}g", Color(0xFFFF9800))
                MacroLegend("Fats", "${f.toInt()}g", Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun MacroLegend(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text("$label: $value", fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun WeightTrendCard(data: List<Pair<String, Double>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = PinkPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Weight Trend", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            val weights = data.map { it.second.toFloat() }.filter { it > 0 }
            if (weights.size < 2) {
                Text("Log weight daily to see trend line", color = Color.Gray, fontSize = 12.sp)
            } else {
                Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    val maxW = weights.max()
                    val minW = weights.min()
                    val range = (maxW - minW).coerceAtLeast(1f)
                    val width = size.width
                    val height = size.height
                    val path = Path()
                    
                    weights.forEachIndexed { i, w ->
                        val x = i * (width / (weights.size - 1))
                        val y = height - ((w - minW) / range * height)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, PinkPrimary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                }
            }
        }
    }
}

@Composable
fun WallOfFameSection(weekData: List<Triple<String, Int, Int>>, waterData: List<Pair<String, Int>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Weekly Wall of Fame", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AchievementCard("Best Steps", "${weekData.maxOf { it.second }}", Icons.Default.EmojiEvents, Modifier.weight(1f))
            AchievementCard("Hydration", "${waterData.maxOf { it.second }}ml", Icons.Default.Star, Modifier.weight(1f))
        }
    }
}

@Composable
fun AchievementCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = PinkPrimary, modifier = Modifier.size(24.dp))
            Text(title, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Bold, color = PinkPrimary)
        }
    }
}

@Composable
fun WorkoutVarietySection(acts: List<ActivityEntity>) {
    val types = acts.groupBy { it.workoutType }.mapValues { it.value.size }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Activity Variety", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            types.forEach { (type, count) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    LinearProgressIndicator(progress = { count.toFloat() / acts.size }, modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape), color = PinkPrimary)
                    Spacer(Modifier.width(12.dp))
                    Text(type, fontSize = 12.sp, modifier = Modifier.width(60.dp))
                }
            }
        }
    }
}

@Composable
fun DailySummaryMainCard(steps: Int, burned: Int, intake: Int) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(PinkPrimary, PinkSecondary)))) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Daily Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = String.format("%.2f", steps * 0.0008), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = Color.White)
                    Text(text = " km", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 8.dp))
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryChip("Burned", "$burned kcal")
                    SummaryChip("Intake", "$intake kcal")
                }
            }
        }
    }
}

@Composable
fun SummaryChip(label: String, value: String) {
    Surface(color = Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun AnalyticsSection(title: String, data: List<Pair<String, Float>>, unit: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (data.isEmpty()) {
                    Box(modifier = Modifier.height(120.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("No data", color = Color.Gray) }
                } else {
                    Row(modifier = Modifier.height(120.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                        val maxVal = data.maxOf { it.second }.coerceAtLeast(1f)
                        data.forEach { (day, value) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val barHeight = (value.coerceAtLeast(0f) / maxVal * 80).dp
                                Box(modifier = Modifier.width(20.dp).height(barHeight).background(brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.7f), color)), shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = day, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
                Text(text = "Total: ${data.sumOf { it.second.toInt() }} $unit", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun getWeekData(activities: List<ActivityEntity>, weekOffset: Int) = List(7) { i ->
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(6 - i + (weekOffset * 7))) }
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    val day = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
    val acts = activities.filter { it.date == date }
    Triple(day, acts.sumOf { it.steps }, acts.sumOf { it.calories })
}

private fun getFoodWeekData(foodLogs: List<FoodLogEntity>, weekOffset: Int) = List(7) { i ->
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(6 - i + (weekOffset * 7))) }
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    val day = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
    day to foodLogs.filter { it.date == date }.sumOf { it.calories }
}

private fun getWaterWeekData(waterLogs: List<WaterLogEntity>, weekOffset: Int) = List(7) { i ->
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(6 - i + (weekOffset * 7))) }
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    val day = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
    day to waterLogs.filter { it.date == date }.sumOf { it.amountMl }
}

private fun getWeightWeekData(weights: List<WeightLogEntity>, weekOffset: Int) = List(7) { i ->
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(6 - i + (weekOffset * 7))) }
    val day = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
    val startOfDay = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
    val endOfDay = cal.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
    day to (weights.filter { it.date in startOfDay..endOfDay }.lastOrNull()?.weight ?: 0.0)
}

private fun getWeekRangeText(weekOffset: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -(weekOffset * 7))
    val end = SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, -6)
    val start = SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
    return if (weekOffset == 0) "This Week" else "$start - $end"
}
