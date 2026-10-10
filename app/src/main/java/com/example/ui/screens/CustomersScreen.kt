package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.Customer
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun CustomersScreen(viewModel: MainViewModel) {
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val invoices by viewModel.invoicesState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    customerToEdit = Customer(name = "", phone = "")
                    showAddDialog = true
                },
                containerColor = ConstructionYellowDark,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search customers by name, phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_customers")
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No customers found. Click + to add.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredCustomers) { cust ->
                        val customerInvoices = invoices.filter { it.customerId == cust.id && it.status != "VOID" }
                        val customerOutstanding = customerInvoices.sumOf { it.balanceDue }

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
                                        Icon(Icons.Default.Person, contentDescription = null, tint = NavyDark)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    StatusBadge(status = if (cust.isActive) "ACTIVE" else "INACTIVE")
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(cust.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (cust.address.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Address: ${cust.address}", fontSize = 12.sp, color = Color.Gray)
                                }
                                if (cust.gstin.isNotBlank()) {
                                    Text("GSTIN: ${cust.gstin}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Outstanding Balance", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            formatCurrency(customerOutstanding),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (customerOutstanding > 0) DangerRed else SuccessGreen
                                        )
                                    }

                                    Row {
                                        // Open Ledger Button
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.selectLedgerCustomer(cust.id)
                                                viewModel.navigateTo(ScreenDestination.CUSTOMER_LEDGER)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Ledger", fontSize = 12.sp)
                                        }

                                        IconButton(onClick = {
                                            customerToEdit = cust
                                            showAddDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyDark)
                                        }

                                        IconButton(onClick = { viewModel.deleteCustomer(cust.id) }) {
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

    // Add / Edit Customer Dialog
    if (showAddDialog && customerToEdit != null) {
        var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
        var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
        var address by remember { mutableStateOf(customerToEdit?.address ?: "") }
        var email by remember { mutableStateOf(customerToEdit?.email ?: "") }
        var gstin by remember { mutableStateOf(customerToEdit?.gstin ?: "") }
        var notes by remember { mutableStateOf(customerToEdit?.notes ?: "") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(if (customerToEdit?.id == 0L) "Add New Customer" else "Edit Customer Details", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name *") },
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
                        label = { Text("Address / Worksites") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it },
                        label = { Text("GSTIN (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Terms") },
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
                        viewModel.saveCustomer(
                            customerToEdit!!.copy(
                                name = name,
                                phone = phone,
                                address = address,
                                email = email,
                                gstin = gstin,
                                notes = notes
                            )
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark)
                ) {
                    Text("Save Customer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
