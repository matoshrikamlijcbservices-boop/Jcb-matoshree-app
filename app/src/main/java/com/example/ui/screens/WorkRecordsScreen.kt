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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.data.model.Customer
import com.example.data.model.Machine
import com.example.data.model.Operator
import com.example.data.model.WorkRecord
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRecordsScreen(viewModel: MainViewModel) {
    val workRecords by viewModel.workRecordsState.collectAsStateWithLifecycle()
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val machines by viewModel.machinesState.collectAsStateWithLifecycle()
    val operators by viewModel.operatorsState.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<WorkRecord?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    recordToEdit = null
                    showDialog = true
                },
                containerColor = ConstructionYellowDark,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_work_record")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Work")
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
                text = "Daily JCB Work Logs (${workRecords.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (workRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No work logs found. Click + to add a daily meter record.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(workRecords) { rec ->
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
                                    Column {
                                        Text(rec.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Date: ${rec.workDate}  |  Site: ${rec.worksite}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    StatusBadge(status = if (rec.invoiceId != null) "BILLED" else "UNBILLED")
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Machine: ${rec.machineName}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyDark)
                                if (!rec.operatorName.isNullOrBlank()) {
                                    Text("Operator: ${rec.operatorName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Work: ${rec.description}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Meter Readings", fontSize = 11.sp, color = Color.Gray)
                                        Text("${rec.openingMeter} → ${rec.closingMeter}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Billable Hours", fontSize = 11.sp, color = Color.Gray)
                                        Text("${rec.billableHours} Hrs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Calculated Cost", fontSize = 11.sp, color = Color.Gray)
                                        Text(formatCurrency(rec.billableHours * rec.hourlyRate), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SuccessGreen)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = {
                                        recordToEdit = rec
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyDark)
                                    }
                                    IconButton(onClick = { viewModel.deleteWorkRecord(rec.id) }) {
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

    // Add / Edit Dialog
    if (showDialog) {
        var date by remember {
            mutableStateOf(recordToEdit?.workDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()))
        }
        var selectedCust by remember {
            mutableStateOf(customers.find { it.id == recordToEdit?.customerId } ?: customers.firstOrNull())
        }
        var custExpanded by remember { mutableStateOf(false) }

        var selectedMach by remember {
            mutableStateOf(machines.find { it.id == recordToEdit?.machineId } ?: machines.firstOrNull())
        }
        var machExpanded by remember { mutableStateOf(false) }

        var selectedOp by remember {
            mutableStateOf(operators.find { it.id == recordToEdit?.operatorId } ?: operators.firstOrNull())
        }
        var opExpanded by remember { mutableStateOf(false) }

        var worksite by remember { mutableStateOf(recordToEdit?.worksite ?: selectedCust?.address ?: "") }
        var description by remember { mutableStateOf(recordToEdit?.description ?: "Excavation and debris clearing") }
        var startMeterText by remember {
            mutableStateOf(recordToEdit?.openingMeter?.toString() ?: selectedMach?.currentMeterReading?.toString() ?: "0.0")
        }
        var endMeterText by remember {
            mutableStateOf(recordToEdit?.closingMeter?.toString() ?: "")
        }
        var manualHoursText by remember {
            mutableStateOf(recordToEdit?.manualHours?.toString() ?: "")
        }
        var nonBillableText by remember {
            mutableStateOf(recordToEdit?.nonBillableHours?.toString() ?: "0.0")
        }
        var rateText by remember {
            mutableStateOf(recordToEdit?.hourlyRate?.toString() ?: selectedMach?.defaultHourlyRate?.toString() ?: "1800")
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (recordToEdit == null) "Log Daily JCB Work" else "Edit Work Log", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Customer Selection
                    item {
                        ExposedDropdownMenuBox(
                            expanded = custExpanded,
                            onExpandedChange = { custExpanded = !custExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCust?.name ?: "Select Customer *",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Customer *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = custExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = custExpanded, onDismissRequest = { custExpanded = false }) {
                                customers.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.name) },
                                        onClick = {
                                            selectedCust = c
                                            if (worksite.isBlank()) worksite = c.address
                                            custExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Machine Selection
                    item {
                        ExposedDropdownMenuBox(
                            expanded = machExpanded,
                            onExpandedChange = { machExpanded = !machExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedMach?.name ?: "Select Machine *",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Machine *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = machExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = machExpanded, onDismissRequest = { machExpanded = false }) {
                                machines.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text("${m.name} (${m.machineCode})") },
                                        onClick = {
                                            selectedMach = m
                                            startMeterText = m.currentMeterReading.toString()
                                            rateText = m.defaultHourlyRate.toString()
                                            machExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Operator Selection
                    item {
                        ExposedDropdownMenuBox(
                            expanded = opExpanded,
                            onExpandedChange = { opExpanded = !opExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedOp?.name ?: "Select Operator",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Operator") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = opExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = opExpanded, onDismissRequest = { opExpanded = false }) {
                                operators.forEach { op ->
                                    DropdownMenuItem(
                                        text = { Text(op.name) },
                                        onClick = {
                                            selectedOp = op
                                            opExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = worksite,
                            onValueChange = { worksite = it },
                            label = { Text("Worksite Location") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Work Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startMeterText,
                                onValueChange = { startMeterText = it },
                                label = { Text("Start Meter") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endMeterText,
                                onValueChange = { endMeterText = it },
                                label = { Text("End Meter") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = nonBillableText,
                                onValueChange = { nonBillableText = it },
                                label = { Text("Non-Billable Break") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = rateText,
                                onValueChange = { rateText = it },
                                label = { Text("Hourly Rate (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cust = selectedCust
                        val mach = selectedMach
                        if (cust == null || mach == null) {
                            viewModel.showMessage("Please select customer and machine")
                            return@Button
                        }
                        val start = startMeterText.toDoubleOrNull() ?: 0.0
                        val end = endMeterText.toDoubleOrNull() ?: 0.0
                        val nonBillable = nonBillableText.toDoubleOrNull() ?: 0.0
                        val rate = rateText.toDoubleOrNull() ?: 1800.0

                        val calculatedHours = if (end > start) max(0.0, (end - start) - nonBillable) else 0.0
                        if (end > 0.0 && end < start) {
                            viewModel.showMessage("Closing meter cannot be less than opening meter")
                            return@Button
                        }

                        val rec = WorkRecord(
                            id = recordToEdit?.id ?: 0L,
                            workDate = date,
                            customerId = cust.id,
                            customerName = cust.name,
                            machineId = mach.id,
                            machineName = mach.name,
                            worksite = worksite,
                            description = description,
                            openingMeter = start,
                            closingMeter = end,
                            nonBillableHours = nonBillable,
                            billableHours = if (calculatedHours > 0) calculatedHours else 1.0,
                            hourlyRate = rate,
                            operatorId = selectedOp?.id,
                            operatorName = selectedOp?.name,
                            invoiceId = recordToEdit?.invoiceId
                        )
                        viewModel.saveWorkRecord(rec)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark)
                ) {
                    Text("Save Log")
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
