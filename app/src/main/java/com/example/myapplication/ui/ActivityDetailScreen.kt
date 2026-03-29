package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.ActivityEntity
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.ui.theme.PinkPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    var activity by remember { mutableStateOf<ActivityEntity?>(null) }

    LaunchedEffect(activityId) {
        activity = db.activityDao().getActivityById(activityId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { activity?.let { onEdit(it.id) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            activity?.let {
                                db.activityDao().deleteActivity(it)
                                onBack()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        activity?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Status Header
                StatusHeader(item)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DetailItem(label = "Activity Name", value = item.name)
                        DetailItem(label = "Workout Type", value = item.workoutType)
                        
                        if (item.targetSteps > 0) {
                            GoalProgressSection(item)
                        } else {
                            DetailItem(label = "Steps", value = item.steps.toString())
                        }
                        
                        DetailItem(label = "Calories Burned", value = "${item.calories} kcal")
                        DetailItem(label = "Date", value = item.date)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { onEdit(item.id) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Activity", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            db.activityDao().deleteActivity(item)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Activity", fontWeight = FontWeight.Bold)
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PinkPrimary)
        }
    }
}

@Composable
fun StatusHeader(item: ActivityEntity) {
    val isTask = item.targetSteps > 0
    val isDone = item.isCompleted
    
    val bgColor = if (isDone) Color(0xFFE8F5E9) else if (isTask) Color(0xFFFFF3E0) else Color.Transparent
    val contentColor = if (isDone) Color(0xFF2E7D32) else if (isTask) Color(0xFFEF6C00) else MaterialTheme.colorScheme.onSurfaceVariant

    if (isTask) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = bgColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isDone) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                    contentDescription = null,
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isDone) "Task Completed!" else "Goal in Progress...",
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun GoalProgressSection(item: ActivityEntity) {
    val progress = (item.steps.toFloat() / item.targetSteps.toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.labelMedium,
                color = PinkPrimary
            )
            Text(
                text = "${item.steps} / ${item.targetSteps} steps",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
            color = if (progress >= 1f) Color(0xFF4CAF50) else PinkPrimary,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = PinkPrimary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
    }
}
