package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel

data class LedgerEntry(
    val date: String,
    val type: String, // INVOICE or PAYMENT
    val refNo: String,
    val description: String,
    val debit: Double, // Invoice amount added
    val credit: Double, // Payment amount subtracted
    val runningBalance: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(viewModel: MainViewModel) {
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val invoices by viewModel.invoicesState.collectAsStateWithLifecycle()
    val payments by viewModel.paymentsState.collectAsStateWithLifecycle()
    val selectedCustomerId by viewModel.selectedLedgerCustomerId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var customerDropdownExpanded by remember { mutableStateOf(false) }

    val activeCustomer = customers.find { it.id == selectedCustomerId } ?: customers.firstOrNull()

    // Calculate customer transactions
    val customerInvoices = invoices.filter { it.customerId == activeCustomer?.id && it.status != "VOID" }
    val customerPayments = payments.filter { it.customerId == activeCustomer?.id }

    val totalBilled = customerInvoices.sumOf { it.grandTotal }
    val totalPaid = customerPayments.sumOf { it.amount }
    val netOutstanding = totalBilled - totalPaid

    // Build unified chronological ledger
    val entries = mutableListOf<LedgerEntry>()
    var running = 0.0

    // Combine invoices and payments sorted by createdAt
    val rawTransactions = (customerInvoices.map { Pair(it.createdAt, it) } + customerPayments.map { Pair(it.createdAt, it) })
        .sortedBy { it.first }

    for (item in rawTransactions) {
        val obj = item.second
        if (obj is com.example.data.model.Invoice) {
            running += obj.grandTotal
            entries.add(
                LedgerEntry(
                    date = obj.invoiceDate,
                    type = "INVOICE",
                    refNo = obj.invoiceNumber,
                    description = "JCB Work: ${obj.worksite.ifEmpty { "Site Work" }}",
                    debit = obj.grandTotal,
                    credit = 0.0,
                    runningBalance = running
                )
            )
        } else if (obj is com.example.data.model.Payment) {
            running -= obj.amount
            entries.add(
                LedgerEntry(
                    date = obj.paymentDate,
                    type = "PAYMENT",
                    refNo = obj.paymentNumber,
                    description = "Payment via ${obj.paymentMethod}",
                    debit = 0.0,
                    credit = obj.amount,
                    runningBalance = running
                )
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Customer Selection
        item {
            ExposedDropdownMenuBox(
                expanded = customerDropdownExpanded,
                onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = activeCustomer?.name ?: "Select Customer",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Customer for Statement") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false }
                ) {
                    customers.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.name} (${c.phone})") },
                            onClick = {
                                viewModel.selectLedgerCustomer(c.id)
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Ledger Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Customer Statement & Account Ledger", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text(
                        activeCustomer?.name ?: "No Customer Selected",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Matoshree Kamalai Land Developers and Builders", color = ConstructionYellow, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Billed (Dr)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(formatCurrency(totalBilled), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Total Paid (Cr)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(formatCurrency(totalPaid), color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Balance Due", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(
                                formatCurrency(netOutstanding),
                                color = if (netOutstanding > 0) DangerRed else SuccessGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ledger History (${entries.size} Entries)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)

                OutlinedButton(
                    onClick = {
                        val csv = StringBuilder()
                        csv.append("Date,Type,Ref,Description,Debit,Credit,RunningBalance\n")
                        entries.forEach {
                            csv.append("\"${it.date}\",\"${it.type}\",\"${it.refNo}\",\"${it.description}\",${it.debit},${it.credit},${it.runningBalance}\n")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Ledger Statement for ${activeCustomer?.name}\n\n$csv")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Ledger CSV"))
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share CSV", fontSize = 12.sp)
                }
            }
        }

        if (entries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions found for this customer.", color = Color.Gray)
                }
            }
        } else {
            items(entries) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(entry.refNo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = entry.type,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (entry.type == "INVOICE") DangerRed else SuccessGreen
                                    )
                                }
                                Text("Date: ${entry.date}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (entry.debit > 0) {
                                    Text("+ ${formatCurrency(entry.debit)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DangerRed)
                                } else {
                                    Text("- ${formatCurrency(entry.credit)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SuccessGreen)
                                }
                                Text("Bal: ${formatCurrency(entry.runningBalance)}", fontSize = 11.sp, color = NavyDark)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(entry.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
