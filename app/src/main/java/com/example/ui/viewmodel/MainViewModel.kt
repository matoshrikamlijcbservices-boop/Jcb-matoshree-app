package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.AppUser
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Invoice
import com.example.data.model.InvoiceLineItem
import com.example.data.model.Machine
import com.example.data.model.Operator
import com.example.data.model.Payment
import com.example.data.model.WorkRecord
import com.example.data.repository.BillingRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScreenDestination(val title: String) {
    DASHBOARD("Dashboard"),
    NEW_BILL("New JCB Bill"),
    ALL_BILLS("All Bills"),
    CUSTOMERS("Customers"),
    MACHINES("JCB Machines"),
    WORK_RECORDS("Work Records"),
    PAYMENTS("Payments"),
    EXPENSES("Expenses"),
    OPERATORS("Operators"),
    REPORTS("Reports & Analytics"),
    CUSTOMER_LEDGER("Customer Ledger"),
    SETTINGS("Business Settings"),
    EXPORT_BACKUP("Data Export & Backup"),
    USER_MANAGEMENT("User Management")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BillingRepository
    private val firestoreSyncManager = FirestoreSyncManager(application)

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = BillingRepository(database.appDao())
    }

    // Navigation state
    private val _currentScreen = MutableStateFlow(ScreenDestination.DASHBOARD)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    // Role simulation
    private val _currentUser = MutableStateFlow(
        AppUser(
            id = 1,
            username = "admin",
            fullName = "Authorized Manager",
            role = "OWNER"
        )
    )
    val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    fun setCurrentUser(user: AppUser) {
        _currentUser.value = user
    }

    // Toast / Feedback events
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    fun showMessage(msg: String) {
        viewModelScope.launch { _snackbarMessage.emit(msg) }
    }

    // Flows from DB
    val settingsState: StateFlow<BusinessSettings> = repository.settingsFlow
        .map { it ?: BusinessSettings() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            BusinessSettings()
        )

    val customersState: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val machinesState: StateFlow<List<Machine>> = repository.allMachines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val operatorsState: StateFlow<List<Operator>> = repository.allOperators
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workRecordsState: StateFlow<List<WorkRecord>> = repository.allWorkRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoicesState: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentsState: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expensesState: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val usersState: StateFlow<List<AppUser>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected customer for Ledger
    private val _selectedLedgerCustomerId = MutableStateFlow<Long?>(null)
    val selectedLedgerCustomerId: StateFlow<Long?> = _selectedLedgerCustomerId.asStateFlow()

    fun selectLedgerCustomer(customerId: Long?) {
        _selectedLedgerCustomerId.value = customerId
    }

    // --- Actions ---

    fun saveSettings(settings: BusinessSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            firestoreSyncManager.syncBusinessSettings(settings)
            showMessage("Settings saved successfully & synced to Firestore")
        }
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            val id = if (customer.id == 0L) {
                val newId = repository.insertCustomer(customer)
                showMessage("Customer '${customer.name}' added successfully")
                newId
            } else {
                repository.updateCustomer(customer)
                showMessage("Customer updated successfully")
                customer.id
            }
            firestoreSyncManager.syncCustomer(customer.copy(id = id))
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
            showMessage("Customer deleted")
        }
    }

    fun saveMachine(machine: Machine) {
        viewModelScope.launch {
            if (machine.id == 0L) {
                repository.insertMachine(machine)
                showMessage("Machine '${machine.name}' added")
            } else {
                repository.updateMachine(machine)
                showMessage("Machine updated")
            }
        }
    }

    fun deleteMachine(id: Long) {
        viewModelScope.launch {
            repository.deleteMachine(id)
            showMessage("Machine removed")
        }
    }

    fun saveOperator(operator: Operator) {
        viewModelScope.launch {
            if (operator.id == 0L) {
                repository.insertOperator(operator)
                showMessage("Operator '${operator.name}' added")
            } else {
                repository.updateOperator(operator)
                showMessage("Operator updated")
            }
        }
    }

    fun deleteOperator(id: Long) {
        viewModelScope.launch {
            repository.deleteOperator(id)
            showMessage("Operator removed")
        }
    }

    fun saveWorkRecord(record: WorkRecord) {
        viewModelScope.launch {
            if (record.id == 0L) {
                repository.insertWorkRecord(record)
                showMessage("Work log saved successfully")
            } else {
                repository.updateWorkRecord(record)
                showMessage("Work log updated")
            }
        }
    }

    fun deleteWorkRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkRecord(id)
            showMessage("Work record deleted")
        }
    }

    suspend fun getNextInvoiceNumber(): String {
        return repository.getNextInvoiceNumber()
    }

    fun createInvoice(
        invoice: Invoice,
        workRecordIds: List<Long> = emptyList(),
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repository.createInvoice(invoice, workRecordIds)
            firestoreSyncManager.syncInvoice(invoice.copy(id = id))
            showMessage("Invoice ${invoice.invoiceNumber} created & synced to Firestore")
            onSuccess(id)
        }
    }

    fun voidInvoice(invoiceId: Long) {
        viewModelScope.launch {
            repository.voidInvoice(invoiceId)
            val inv = repository.getInvoiceById(invoiceId)
            if (inv != null) firestoreSyncManager.syncInvoice(inv)
            showMessage("Invoice marked as VOID & updated in Firestore")
        }
    }

    fun deleteInvoice(id: Long) {
        viewModelScope.launch {
            repository.deleteInvoice(id)
            showMessage("Invoice deleted")
        }
    }

    fun recordPayment(payment: Payment) {
        viewModelScope.launch {
            val id = repository.recordPayment(payment)
            firestoreSyncManager.syncPayment(payment.copy(id = id))
            // Also sync updated invoice if linked
            payment.invoiceId?.let { invId ->
                val inv = repository.getInvoiceById(invId)
                if (inv != null) firestoreSyncManager.syncInvoice(inv)
            }
            showMessage("Payment of ₹${payment.amount} recorded & synced to Firestore")
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            showMessage("Payment removed")
        }
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            val id = if (expense.id == 0L) {
                val newId = repository.insertExpense(expense)
                showMessage("Expense of ₹${expense.amount} logged & synced")
                newId
            } else {
                repository.updateExpense(expense)
                showMessage("Expense updated")
                expense.id
            }
            firestoreSyncManager.syncExpense(expense.copy(id = id))
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
            showMessage("Expense deleted")
        }
    }

    fun saveUser(user: AppUser) {
        viewModelScope.launch {
            if (user.id == 0L) {
                repository.insertUser(user)
                showMessage("User '${user.fullName}' added")
            } else {
                repository.updateUser(user)
                showMessage("User updated")
            }
        }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch {
            repository.deleteUser(id)
            showMessage("User deleted")
        }
    }

    // CSV & Backup Generation
    fun generateCsvExport(type: String): String {
        val sb = StringBuilder()
        when (type) {
            "CUSTOMERS" -> {
                sb.append("ID,Name,Phone,Address,Email,GSTIN,Active\n")
                customersState.value.forEach {
                    sb.append("${it.id},\"${it.name}\",\"${it.phone}\",\"${it.address}\",\"${it.email}\",\"${it.gstin}\",${it.isActive}\n")
                }
            }
            "MACHINES" -> {
                sb.append("ID,Code,Name,RegNumber,Type,HourlyRate,CurrentMeter,Status\n")
                machinesState.value.forEach {
                    sb.append("${it.id},\"${it.machineCode}\",\"${it.name}\",\"${it.regNumber}\",\"${it.machineType}\",${it.defaultHourlyRate},${it.currentMeterReading},\"${it.status}\"\n")
                }
            }
            "WORK_RECORDS" -> {
                sb.append("ID,Date,Customer,Machine,Worksite,StartMeter,EndMeter,BillableHours,Rate,Operator\n")
                workRecordsState.value.forEach {
                    sb.append("${it.id},\"${it.workDate}\",\"${it.customerName}\",\"${it.machineName}\",\"${it.worksite}\",${it.openingMeter},${it.closingMeter},${it.billableHours},${it.hourlyRate},\"${it.operatorName ?: ""}\"\n")
                }
            }
            "INVOICES" -> {
                sb.append("ID,InvoiceNumber,Date,Customer,Total,Paid,BalanceDue,Status\n")
                invoicesState.value.forEach {
                    sb.append("${it.id},\"${it.invoiceNumber}\",\"${it.invoiceDate}\",\"${it.customerName}\",${it.grandTotal},${it.amountPaid},${it.balanceDue},\"${it.status}\"\n")
                }
            }
            "PAYMENTS" -> {
                sb.append("ID,ReceiptNumber,Date,Customer,Amount,Method,Ref,InvoiceNo\n")
                paymentsState.value.forEach {
                    sb.append("${it.id},\"${it.paymentNumber}\",\"${it.paymentDate}\",\"${it.customerName}\",${it.amount},\"${it.paymentMethod}\",\"${it.transactionRef}\",\"${it.invoiceNumber ?: ""}\"\n")
                }
            }
            "EXPENSES" -> {
                sb.append("ID,Date,Category,Amount,Machine,Supplier,Method,Description\n")
                expensesState.value.forEach {
                    sb.append("${it.id},\"${it.expenseDate}\",\"${it.category}\",${it.amount},\"${it.machineName ?: ""}\",\"${it.supplier}\",\"${it.paymentMethod}\",\"${it.description}\"\n")
                }
            }
        }
        return sb.toString()
    }

    fun generateFullBackupJson(): String {
        val root = JSONObject()
        root.put("companyName", "Matoshree Kamalai Land Developers and Builders")
        root.put("backupDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date()))
        root.put("customersCount", customersState.value.size)
        root.put("machinesCount", machinesState.value.size)
        root.put("invoicesCount", invoicesState.value.size)
        root.put("paymentsCount", paymentsState.value.size)
        root.put("expensesCount", expensesState.value.size)
        return root.toString(2)
    }
}
