package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.NavyDark
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val currentSettings by viewModel.settingsState.collectAsStateWithLifecycle()

    var companyName by remember(currentSettings) { mutableStateOf(currentSettings.companyName) }
    var tagline by remember(currentSettings) { mutableStateOf(currentSettings.tagline) }
    var phone by remember(currentSettings) { mutableStateOf(currentSettings.phone) }
    var email by remember(currentSettings) { mutableStateOf(currentSettings.email) }
    var address by remember(currentSettings) { mutableStateOf(currentSettings.address) }
    var gstin by remember(currentSettings) { mutableStateOf(currentSettings.gstin) }
    var invoicePrefix by remember(currentSettings) { mutableStateOf(currentSettings.invoicePrefix) }
    var defaultRateText by remember(currentSettings) { mutableStateOf(currentSettings.defaultHourlyRate.toString()) }

    var upiId by remember(currentSettings) { mutableStateOf(currentSettings.upiId) }
    var bankName by remember(currentSettings) { mutableStateOf(currentSettings.bankName) }
    var accountHolder by remember(currentSettings) { mutableStateOf(currentSettings.accountHolder) }
    var accountNumber by remember(currentSettings) { mutableStateOf(currentSettings.accountNumber) }
    var ifscCode by remember(currentSettings) { mutableStateOf(currentSettings.ifscCode) }
    var paymentInstructions by remember(currentSettings) { mutableStateOf(currentSettings.paymentInstructions) }
    var invoiceTerms by remember(currentSettings) { mutableStateOf(currentSettings.invoiceTerms) }

    var taxEnabled by remember(currentSettings) { mutableStateOf(currentSettings.taxEnabled) }
    var cgstText by remember(currentSettings) { mutableStateOf(currentSettings.cgstRate.toString()) }
    var sgstText by remember(currentSettings) { mutableStateOf(currentSettings.sgstRate.toString()) }
    var igstText by remember(currentSettings) { mutableStateOf(currentSettings.igstRate.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = NavyDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Business & Legal Identity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_company_name")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Tagline / Subheading") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Business Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gstin,
                            onValueChange = { gstin = it },
                            label = { Text("GSTIN (Optional)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = invoicePrefix,
                            onValueChange = { invoicePrefix = it },
                            label = { Text("Invoice Prefix") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = defaultRateText,
                        onValueChange = { defaultRateText = it },
                        label = { Text("Default Machine Rate (₹/hr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Bank Details & Payment Instructions Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Instructions & Bank Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID (e.g. business@upi)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text("Bank Name") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ifscCode,
                            onValueChange = { ifscCode = it },
                            label = { Text("IFSC Code") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = accountHolder,
                            onValueChange = { accountHolder = it },
                            label = { Text("Account Holder") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = { accountNumber = it },
                            label = { Text("Account Number") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = paymentInstructions,
                        onValueChange = { paymentInstructions = it },
                        label = { Text("Payment Instructions for Invoices") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = invoiceTerms,
                        onValueChange = { invoiceTerms = it },
                        label = { Text("Invoice Terms & Conditions Footer") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Tax Settings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("GST & Tax Calculation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
                            Text(if (taxEnabled) "Tax is active on new bills" else "Disabled until configured", fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = taxEnabled,
                            onCheckedChange = { taxEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ConstructionYellowDark)
                        )
                    }

                    if (taxEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cgstText,
                                onValueChange = { cgstText = it },
                                label = { Text("CGST %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sgstText,
                                onValueChange = { sgstText = it },
                                label = { Text("SGST %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = igstText,
                                onValueChange = { igstText = it },
                                label = { Text("IGST %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val rate = defaultRateText.toDoubleOrNull() ?: 1800.0
                    val cgst = cgstText.toDoubleOrNull() ?: 9.0
                    val sgst = sgstText.toDoubleOrNull() ?: 9.0
                    val igst = igstText.toDoubleOrNull() ?: 18.0

                    viewModel.saveSettings(
                        currentSettings.copy(
                            companyName = companyName,
                            tagline = tagline,
                            phone = phone,
                            email = email,
                            address = address,
                            gstin = gstin,
                            invoicePrefix = invoicePrefix,
                            defaultHourlyRate = rate,
                            upiId = upiId,
                            bankName = bankName,
                            accountHolder = accountHolder,
                            accountNumber = accountNumber,
                            ifscCode = ifscCode,
                            paymentInstructions = paymentInstructions,
                            invoiceTerms = invoiceTerms,
                            taxEnabled = taxEnabled,
                            cgstRate = cgst,
                            sgstRate = sgst,
                            igstRate = igst
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_save_settings")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Business Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
