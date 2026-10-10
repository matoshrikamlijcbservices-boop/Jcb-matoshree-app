package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Invoice
import com.example.data.model.Machine
import com.example.data.model.Operator
import com.example.data.model.Payment
import com.example.data.model.WorkRecord
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager(private val context: Context) {

    private val TAG = "FirestoreSyncManager"

    // Initialized with the custom database ID from firebase_applet_config.xml
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private fun currentUserId(): String? = Firebase.auth.currentUser?.uid

    suspend fun syncBusinessSettings(settings: BusinessSettings) {
        val uid = currentUserId() ?: return
        try {
            val map = mapOf(
                "userId" to uid,
                "companyName" to settings.companyName,
                "tagline" to settings.tagline,
                "phone" to settings.phone,
                "email" to settings.email,
                "address" to settings.address,
                "gstin" to settings.gstin,
                "invoicePrefix" to settings.invoicePrefix,
                "defaultHourlyRate" to settings.defaultHourlyRate,
                "upiId" to settings.upiId,
                "bankName" to settings.bankName,
                "accountHolder" to settings.accountHolder,
                "accountNumber" to settings.accountNumber,
                "ifscCode" to settings.ifscCode,
                "paymentInstructions" to settings.paymentInstructions,
                "invoiceTerms" to settings.invoiceTerms,
                "taxEnabled" to settings.taxEnabled,
                "cgstRate" to settings.cgstRate,
                "sgstRate" to settings.sgstRate,
                "igstRate" to settings.igstRate
            )
            db.collection("businesses").document(uid).set(map, SetOptions.merge()).await()
            Log.d(TAG, "Business settings synced to Firestore for $uid")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync settings to Firestore: ${e.message}")
        }
    }

    suspend fun syncCustomer(customer: Customer) {
        val uid = currentUserId() ?: return
        try {
            val docId = "cust_${customer.id}"
            val map = mapOf(
                "userId" to uid,
                "name" to customer.name,
                "phone" to customer.phone,
                "address" to customer.address,
                "email" to customer.email,
                "gstin" to customer.gstin,
                "notes" to customer.notes,
                "isActive" to customer.isActive
            )
            db.collection("customers").document(docId).set(map, SetOptions.merge()).await()
            Log.d(TAG, "Customer ${customer.name} synced to Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync customer to Firestore: ${e.message}")
        }
    }

    suspend fun syncInvoice(invoice: Invoice) {
        val uid = currentUserId() ?: return
        try {
            val docId = "inv_${invoice.id}"
            val map = mapOf(
                "userId" to uid,
                "invoiceNumber" to invoice.invoiceNumber,
                "invoiceDate" to invoice.invoiceDate,
                "dueDate" to invoice.dueDate,
                "customerId" to invoice.customerId.toString(),
                "customerName" to invoice.customerName,
                "customerPhone" to invoice.customerPhone,
                "customerAddress" to invoice.customerAddress,
                "customerGstin" to invoice.customerGstin,
                "worksite" to invoice.worksite,
                "machineName" to invoice.machineName,
                "lineItemsJson" to invoice.lineItemsJson,
                "subtotal" to invoice.subtotal,
                "additionalCharges" to invoice.additionalCharges,
                "dieselChargesBilled" to invoice.dieselChargesBilled,
                "transportCharges" to invoice.transportCharges,
                "operatorCharges" to invoice.operatorCharges,
                "discount" to invoice.discount,
                "taxAmount" to invoice.taxAmount,
                "taxDetails" to invoice.taxDetails,
                "grandTotal" to invoice.grandTotal,
                "amountPaid" to invoice.amountPaid,
                "balanceDue" to invoice.balanceDue,
                "status" to invoice.status,
                "paymentTerms" to invoice.paymentTerms,
                "notes" to invoice.notes
            )
            db.collection("invoices").document(docId).set(map, SetOptions.merge()).await()
            Log.d(TAG, "Invoice ${invoice.invoiceNumber} synced to Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync invoice to Firestore: ${e.message}")
        }
    }

    suspend fun syncPayment(payment: Payment) {
        val uid = currentUserId() ?: return
        try {
            val docId = "pay_${payment.id}"
            val map = mapOf(
                "userId" to uid,
                "paymentNumber" to payment.paymentNumber,
                "customerId" to payment.customerId.toString(),
                "customerName" to payment.customerName,
                "invoiceId" to (payment.invoiceId?.toString() ?: ""),
                "invoiceNumber" to (payment.invoiceNumber ?: ""),
                "paymentDate" to payment.paymentDate,
                "amount" to payment.amount,
                "paymentMethod" to payment.paymentMethod,
                "transactionRef" to payment.transactionRef,
                "notes" to payment.notes,
                "recordedBy" to payment.recordedBy
            )
            db.collection("payments").document(docId).set(map, SetOptions.merge()).await()
            Log.d(TAG, "Payment ${payment.paymentNumber} synced to Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync payment to Firestore: ${e.message}")
        }
    }

    suspend fun syncExpense(expense: Expense) {
        val uid = currentUserId() ?: return
        try {
            val docId = "exp_${expense.id}"
            val map = mapOf(
                "userId" to uid,
                "expenseDate" to expense.expenseDate,
                "category" to expense.category,
                "amount" to expense.amount,
                "machineId" to (expense.machineId?.toString() ?: ""),
                "machineName" to (expense.machineName ?: ""),
                "supplier" to expense.supplier,
                "paymentMethod" to expense.paymentMethod,
                "referenceNo" to expense.referenceNo,
                "description" to expense.description
            )
            db.collection("expenses").document(docId).set(map, SetOptions.merge()).await()
            Log.d(TAG, "Expense ${expense.id} synced to Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync expense to Firestore: ${e.message}")
        }
    }
}
