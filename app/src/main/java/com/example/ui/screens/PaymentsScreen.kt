package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.Payment
import com.example.ui.components.PdfGenerator
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(viewModel: MainViewModel) {
    val payments by viewModel.paymentsState.collectAsStateWithLifecycle()
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val invoices by viewModel.invoicesState.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }

    val totalCollected = payments.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = SuccessGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_payment")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Record Payment")
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
                    Text("Total Payments Collected", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatCurrency(totalCollected),
                        color = SuccessGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${payments.size} verified transactions", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Receipts & Payment History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyDark)
            Spacer(modifier = Modifier.height(10.dp))

            if (payments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No payment records found. Tap + to record a customer payment.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(payments) { p ->
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
                                        Text(p.paymentNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                                        Text("Date: ${p.paymentDate}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text(
                                        formatCurrency(p.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = SuccessGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Customer: ${p.customerName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Mode: ${p.paymentMethod}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (p.transactionRef.isNotBlank()) {
                                        Text("Ref: ${p.transactionRef}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                if (!p.invoiceNumber.isNullOrBlank()) {
                                    Text("Linked Invoice: ${p.invoiceNumber}", fontSize = 12.sp, color = ConstructionYellowDark)
                                }
                                if (p.notes.isNotBlank()) {
                                    Text("Notes: ${p.notes}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val pdfFile = PdfGenerator.generatePaymentReceiptPdf(context, p, settings)
                                            if (pdfFile != null) {
                                                PdfGenerator.sharePdf(context, pdfFile, "Receipt ${p.paymentNumber}")
                                            } else {
                                                Toast.makeText(context, "Could not generate receipt", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp), tint = DangerRed)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Receipt PDF", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { viewModel.deletePayment(p) }) {
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

    // Add Payment Dialog
    if (showDialog) {
        var selectedCust by remember { mutableStateOf(customers.firstOrNull()) }
        var custExpanded by remember { mutableStateOf(false) }

        val customerInvoices = invoices.filter { it.customerId == selectedCust?.id && it.balanceDue > 0.01 && it.status != "VOID" }
        var selectedInv by remember { mutableStateOf<Invoice?>(customerInvoices.firstOrNull()) }
        var invExpanded by remember { mutableStateOf(false) }

        var amountText by remember {
            mutableStateOf(if (selectedInv != null) String.format(Locale.ENGLISH, "%.2f", selectedInv!!.balanceDue) else "")
        }
        var method by remember { mutableStateOf("UPI") }
        var refNo by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date())) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Record Customer Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Customer Dropdown
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
                                        selectedInv = invoices.firstOrNull { it.customerId == c.id && it.balanceDue > 0.01 }
                                        if (selectedInv != null) {
                                            amountText = String.format(Locale.ENGLISH, "%.2f", selectedInv!!.balanceDue)
                                        }
                                        custExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Optional Invoice Dropdown
                    if (customerInvoices.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = invExpanded,
                            onExpandedChange = { invExpanded = !invExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedInv?.invoiceNumber ?: "Advance / Direct Payment",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Against Invoice (Optional)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = invExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = invExpanded, onDismissRequest = { invExpanded = false }) {
                                customerInvoices.forEach { inv ->
                                    DropdownMenuItem(
                                        text = { Text("${inv.invoiceNumber} (Bal: ₹${inv.balanceDue})") },
                                        onClick = {
                                            selectedInv = inv
                                            amountText = String.format(Locale.ENGLISH, "%.2f", inv.balanceDue)
                                            invExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Payment Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = method,
                        onValueChange = { method = it },
                        label = { Text("Payment Method (UPI, CASH, BANK, CHEQUE)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = refNo,
                        onValueChange = { refNo = it },
                        label = { Text("Transaction Reference No.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Payment Date (DD/MM/YYYY)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cust = selectedCust
                        if (cust == null) {
                            viewModel.showMessage("Please select a customer")
                            return@Button
                        }
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt <= 0.0) {
                            viewModel.showMessage("Please enter a valid payment amount")
                            return@Button
                        }

                        val payNumber = "REC-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.ENGLISH).format(Date())
                        val p = Payment(
                            paymentNumber = payNumber,
                            customerId = cust.id,
                            customerName = cust.name,
                            invoiceId = selectedInv?.id,
                            invoiceNumber = selectedInv?.invoiceNumber,
                            paymentDate = date,
                            amount = amt,
                            paymentMethod = method.uppercase(),
                            transactionRef = refNo,
                            notes = notes
                        )
                        viewModel.recordPayment(p)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Save Payment")
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
