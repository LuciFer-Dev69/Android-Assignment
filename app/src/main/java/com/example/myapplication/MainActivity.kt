package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.myapplication.data.ActivityEntity
import com.example.myapplication.data.ActivityRepository
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.data.WorkoutRepository
import com.example.myapplication.notifications.WaterReminderWorker
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity(), SensorEventListener, ImageLoaderFactory {
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var repository: UserPreferencesRepository? = null
    private var db: AppDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        repository = UserPreferencesRepository(applicationContext)
        db = AppDatabase.getDatabase(this)
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        scheduleReminders()
        enableEdgeToEdge()
        
        setContent {
            val isDarkModeSaved by repository!!.isDarkMode.collectAsState(initial = isSystemInDarkTheme())
            val isLoggedIn by repository!!.isLoggedIn.collectAsState(initial = false)
            
            // Handle Permissions
            val context = LocalContext.current
            val permissionsToRequest = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val granted = permissions.entries.all { it.value }
                if (granted) {
                    registerStepSensor()
                }
            }

            LaunchedEffect(Unit) {
                launcher.launch(permissionsToRequest.toTypedArray())
            }

            MyApplicationTheme(darkTheme = isDarkModeSaved) {
                AppNavigation(repository!!, isLoggedIn)
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    private fun scheduleReminders() {
        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(4, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WaterReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun registerStepSensor() {
        stepSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            saveStepsToDb(totalSteps)
        }
    }

    private fun saveStepsToDb(totalSteps: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        lifecycleScope.launch {
            val userId = repository?.userId?.firstOrNull() ?: return@launch
            val existing = db?.activityDao()?.getAllActivities()?.firstOrNull()?.find { 
                it.date == today && it.name == "Walking (Auto)" && it.userId == userId
            }
            
            if (existing != null) {
                db?.activityDao()?.updateActivity(existing.copy(steps = totalSteps))
            } else {
                db?.activityDao()?.insertActivity(
                    ActivityEntity(
                        userId = userId,
                        name = "Walking (Auto)",
                        steps = totalSteps,
                        workoutType = "Walking",
                        calories = (totalSteps * 0.04).toInt(),
                        date = today
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        registerStepSensor()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Activities : Screen("activity_log", "Activities", Icons.AutoMirrored.Filled.List)
    object AddActivity : Screen("add_activity", "Add", Icons.Default.AddCircle)
    object Tips : Screen("workout_suggestions", "Tips", Icons.Default.Lightbulb)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Admin : Screen("admin_dashboard", "Admin", Icons.Default.AdminPanelSettings)
}

@Composable
fun AppNavigation(repository: UserPreferencesRepository, isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    
    // Manual Dependency Injection - Repositories
    val activityRepository = remember { 
        ActivityRepository(db.activityDao(), db.waterDao(), db.foodDao(), db.weightDao()) 
    }
    val workoutRepository = remember { WorkoutRepository() }
    
    // ViewModels
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            activityRepository = activityRepository,
            userPreferencesRepository = repository
        )
    )
    
    val workoutViewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModelFactory(workoutRepository)
    )

    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModelFactory(
            userDao = db.userDao(),
            activityDao = db.activityDao(),
            waterDao = db.waterDao(),
            foodDao = db.foodDao()
        )
    )

    val homeState by homeViewModel.homeState.collectAsState()
    val waterToday by homeViewModel.waterToday.collectAsState()
    val activities by homeViewModel.activities.collectAsState()
    val waterLogs by homeViewModel.waterLogs.collectAsState()
    val allFoodLogs by homeViewModel.allFoodLogs.collectAsState()
    val weightHistory by homeViewModel.weightHistory.collectAsState()
    
    // New Collected States for Analytics
    val weeklyStepAverage by homeViewModel.weeklyStepAverage.collectAsState()
    val activityComparisonText by homeViewModel.activityComparisonText.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var isAdminLoggedIn by remember { mutableStateOf(false) }

    val bottomNavItems = if (isAdminLoggedIn) {
        listOf(Screen.Admin, Screen.Activities, Screen.Profile)
    } else {
        listOf(
            Screen.Home,
            Screen.Activities,
            Screen.AddActivity,
            Screen.Tips,
            Screen.Profile
        )
    }

    val showBottomBar = isLoggedIn && currentDestination?.route in bottomNavItems.map { (it as Screen).route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) (if (isAdminLoggedIn) "admin_dashboard" else "home") else "login",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            composable("login") {
                LoginScreen(
                    userDao = db.userDao(),
                    repository = repository,
                    onLoginSuccess = { isAdmin ->
                        isAdminLoggedIn = isAdmin
                        navController.navigate(if (isAdmin) "admin_dashboard" else "home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToSignup = {
                        navController.navigate("signup")
                    }
                )
            }

            composable("signup") {
                SignupScreen(
                    userDao = db.userDao(),
                    repository = repository,
                    onSignupSuccess = {
                        navController.navigate("home") {
                            popUpTo("signup") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login")
                    }
                )
            }
            
            composable("home") {
                HomeScreen(
                    repository = repository,
                    stepsToday = homeState.stepsToday,
                    stepGoal = homeState.stepGoal,
                    caloriesBurned = homeState.caloriesBurned,
                    workoutsCount = homeState.workoutsCount,
                    waterToday = waterToday ?: 0,
                    weeklyStepAverage = weeklyStepAverage,
                    activityComparisonText = activityComparisonText,
                    onAddActivityClick = { navController.navigate("add_activity") },
                    onViewLogsClick = { navController.navigate("activity_log") },
                    onWorkoutSuggestionsClick = { navController.navigate("workout_suggestions") },
                    onNutrientsClick = { navController.navigate("nutrients") },
                    onProfileClick = { navController.navigate("profile") },
                    onWaterClick = { navController.navigate("water_tracker") },
                    onBmiClick = { navController.navigate("bmi_calculator") },
                    onCaloriesClick = { navController.navigate("food_tracker") },
                    onAnalyticsClick = { navController.navigate("analytics") },
                    onWeightClick = { navController.navigate("weight_tracker") }
                )
            }

            composable("admin_dashboard") {
                AdminDashboard(
                    viewModel = adminViewModel,
                    onBack = { 
                        isAdminLoggedIn = false
                        navController.navigate("home") 
                    },
                    onManageUsers = { navController.navigate("user_management") }
                )
            }

            composable("user_management") {
                UserManagementScreen(
                    viewModel = adminViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("water_tracker") {
                WaterTrackerScreen(
                    currentWaterMl = waterToday ?: 0,
                    waterLogs = waterLogs,
                    onAddWater = { amount -> homeViewModel.addWater(amount) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("bmi_calculator") {
                BMICalculatorScreen(
                    viewModel = homeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("food_tracker") {
                FoodTrackerScreen(
                    viewModel = homeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("analytics") {
                WeeklyAnalyticsScreen(
                    activities = activities,
                    waterLogs = waterLogs,
                    foodLogs = allFoodLogs,
                    weightHistory = weightHistory,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("profile") {
                ProfileScreen(
                    repository = repository, 
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        scope.launch {
                            repository.clearUserData()
                            isAdminLoggedIn = false
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
            
            composable("add_activity") {
                AddActivityScreen(repository = repository, onBack = { navController.popBackStack() })
            }
            
            composable("activity_log") {
                ActivityLogScreen(
                    repository = activityRepository,
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
                AddActivityScreen(repository = repository, activityId = activityId, onBack = { navController.popBackStack() })
            }
            
            composable("workout_suggestions") {
                WorkoutSuggestionsScreen(
                    viewModel = workoutViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("nutrients") {
                NutrientListScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("weight_tracker") {
                WeightTrackerScreen(
                    viewModel = homeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
