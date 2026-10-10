package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.AppUser
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Invoice
import com.example.data.model.Machine
import com.example.data.model.Operator
import com.example.data.model.Payment
import com.example.data.model.WorkRecord
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class BillingRepository(private val dao: AppDao) {

    // Settings
    val settingsFlow: Flow<BusinessSettings?> = dao.getSettingsFlow()
    suspend fun getSettings(): BusinessSettings {
        return dao.getSettings() ?: BusinessSettings()
    }
    suspend fun saveSettings(settings: BusinessSettings) = dao.saveSettings(settings)

    // Customers
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    suspend fun getCustomerById(id: Long) = dao.getCustomerById(id)
    suspend fun insertCustomer(customer: Customer) = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(id: Long) = dao.deleteCustomer(id)

    // Machines
    val allMachines: Flow<List<Machine>> = dao.getAllMachines()
    suspend fun getMachineById(id: Long) = dao.getMachineById(id)
    suspend fun insertMachine(machine: Machine) = dao.insertMachine(machine)
    suspend fun updateMachine(machine: Machine) = dao.updateMachine(machine)
    suspend fun deleteMachine(id: Long) = dao.deleteMachine(id)

    // Operators
    val allOperators: Flow<List<Operator>> = dao.getAllOperators()
    suspend fun getOperatorById(id: Long) = dao.getOperatorById(id)
    suspend fun insertOperator(operator: Operator) = dao.insertOperator(operator)
    suspend fun updateOperator(operator: Operator) = dao.updateOperator(operator)
    suspend fun deleteOperator(id: Long) = dao.deleteOperator(id)

    // Work Records
    val allWorkRecords: Flow<List<WorkRecord>> = dao.getAllWorkRecords()
    fun getWorkRecordsByCustomer(customerId: Long): Flow<List<WorkRecord>> =
        dao.getWorkRecordsByCustomer(customerId)
    fun getUnbilledWorkRecordsForCustomer(customerId: Long): Flow<List<WorkRecord>> =
        dao.getUnbilledWorkRecordsForCustomer(customerId)
    suspend fun insertWorkRecord(record: WorkRecord): Long {
        val id = dao.insertWorkRecord(record)
        // If closing meter is greater than machine current reading, update machine
        val machine = dao.getMachineById(record.machineId)
        if (machine != null && record.closingMeter > machine.currentMeterReading) {
            dao.updateMachine(machine.copy(currentMeterReading = record.closingMeter))
        }
        return id
    }
    suspend fun updateWorkRecord(record: WorkRecord) = dao.updateWorkRecord(record)
    suspend fun deleteWorkRecord(id: Long) = dao.deleteWorkRecord(id)

    // Invoices
    val allInvoices: Flow<List<Invoice>> = dao.getAllInvoices()
    fun getInvoicesByCustomer(customerId: Long): Flow<List<Invoice>> =
        dao.getInvoicesByCustomer(customerId)
    suspend fun getInvoiceById(id: Long) = dao.getInvoiceById(id)

    suspend fun createInvoice(
        invoice: Invoice,
        associatedWorkRecordIds: List<Long> = emptyList()
    ): Long {
        val invoiceId = dao.insertInvoice(invoice)
        // Attach work records
        for (recId in associatedWorkRecordIds) {
            dao.attachInvoiceToWorkRecord(recId, invoiceId)
        }
        return invoiceId
    }

    suspend fun updateInvoice(invoice: Invoice) = dao.updateInvoice(invoice)

    suspend fun voidInvoice(invoiceId: Long) {
        val inv = dao.getInvoiceById(invoiceId) ?: return
        dao.updateInvoice(inv.copy(status = "VOID"))
        dao.detachInvoiceFromWorkRecords(invoiceId)
    }

    suspend fun deleteInvoice(id: Long) {
        dao.detachInvoiceFromWorkRecords(id)
        dao.deleteInvoice(id)
    }

    suspend fun getNextInvoiceNumber(): String {
        val settings = getSettings()
        val count = dao.getInvoiceCount()
        val year = SimpleDateFormat("yyyy", Locale.ENGLISH).format(Date())
        return "${settings.invoicePrefix}$year-${String.format(Locale.ENGLISH, "%03d", count + 1)}"
    }

    // Payments
    val allPayments: Flow<List<Payment>> = dao.getAllPayments()
    fun getPaymentsByCustomer(customerId: Long): Flow<List<Payment>> =
        dao.getPaymentsByCustomer(customerId)
    fun getPaymentsByInvoice(invoiceId: Long): Flow<List<Payment>> =
        dao.getPaymentsByInvoice(invoiceId)

    suspend fun recordPayment(payment: Payment): Long {
        val paymentId = dao.insertPayment(payment)
        // If this payment is linked to an invoice, allocate and update invoice balance
        payment.invoiceId?.let { invId ->
            val invoice = dao.getInvoiceById(invId)
            if (invoice != null && invoice.status != "VOID") {
                val newAmountPaid = invoice.amountPaid + payment.amount
                val newBalanceDue = max(0.0, invoice.grandTotal - newAmountPaid)
                val newStatus = when {
                    newBalanceDue <= 0.01 -> "PAID"
                    newAmountPaid > 0.0 -> "PARTIALLY_PAID"
                    else -> "UNPAID"
                }
                dao.updateInvoicePayment(invoice.id, newAmountPaid, newBalanceDue, newStatus)
            }
        }
        return paymentId
    }

    suspend fun deletePayment(payment: Payment) {
        // If linked to an invoice, rollback the allocated amount
        payment.invoiceId?.let { invId ->
            val invoice = dao.getInvoiceById(invId)
            if (invoice != null && invoice.status != "VOID") {
                val newAmountPaid = max(0.0, invoice.amountPaid - payment.amount)
                val newBalanceDue = max(0.0, invoice.grandTotal - newAmountPaid)
                val newStatus = when {
                    newBalanceDue <= 0.01 -> "PAID"
                    newAmountPaid > 0.0 -> "PARTIALLY_PAID"
                    else -> "UNPAID"
                }
                dao.updateInvoicePayment(invoice.id, newAmountPaid, newBalanceDue, newStatus)
            }
        }
        dao.deletePayment(payment.id)
    }

    // Expenses
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    fun getExpensesByMachine(machineId: Long): Flow<List<Expense>> =
        dao.getExpensesByMachine(machineId)
    suspend fun insertExpense(expense: Expense) = dao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)
    suspend fun deleteExpense(id: Long) = dao.deleteExpense(id)

    // Users
    val allUsers: Flow<List<AppUser>> = dao.getAllUsers()
    suspend fun insertUser(user: AppUser) = dao.insertUser(user)
    suspend fun updateUser(user: AppUser) = dao.updateUser(user)
    suspend fun deleteUser(id: Long) = dao.deleteUser(id)
}
