package com.example.myapplication.ui

import com.example.myapplication.BuildConfig

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.ActivityRepository
import com.example.myapplication.data.ChatDao
import com.example.myapplication.data.ChatMessageEntity
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.ui.theme.DarkPinkGradient
import com.example.myapplication.ui.theme.PinkGradient
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(
    private val chatDao: ChatDao,
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    val messages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var isTyping by mutableStateOf(false)

        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private suspend fun getContextPrompt(): String {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val steps = activityRepository.getAllActivities().first().filter { it.date == today }.sumOf { it.steps }
        val water = activityRepository.getTodayWaterSum(today).first() ?: 0
        val latestWeightLog = activityRepository.getLatestWeight().first()
        val weight = latestWeightLog?.weight?.toString() ?: "not logged yet"
        val goal = userPreferencesRepository.stepGoal.first()
        val username = userPreferencesRepository.username.first() ?: "User"

        return """
            You are $username's personal AI Fitness Coach. 
            Here is the user's current data for today ($today):
            - Steps taken: $steps (Daily Goal: $goal)
            - Water intake: ${water}ml
            - Current Weight: $weight kg
            
            Instructions:
            1. Be encouraging and concise.
            2. Use the data above to provide specific feedback. 
            3. If steps are low, suggest a quick walk. If water is low, remind them to drink.
            4. Keep responses under 3 sentences unless asked for a detailed plan.
        """.trimIndent()
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            chatDao.insertMessage(ChatMessageEntity(text = text, isUser = true))
            isTyping = true
            
            try {
                val context = getContextPrompt()
                val response = generativeModel.generateContent(
                    content { text("$context\n\nUser Question: $text") }
                )
                val responseText = response.text ?: "I'm here to help with your fitness journey! What's on your mind?"
                chatDao.insertMessage(ChatMessageEntity(text = responseText, isUser = false))
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Gemini API Error", e)
                chatDao.insertMessage(ChatMessageEntity(
                    text = "Coach: I'm having trouble connecting right now. Please check your internet or try again later.",
                    isUser = false
                ))
            } finally {
                isTyping = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch { chatDao.clearChat() }
    }
}

class ChatViewModelFactory(
    private val chatDao: ChatDao,
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatDao, activityRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAssistantScreen(
    chatDao: ChatDao,
    activityRepository: ActivityRepository,
    userPreferencesRepository: UserPreferencesRepository,
    onBack: () -> Unit
) {
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(chatDao, activityRepository, userPreferencesRepository)
    )
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val backgroundBrush = if (isSystemInDarkTheme()) DarkPinkGradient else PinkGradient

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("AI Fitness Coach", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Chat")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message)
                    }
                    if (viewModel.isTyping) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Coach is thinking...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Surface(
                    tonalElevation = 8.dp, 
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).navigationBarsPadding().imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask about your progress...") },
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank() && !viewModel.isTyping,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
    val shape = if (isUser) RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(color = color, shape = shape, tonalElevation = 2.dp) {
            Text(text = message.text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = textColor, fontSize = 15.sp)
        }
        Text(
            text = if (isUser) "You" else "Coach",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            color = MaterialTheme.colorScheme.outline
        )
    }
}
