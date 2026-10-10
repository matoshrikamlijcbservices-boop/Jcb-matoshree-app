package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.example.data.model.InvoiceLineItem
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionYellow
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * State representation of a single Working Entry (Bucket or Breaker)
 * Using Compose MutableState for reactive real-time recalculation
 */
class MachineWorkEntryState(
    val id: String = java.util.UUID.randomUUID().toString(),
    initialWorkType: String = "BUCKET", // BUCKET or BREAKER
    initialStartHour: Int = 0,
    initialStartMinute: Int = 0,
    initialEndHour: Int = 0,
    initialEndMinute: Int = 0,
    initialRate: Double = 1500.0,
    initialIsSet: Boolean = false
) {
    var workType by mutableStateOf(initialWorkType)
    var startHour by mutableIntStateOf(initialStartHour)
    var startMinute by mutableIntStateOf(initialStartMinute)
    var endHour by mutableIntStateOf(initialEndHour)
    var endMinute by mutableIntStateOf(initialEndMinute)
    var rate by mutableDoubleStateOf(initialRate)
    var isTimeSet by mutableStateOf(initialIsSet)

    val startTimeString: String
        get() = if (!isTimeSet && startHour == 0 && startMinute == 0) "00:00" else formatTime12h(startHour, startMinute)

    val endTimeString: String
        get() = if (!isTimeSet && endHour == 0 && endMinute == 0) "00:00" else formatTime12h(endHour, endMinute)

    val calculatedHours: Double
        get() {
            if (!isTimeSet && startHour == 0 && startMinute == 0 && endHour == 0 && endMinute == 0) {
                return 0.0
            }
            var diffMin = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)
            if (diffMin < 0) {
                diffMin += 24 * 60 // rolled over to next day
            }
            val h = diffMin / 60.0
            return (Math.round(h * 100.0) / 100.0)
        }

    val calculatedAmount: Double
        get() {
            val amt = calculatedHours * rate
            return (Math.round(amt * 100.0) / 100.0)
        }
}

/**
 * State representation of a Machine in the bill
 */
class MachineBillState(
    val id: String = java.util.UUID.randomUUID().toString(),
    initialMachineName: String = "Machine 1",
    val entries: SnapshotStateList<MachineWorkEntryState> = mutableStateListOf()
) {
    var machineName by mutableStateOf(initialMachineName)

    val bucketTotal: Double
        get() = entries.filter { it.workType == "BUCKET" }.sumOf { it.calculatedAmount }

    val breakerTotal: Double
        get() = entries.filter { it.workType == "BREAKER" }.sumOf { it.calculatedAmount }

    val totalAmount: Double
        get() = bucketTotal + breakerTotal
}

/**
 * Helper to format 24-hr hour and minute to readable 12-hr AM/PM
 */
fun formatTime12h(hour: Int, minute: Int): String {
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val ampm = if (hour < 12) "AM" else "PM"
    val minStr = String.format(Locale.ENGLISH, "%02d", minute)
    return String.format(Locale.ENGLISH, "%02d:%s %s", h, minStr, ampm)
}

