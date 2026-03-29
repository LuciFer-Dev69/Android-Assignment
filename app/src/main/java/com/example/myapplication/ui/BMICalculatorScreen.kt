package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.PinkPrimary
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var heightMain by remember { mutableStateOf("") }
    var heightInches by remember { mutableStateOf("") }
    var bmiResult by remember { mutableStateOf<Double?>(null) }
    var isMetric by remember { mutableStateOf(true) }

    val bmiCategory = when {
        bmiResult == null -> ""
        bmiResult!! < 18.5 -> "Underweight"
        bmiResult!! < 25.0 -> "Healthy Weight"
        bmiResult!! < 30.0 -> "Overweight"
        else -> "Obesity"
    }

    val categoryColor = when (bmiCategory) {
        "Healthy Weight" -> Color(0xFF4CAF50)
        "Underweight" -> Color(0xFFFFC107)
        "Overweight" -> Color(0xFFFF9800)
        "Obesity" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BMI Calculator", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Calculate your Body Mass Index and save it to your profile history.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Unit Switcher
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
                        .background(if (isMetric) PinkPrimary else Color.Transparent, RoundedCornerShape(25.dp))
                        .clickable { 
                            isMetric = true 
                            bmiResult = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Metric (kg/cm)", fontWeight = FontWeight.Bold, color = if (isMetric) Color.White else Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (!isMetric) PinkPrimary else Color.Transparent, RoundedCornerShape(25.dp))
                        .clickable { 
                            isMetric = false 
                            bmiResult = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Imperial (lb/ft)", fontWeight = FontWeight.Bold, color = if (!isMetric) Color.White else Color.Gray)
                }
            }

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(if (isMetric) "Weight (kg)" else "Weight (lbs)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            if (isMetric) {
                OutlinedTextField(
                    value = heightMain,
                    onValueChange = { heightMain = it },
                    label = { Text("Height (cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = heightMain,
                        onValueChange = { heightMain = it },
                        label = { Text("Height (ft)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = heightInches,
                        onValueChange = { heightInches = it },
                        label = { Text("Inches") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val w = weight.toDoubleOrNull() ?: 0.0
                    if (isMetric) {
                        val hCm = heightMain.toDoubleOrNull() ?: 0.0
                        if (w > 0 && hCm > 0) {
                            val hM = hCm / 100.0
                            bmiResult = w / hM.pow(2)
                            viewModel.logWeight(w, "Logged via BMI Calculator (Metric)")
                        }
                    } else {
                        val ft = heightMain.toDoubleOrNull() ?: 0.0
                        val inch = heightInches.toDoubleOrNull() ?: 0.0
                        val totalInches = (ft * 12) + inch
                        if (w > 0 && totalInches > 0) {
                            // Imperial formula: BMI = 703 * weight(lb) / height(in)^2
                            bmiResult = 703 * w / totalInches.pow(2)
                            // Log weight in kg to database
                            viewModel.logWeight(w * 0.453592, "Logged via BMI Calculator (Imperial)")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) {
                Text("Calculate & Sync", fontWeight = FontWeight.Bold)
            }

            if (bmiResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Your BMI is", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = String.format("%.1f", bmiResult),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = categoryColor
                        )
                        Text(
                            text = bmiCategory,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Weight synced to your profile",
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = getBmiAdvice(bmiCategory),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // --- Extra Features Section ---
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "BMI Reference Guide",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            BmiReferenceCard()

            HealthTipsSection()
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun BmiReferenceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BmiReferenceItem("Underweight", "< 18.5", Color(0xFFFFC107))
            BmiReferenceItem("Healthy", "18.5 - 24.9", Color(0xFF4CAF50))
            BmiReferenceItem("Overweight", "25.0 - 29.9", Color(0xFFFF9800))
            BmiReferenceItem("Obesity", "> 30.0", Color(0xFFF44336))
        }
    }
}

@Composable
fun BmiReferenceItem(label: String, range: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.Medium)
        }
        Text(range, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HealthTipsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Healthy Tips",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        HealthTipCard(
            title = "Balanced Diet",
            description = "Focus on whole grains, lean proteins, and plenty of fruits and vegetables.",
            icon = Icons.Default.Info,
            color = Color(0xFF2196F3)
        )
        
        HealthTipCard(
            title = "Regular Exercise",
            description = "Aim for at least 150 minutes of moderate aerobic activity per week.",
            icon = Icons.Default.Scale,
            color = Color(0xFF9C27B0)
        )
    }
}

@Composable
fun HealthTipCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = color)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

private fun getBmiAdvice(category: String): String {
    return when (category) {
        "Underweight" -> "Consider consulting a nutritionist to help you gain weight in a healthy way."
        "Healthy Weight" -> "Great job! Maintain your current lifestyle and balanced diet."
        "Overweight" -> "Try to incorporate more physical activity and monitor your calorie intake."
        "Obesity" -> "It is recommended to consult with a healthcare professional for a personalized health plan."
        else -> ""
    }
}
