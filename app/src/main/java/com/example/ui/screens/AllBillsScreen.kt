package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.Invoice
import com.example.data.model.InvoiceLineItem
import com.example.data.model.Payment
import com.example.ui.components.PdfGenerator
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AllBillsScreen(viewModel: MainViewModel) {
    val invoices by viewModel.invoicesState.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }

    // Dialog state for payment recording
    var invoiceForPayment by remember { mutableStateOf<Invoice?>(null) }
    var paymentAmountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("UPI") }
    var paymentRef by remember { mutableStateOf("") }

    // Dialog state for invoice details
    var selectedInvoiceForDetails by remember { mutableStateOf<Invoice?>(null) }

    val filteredInvoices = invoices.filter { inv ->
        val matchesSearch = inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.customerName.contains(searchQuery, ignoreCase = true) ||
                inv.worksite.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (statusFilter) {
            "ALL" -> true
            "UNPAID" -> inv.status == "UNPAID"
            "PARTIAL" -> inv.status == "PARTIALLY_PAID"
            "PAID" -> inv.status == "PAID"
            "VOID" -> inv.status == "VOID"
            else -> true
        }
        matchesSearch && matchesStatus
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by invoice #, customer, worksite...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_search_invoices")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filters = listOf(
                Pair("ALL", "All Bills"),
                Pair("UNPAID", "Unpaid"),
                Pair("PARTIAL", "Partially Paid"),
                Pair("PAID", "Paid"),
                Pair("VOID", "Void")
            )
            items(filters) { (key, label) ->
                FilterChip(
                    selected = statusFilter == key,
                    onClick = { statusFilter = key },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyDark,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredInvoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No bills match the filter", fontSize = 15.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.NEW_BILL) },
                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create New JCB Bill")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredInvoices) { inv ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedInvoiceForDetails = inv }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
                                    Text("Date: ${inv.invoiceDate}", fontSize = 11.sp, color = Color.Gray)
                                }
                                StatusBadge(status = inv.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(inv.customerName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            if (inv.worksite.isNotBlank()) {
                                Text("Worksite: ${inv.worksite}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Amount", fontSize = 11.sp, color = Color.Gray)
                                    Text(formatCurrency(inv.grandTotal), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Paid", fontSize = 11.sp, color = Color.Gray)
                                    Text(formatCurrency(inv.amountPaid), fontWeight = FontWeight.Medium, fontSize = 13.sp, color = SuccessGreen)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Balance Due", fontSize = 11.sp, color = Color.Gray)
                                    Text(formatCurrency(inv.balanceDue), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (inv.balanceDue > 0) DangerRed else SuccessGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // PDF Download & Share
                                OutlinedButton(
                                    onClick = {
                                        val pdfFile = PdfGenerator.generateInvoicePdf(context, inv, settings)
                                        if (pdfFile != null) {
                                            PdfGenerator.sharePdf(context, pdfFile, "Invoice ${inv.invoiceNumber}")
                                        } else {
                                            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_pdf_${inv.invoiceNumber}")
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = DangerRed)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // PNG Image Download & Share
                                OutlinedButton(
                                    onClick = {
                                        val pngFile = PdfGenerator.generateInvoicePng(context, inv, settings)
                                        if (pngFile != null) {
                                            PdfGenerator.sharePng(context, pngFile, "Invoice ${inv.invoiceNumber}")
                                        } else {
                                            Toast.makeText(context, "Failed to generate PNG", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_png_${inv.invoiceNumber}")
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = InfoBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PNG", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Quick Collect Payment (if balance remains)
                                if (inv.balanceDue > 0.01 && inv.status != "VOID") {
                                    Button(
                                        onClick = {
                                            invoiceForPayment = inv
                                            paymentAmountText = String.format(Locale.ENGLISH, "%.2f", inv.balanceDue)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("btn_pay_${inv.invoiceNumber}")
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Collect", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Void button
                                if (inv.status != "VOID") {
                                    IconButton(onClick = { viewModel.voidInvoice(inv.id) }) {
                                        Icon(Icons.Default.Block, contentDescription = "Void", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Collection Dialog
    invoiceForPayment?.let { inv ->
        AlertDialog(
            onDismissRequest = { invoiceForPayment = null },
            title = { Text("Collect Payment for ${inv.invoiceNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Customer: ${inv.customerName}", fontWeight = FontWeight.Bold)
                    Text("Total Balance Due: ${formatCurrency(inv.balanceDue)}", color = DangerRed)

                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Amount to Collect (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Payment Mode (UPI / CASH / BANK / CHEQUE)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = paymentRef,
                        onValueChange = { paymentRef = it },
                        label = { Text("Transaction Reference / Cheque No.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountText.toDoubleOrNull() ?: 0.0
                        if (amount <= 0.0) {
                            viewModel.showMessage("Please enter a valid amount")
                            return@Button
                        }
                        val payment = Payment(
                            paymentNumber = "REC-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.ENGLISH).format(Date()),
                            customerId = inv.customerId,
                            customerName = inv.customerName,
                            invoiceId = inv.id,
                            invoiceNumber = inv.invoiceNumber,
                            paymentDate = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date()),
                            amount = amount,
                            paymentMethod = paymentMethod.uppercase(),
                            transactionRef = paymentRef,
                            notes = "Payment against invoice ${inv.invoiceNumber}"
                        )
                        viewModel.recordPayment(payment)
                        invoiceForPayment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Save Payment")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { invoiceForPayment = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full Details Modal Dialog
    selectedInvoiceForDetails?.let { inv ->
        AlertDialog(
            onDismissRequest = { selectedInvoiceForDetails = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Invoice: ${inv.invoiceNumber}", fontWeight = FontWeight.Bold)
                    StatusBadge(status = inv.status)
                }
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("Matoshree Kamalai Land Developers and Builders", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 12.sp)
                        Text("Date: ${inv.invoiceDate}  |  Customer: ${inv.customerName}")
                        Text("Worksite: ${inv.worksite}")
                        Divider()
                        Text("Line Items Breakdown:", fontWeight = FontWeight.SemiBold)
                        val items = InvoiceLineItem.listFromJson(inv.lineItemsJson)
                        items.forEach { li ->
                            Text("• ${li.description}: ${li.hours} hrs @ ₹${li.hourlyRate} = ₹${li.amount}", fontSize = 12.sp)
                        }
                        Divider()
                        Text("Subtotal: ${formatCurrency(inv.subtotal)}")
                        if (inv.dieselChargesBilled > 0) Text("Diesel Billed: ${formatCurrency(inv.dieselChargesBilled)}")
                        if (inv.transportCharges > 0) Text("Transport: ${formatCurrency(inv.transportCharges)}")
                        if (inv.operatorCharges > 0) Text("Operator: ${formatCurrency(inv.operatorCharges)}")
                        if (inv.discount > 0) Text("Discount: -${formatCurrency(inv.discount)}", color = DangerRed)
                        if (inv.taxAmount > 0) Text("Tax: ${formatCurrency(inv.taxAmount)}")
                        Text("Grand Total: ${formatCurrency(inv.grandTotal)}", fontWeight = FontWeight.Bold, color = NavyDark)
                        Text("Amount Paid: ${formatCurrency(inv.amountPaid)}", color = SuccessGreen)
                        Text("Balance Due: ${formatCurrency(inv.balanceDue)}", fontWeight = FontWeight.Bold, color = DangerRed)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val pngFile = PdfGenerator.generateInvoicePng(context, inv, settings)
                            if (pngFile != null) {
                                PdfGenerator.sharePng(context, pngFile, "Invoice ${inv.invoiceNumber}")
                            } else {
                                Toast.makeText(context, "Failed to generate PNG", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = InfoBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PNG")
                    }

                    Button(
                        onClick = {
                            val pdfFile = PdfGenerator.generateInvoicePdf(context, inv, settings)
                            if (pdfFile != null) {
                                PdfGenerator.sharePdf(context, pdfFile, "Invoice ${inv.invoiceNumber}")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedInvoiceForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}
