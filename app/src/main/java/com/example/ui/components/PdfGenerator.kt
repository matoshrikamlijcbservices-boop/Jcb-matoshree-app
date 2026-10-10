package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.BusinessSettings
import com.example.data.model.Invoice
import com.example.data.model.InvoiceLineItem
import com.example.data.model.Payment
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfGenerator {

    /**
     * Generates a high-quality A4 PDF for an Invoice
     */
    fun generateInvoicePdf(
        context: Context,
        invoice: Invoice,
        settings: BusinessSettings
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Navy Header Banner
        paint.color = Color.rgb(15, 23, 42) // Dark Navy #0F172A
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Accent Yellow Construction Stripe
        paint.color = Color.rgb(245, 158, 11) // Construction Yellow #F59E0B
        canvas.drawRect(0f, 90f, 595f, 96f, paint)

        // Company Name in Header
        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.isFakeBoldText = true
        canvas.drawText("Matoshree Kamalai Land Developers and Builders", 24f, 38f, paint)

        // Tagline & Contact in Header
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawText(settings.tagline, 24f, 54f, paint)
        canvas.drawText("Phone: ${settings.phone}  |  Email: ${settings.email}", 24f, 70f, paint)
        canvas.drawText("Address: ${settings.address}", 24f, 82f, paint)

        // INVOICE Title on right side
        paint.color = Color.rgb(245, 158, 11)
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("TAX INVOICE", 440f, 42f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("No: ${invoice.invoiceNumber}", 440f, 60f, paint)
        canvas.drawText("Date: ${invoice.invoiceDate}", 440f, 74f, paint)

        // Customer & Site Info Box
        var y = 120f
        paint.color = Color.rgb(241, 245, 249) // Light grey card
        canvas.drawRoundRect(RectF(24f, y, 571f, y + 70f), 6f, 6f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10.5f
        paint.isFakeBoldText = true
        canvas.drawText("BILLED TO / CUSTOMER DETAILS", 36f, y + 20f, paint)
        canvas.drawText("SITE & MACHINERY", 320f, y + 20f, paint)

        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawText("Client: ${invoice.customerName}", 36f, y + 36f, paint)
        canvas.drawText("Phone: ${invoice.customerPhone}", 36f, y + 49f, paint)
        if (invoice.customerAddress.isNotBlank()) {
            canvas.drawText("Address: ${invoice.customerAddress.take(40)}", 36f, y + 62f, paint)
        }

        canvas.drawText("Worksite: ${invoice.worksite.ifEmpty { "Client Location" }}", 320f, y + 36f, paint)
        canvas.drawText("Machine: ${invoice.machineName.ifEmpty { "JCB Earthmover" }}", 320f, y + 49f, paint)
        if (invoice.dueDate.isNotBlank()) {
            canvas.drawText("Due Date: ${invoice.dueDate}", 320f, y + 62f, paint)
        }

        // Table Header
        y += 88f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(RectF(24f, y, 571f, y + 24f), 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("Description of Work", 36f, y + 16f, paint)
        canvas.drawText("Date", 230f, y + 16f, paint)
        canvas.drawText("Mtr Start", 300f, y + 16f, paint)
        canvas.drawText("Mtr End", 360f, y + 16f, paint)
        canvas.drawText("Hours", 420f, y + 16f, paint)
        canvas.drawText("Rate (₹)", 470f, y + 16f, paint)
        canvas.drawText("Amount (₹)", 515f, y + 16f, paint)

        // Line Items
        y += 26f
        val lineItems = InvoiceLineItem.listFromJson(invoice.lineItemsJson)
        paint.isFakeBoldText = false
        paint.color = Color.rgb(30, 41, 59)

        if (lineItems.isEmpty()) {
            canvas.drawText(invoice.worksite.ifEmpty { "Earthmoving & JCB excavation services" }, 36f, y + 16f, paint)
            canvas.drawText(invoice.invoiceDate, 230f, y + 16f, paint)
            canvas.drawText("1.0", 420f, y + 16f, paint)
            canvas.drawText(String.format(Locale.ENGLISH, "%.2f", invoice.subtotal), 470f, y + 16f, paint)
            canvas.drawText(String.format(Locale.ENGLISH, "%.2f", invoice.subtotal), 515f, y + 16f, paint)
            y += 24f
        } else {
            for (item in lineItems) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(24f, y, 571f, y + 20f, paint)

                paint.color = Color.rgb(30, 41, 59)
                paint.textSize = 9f
                canvas.drawText(item.description.take(30), 36f, y + 14f, paint)
                canvas.drawText(item.workDate, 230f, y + 14f, paint)
                canvas.drawText(if (item.openingMeter > 0) String.format(Locale.ENGLISH, "%.1f", item.openingMeter) else "-", 300f, y + 14f, paint)
                canvas.drawText(if (item.closingMeter > 0) String.format(Locale.ENGLISH, "%.1f", item.closingMeter) else "-", 360f, y + 14f, paint)
                canvas.drawText(String.format(Locale.ENGLISH, "%.2f", item.hours), 420f, y + 14f, paint)
                canvas.drawText(String.format(Locale.ENGLISH, "%.2f", item.hourlyRate), 470f, y + 14f, paint)
                canvas.drawText(String.format(Locale.ENGLISH, "%.2f", item.amount), 515f, y + 14f, paint)

                y += 22f
            }
        }

        // Horizontal Separator
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(24f, y + 4f, 571f, y + 4f, paint)
        y += 14f

        // Financial Calculations Box (Right Column) & Payment Details (Left Column)
        val leftX = 24f
        val rightX = 350f

        // Left Column: Bank & Payment Instructions
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("BANK & PAYMENT DETAILS", leftX, y + 12f, paint)

        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(71, 85, 105)
        var bankY = y + 26f
        canvas.drawText("Bank: ${settings.bankName}", leftX, bankY, paint)
        bankY += 13f
        canvas.drawText("Account No: ${settings.accountNumber}", leftX, bankY, paint)
        bankY += 13f
        canvas.drawText("IFSC Code: ${settings.ifscCode}", leftX, bankY, paint)
        bankY += 13f
        canvas.drawText("UPI ID: ${settings.upiId}", leftX, bankY, paint)
        bankY += 15f
        canvas.drawText(settings.paymentInstructions.take(55), leftX, bankY, paint)

        // Right Column: Summary Table
        var calcY = y + 12f
        paint.textSize = 9.5f
        paint.color = Color.rgb(51, 65, 85)

        fun drawSummaryRow(label: String, value: Double, isBold: Boolean = false, isAccent: Boolean = false) {
            paint.isFakeBoldText = isBold
            paint.color = if (isAccent) Color.rgb(15, 23, 42) else Color.rgb(51, 65, 85)
            canvas.drawText(label, rightX, calcY, paint)
            canvas.drawText("₹ ${String.format(Locale.ENGLISH, "%,.2f", value)}", 500f, calcY, paint)
            calcY += 15f
        }

        drawSummaryRow("Subtotal:", invoice.subtotal)
        if (invoice.dieselChargesBilled > 0) drawSummaryRow("Diesel Billed:", invoice.dieselChargesBilled)
        if (invoice.transportCharges > 0) drawSummaryRow("Transport Charges:", invoice.transportCharges)
        if (invoice.operatorCharges > 0) drawSummaryRow("Operator Charges:", invoice.operatorCharges)
        if (invoice.additionalCharges > 0) drawSummaryRow("Other Charges:", invoice.additionalCharges)
        if (invoice.discount > 0) drawSummaryRow("Discount:", -invoice.discount)
        if (invoice.taxAmount > 0) drawSummaryRow("GST Tax:", invoice.taxAmount)

        // Grand Total Highlight
        calcY += 4f
        paint.color = Color.rgb(254, 243, 199) // Light amber box
        canvas.drawRoundRect(RectF(rightX - 6f, calcY - 12f, 571f, calcY + 16f), 4f, 4f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("Grand Total:", rightX, calcY + 6f, paint)
        canvas.drawText("₹ ${String.format(Locale.ENGLISH, "%,.2f", invoice.grandTotal)}", 490f, calcY + 6f, paint)

        calcY += 28f
        drawSummaryRow("Amount Paid:", invoice.amountPaid)
        drawSummaryRow("Balance Due:", invoice.balanceDue, isBold = true, isAccent = true)

        // Terms & Conditions Footer
        val footerY = 720f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawLine(24f, footerY, 571f, footerY, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("Terms & Conditions:", 24f, footerY + 14f, paint)

        paint.isFakeBoldText = false
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7.5f
        val terms = settings.invoiceTerms.split("\n")
        var tY = footerY + 26f
        for (term in terms.take(3)) {
            canvas.drawText(term, 24f, tY, paint)
            tY += 10f
        }

        // Authorized Signature
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("For Matoshree Kamalai Land Developers and Builders", 320f, footerY + 30f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Authorized Signatory", 400f, footerY + 68f, paint)

        document.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Invoice_${invoice.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()
            file
        } catch (e: Exception) {
            document.close()
            null
        }
    }

    /**
     * Generates a payment receipt PDF
     */
    fun generatePaymentReceiptPdf(
        context: Context,
        payment: Payment,
        settings: BusinessSettings
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 420, 1).create() // Half sheet / A5 voucher
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply { isAntiAlias = true }

        // Navy Header
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Yellow Stripe
        paint.color = Color.rgb(245, 158, 11)
        canvas.drawRect(0f, 75f, 595f, 80f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawText("Matoshree Kamalai Land Developers and Builders", 24f, 34f, paint)

        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawText("OFFICIAL PAYMENT RECEIPT  |  Phone: ${settings.phone}", 24f, 52f, paint)

        paint.color = Color.rgb(245, 158, 11)
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("RECEIPT", 480f, 36f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("No: ${payment.paymentNumber}", 460f, 52f, paint)
        canvas.drawText("Date: ${payment.paymentDate}", 460f, 65f, paint)

        // Body Content
        var y = 110f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(RectF(24f, y, 571f, y + 170f), 6f, 6f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("Received with thanks from:", 40f, y + 30f, paint)
        paint.textSize = 13f
        canvas.drawText(payment.customerName, 210f, y + 30f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("Amount Received:", 40f, y + 60f, paint)
        paint.isFakeBoldText = true
        paint.textSize = 14f
        paint.color = Color.rgb(16, 185, 129) // Green
        canvas.drawText("₹ ${String.format(Locale.ENGLISH, "%,.2f", payment.amount)}", 210f, y + 60f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Payment Mode: ${payment.paymentMethod}", 40f, y + 90f, paint)
        if (payment.transactionRef.isNotBlank()) {
            canvas.drawText("Transaction Ref: ${payment.transactionRef}", 280f, y + 90f, paint)
        }
        if (!payment.invoiceNumber.isNullOrBlank()) {
            canvas.drawText("Against Invoice: ${payment.invoiceNumber}", 40f, y + 115f, paint)
        }
        if (payment.notes.isNotBlank()) {
            canvas.drawText("Remarks: ${payment.notes}", 40f, y + 140f, paint)
        }

        // Footer signature
        y += 210f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("Recorded By: ${payment.recordedBy}", 40f, y, paint)
        canvas.drawText("For Matoshree Kamalai Land Developers and Builders", 310f, y, paint)
        paint.isFakeBoldText = false
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Authorized Signatory", 400f, y + 30f, paint)

        document.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Receipt_${payment.paymentNumber}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()
            file
        } catch (e: Exception) {
            document.close()
            null
        }
    }

    /**
     * Share or open the generated PDF
     */
    fun sharePdf(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share $title"))
    }
}
