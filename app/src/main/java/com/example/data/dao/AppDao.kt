package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface AppDao {

    // --- Business Settings ---
    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<BusinessSettings?>

    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): BusinessSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: BusinessSettings)

    // --- Customers ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomer(id: Long)

    // --- Machines ---
    @Query("SELECT * FROM machines ORDER BY name ASC")
    fun getAllMachines(): Flow<List<Machine>>

    @Query("SELECT * FROM machines WHERE id = :id")
    suspend fun getMachineById(id: Long): Machine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachine(machine: Machine): Long

    @Update
    suspend fun updateMachine(machine: Machine)

    @Query("DELETE FROM machines WHERE id = :id")
    suspend fun deleteMachine(id: Long)

    // --- Operators ---
    @Query("SELECT * FROM operators ORDER BY name ASC")
    fun getAllOperators(): Flow<List<Operator>>

    @Query("SELECT * FROM operators WHERE id = :id")
    suspend fun getOperatorById(id: Long): Operator?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperator(operator: Operator): Long

    @Update
    suspend fun updateOperator(operator: Operator)

    @Query("DELETE FROM operators WHERE id = :id")
    suspend fun deleteOperator(id: Long)

    // --- Work Records ---
    @Query("SELECT * FROM work_records ORDER BY createdAt DESC")
    fun getAllWorkRecords(): Flow<List<WorkRecord>>

    @Query("SELECT * FROM work_records WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getWorkRecordsByCustomer(customerId: Long): Flow<List<WorkRecord>>

    @Query("SELECT * FROM work_records WHERE customerId = :customerId AND invoiceId IS NULL ORDER BY createdAt DESC")
    fun getUnbilledWorkRecordsForCustomer(customerId: Long): Flow<List<WorkRecord>>

    @Query("SELECT * FROM work_records WHERE id = :id")
    suspend fun getWorkRecordById(id: Long): WorkRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkRecord(record: WorkRecord): Long

    @Update
    suspend fun updateWorkRecord(record: WorkRecord)

    @Query("UPDATE work_records SET invoiceId = :invoiceId WHERE id = :recordId")
    suspend fun attachInvoiceToWorkRecord(recordId: Long, invoiceId: Long)

    @Query("UPDATE work_records SET invoiceId = NULL WHERE invoiceId = :invoiceId")
    suspend fun detachInvoiceFromWorkRecords(invoiceId: Long)

    @Query("DELETE FROM work_records WHERE id = :id")
    suspend fun deleteWorkRecord(id: Long)

    // --- Invoices ---
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY id DESC")
    fun getInvoicesByCustomer(customerId: Long): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): Invoice?

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getInvoiceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Query("UPDATE invoices SET amountPaid = :amountPaid, balanceDue = :balanceDue, status = :status WHERE id = :id")
    suspend fun updateInvoicePayment(id: Long, amountPaid: Double, balanceDue: Double, status: String)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: Long)

    // --- Payments ---
    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY id DESC")
    fun getPaymentsByCustomer(customerId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY id DESC")
    fun getPaymentsByInvoice(invoiceId: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: Long)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE machineId = :machineId ORDER BY id DESC")
    fun getExpensesByMachine(machineId: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)

    // --- Users ---
    @Query("SELECT * FROM app_users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<AppUser>>

    @Query("SELECT COUNT(*) FROM app_users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUser): Long

    @Update
    suspend fun updateUser(user: AppUser)

    @Query("DELETE FROM app_users WHERE id = :id")
    suspend fun deleteUser(id: Long)
}
