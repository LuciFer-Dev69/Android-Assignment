package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.UserEntity
import com.example.myapplication.ui.theme.PinkGradient
import com.example.myapplication.ui.theme.PinkPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val users by viewModel.allUsers.collectAsState()
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(PinkGradient)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("User Directory", fontWeight = FontWeight.Black, color = PinkPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PinkPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.8f))
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(users) { user ->
                    UserItem(
                        user = user,
                        onEdit = { userToEdit = user },
                        onDelete = { userToDelete = user }
                    )
                }
            }

            // Delete Confirmation Dialog
            if (userToDelete != null) {
                AlertDialog(
                    onDismissRequest = { userToDelete = null },
                    title = { Text("Terminate Account", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to permanently delete ${userToDelete?.username}? All health data will be lost.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteUser(userToDelete!!)
                                userToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Confirm Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { userToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Edit User Dialog
            if (userToEdit != null) {
                var newName by remember { mutableStateOf(userToEdit!!.username) }
                var isAdmin by remember { mutableStateOf(userToEdit!!.isAdmin) }
                
                AlertDialog(
                    onDismissRequest = { userToEdit = null },
                    title = { Text("Modify User Access", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Account Name") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = isAdmin, 
                                    onCheckedChange = { isAdmin = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PinkPrimary)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Grant Administrative Privileges")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.updateUser(userToEdit!!.copy(username = newName, isAdmin = isAdmin))
                                userToEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                        ) {
                            Text("Apply Changes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { userToEdit = null }) {
                            Text("Dismiss")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UserItem(user: UserEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (user.isAdmin) Color.Black else PinkPrimary.copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    modifier = Modifier.padding(14.dp),
                    tint = if (user.isAdmin) Color.White else PinkPrimary
                )
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (user.isAdmin) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = Color.Black,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "ADMINISTRATOR", 
                            color = Color.White, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null, tint = PinkPrimary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}
