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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Operator
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NavyDark
import com.example.ui.viewmodel.MainViewModel

@Composable
fun OperatorsScreen(viewModel: MainViewModel) {
    val operators by viewModel.operatorsState.collectAsStateWithLifecycle()
    val workRecords by viewModel.workRecordsState.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var operatorToEdit by remember { mutableStateOf<Operator?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    operatorToEdit = null
                    showDialog = true
                },
                containerColor = ConstructionYellowDark,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_operator")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Operator")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "JCB & Heavy Equipment Operators (${operators.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (operators.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No operators added. Tap + to register an operator.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(operators) { op ->
                        val opHours = workRecords.filter { it.operatorId == op.id }.sumOf { it.billableHours }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Badge, contentDescription = null, tint = NavyDark)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(op.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    StatusBadge(status = op.status)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(op.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (op.address.isNotBlank()) {
                                    Text("Address: ${op.address}", fontSize = 12.sp, color = Color.Gray)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Wage: ₹${op.wageRate} (${op.wageType})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyDark)
                                    Text("Logged: ${String.format("%.1f", opHours)} Hrs", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = ConstructionYellowDark)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = {
                                        operatorToEdit = op
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyDark)
                                    }
                                    IconButton(onClick = { viewModel.deleteOperator(op.id) }) {
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
        var name by remember { mutableStateOf(operatorToEdit?.name ?: "") }
        var phone by remember { mutableStateOf(operatorToEdit?.phone ?: "") }
        var address by remember { mutableStateOf(operatorToEdit?.address ?: "") }
        var wageType by remember { mutableStateOf(operatorToEdit?.wageType ?: "DAILY") }
        var wageRateText by remember { mutableStateOf(operatorToEdit?.wageRate?.toString() ?: "900") }
        var status by remember { mutableStateOf(operatorToEdit?.status ?: "ACTIVE") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (operatorToEdit == null) "Add Machine Operator" else "Edit Operator Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Operator Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wageType,
                        onValueChange = { wageType = it },
                        label = { Text("Wage Arrangement (DAILY / HOURLY / MONTHLY)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wageRateText,
                        onValueChange = { wageRateText = it },
                        label = { Text("Wage Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank()) {
                            viewModel.showMessage("Name and Phone are required")
                            return@Button
                        }
                        val rate = wageRateText.toDoubleOrNull() ?: 800.0
                        val op = Operator(
                            id = operatorToEdit?.id ?: 0L,
                            name = name,
                            phone = phone,
                            address = address,
                            wageType = wageType.uppercase(),
                            wageRate = rate,
                            status = status
                        )
                        viewModel.saveOperator(op)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark)
                ) {
                    Text("Save Operator")
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
