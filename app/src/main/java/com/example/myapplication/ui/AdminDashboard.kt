package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.PinkGradient
import com.example.myapplication.ui.theme.PinkPrimary
import com.example.myapplication.ui.theme.PinkSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onManageUsers: () -> Unit
) {
    val userCount by viewModel.userCount.collectAsState()
    val waterStats by viewModel.globalWaterStats.collectAsState()
    val activityStats by viewModel.globalActivityStats.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(PinkGradient)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Command Center", fontWeight = FontWeight.Black, color = PinkPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PinkPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = onManageUsers) {
                            Icon(Icons.Default.Group, contentDescription = "Users", tint = PinkPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.8f))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Welcome Header
                Column {
                    Text("Welcome, Admin", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Platform health is looking great today!", color = Color.White.copy(alpha = 0.8f))
                }

                // Stats Grid
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AdminStatCard("ACTIVE USERS", "$userCount", Icons.Default.People, Color(0xFFE91E63), Modifier.weight(1f))
                        AdminStatCard("TOTAL STEPS", "${activityStats.totalSteps}", Icons.AutoMirrored.Filled.DirectionsWalk, Color(0xFFFF5722), Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AdminStatCard("HYDRATION", "${waterStats}ml", Icons.Default.WaterDrop, Color(0xFF2196F3), Modifier.weight(1f))
                        AdminStatCard("CALORIES", "${activityStats.totalCaloriesBurned}", Icons.Default.Whatshot, Color(0xFF4CAF50), Modifier.weight(1f))
                    }
                }

                // Workout Distribution Card
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, null, tint = PinkPrimary)
                            Spacer(Modifier.width(12.dp))
                            Text("Workout Distribution", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(20.dp))
                        
                        if (activityStats.workoutTypeDistribution.isEmpty()) {
                            Text("No activity data recorded yet.", color = Color.Gray)
                        } else {
                            activityStats.workoutTypeDistribution.forEach { (type, count) ->
                                val total = activityStats.workoutTypeDistribution.values.sum().toFloat()
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(type, fontWeight = FontWeight.Medium)
                                        Text("$count logs", color = PinkPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { if (total > 0) count / total else 0f },
                                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                                        color = PinkPrimary,
                                        trackColor = Color.LightGray.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Powerful Quick Actions
                Text("System Operations", fontWeight = FontWeight.Bold, color = Color.White)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminActionBtn("Users", Icons.Default.Group, onManageUsers, Modifier.weight(1f))
                    AdminActionBtn("BroadCast", Icons.Default.NotificationsActive, {}, Modifier.weight(1f))
                    AdminActionBtn("Export", Icons.Default.Download, {}, Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 1.sp)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun AdminActionBtn(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
