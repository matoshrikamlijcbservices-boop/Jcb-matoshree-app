package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ExportBackupScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    fun shareContent(text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, title))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Data Export & Security Management", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Matoshree Kamalai Land Developers and Builders",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Generate authorized CSV reports and complete structural JSON data backups for accounting, audits, and external record keeping.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Text("Table CSV Exports", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyDark)
        }

        val exportItems = listOf(
            Pair("CUSTOMERS", "Export Customers CSV"),
            Pair("MACHINES", "Export JCB Fleet & Machines CSV"),
            Pair("WORK_RECORDS", "Export Daily Work Logs CSV"),
            Pair("INVOICES", "Export Invoices & Billing CSV"),
            Pair("PAYMENTS", "Export Payments & Receipts CSV"),
            Pair("EXPENSES", "Export Business Expenses CSV")
        )

        items(exportItems.size) { index ->
            val (type, label) = exportItems[index]
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Button(
                        onClick = {
                            val csv = viewModel.generateCsvExport(type)
                            shareContent(csv, "$label - Matoshree Kamalai")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Full System JSON Snapshot", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Creates a comprehensive structured summary snapshot of all records.", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val json = viewModel.generateFullBackupJson()
                            shareContent(json, "Full Database Backup - Matoshree Kamalai")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export & Share System Snapshot")
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Backup & Recovery Notice", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "All operational data is safely committed to the persistent database storage. Regular CSV exports should be preserved in secure business drives for compliance and auditing.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
