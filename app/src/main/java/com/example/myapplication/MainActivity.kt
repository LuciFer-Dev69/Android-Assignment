package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.data.WaterLogEntity
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = UserPreferencesRepository(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            val isDarkModeSaved by repository.isDarkMode.collectAsState(initial = isSystemInDarkTheme())
            
            MyApplicationTheme(darkTheme = isDarkModeSaved) {
                AppNavigation(repository)
            }
        }
    }
}

@Composable
fun AppNavigation(repository: UserPreferencesRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    
    // Date for today
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Collect data for Home Screen
    val activities by db.activityDao().getAllActivities().collectAsState(initial = emptyList())
    val stepGoal by repository.stepGoal.collectAsState(initial = 5000)
    val waterToday by db.waterDao().getTodayWaterSum(today).collectAsState(initial = 0)
    
    val todayActivities = activities.filter { it.date == today }
    val stepsToday = todayActivities.sumOf { it.steps }
    val caloriesBurned = todayActivities.sumOf { it.calories }
    val workoutsCount = todayActivities.size

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "welcome",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            composable("welcome") {
                LoginScreen(onStartClick = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                })
            }
            
            composable("home") {
                HomeScreen(
                    stepsToday = stepsToday,
                    stepGoal = stepGoal,
                    caloriesBurned = caloriesBurned,
                    workoutsCount = workoutsCount,
                    onAddActivityClick = { navController.navigate("add_activity") },
                    onViewLogsClick = { navController.navigate("activity_log") },
                    onWorkoutSuggestionsClick = { navController.navigate("workout_suggestions") },
                    onNutrientsClick = { navController.navigate("nutrients") },
                    onProfileClick = { navController.navigate("profile") },
                    onWaterClick = { navController.navigate("water_tracker") },
                    onBmiClick = { navController.navigate("bmi_calculator") },
                    onAnalyticsClick = { navController.navigate("analytics") }
                )
            }

            composable("water_tracker") {
                WaterTrackerScreen(
                    currentWaterMl = waterToday ?: 0,
                    onAddWater = { amount ->
                        scope.launch {
                            db.waterDao().insertWaterLog(WaterLogEntity(amountMl = amount, date = today))
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("bmi_calculator") {
                BMICalculatorScreen(onBack = { navController.popBackStack() })
            }

            composable("analytics") {
                WeeklyAnalyticsScreen(
                    activities = activities,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("profile") {
                ProfileScreen(repository = repository, onBack = { navController.popBackStack() })
            }
            
            composable("add_activity") {
                AddActivityScreen(onBack = { navController.popBackStack() })
            }
            
            composable("activity_log") {
                ActivityLogScreen(
                    onBack = { navController.popBackStack() },
                    onActivityClick = { id -> navController.navigate("activity_detail/$id") },
                    onEditActivity = { id -> navController.navigate("edit_activity/$id") }
                )
            }
            
            composable(
                route = "activity_detail/{activityId}",
                arguments = listOf(navArgument("activityId") { type = NavType.IntType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getInt("activityId") ?: 0
                ActivityDetailScreen(
                    activityId = activityId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate("edit_activity/$id") }
                )
            }
            
            composable(
                route = "edit_activity/{activityId}",
                arguments = listOf(navArgument("activityId") { type = NavType.IntType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getInt("activityId")
                AddActivityScreen(activityId = activityId, onBack = { navController.popBackStack() })
            }
            
            composable("workout_suggestions") {
                WorkoutSuggestionsScreen(onBack = { navController.popBackStack() })
            }

            composable("nutrients") {
                NutrientListScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
