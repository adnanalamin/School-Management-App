package com.example.features.fees.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Fee category classifications supported by the institution billing system.
 */
enum class FeeCategory(val displayName: String, val code: String) {
    TUITION("Tuition Fee", "TUI"),
    LABORATORY("Laboratory & STEM", "LAB"),
    LIBRARY("Library & Digital Resources", "LIB"),
    TRANSPORT("Bus Transport", "TRN"),
    SPORTS("Sports & Athletics", "SPT"),
    DEVELOPMENT("Campus Development", "DEV"),
    EXAMINATION("Term Examination", "EXM");

    companion object {
        fun fromString(value: String?): FeeCategory {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) || it.code.equals(value, ignoreCase = true)
            } ?: TUITION
        }
    }
}

/**
 * Current lifecycle status of an invoice.
 */
enum class InvoiceStatus(val displayName: String) {
    PAID("Paid"),
    PARTIAL("Partially Paid"),
    PENDING("Pending Payment"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String?): InvoiceStatus {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            } ?: PENDING
        }
    }
}

/**
 * Supported payment channels for fee collections.
 */
enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    BANK_TRANSFER("Bank Wire / ACH"),
    CARD("Credit / Debit Card"),
    MOBILE_PAY("Mobile Payment"),
    CHEQUE("Bank Cheque");

    companion object {
        fun fromString(value: String?): PaymentMethod {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            } ?: CASH
        }
    }
}

/**
 * Itemized fee component within an invoice.
 */
data class FeeItem(
    val id: String,
    val category: FeeCategory,
    val description: String,
    val amount: Double
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "category" to category.name,
        "description" to description,
        "amount" to amount
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any>): FeeItem {
            return FeeItem(
                id = data["id"] as? String ?: "",
                category = FeeCategory.fromString(data["category"] as? String),
                description = data["description"] as? String ?: "",
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}

/**
 * Recorded payment transaction against an invoice.
 */
data class PaymentTransaction(
    val transactionId: String,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val receiptNumber: String,
    val recordedById: String,
    val recordedByName: String,
    val notes: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "transactionId" to transactionId,
        "amount" to amount,
        "paymentDate" to paymentDate,
        "paymentMethod" to paymentMethod.name,
        "receiptNumber" to receiptNumber,
        "recordedById" to recordedById,
        "recordedByName" to recordedByName,
        "notes" to notes
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any>): PaymentTransaction {
            return PaymentTransaction(
                transactionId = data["transactionId"] as? String ?: "",
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                paymentDate = (data["paymentDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                paymentMethod = PaymentMethod.fromString(data["paymentMethod"] as? String),
                receiptNumber = data["receiptNumber"] as? String ?: "",
                recordedById = data["recordedById"] as? String ?: "",
                recordedByName = data["recordedByName"] as? String ?: "",
                notes = data["notes"] as? String ?: ""
            )
        }
    }
}

/**
 * FeeInvoice represents an institutional invoice billed to a student.
 * Scoped to `schools/{schoolId}/fees/{feeId}`.
 */
data class FeeInvoice(
    override val id: String,
    override val schoolId: String,
    val invoiceNumber: String,
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val gradeLevel: String,
    val section: String,
    val academicTerm: String,
    val dueDate: String, // Format: YYYY-MM-DD
    val items: List<FeeItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val fineAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val status: InvoiceStatus = InvoiceStatus.PENDING,
    val transactions: List<PaymentTransaction> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "invoiceNumber" to invoiceNumber,
        "studentId" to studentId,
        "studentName" to studentName,
        "rollNumber" to rollNumber,
        "gradeLevel" to gradeLevel,
        "section" to section,
        "academicTerm" to academicTerm,
        "dueDate" to dueDate,
        "items" to items.map { it.toFirestoreMap() },
        "totalAmount" to totalAmount,
        "discountAmount" to discountAmount,
        "fineAmount" to fineAmount,
        "paidAmount" to paidAmount,
        "dueAmount" to dueAmount,
        "status" to status.name,
        "transactions" to transactions.map { it.toFirestoreMap() },
        "createdAt" to createdAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): FeeInvoice {
            val itemsRaw = data["items"] as? List<Map<String, Any>> ?: emptyList()
            val parsedItems = itemsRaw.map { FeeItem.fromFirestoreMap(it) }

            val transRaw = data["transactions"] as? List<Map<String, Any>> ?: emptyList()
            val parsedTransactions = transRaw.map { PaymentTransaction.fromFirestoreMap(it) }

            return FeeInvoice(
                id = id,
                schoolId = schoolId,
                invoiceNumber = data["invoiceNumber"] as? String ?: "",
                studentId = data["studentId"] as? String ?: "",
                studentName = data["studentName"] as? String ?: "",
                rollNumber = data["rollNumber"] as? String ?: "",
                gradeLevel = data["gradeLevel"] as? String ?: "",
                section = data["section"] as? String ?: "",
                academicTerm = data["academicTerm"] as? String ?: "",
                dueDate = data["dueDate"] as? String ?: "",
                items = parsedItems,
                totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                discountAmount = (data["discountAmount"] as? Number)?.toDouble() ?: 0.0,
                fineAmount = (data["fineAmount"] as? Number)?.toDouble() ?: 0.0,
                paidAmount = (data["paidAmount"] as? Number)?.toDouble() ?: 0.0,
                dueAmount = (data["dueAmount"] as? Number)?.toDouble() ?: 0.0,
                status = InvoiceStatus.fromString(data["status"] as? String),
                transactions = parsedTransactions,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }

        fun createInvoice(
            id: String,
            schoolId: String,
            invoiceNumber: String,
            studentId: String,
            studentName: String,
            rollNumber: String,
            gradeLevel: String,
            section: String,
            academicTerm: String,
            dueDate: String,
            items: List<FeeItem>,
            discountAmount: Double = 0.0,
            fineAmount: Double = 0.0,
            paidAmount: Double = 0.0,
            transactions: List<PaymentTransaction> = emptyList(),
            createdAt: Long = System.currentTimeMillis()
        ): FeeInvoice {
            val grossAmount = items.sumOf { it.amount }
            val totalAmount = (grossAmount - discountAmount + fineAmount).coerceAtLeast(0.0)
            val computedDue = (totalAmount - paidAmount).coerceAtLeast(0.0)

            val status = when {
                computedDue <= 0.0 -> InvoiceStatus.PAID
                paidAmount > 0.0 -> InvoiceStatus.PARTIAL
                else -> InvoiceStatus.PENDING
            }

            return FeeInvoice(
                id = id,
                schoolId = schoolId,
                invoiceNumber = invoiceNumber,
                studentId = studentId,
                studentName = studentName,
                rollNumber = rollNumber,
                gradeLevel = gradeLevel,
                section = section,
                academicTerm = academicTerm,
                dueDate = dueDate,
                items = items,
                totalAmount = totalAmount,
                discountAmount = discountAmount,
                fineAmount = fineAmount,
                paidAmount = paidAmount,
                dueAmount = computedDue,
                status = status,
                transactions = transactions,
                createdAt = createdAt
            )
        }
    }
}
