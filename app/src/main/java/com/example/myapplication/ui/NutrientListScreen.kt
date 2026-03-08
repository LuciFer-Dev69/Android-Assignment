package com.example.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.Nutrient
import com.example.myapplication.data.RetrofitInstance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutrientListScreen(onBack: () -> Unit) {
    var nutrients by remember { mutableStateOf<List<Nutrient>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var selectedNutrient by remember { mutableStateOf<Nutrient?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.getNutrients()
            nutrients = response.results.filter { it.name.isNotBlank() }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load nutrition info: ${e.message}"
            isLoading = false
        }
    }

    if (selectedNutrient != null) {
        NutrientDetailScreen(
            nutrient = selectedNutrient!!,
            onBack = { selectedNutrient = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Nutrition Guide", fontWeight = FontWeight.Bold) },
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { 
                                isLoading = true
                                errorMessage = null
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(nutrients) { nutrient ->
                            NutrientListItem(
                                nutrient = nutrient,
                                onClick = { selectedNutrient = nutrient }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutrientListItem(nutrient: Nutrient, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small preview image
            Surface(
                modifier = Modifier.size(60.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (nutrient.imageUrl != null) {
                    AsyncImage(
                        model = nutrient.imageUrl,
                        contentDescription = nutrient.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nutrient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (nutrient.description.isNotBlank()) {
                    Text(
                        text = nutrient.description.replace(Regex("<[^>]*>"), ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, // This will be flipped in LTR or use a forward icon
                    contentDescription = "Details",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
