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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ReportsScreen(viewModel: MainViewModel) {
    val invoices by viewModel.invoicesState.collectAsStateWithLifecycle()
    val payments by viewModel.paymentsState.collectAsStateWithLifecycle()
    val expenses by viewModel.expensesState.collectAsStateWithLifecycle()
    val workRecords by viewModel.workRecordsState.collectAsStateWithLifecycle()
    val machines by viewModel.machinesState.collectAsStateWithLifecycle()
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val totalBilled = invoices.filter { it.status != "VOID" }.sumOf { it.grandTotal }
    val totalCollected = payments.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netResult = totalBilled - totalExpenses

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Executive Financial Report", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Matoshree Kamalai Land Developers and Builders", color = ConstructionYellow, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Billed", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(formatCurrency(totalBilled), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Total Collected", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(formatCurrency(totalCollected), color = SuccessGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Expenses", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(formatCurrency(totalExpenses), color = DangerRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Operating Result (Estimated):", color = Color(0xFFE2E8F0), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(
                            formatCurrency(netResult),
                            color = if (netResult >= 0) SuccessGreen else DangerRed,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("* Estimated pre-tax operating profit based on accrued invoicing minus operational expenses.", color = Color(0xFF64748B), fontSize = 10.sp)
                }
            }
        }

        // Customer Revenue Ranking
        item {
            Text("Top Customers by Billed Revenue", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyDark)
        }

        val customerRankings = customers.map { c ->
            val cTotal = invoices.filter { it.customerId == c.id && it.status != "VOID" }.sumOf { it.grandTotal }
            Pair(c.name, cTotal)
        }.sortedByDescending { it.second }.take(5)

        items(customerRankings) { (name, amount) ->
            val percentage = if (totalBilled > 0) (amount / totalBilled).toFloat() else 0f
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
                        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(formatCurrency(amount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { percentage.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = ConstructionYellowDark,
                        trackColor = Color(0xFFF1F5F9)
                    )
                }
            }
        }

        // Machine Utilization Report
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Machine Utilization & Profitability", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyDark)
        }

        items(machines) { m ->
            val mWork = workRecords.filter { it.machineId == m.id }
            val mHours = mWork.sumOf { it.billableHours }
            val mRev = mWork.sumOf { it.billableHours * it.hourlyRate }
            val mExp = expenses.filter { it.machineId == m.id }.sumOf { it.amount }
            val mProfit = mRev - mExp

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
                        Text(m.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${String.format("%.1f", mHours)} Hours", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyDark)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Revenue: ${formatCurrency(mRev)}", fontSize = 12.sp, color = SuccessGreen)
                        Text("Expenses: ${formatCurrency(mExp)}", fontSize = 12.sp, color = DangerRed)
                        Text("Net: ${formatCurrency(mProfit)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (mProfit >= 0) NavyDark else DangerRed)
                    }
                }
            }
        }

        // Expense Breakdown
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Expense Breakdown by Category", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyDark)
        }

        val categoryGroups = expenses.groupBy { it.category }
        items(categoryGroups.entries.toList()) { (cat, list) ->
            val catTotal = list.sumOf { it.amount }
            val pct = if (totalExpenses > 0) (catTotal / totalExpenses).toFloat() else 0f
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
                        Text(cat.replace("_", " "), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(formatCurrency(catTotal), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DangerRed)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { pct.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = DangerRed,
                        trackColor = Color(0xFFF1F5F9)
                    )
                }
            }
        }
    }
}
