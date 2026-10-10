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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.model.Expense
import com.example.data.model.Machine
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NavyDark
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: MainViewModel) {
    val expenses by viewModel.expensesState.collectAsStateWithLifecycle()
    val machines by viewModel.machinesState.collectAsStateWithLifecycle()

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "DIESEL",
        "OPERATOR_WAGES",
        "MAINTENANCE",
        "SPARE_PARTS",
        "TRANSPORT",
        "INSURANCE",
        "PERMITS",
        "OTHER"
    )

    val filteredExpenses = expenses.filter {
        selectedCategoryFilter == "ALL" || it.category == selectedCategoryFilter
    }

    val totalAmount = expenses.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = DangerRed,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_expense")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Business Expenses", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatCurrency(totalAmount),
                        color = DangerRed,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${expenses.size} expenses logged", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "ALL",
                        onClick = { selectedCategoryFilter = "ALL" },
                        label = { Text("All Categories") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat.replace("_", " ")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No expenses found for this category.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredExpenses) { exp ->
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
                                        Text(exp.category.replace("_", " "), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                                        Text("Date: ${exp.expenseDate}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text(
                                        formatCurrency(exp.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = DangerRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(exp.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (!exp.machineName.isNullOrBlank()) {
                                    Text("Machine: ${exp.machineName}", fontSize = 12.sp, color = ConstructionYellowDark)
                                }
                                if (exp.supplier.isNotBlank()) {
                                    Text("Supplier/Payee: ${exp.supplier}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { viewModel.deleteExpense(exp.id) }) {
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

    // Add Expense Dialog
    if (showDialog) {
        var category by remember { mutableStateOf("DIESEL") }
        var catExpanded by remember { mutableStateOf(false) }

        var selectedMach by remember { mutableStateOf<Machine?>(machines.firstOrNull()) }
        var machExpanded by remember { mutableStateOf(false) }

        var amountText by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("Fuel refill for site") }
        var supplier by remember { mutableStateOf("Bharat Petroleum") }
        var method by remember { mutableStateOf("UPI") }
        var refNo by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Log New Business Expense", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        ExposedDropdownMenuBox(
                            expanded = catExpanded,
                            onExpandedChange = { catExpanded = !catExpanded }
                        ) {
                            OutlinedTextField(
                                value = category.replace("_", " "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Expense Category *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                categories.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.replace("_", " ")) },
                                        onClick = {
                                            category = c
                                            catExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        ExposedDropdownMenuBox(
                            expanded = machExpanded,
                            onExpandedChange = { machExpanded = !machExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedMach?.name ?: "None / General Business",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Target JCB Machine (Optional)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = machExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = machExpanded, onDismissRequest = { machExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("None / General Business") },
                                    onClick = {
                                        selectedMach = null
                                        machExpanded = false
                                    }
                                )
                                machines.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m.name) },
                                        onClick = {
                                            selectedMach = m
                                            machExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Amount (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Expense Description *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = supplier,
                            onValueChange = { supplier = it },
                            label = { Text("Vendor / Supplier Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = method,
                            onValueChange = { method = it },
                            label = { Text("Payment Mode (UPI, CASH, BANK)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt <= 0.0) {
                            viewModel.showMessage("Please enter valid expense amount")
                            return@Button
                        }
                        val exp = Expense(
                            expenseDate = date,
                            category = category,
                            amount = amt,
                            machineId = selectedMach?.id,
                            machineName = selectedMach?.name,
                            supplier = supplier,
                            paymentMethod = method.uppercase(),
                            referenceNo = refNo,
                            description = description
                        )
                        viewModel.saveExpense(exp)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Save Expense")
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