@Composable
fun NewBillScreen(viewModel: MainViewModel) {
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Calendar & Date Picker
    val calendar = remember { Calendar.getInstance() }
    var billDate by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date()))
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                billDate = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Customer Information
    var customerNameInput by remember { mutableStateOf("") }
    var customerPhoneInput by remember { mutableStateOf("") }
    var customerAddressInput by remember { mutableStateOf("") }
    var showCustomerSuggestions by remember { mutableStateOf(false) }

    // Sorted customers alphabetically A to Z
    val sortedCustomers = remember(customers) {
        customers.sortedBy { it.name.lowercase(Locale.ENGLISH) }
    }

    // Filtered matching customers
    val matchingCustomers = remember(customerNameInput, sortedCustomers) {
        if (customerNameInput.isBlank()) {
            sortedCustomers
        } else {
            sortedCustomers.filter {
                it.name.contains(customerNameInput, ignoreCase = true) ||
                        it.phone.contains(customerNameInput, ignoreCase = true)
            }
        }
    }

    // Default Rates at Top for Bucket and Breaker
    var bucketDefaultRateText by remember { mutableStateOf("1500") }
    var breakerDefaultRateText by remember { mutableStateOf("2000") }

    LaunchedEffect(settings) {
        if (settings.defaultBucketRate > 0) {
            bucketDefaultRateText = settings.defaultBucketRate.toInt().toString()
        }
        if (settings.defaultBreakerRate > 0) {
            breakerDefaultRateText = settings.defaultBreakerRate.toInt().toString()
        }
    }

    val currentBucketRate = bucketDefaultRateText.toDoubleOrNull() ?: 1500.0
    val currentBreakerRate = breakerDefaultRateText.toDoubleOrNull() ?: 2000.0

    // Machines list: initially Machine 1 starts clean with no entries until user adds Bucket or Breaker
    val machines = remember {
        mutableStateListOf<MachineBillState>().apply {
            val initialMachine = MachineBillState(initialMachineName = "Machine 1")
            add(initialMachine)
        }
    }

    // Payment Section
    var amountPaidText by remember { mutableStateOf("") }
    var paymentRemarksInput by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Real-time calculated totals
    val totalBucketAmount = machines.sumOf { it.bucketTotal }
    val totalBreakerAmount = machines.sumOf { it.breakerTotal }
    val grandestGrandTotal = totalBucketAmount + totalBreakerAmount

    val amountPaid = amountPaidText.toDoubleOrNull() ?: 0.0
    val remainingBalance = max(0.0, grandestGrandTotal - amountPaid)

    // Helper clock picker invocation
    fun openClockPicker(
        initialHour: Int,
        initialMinute: Int,
        onTimeSelected: (Int, Int) -> Unit
    ) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            initialHour,
            initialMinute,
            false // 12-hour AM/PM clock
        ).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header & Date with Calendar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Matoshree Kamalai Land Developers and Builders",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "New Working Entries Bill",
                        color = ConstructionYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date row with prominent Calendar Picker
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(ConstructionYellow, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Calendar",
                                        tint = NavyDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Bill Date (Tap to Change)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text(billDate, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            Button(
                                onClick = { datePickerDialog.show() },
                                colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_select_calendar_date")
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calendar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Customer Information Card with Alphabetical Suggestions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NavyDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Customer Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Customer Name Input
                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = {
                            customerNameInput = it
                            showCustomerSuggestions = true
                        },
                        label = { Text("Customer Name * (Type to search or add new)") },
                        trailingIcon = {
                            if (customerNameInput.isNotBlank()) {
                                IconButton(onClick = {
                                    customerNameInput = ""
                                    customerPhoneInput = ""
                                    customerAddressInput = ""
                                    showCustomerSuggestions = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_name")
                    )

                    // Alphabetical suggestions list
                    if (showCustomerSuggestions && matchingCustomers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Saved Customers (${matchingCustomers.size} found - A to Z)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "Tap to select",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 4.dp))

                                matchingCustomers.take(6).forEach { cust ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                customerNameInput = cust.name
                                                customerPhoneInput = cust.phone
                                                customerAddressInput = cust.address
                                                showCustomerSuggestions = false
                                            }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = cust.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = NavyDark
                                            )
                                            if (cust.phone.isNotBlank()) {
                                                Text(
                                                    text = "📞 ${cust.phone}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                        }

                                        Surface(
                                            color = Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Saved",
                                                color = Color(0xFF16A34A),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Check if current name matches an existing customer
                    val isExistingCustomer = sortedCustomers.any {
                        it.name.equals(customerNameInput.trim(), ignoreCase = true)
                    }

                    if (customerNameInput.isNotBlank() && !isExistingCustomer) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = ConstructionYellowDark, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New customer will be automatically saved directly to directory",
                                fontSize = 11.sp,
                                color = ConstructionYellowDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Customer Phone Input
                    OutlinedTextField(
                        value = customerPhoneInput,
                        onValueChange = { customerPhoneInput = it },
                        label = { Text("Customer Phone Number *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_phone")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Customer Worksite / Address (Optional)
                    OutlinedTextField(
                        value = customerAddressInput,
                        onValueChange = { customerAddressInput = it },
                        label = { Text("Worksite Location / Address (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Default Rates Card (Before all machines at top)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = ConstructionYellowDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Default Hourly Rates", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
                        }

                        // Button to persist as system default
                        OutlinedButton(
                            onClick = {
                                viewModel.saveSettings(
                                    settings.copy(
                                        defaultBucketRate = currentBucketRate,
                                        defaultBreakerRate = currentBreakerRate
                                    )
                                )
                                viewModel.showMessage("Saved as default rates (Bucket: ₹$bucketDefaultRateText, Breaker: ₹$breakerDefaultRateText)")
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Set as Default", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = bucketDefaultRateText,
                            onValueChange = {
                                bucketDefaultRateText = it
                                val newRate = it.toDoubleOrNull() ?: 1500.0
                                // Update all existing bucket entries
                                machines.forEach { m ->
                                    m.entries.filter { e -> e.workType == "BUCKET" }.forEach { e ->
                                        e.rate = newRate
                                    }
                                }
                            },
                            label = { Text("Bucket Rate (₹/hr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_bucket_default_rate")
                        )

                        OutlinedTextField(
                            value = breakerDefaultRateText,
                            onValueChange = {
                                breakerDefaultRateText = it
                                val newRate = it.toDoubleOrNull() ?: 2000.0
                                // Update all existing breaker entries
                                machines.forEach { m ->
                                    m.entries.filter { e -> e.workType == "BREAKER" }.forEach { e ->
                                        e.rate = newRate
                                    }
                                }
                            },
                            label = { Text("Breaker Rate (₹/hr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_breaker_default_rate")
                        )
                    }
                }
            }
        }

        // Section Title: Working Entries & Add Machine Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Working Entries", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                    Text("Unlimited machines, bucket & breaker shifts", fontSize = 11.sp, color = Color.Gray)
                }

                // Add Another Machine Button (Machine 1, Machine 2, Machine 3...)
                Button(
                    onClick = {
                        val nextIndex = machines.size + 1
                        val newMachine = MachineBillState(initialMachineName = "Machine $nextIndex")
                        // Machine starts empty until user clicks + Bucket or + Breaker
                        machines.add(newMachine)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_another_machine")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Machine", fontSize = 12.sp)
                }
            }
        }

        // Machines & Working Entries Rendering
        items(machines.size) { mIndex ->
            val machine = machines[mIndex]

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Machine Header (Name / ID & Delete button if > 1 machine)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = machine.machineName,
                            onValueChange = { machine.machineName = it },
                            label = { Text("Machine Name / Number") },
                            modifier = Modifier.weight(1f)
                        )

                        if (machines.size > 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { machines.removeAt(mIndex) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Machine", tint = DangerRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons to add Bucket or Breaker work entries (Unlimited times)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                machine.entries.add(
                                    MachineWorkEntryState(
                                        initialWorkType = "BUCKET",
                                        initialStartHour = 0,
                                        initialStartMinute = 0,
                                        initialEndHour = 0,
                                        initialEndMinute = 0,
                                        initialRate = currentBucketRate,
                                        initialIsSet = false
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ConstructionYellowDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Bucket", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ConstructionYellowDark)
                        }

                        OutlinedButton(
                            onClick = {
                                machine.entries.add(
                                    MachineWorkEntryState(
                                        initialWorkType = "BREAKER",
                                        initialStartHour = 0,
                                        initialStartMinute = 0,
                                        initialEndHour = 0,
                                        initialEndMinute = 0,
                                        initialRate = currentBreakerRate,
                                        initialIsSet = false
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = InfoBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Breaker", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InfoBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Entries list under this machine
                    machine.entries.forEachIndexed { eIndex, entry ->
                        val isBucket = entry.workType == "BUCKET"
                        val tagColor = if (isBucket) ConstructionYellowDark else InfoBlue
                        val tagBg = if (isBucket) Color(0xFFFEF3C7) else Color(0xFFDBEAFE)

                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Top Entry Row: Badge & Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = tagBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (isBucket) "BUCKET" else "BREAKER",
                                            color = tagColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (machine.entries.size > 1) {
                                        IconButton(
                                            onClick = { machine.entries.removeAt(eIndex) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Clock Pickers: Start Time & End Time
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Start Time Clock Button
                                    OutlinedButton(
                                        onClick = {
                                            openClockPicker(entry.startHour, entry.startMinute) { hour, minute ->
                                                entry.startHour = hour
                                                entry.startMinute = minute
                                                entry.isTimeSet = true
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("Start Time", fontSize = 9.sp, color = Color.Gray)
                                            Text(entry.startTimeString, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // End Time Clock Button
                                    OutlinedButton(
                                        onClick = {
                                            openClockPicker(entry.endHour, entry.endMinute) { hour, minute ->
                                                entry.endHour = hour
                                                entry.endMinute = minute
                                                entry.isTimeSet = true
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("End Time", fontSize = 9.sp, color = Color.Gray)
                                            Text(entry.endTimeString, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Calculations Summary for this entry:
                                // Normal theme-matching light surface (not harsh stark white)
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${entry.calculatedHours} hrs @ ₹${entry.rate.toInt()}/hr",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = NavyDark
                                            )
                                            Text(
                                                text = "(${entry.startTimeString} to ${entry.endTimeString})",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Amount",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = formatCurrency(entry.calculatedAmount),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NavyDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))

                    // Machine Specific Totals Summary (Machine 1 Total, Bucket Total, Breaker Total)
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${machine.machineName} Total:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = NavyDark
                                )
                                Text(
                                    text = formatCurrency(machine.totalAmount),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = ConstructionYellowDark
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Bucket Total: ${formatCurrency(machine.bucketTotal)}", fontSize = 11.sp, color = Color(0xFF475569))
                                Text("Breaker Total: ${formatCurrency(machine.breakerTotal)}", fontSize = 11.sp, color = Color(0xFF475569))
                            }
                        }
                    }
                }
            }
        }

        // Summary Card: Total Bucket, Total Breaker, and Grandest Grand Amount
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Working Entries Grand Summary",
                        color = ConstructionYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Bucket Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Bucket Amount:", color = Color.White, fontSize = 13.sp)
                        Text(formatCurrency(totalBucketAmount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Breaker Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Breaker Amount:", color = Color.White, fontSize = 13.sp)
                        Text(formatCurrency(totalBreakerAmount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 10.dp))

                    // Grandest Grand Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grandest Grand Total:",
                            color = ConstructionYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = formatCurrency(grandestGrandTotal),
                            color = ConstructionYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        // Payment Option & Dynamic Color-Coded Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Status & Collection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick shortcut chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { amountPaidText = grandestGrandTotal.toInt().toString() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Full Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }

                        OutlinedButton(
                            onClick = { amountPaidText = (grandestGrandTotal / 2).toInt().toString() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("50% Paid", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { amountPaidText = "0" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Unpaid (₹0)", fontSize = 11.sp, color = DangerRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountPaidText,
                        onValueChange = { amountPaidText = it },
                        label = { Text("Amount Paid / Received (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_amount_paid")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Visual Status Box according to user specifications:
                    // Paid in green color if full amount paid
                    // Partially paid if some part is given, and remaining amount shown
                    // Not paid in red color if nothing given
                    when {
                        grandestGrandTotal > 0.0 && amountPaid >= grandestGrandTotal - 0.01 -> {
                            // FULLY PAID: GREEN COLOR
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, Color(0xFF16A34A), RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "PAID (FULL AMOUNT RECEIVED)",
                                            color = Color(0xFF15803D),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Full amount of ${formatCurrency(grandestGrandTotal)} received. Remaining balance: ₹0",
                                            color = Color(0xFF166534),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        amountPaid > 0.0 -> {
                            // PARTIALLY PAID: AMBER / ORANGE COLOR with REMAINING AMOUNT SHOWN
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "PARTIALLY PAID",
                                            color = Color(0xFFB45309),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Amount Paid: ${formatCurrency(amountPaid)}",
                                            color = Color(0xFF92400E),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Remaining Amount to Collect: ${formatCurrency(remainingBalance)}",
                                            color = DangerRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            // NOT PAID: RED COLOR
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "NOT PAID (UNPAID)",
                                            color = Color(0xFFB91C1C),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Nothing given yet. Remaining Amount: ${formatCurrency(grandestGrandTotal)}",
                                            color = Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = paymentRemarksInput,
                        onValueChange = { paymentRemarksInput = it },
                        label = { Text("Payment Remarks / Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Save & Finalize Working Entries Bill Button
        item {
            Button(
                onClick = {
                    val name = customerNameInput.trim()
                    val phone = customerPhoneInput.trim()

                    if (name.isBlank()) {
                        viewModel.showMessage("Please enter the customer name")
                        return@Button
                    }
                    if (phone.isBlank()) {
                        viewModel.showMessage("Please enter the customer phone number")
                        return@Button
                    }

                    // Ensure at least one entry has hours > 0
                    val hasWorkingEntries = machines.any { m -> m.entries.any { it.calculatedHours > 0 } }
                    if (!hasWorkingEntries) {
                        viewModel.showMessage("Please set clock start and end time for working entries")
                        return@Button
                    }

                    isSaving = true

                    // Build line items from all machines' bucket & breaker entries
                    val lineItems = mutableListOf<InvoiceLineItem>()
                    machines.forEach { m ->
                        m.entries.forEach { e ->
                            lineItems.add(
                                InvoiceLineItem(
                                    description = "${m.machineName} - ${e.workType} (${e.startTimeString} to ${e.endTimeString})",
                                    workDate = billDate,
                                    openingMeter = 0.0,
                                    closingMeter = 0.0,
                                    hours = e.calculatedHours,
                                    hourlyRate = e.rate,
                                    amount = e.calculatedAmount,
                                    machineLabel = m.machineName,
                                    workType = e.workType,
                                    startTime = e.startTimeString,
                                    endTime = e.endTimeString
                                )
                            )
                        }
                    }

                    // Auto-generate invoice number in the background
                    val year = SimpleDateFormat("yyyy", Locale.ENGLISH).format(Date())
                    val timestamp = SimpleDateFormat("MMdd-HHmmss", Locale.ENGLISH).format(Date())
                    val generatedInvNo = "${settings.invoicePrefix}$year-$timestamp"

                    val finalStatus = when {
                        grandestGrandTotal > 0 && amountPaid >= grandestGrandTotal - 0.01 -> "PAID"
                        amountPaid > 0 -> "PARTIALLY_PAID"
                        else -> "UNPAID"
                    }

                    val newInvoice = Invoice(
                        invoiceNumber = generatedInvNo,
                        invoiceDate = billDate,
                        dueDate = billDate,
                        customerId = 0L, // will be resolved/saved in ViewModel
                        customerName = name,
                        customerPhone = phone,
                        customerAddress = customerAddressInput.trim(),
                        worksite = customerAddressInput.trim(),
                        machineName = machines.joinToString(", ") { it.machineName },
                        lineItemsJson = InvoiceLineItem.listToJson(lineItems),
                        subtotal = grandestGrandTotal,
                        additionalCharges = 0.0,
                        discount = 0.0,
                        grandTotal = grandestGrandTotal,
                        amountPaid = 0.0, // Initial base is 0.0; recordPayment below will allocate exact paymentAmount
                        balanceDue = grandestGrandTotal,
                        status = "UNPAID",
                        paymentTerms = settings.invoiceTerms,
                        notes = paymentRemarksInput
                    )

                    viewModel.saveBillWithCustomerAndPayment(
                        customerName = name,
                        customerPhone = phone,
                        customerAddress = customerAddressInput.trim(),
                        invoice = newInvoice,
                        paymentAmount = amountPaid
                    ) {
                        isSaving = false
                        viewModel.navigateTo(ScreenDestination.ALL_BILLS)
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionYellowDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_working_entries_bill")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSaving) "Saving Bill..." else "Save Working Entries Bill",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
