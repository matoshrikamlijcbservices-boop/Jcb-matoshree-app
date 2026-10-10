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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellow
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val invoices by viewModel.invoicesState.collectAsStateWithLifecycle()
    val payments by viewModel.paymentsState.collectAsStateWithLifecycle()
    val expenses by viewModel.expensesState.collectAsStateWithLifecycle()
    val workRecords by viewModel.workRecordsState.collectAsStateWithLifecycle()
    val machines by viewModel.machinesState.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("All Time") }

    // Aggregations
    val totalBilled = invoices.filter { it.status != "VOID" }.sumOf { it.grandTotal }
    val totalCollected = payments.sumOf { it.amount }
    val totalOutstanding = invoices.filter { it.status != "VOID" }.sumOf { it.balanceDue }
    val totalExpenses = expenses.sumOf { it.amount }
    val estimatedOperatingProfit = totalBilled - totalExpenses
    val totalHours = workRecords.sumOf { it.billableHours }
    val unpaidCount = invoices.count { it.status == "UNPAID" || it.status == "PARTIALLY_PAID" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Matoshree Kamalai Land Developers and Builders",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "JCB Earthmoving & Construction Management",
                                color = ConstructionYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ConstructionYellow, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = NavyDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Operating Result", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(
                                text = formatCurrency(estimatedOperatingProfit),
                                color = if (estimatedOperatingProfit >= 0) SuccessGreen else DangerRed,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Working Hours", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(
                                text = String.format("%.1f Hrs", totalHours),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(ScreenDestination.NEW_BILL) },
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_new_bill")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Bill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.navigateTo(ScreenDestination.PAYMENTS) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_payment")
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.WORK_RECORDS) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_work_log")
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Work", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Billed",
                        value = formatCurrency(totalBilled),
                        icon = Icons.Default.Receipt,
                        iconColor = InfoBlue,
                        subtitle = "${invoices.size} Invoices",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Payments Collected",
                        value = formatCurrency(totalCollected),
                        icon = Icons.Default.AccountBalance,
                        iconColor = SuccessGreen,
                        subtitle = "${payments.size} Receipts",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Outstanding Balance",
                        value = formatCurrency(totalOutstanding),
                        icon = Icons.Default.PendingActions,
                        iconColor = DangerRed,
                        subtitle = "$unpaidCount Unpaid/Partial",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Expenses",
                        value = formatCurrency(totalExpenses),
                        icon = Icons.Default.MoneyOff,
                        iconColor = ConstructionYellowDark,
                        subtitle = "${expenses.size} Entries",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Machine Utilization Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "JCB Fleet & Machines (${machines.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    color = ConstructionYellowDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.MACHINES) }
                )
            }
        }

        items(machines.take(3)) { machine ->
            val machineWork = workRecords.filter { it.machineId == machine.id }
            val machineHours = machineWork.sumOf { it.billableHours }
            val machineExpenses = expenses.filter { it.machineId == machine.id }.sumOf { it.amount }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = machine.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(status = machine.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Code: ${machine.machineCode} | Rate: ₹${machine.defaultHourlyRate}/hr",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format("%.1f Hrs", machineHours),
                            fontWeight = FontWeight.Bold,
                            color = NavyDark,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Fuel/Exp: ${formatCurrency(machineExpenses)}",
                            fontSize = 11.sp,
                            color = DangerRed
                        )
                    }
                }
            }
        }

        // Recent Invoices
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Invoices",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See All",
                    color = ConstructionYellowDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.ALL_BILLS) }
                )
            }
        }

        if (invoices.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No invoices generated yet. Click 'New Bill' to create one.", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(invoices.take(4)) { inv ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(ScreenDestination.ALL_BILLS) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(inv.customerName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(inv.invoiceDate, fontSize = 11.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatCurrency(inv.grandTotal), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusBadge(status = inv.status)
                        }
                    }
                }
            }
        }
    }
}
