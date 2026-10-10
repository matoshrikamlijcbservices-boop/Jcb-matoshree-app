package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BusinessSettings
import com.example.data.model.InvoiceLineItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.max

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify company name and app resources`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        val companyName = context.getString(R.string.company_name)

        assertEquals("Matoshree Kamalai", appName)
        assertEquals("Matoshree Kamalai Land Developers and Builders", companyName)

        val settings = BusinessSettings()
        assertEquals("Matoshree Kamalai Land Developers and Builders", settings.companyName)
    }

    @Test
    fun `verify jcb billing arithmetic and meter hours calculation`() {
        // Meter calculation
        val openingMeter = 1250.0
        val closingMeter = 1258.5
        val nonBillableHours = 0.5
        val actualHours = max(0.0, closingMeter - openingMeter)
        val billableHours = max(0.0, actualHours - nonBillableHours)

        assertEquals(8.5, actualHours, 0.001)
        assertEquals(8.0, billableHours, 0.001)

        val hourlyRate = 1800.0
        val lineAmount = billableHours * hourlyRate
        assertEquals(14400.0, lineAmount, 0.001)

        // Line item serialization
        val lineItem = InvoiceLineItem(
            description = "Land leveling & excavation",
            workDate = "09/10/2026",
            openingMeter = openingMeter,
            closingMeter = closingMeter,
            hours = billableHours,
            hourlyRate = hourlyRate,
            amount = lineAmount
        )
        val json = InvoiceLineItem.listToJson(listOf(lineItem))
        val parsed = InvoiceLineItem.listFromJson(json)
        assertEquals(1, parsed.size)
        assertEquals(lineAmount, parsed[0].amount, 0.001)
    }

    @Test
    fun `verify invoice tax discounts and balance due`() {
        val subtotal = 14400.0
        val dieselBilled = 2500.0
        val transportCharges = 1000.0
        val discount = 500.0
        val taxableAmount = subtotal + dieselBilled + transportCharges - discount
        assertEquals(17400.0, taxableAmount, 0.001)

        // 18% GST (9% CGST + 9% SGST)
        val tax = taxableAmount * 0.18
        assertEquals(3132.0, tax, 0.001)

        val grandTotal = taxableAmount + tax
        assertEquals(20532.0, grandTotal, 0.001)

        // Advance payment applied
        val advance = 10000.0
        val balanceDue = grandTotal - advance
        assertEquals(10532.0, balanceDue, 0.001)
        assertTrue(balanceDue > 0)
    }
}
