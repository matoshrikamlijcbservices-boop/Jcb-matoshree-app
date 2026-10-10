package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BusinessSettings::class,
        Customer::class,
        Machine::class,
        Operator::class,
        WorkRecord::class,
        Invoice::class,
        Payment::class,
        Expense::class,
        AppUser::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "matoshree_kamalai_jcb_billing.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: AppDao) {
                // Initialize Company Business Settings with the mandatory name
                dao.saveSettings(
                    BusinessSettings(
                        id = 1L,
                        companyName = "Matoshree Kamalai Land Developers and Builders",
                        tagline = "Heavy Earthmoving Machinery & Civil Construction",
                        phone = "+91 98220 12345",
                        email = "info@matoshreekamalai.com",
                        address = "Plot No. 14, Ring Road, Near MIDC, Maharashtra, India",
                        gstin = "",
                        invoicePrefix = "MKLDB-2026-",
                        defaultHourlyRate = 1800.0,
                        upiId = "matoshreekamalai@okhdfcbank",
                        bankName = "HDFC Bank",
                        accountHolder = "Matoshree Kamalai Land Developers and Builders",
                        accountNumber = "50200067891234",
                        ifscCode = "HDFC0001234",
                        paymentInstructions = "Payment via UPI, NEFT or RTGS. Please quote invoice number in remarks.",
                        invoiceTerms = "1. Minimum billing duration: 1 hour.\n2. Machine working hours calculated from meter start to finish.\n3. Fuel/Diesel provided by contractor unless negotiated otherwise.\n4. Payment due within 7 days of invoice issue.",
                        taxEnabled = false
                    )
                )

                // Initialize Default Admin User
                dao.insertUser(
                    AppUser(
                        username = "admin",
                        fullName = "Authorized Manager",
                        role = "OWNER",
                        email = "admin@matoshreekamalai.com",
                        phone = "+91 98220 12345",
                        pin = "1234",
                        isActive = true
                    )
                )

                // Initialize Standard Earthmoving Machines
                val m1Id = dao.insertMachine(
                    Machine(
                        machineCode = "JCB-01",
                        name = "JCB 3DX Super Backhoe",
                        regNumber = "MH-12-PQ-5580",
                        makeModel = "JCB 3DX Super 4WD",
                        machineType = "Backhoe Loader",
                        defaultHourlyRate = 1800.0,
                        currentMeterReading = 2450.5,
                        status = "ACTIVE",
                        notes = "Heavy excavator bucket & loader shovel"
                    )
                )

                val m2Id = dao.insertMachine(
                    Machine(
                        machineCode = "JCB-02",
                        name = "JCB JS205 Tracked Excavator",
                        regNumber = "MH-12-TR-9901",
                        makeModel = "JCB JS205 Heavy Excavator",
                        machineType = "Excavator",
                        defaultHourlyRate = 2600.0,
                        currentMeterReading = 1820.0,
                        status = "ACTIVE",
                        notes = "20-ton tracked excavator for hard rock & foundation digging"
                    )
                )

                // Initialize Operators
                val op1Id = dao.insertOperator(
                    Operator(
                        name = "Ramesh Pawar",
                        phone = "+91 97654 32101",
                        address = "Hadapsar, Pune",
                        wageType = "DAILY",
                        wageRate = 900.0,
                        status = "ACTIVE",
                        notes = "Senior backhoe operator, 8 years experience"
                    )
                )

                val op2Id = dao.insertOperator(
                    Operator(
                        name = "Suresh Jadhav",
                        phone = "+91 98812 34567",
                        address = "Saswad, Pune",
                        wageType = "MONTHLY",
                        wageRate = 22000.0,
                        status = "ACTIVE",
                        notes = "Heavy excavator specialist"
                    )
                )

                // Initialize Sample Customer
                val c1Id = dao.insertCustomer(
                    Customer(
                        name = "Aditya Infra Projects",
                        phone = "+91 94220 11223",
                        address = "Sector 5, Kharadi Industrial Zone, Pune",
                        email = "contact@adityainfra.com",
                        gstin = "27AABCU9603R1ZM",
                        notes = "Regular commercial foundation client",
                        isActive = true
                    )
                )

                val c2Id = dao.insertCustomer(
                    Customer(
                        name = "Kishor Patil (Farmer)",
                        phone = "+91 98500 88776",
                        address = "Village Uruli Kanchan, Taluka Haveli",
                        email = "",
                        gstin = "",
                        notes = "Farm pond digging and land leveling",
                        isActive = true
                    )
                )

                // Sample Work Record
                dao.insertWorkRecord(
                    WorkRecord(
                        workDate = "2026-10-05",
                        customerId = c1Id,
                        customerName = "Aditya Infra Projects",
                        machineId = m1Id,
                        machineName = "JCB 3DX Super Backhoe",
                        worksite = "Kharadi Site Phase 2",
                        description = "Basement excavation & debris clearance",
                        openingMeter = 2442.0,
                        closingMeter = 2450.5,
                        manualHours = 8.5,
                        nonBillableHours = 0.5,
                        billableHours = 8.0,
                        hourlyRate = 1800.0,
                        operatorId = op1Id,
                        operatorName = "Ramesh Pawar",
                        invoiceId = null,
                        notes = "Completed smoothly on schedule."
                    )
                )

                // Sample Expense
                dao.insertExpense(
                    Expense(
                        expenseDate = "2026-10-04",
                        category = "DIESEL",
                        amount = 4500.0,
                        machineId = m1Id,
                        machineName = "JCB 3DX Super Backhoe",
                        supplier = "Bharat Petroleum, Uruli Kanchan",
                        paymentMethod = "UPI",
                        referenceNo = "UPI-REF-992817",
                        description = "50 Liters diesel refilled for site work"
                    )
                )
            }
        }
    }
}
