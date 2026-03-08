package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(onBack: () -> Unit) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var bmiResult by remember { mutableStateOf<Double?>(null) }

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
                text = "Calculate your Body Mass Index to track your fitness level.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Button(
                onClick = {
                    val w = weight.toDoubleOrNull()
                    val h = height.toDoubleOrNull()?.div(100.0) // Convert cm to m
                    if (w != null && h != null && h > 0) {
                        bmiResult = w / h.pow(2)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Calculate BMI", fontWeight = FontWeight.Bold)
            }

            if (bmiResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.1f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(categoryColor))
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
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = getBmiAdvice(bmiCategory),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
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
