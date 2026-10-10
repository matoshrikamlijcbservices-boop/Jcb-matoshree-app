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
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
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
import com.example.data.model.Machine
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NavyDark
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MachinesScreen(viewModel: MainViewModel) {
    val machines by viewModel.machinesState.collectAsStateWithLifecycle()
    val workRecords by viewModel.workRecordsState.collectAsStateWithLifecycle()
    val expenses by viewModel.expensesState.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var machineToEdit by remember { mutableStateOf<Machine?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    machineToEdit = Machine(
                        machineCode = "JCB-0${machines.size + 1}",
                        name = "JCB 3DX Super",
                        regNumber = "",
                        makeModel = "JCB 3DX EcoXcellence",
                        machineType = "Backhoe Loader",
                        defaultHourlyRate = 1800.0,
                        currentMeterReading = 0.0
                    )
                    showDialog = true
                },
                containerColor = ConstructionYellowDark,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_machine")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add JCB")
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
                text = "Earthmoving Machinery Fleet (${machines.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (machines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No machines configured. Tap + to register a machine.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(machines) { m ->
                        val mHours = workRecords.filter { it.machineId == m.id }.sumOf { it.billableHours }
                        val mExp = expenses.filter { it.machineId == m.id }.sumOf { it.amount }

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
                                        Icon(Icons.Default.Agriculture, contentDescription = null, tint = ConstructionYellowDark)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(m.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("Code: ${m.machineCode} | Reg: ${m.regNumber.ifEmpty { "N/A" }}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                    StatusBadge(status = m.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Current Meter", fontSize = 11.sp, color = Color.Gray)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp), tint = NavyDark)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${m.currentMeterReading} Hrs", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Default Rate", fontSize = 11.sp, color = Color.Gray)
                                        Text("₹${m.defaultHourlyRate}/hr", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyDark)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Logged Hours", fontSize = 11.sp, color = Color.Gray)
                                        Text(String.format("%.1f Hrs", mHours), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Fuel & Maintenance: ${formatCurrency(mExp)}", fontSize = 12.sp, color = DangerRed)
                                    Row {
                                        IconButton(onClick = {
                                            machineToEdit = m
                                            showDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyDark)
                                        }
                                        IconButton(onClick = { viewModel.deleteMachine(m.id) }) {
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
    }

    // Add / Edit Machine Dialog
    if (showDialog && machineToEdit != null) {
        var code by remember { mutableStateOf(machineToEdit?.machineCode ?: "") }
        var name by remember { mutableStateOf(machineToEdit?.name ?: "") }
        var regNo by remember { mutableStateOf(machineToEdit?.regNumber ?: "") }
        var type by remember { mutableStateOf(machineToEdit?.machineType ?: "Backhoe Loader") }
        var rateText by remember { mutableStateOf(machineToEdit?.defaultHourlyRate?.toString() ?: "1800") }
        var meterText by remember { mutableStateOf(machineToEdit?.currentMeterReading?.toString() ?: "0") }
        var status by remember { mutableStateOf(machineToEdit?.status ?: "ACTIVE") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (machineToEdit?.id == 0L) "Register New Machine" else "Edit Machine Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Machine ID / Code (e.g. JCB-01)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Machine Name (e.g. JCB 3DX Super)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = regNo,
                        onValueChange = { regNo = it },
                        label = { Text("Registration Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Default Hourly Rate (₹/hr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = meterText,
                        onValueChange = { meterText = it },
                        label = { Text("Current Meter Reading (Hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("Status (ACTIVE / MAINTENANCE / INACTIVE)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = rateText.toDoubleOrNull() ?: 1800.0
                        val meter = meterText.toDoubleOrNull() ?: 0.0
                        viewModel.saveMachine(
                            machineToEdit!!.copy(
                                machineCode = code,
                                name = name,
                                regNumber = regNo,
                                defaultHourlyRate = rate,
                                currentMeterReading = meter,
                                status = status.uppercase()
                            )
                        )
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark)
                ) {
                    Text("Save Machine")
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
