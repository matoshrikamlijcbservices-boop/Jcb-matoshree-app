package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.Invoice
import com.example.data.model.InvoiceLineItem
import com.example.data.model.Machine
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellow
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBillScreen(viewModel: MainViewModel) {
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val machines by viewModel.machinesState.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val workRecords by viewModel.workRecordsState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    // Invoice Header Fields
    var invoiceNumber by remember { mutableStateOf("") }
    var invoiceDate by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date()))
    }
    var dueDate by remember { mutableStateOf("") }
    var worksite by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Customer Selection
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    // Machine Selection
    var selectedMachine by remember { mutableStateOf<Machine?>(null) }
    var machineDropdownExpanded by remember { mutableStateOf(false) }

    // Line Items
    val lineItems = remember { mutableStateListOf<InvoiceLineItem>() }

    // Additional Charges
    var additionalChargesText by remember { mutableStateOf("") }
    var dieselChargesText by remember { mutableStateOf("") }
    var transportChargesText by remember { mutableStateOf("") }
    var operatorChargesText by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("") }
    var advancePaidText by remember { mutableStateOf("") }

    // Previews & Dialogs
    var showPreviewDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Load next invoice number on start
    LaunchedEffect(Unit) {
        invoiceNumber = viewModel.getNextInvoiceNumber()
        if (selectedMachine == null && machines.isNotEmpty()) {
            selectedMachine = machines.first()
        }
    }

    // Add initial line item if empty
    LaunchedEffect(selectedMachine) {
        if (lineItems.isEmpty()) {
            val rate = selectedMachine?.defaultHourlyRate ?: settings.defaultHourlyRate
            lineItems.add(
                InvoiceLineItem(
                    description = "JCB Earthmoving & Excavation",
                    workDate = invoiceDate,
                    openingMeter = selectedMachine?.currentMeterReading ?: 0.0,
                    closingMeter = (selectedMachine?.currentMeterReading ?: 0.0) + 4.0,
                    hours = 4.0,
                    hourlyRate = rate,
                    amount = 4.0 * rate
                )
            )
        }
    }

    // Computed financial totals
    val subtotal by remember {
        derivedStateOf { lineItems.sumOf { it.amount } }
    }
    val additionalCharges = additionalChargesText.toDoubleOrNull() ?: 0.0
    val dieselCharges = dieselChargesText.toDoubleOrNull() ?: 0.0
    val transportCharges = transportChargesText.toDoubleOrNull() ?: 0.0
    val operatorCharges = operatorChargesText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val advancePaid = advancePaidText.toDoubleOrNull() ?: 0.0

    val taxableAmount = max(0.0, subtotal + additionalCharges + dieselCharges + transportCharges + operatorCharges - discount)
    val taxAmount = if (settings.taxEnabled) {
        taxableAmount * (settings.cgstRate + settings.sgstRate) / 100.0
    } else 0.0

    val grandTotal = max(0.0, taxableAmount + taxAmount)
    val balanceDue = max(0.0, grandTotal - advancePaid)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "New JCB Tax Invoice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Matoshree Kamalai Land Developers and Builders",
                        fontSize = 12.sp,
                        color = ConstructionYellowDark,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = invoiceNumber,
                            onValueChange = { invoiceNumber = it },
                            label = { Text("Invoice No.") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_invoice_number")
                        )
                        OutlinedTextField(
                            value = invoiceDate,
                            onValueChange = { invoiceDate = it },
                            label = { Text("Date (DD/MM/YYYY)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_invoice_date")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Customer Dropdown
                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: "Select Customer *",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Customer *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("dropdown_customer")
                        )
                        ExposedDropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            customers.forEach { cust ->
                                DropdownMenuItem(
                                    text = { Text("${cust.name} (${cust.phone})") },
                                    onClick = {
                                        selectedCustomer = cust
                                        customerDropdownExpanded = false
                                        if (worksite.isBlank() && cust.address.isNotBlank()) {
                                            worksite = cust.address
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Machine Dropdown
                    ExposedDropdownMenuBox(
                        expanded = machineDropdownExpanded,
                        onExpandedChange = { machineDropdownExpanded = !machineDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMachine?.name ?: "Select Machine",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("JCB Machine") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = machineDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("dropdown_machine")
                        )
                        ExposedDropdownMenu(
                            expanded = machineDropdownExpanded,
                            onDismissRequest = { machineDropdownExpanded = false }
                        ) {
                            machines.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.name} (${m.machineCode} - ₹${m.defaultHourlyRate}/hr)") },
                                    onClick = {
                                        selectedMachine = m
                                        machineDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = worksite,
                        onValueChange = { worksite = it },
                        label = { Text("Worksite Location") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_worksite")
                    )
                }
            }
        }

        // Work Line Items Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Work Entries & Line Items",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        val rate = selectedMachine?.defaultHourlyRate ?: settings.defaultHourlyRate
                        lineItems.add(
                            InvoiceLineItem(
                                description = "JCB Earthmoving Work",
                                workDate = invoiceDate,
                                openingMeter = 0.0,
                                closingMeter = 0.0,
                                hours = 1.0,
                                hourlyRate = rate,
                                amount = rate
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_line_item")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Entry", fontSize = 12.sp)
                }
            }
        }

        itemsIndexed(lineItems) { index, item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Item #${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyDark
                        )
                        if (lineItems.size > 1) {
                            IconButton(onClick = { lineItems.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DangerRed)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = item.description,
                        onValueChange = { newDesc ->
                            lineItems[index] = item.copy(description = newDesc)
                        },
                        label = { Text("Work Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (item.openingMeter > 0) item.openingMeter.toString() else "",
                            onValueChange = { text ->
                                val start = text.toDoubleOrNull() ?: 0.0
                                val end = item.closingMeter
                                val calcHours = if (end > start) end - start else item.hours
                                val newAmount = calcHours * item.hourlyRate
                                lineItems[index] = item.copy(
                                    openingMeter = start,
                                    hours = calcHours,
                                    amount = newAmount
                                )
                            },
                            label = { Text("Start Mtr") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = if (item.closingMeter > 0) item.closingMeter.toString() else "",
                            onValueChange = { text ->
                                val end = text.toDoubleOrNull() ?: 0.0
                                val start = item.openingMeter
                                val calcHours = if (end > start) end - start else item.hours
                                val newAmount = calcHours * item.hourlyRate
                                lineItems[index] = item.copy(
                                    closingMeter = end,
                                    hours = calcHours,
                                    amount = newAmount
                                )
                            },
                            label = { Text("End Mtr") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = item.hours.toString(),
                            onValueChange = { text ->
                                val h = text.toDoubleOrNull() ?: 0.0
                                lineItems[index] = item.copy(hours = h, amount = h * item.hourlyRate)
                            },
                            label = { Text("Hours") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = item.hourlyRate.toString(),
                            onValueChange = { text ->
                                val r = text.toDoubleOrNull() ?: 0.0
                                lineItems[index] = item.copy(hourlyRate = r, amount = item.hours * r)
                            },
                            label = { Text("Rate (₹/hr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 10.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Item Amount", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                formatCurrency(item.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyDark
                            )
                        }
                    }
                }
            }
        }

        // Additional Charges & Adjustments
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Additional Charges & Discounts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dieselChargesText,
                            onValueChange = { dieselChargesText = it },
                            label = { Text("Diesel Billed (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = transportChargesText,
                            onValueChange = { transportChargesText = it },
                            label = { Text("Transport (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = operatorChargesText,
                            onValueChange = { operatorChargesText = it },
                            label = { Text("Operator (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = additionalChargesText,
                            onValueChange = { additionalChargesText = it },
                            label = { Text("Other Charges (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it },
                            label = { Text("Discount (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = advancePaidText,
                            onValueChange = { advancePaidText = it },
                            label = { Text("Advance Paid (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Bill Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Calculation Summary", color = ConstructionYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", color = Color.White)
                        Text(formatCurrency(subtotal), color = Color.White)
                    }
                    if (dieselCharges > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diesel Billed:", color = Color(0xFFCBD5E1))
                            Text(formatCurrency(dieselCharges), color = Color(0xFFCBD5E1))
                        }
                    }
                    if (transportCharges > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Transport Charges:", color = Color(0xFFCBD5E1))
                            Text(formatCurrency(transportCharges), color = Color(0xFFCBD5E1))
                        }
                    }
                    if (operatorCharges > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Operator Charges:", color = Color(0xFFCBD5E1))
                            Text(formatCurrency(operatorCharges), color = Color(0xFFCBD5E1))
                        }
                    }
                    if (discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount Applied:", color = DangerRed)
                            Text("- ${formatCurrency(discount)}", color = DangerRed)
                        }
                    }
                    if (settings.taxEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST (${settings.cgstRate + settings.sgstRate}%):", color = Color(0xFFCBD5E1))
                            Text(formatCurrency(taxAmount), color = Color(0xFFCBD5E1))
                        }
                    }

                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total:", color = ConstructionYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(formatCurrency(grandTotal), color = ConstructionYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (advancePaid > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Advance/Credit Paid:", color = SuccessGreen)
                            Text("- ${formatCurrency(advancePaid)}", color = SuccessGreen)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Balance Due:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(formatCurrency(balanceDue), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showPreviewDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preview Bill")
                }

                Button(
                    onClick = {
                        val cust = selectedCustomer
                        if (cust == null) {
                            viewModel.showMessage("Please select a customer for this bill")
                            return@Button
                        }
                        if (invoiceNumber.isBlank()) {
                            viewModel.showMessage("Please enter a valid invoice number")
                            return@Button
                        }
                        if (lineItems.isEmpty()) {
                            viewModel.showMessage("Please add at least one line item")
                            return@Button
                        }

                        isSaving = true
                        val newInvoice = Invoice(
                            invoiceNumber = invoiceNumber,
                            invoiceDate = invoiceDate,
                            dueDate = dueDate,
                            customerId = cust.id,
                            customerName = cust.name,
                            customerPhone = cust.phone,
                            customerAddress = cust.address,
                            customerGstin = cust.gstin,
                            worksite = worksite,
                            machineName = selectedMachine?.name ?: "",
                            lineItemsJson = InvoiceLineItem.listToJson(lineItems),
                            subtotal = subtotal,
                            additionalCharges = additionalCharges,
                            dieselChargesBilled = dieselCharges,
                            transportCharges = transportCharges,
                            operatorCharges = operatorCharges,
                            discount = discount,
                            taxAmount = taxAmount,
                            taxDetails = if (settings.taxEnabled) "CGST: ${settings.cgstRate}%, SGST: ${settings.sgstRate}%" else "Tax Exempt / Nil",
                            grandTotal = grandTotal,
                            amountPaid = advancePaid,
                            balanceDue = balanceDue,
                            status = when {
                                balanceDue <= 0.01 -> "PAID"
                                advancePaid > 0.0 -> "PARTIALLY_PAID"
                                else -> "UNPAID"
                            },
                            paymentTerms = settings.invoiceTerms,
                            notes = notes
                        )

                        viewModel.createInvoice(newInvoice) {
                            isSaving = false
                            viewModel.navigateTo(ScreenDestination.ALL_BILLS)
                        }
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_invoice")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSaving) "Saving..." else "Save Invoice")
                }
            }
        }
    }

    // Bill Preview Dialog
    if (showPreviewDialog) {
        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = {
                Text("Bill Preview: $invoiceNumber", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Company: Matoshree Kamalai Land Developers and Builders", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("Customer: ${selectedCustomer?.name ?: "N/A"}")
                    Text("Machine: ${selectedMachine?.name ?: "N/A"}")
                    Text("Worksite: $worksite")
                    Text("Line Items: ${lineItems.size} entries")
                    Divider()
                    Text("Subtotal: ${formatCurrency(subtotal)}")
                    Text("Grand Total: ${formatCurrency(grandTotal)}", fontWeight = FontWeight.Bold, color = NavyDark)
                    Text("Balance Due: ${formatCurrency(balanceDue)}", fontWeight = FontWeight.Bold, color = DangerRed)
                }
            },
            confirmButton = {
                Button(onClick = { showPreviewDialog = false }) {
                    Text("Close Preview")
                }
            }
        )
    }
}
