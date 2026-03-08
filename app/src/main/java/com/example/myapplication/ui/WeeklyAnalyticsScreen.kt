package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.ActivityEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyAnalyticsScreen(
    activities: List<ActivityEntity>,
    onBack: () -> Unit
) {
    val last7Days = remember(activities) { getLast7DaysData(activities) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Analytics", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Step Trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Simple Custom Bar Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .height(200.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxSteps = last7Days.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
                    
                    last7Days.forEach { (day, steps) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val barHeight = (steps.toFloat() / maxSteps * 150).dp
                            Box(
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(barHeight)
                                    .background(
                                        color = if (steps > 5000) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = day, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Summary Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnalyticsStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Avg Steps",
                    value = "${if (last7Days.isNotEmpty()) last7Days.map { it.second }.average().toInt() else 0}",
                    color = MaterialTheme.colorScheme.primary
                )
                AnalyticsStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total kcal",
                    value = "${activities.sumOf { it.calories }}",
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(modifier: Modifier, title: String, value: String, color: Color) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

private fun getLast7DaysData(activities: List<ActivityEntity>): List<Pair<String, Int>> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val calendar = Calendar.getInstance()
    
    val results = mutableListOf<Pair<String, Int>>()
    
    for (i in 6 downTo 0) {
        val tempCal = Calendar.getInstance()
        tempCal.add(Calendar.DAY_OF_YEAR, -i)
        val dateString = dateFormat.format(tempCal.time)
        val dayName = dayFormat.format(tempCal.time)
        
        val dailySteps = activities.filter { it.date == dateString }.sumOf { it.steps }
        results.add(dayName to dailySteps)
    }
    
    return results
}
