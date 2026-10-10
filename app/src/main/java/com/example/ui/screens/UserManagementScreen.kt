package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppUser
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun UserManagementScreen(viewModel: MainViewModel) {
    val users by viewModel.usersState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = NavyDark,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_user")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Current Active Role Card
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Session Role", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(currentUser.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Role: ${currentUser.role}", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setCurrentUser(currentUser.copy(role = "OWNER", fullName = "Authorized Owner"))
                                viewModel.showMessage("Switched to Owner Mode (Full Access)")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Owner Mode", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.setCurrentUser(currentUser.copy(role = "STAFF", fullName = "Site Supervisor"))
                                viewModel.showMessage("Switched to Staff Mode (Operations Only)")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Staff Mode", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Registered Operators & Authorized Staff", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyDark)
            Spacer(modifier = Modifier.height(10.dp))

            if (users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No user accounts found. Tap + to register staff.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(users) { u ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = NavyDark)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(u.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Username: ${u.username}  |  Role: ${u.role}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (u.phone.isNotBlank()) {
                                            Text("Phone: ${u.phone}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                if (u.role != "OWNER" || users.size > 1) {
                                    IconButton(onClick = { viewModel.deleteUser(u.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var username by remember { mutableStateOf("") }
        var fullName by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("STAFF") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Register New Authorized User", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role (OWNER / STAFF)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.isBlank() || username.isBlank()) {
                            viewModel.showMessage("Name and Username are required")
                            return@Button
                        }
                        val user = AppUser(
                            username = username,
                            fullName = fullName,
                            role = role.uppercase(),
                            phone = phone,
                            email = email
                        )
                        viewModel.saveUser(user)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark)
                ) {
                    Text("Save User")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
