package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "business_settings")
data class BusinessSettings(
    @PrimaryKey val id: Long = 1L,
    val companyName: String = "Matoshree Kamalai Land Developers and Builders",
    val tagline: String = "Heavy Earthmoving Machinery & Infrastructure Contractors",
    val phone: String = "+91 98220 00000",
    val email: String = "contact@matoshreekamalai.com",
    val address: String = "Plot No. 12, Industrial Area, Pune-Solapur Highway, Maharashtra, India",
    val gstin: String = "",
    val invoicePrefix: String = "MKLDB-",
    val defaultHourlyRate: Double = 1800.0,
    val upiId: String = "matoshreekamalai@upi",
    val bankName: String = "State Bank of India",
    val accountHolder: String = "Matoshree Kamalai Land Developers and Builders",
    val accountNumber: String = "39281029384",
    val ifscCode: String = "SBIN0001234",
    val paymentInstructions: String = "Please pay via UPI or Direct NEFT/RTGS Transfer. Share payment screenshot upon completion.",
    val invoiceTerms: String = "1. Minimum billing 1 hour.\n2. Machine diesel provided by contractor unless specified.\n3. Operator food and accommodation by client for remote sites.\n4. Payment due within 7 days of invoice generation.",
    val taxEnabled: Boolean = false,
    val cgstRate: Double = 9.0,
    val sgstRate: Double = 9.0,
    val igstRate: Double = 18.0
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String,
    val address: String = "",
    val email: String = "",
    val gstin: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "machines")
data class Machine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val machineCode: String, // e.g. "JCB-01"
    val name: String, // e.g. "JCB 3DX Super"
    val regNumber: String = "", // e.g. "MH-12-FC-4482"
    val makeModel: String = "JCB 3DX EcoXcellence",
    val machineType: String = "Backhoe Loader", // Backhoe Loader, Excavator, Roller, Breaker
    val defaultHourlyRate: Double = 1800.0,
    val currentMeterReading: Double = 0.0,
    val status: String = "ACTIVE", // ACTIVE, MAINTENANCE, INACTIVE
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "operators")
data class Operator(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String,
    val address: String = "",
    val wageType: String = "DAILY", // HOURLY, DAILY, MONTHLY
    val wageRate: Double = 800.0,
    val status: String = "ACTIVE", // ACTIVE, INACTIVE
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "work_records")
data class WorkRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val workDate: String, // YYYY-MM-DD or DD/MM/YYYY
    val customerId: Long,
    val customerName: String,
    val machineId: Long,
    val machineName: String,
    val worksite: String,
    val description: String,
    val openingMeter: Double,
    val closingMeter: Double,
    val manualHours: Double = 0.0,
    val nonBillableHours: Double = 0.0,
    val billableHours: Double,
    val hourlyRate: Double,
    val operatorId: Long? = null,
    val operatorName: String? = null,
    val invoiceId: Long? = null, // null if unbilled
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class InvoiceLineItem(
    val description: String,
    val workDate: String,
    val openingMeter: Double = 0.0,
    val closingMeter: Double = 0.0,
    val hours: Double,
    val hourlyRate: Double,
    val amount: Double
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("description", description)
            put("workDate", workDate)
            put("openingMeter", openingMeter)
            put("closingMeter", closingMeter)
            put("hours", hours)
            put("hourlyRate", hourlyRate)
            put("amount", amount)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): InvoiceLineItem {
            return InvoiceLineItem(
                description = obj.optString("description", ""),
                workDate = obj.optString("workDate", ""),
                openingMeter = obj.optDouble("openingMeter", 0.0),
                closingMeter = obj.optDouble("closingMeter", 0.0),
                hours = obj.optDouble("hours", 0.0),
                hourlyRate = obj.optDouble("hourlyRate", 0.0),
                amount = obj.optDouble("amount", 0.0)
            )
        }

        fun listToJson(items: List<InvoiceLineItem>): String {
            val array = JSONArray()
            items.forEach { array.put(it.toJsonObject()) }
            return array.toString()
        }

        fun listFromJson(json: String?): List<InvoiceLineItem> {
            if (json.isNullOrBlank()) return emptyList()
            val list = mutableListOf<InvoiceLineItem>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    list.add(fromJsonObject(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                // Return empty list on parse error
            }
            return list
        }
    }
}

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceNumber: String, // e.g. "MKLDB-2026-001"
    val invoiceDate: String,
    val dueDate: String = "",
    val customerId: Long,
    val customerName: String,
    val customerPhone: String = "",
    val customerAddress: String = "",
    val customerGstin: String = "",
    val worksite: String = "",
    val machineName: String = "",
    val lineItemsJson: String = "[]",
    val subtotal: Double,
    val additionalCharges: Double = 0.0,
    val dieselChargesBilled: Double = 0.0,
    val transportCharges: Double = 0.0,
    val operatorCharges: Double = 0.0,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val taxDetails: String = "",
    val grandTotal: Double,
    val amountPaid: Double = 0.0,
    val balanceDue: Double,
    val status: String = "UNPAID", // UNPAID, PARTIALLY_PAID, PAID, VOID
    val paymentTerms: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val paymentNumber: String, // e.g. "REC-2026-001"
    val customerId: Long,
    val customerName: String,
    val invoiceId: Long? = null,
    val invoiceNumber: String? = null,
    val paymentDate: String,
    val amount: Double,
    val paymentMethod: String = "CASH", // CASH, UPI, BANK_TRANSFER, CHEQUE
    val transactionRef: String = "",
    val notes: String = "",
    val recordedBy: String = "Administrator",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val expenseDate: String,
    val category: String, // DIESEL, OPERATOR_WAGES, MAINTENANCE, SPARE_PARTS, TRANSPORT, INSURANCE, PERMITS, OTHER
    val amount: Double,
    val machineId: Long? = null,
    val machineName: String? = null,
    val supplier: String = "",
    val paymentMethod: String = "CASH",
    val referenceNo: String = "",
    val description: String,
    val receiptUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_users")
data class AppUser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val username: String,
    val fullName: String,
    val role: String = "OWNER", // OWNER, STAFF
    val email: String = "",
    val phone: String = "",
    val pin: String = "1234",
    val isActive: Boolean = true
)
