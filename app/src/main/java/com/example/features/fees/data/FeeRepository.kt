package com.example.features.fees.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.fees.domain.FeeCategory
import com.example.features.fees.domain.FeeInvoice
import com.example.features.fees.domain.FeeItem
import com.example.features.fees.domain.InvoiceStatus
import com.example.features.fees.domain.PaymentMethod
import com.example.features.fees.domain.PaymentTransaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FeeRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FeeRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getInvoices(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        status: InvoiceStatus? = null,
        studentId: String? = null,
        gradeLevel: String? = null
    ): List<FeeInvoice> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.feesPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededInvoices(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    FeeInvoice.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterInvoices(list, status, studentId, gradeLevel)
        } catch (e: Exception) {
            Log.w("FeeRepository", "Error reading invoices from Firestore, falling back to preseeded data", e)
            val seeded = getPreSeededInvoices(schoolId)
            filterInvoices(seeded, status, studentId, gradeLevel)
        }
    }

    private fun filterInvoices(
        invoices: List<FeeInvoice>,
        status: InvoiceStatus?,
        studentId: String?,
        gradeLevel: String?
    ): List<FeeInvoice> {
        return invoices.filter { inv ->
            (status == null || inv.status == status) &&
            (studentId == null || inv.studentId == studentId) &&
            (gradeLevel == null || inv.gradeLevel.contains(gradeLevel, ignoreCase = true))
        }
    }

    suspend fun getInvoiceById(schoolId: String, invoiceId: String): FeeInvoice? {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.feesPath(schoolId)
            val doc = db.collection(path).document(invoiceId).get().await()
            if (doc.exists() && doc.data != null) {
                FeeInvoice.fromFirestoreMap(doc.id, schoolId, doc.data!!)
            } else {
                getPreSeededInvoices(schoolId).firstOrNull { it.id == invoiceId }
            }
        } catch (e: Exception) {
            getPreSeededInvoices(schoolId).firstOrNull { it.id == invoiceId }
        }
    }

    suspend fun saveInvoice(invoice: FeeInvoice): Result<FeeInvoice> {
        return try {
            TenantPathProvider.verifyTenantBoundary(invoice.schoolId)
            val path = TenantPathProvider.feesPath(invoice.schoolId)
            db.collection(path).document(invoice.id).set(invoice.toFirestoreMap()).await()
            Result.success(invoice)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun recordPayment(
        schoolId: String,
        invoiceId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        notes: String,
        actorId: String,
        actorName: String
    ): Result<FeeInvoice> {
        return try {
            val existing = getInvoiceById(schoolId, invoiceId)
                ?: return Result.failure(Exception("Invoice '$invoiceId' not found"))

            val receiptNum = generateReceiptNumber(schoolId)
            val transaction = PaymentTransaction(
                transactionId = "tx_${UUID.randomUUID().toString().take(8)}",
                amount = amount,
                paymentDate = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                receiptNumber = receiptNum,
                recordedById = actorId,
                recordedByName = actorName,
                notes = notes
            )

            val updatedPaid = existing.paidAmount + amount
            val updatedDue = (existing.totalAmount - updatedPaid).coerceAtLeast(0.0)
            val updatedStatus = if (updatedDue <= 0.0) InvoiceStatus.PAID else InvoiceStatus.PARTIAL

            val updatedInvoice = existing.copy(
                paidAmount = updatedPaid,
                dueAmount = updatedDue,
                status = updatedStatus,
                transactions = existing.transactions + transaction
            )

            saveInvoice(updatedInvoice)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun generateInvoiceNumber(schoolId: String): String {
        val cleanPrefix = schoolId.replace("sch_", "").take(3).uppercase()
        val randomSeq = (1000..9999).random()
        return "INV-$cleanPrefix-2026-$randomSeq"
    }

    fun generateReceiptNumber(schoolId: String): String {
        val cleanPrefix = schoolId.replace("sch_", "").take(3).uppercase()
        val randomSeq = (10000..99999).random()
        return "RCP-$cleanPrefix-$randomSeq"
    }

    fun getPreSeededInvoices(schoolId: String): List<FeeInvoice> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                FeeInvoice.createInvoice(
                    id = "inv_oxf_01",
                    schoolId = schoolId,
                    invoiceNumber = "INV-OXF-2026-1001",
                    studentId = "stu_oxf_01",
                    studentName = "Amina Rahman",
                    rollNumber = "OXF-10A-001",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    academicTerm = "Term 1 (Fall 2026)",
                    dueDate = "2026-03-15",
                    items = listOf(
                        FeeItem("fi_01", FeeCategory.TUITION, "Grade 10 Core Tuition", 1200.0),
                        FeeItem("fi_02", FeeCategory.LABORATORY, "Science Lab Consumables", 200.0),
                        FeeItem("fi_03", FeeCategory.LIBRARY, "Digital Library Access", 100.0)
                    ),
                    discountAmount = 0.0,
                    fineAmount = 0.0,
                    paidAmount = 1500.0,
                    transactions = listOf(
                        PaymentTransaction(
                            transactionId = "tx_oxf_101",
                            amount = 1500.0,
                            paymentDate = System.currentTimeMillis() - 864000000L,
                            paymentMethod = PaymentMethod.BANK_TRANSFER,
                            receiptNumber = "RCP-OXF-8801",
                            recordedById = "usr_accountant",
                            recordedByName = "Robert Vance",
                            notes = "Full term wire settlement received"
                        )
                    )
                ),
                FeeInvoice.createInvoice(
                    id = "inv_oxf_02",
                    schoolId = schoolId,
                    invoiceNumber = "INV-OXF-2026-1002",
                    studentId = "stu_oxf_02",
                    studentName = "Oliver Smith",
                    rollNumber = "OXF-10A-002",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    academicTerm = "Term 1 (Fall 2026)",
                    dueDate = "2026-03-30",
                    items = listOf(
                        FeeItem("fi_04", FeeCategory.TUITION, "Grade 10 Core Tuition", 1200.0),
                        FeeItem("fi_05", FeeCategory.TRANSPORT, "North Bus Route 4", 300.0),
                        FeeItem("fi_06", FeeCategory.SPORTS, "Athletics Facility Pass", 100.0)
                    ),
                    discountAmount = 0.0,
                    fineAmount = 0.0,
                    paidAmount = 800.0,
                    transactions = listOf(
                        PaymentTransaction(
                            transactionId = "tx_oxf_102",
                            amount = 800.0,
                            paymentDate = System.currentTimeMillis() - 432000000L,
                            paymentMethod = PaymentMethod.CARD,
                            receiptNumber = "RCP-OXF-8802",
                            recordedById = "usr_accountant",
                            recordedByName = "Robert Vance",
                            notes = "Installment 1 of 2 settled"
                        )
                    )
                ),
                FeeInvoice(
                    id = "inv_oxf_03",
                    schoolId = schoolId,
                    invoiceNumber = "INV-OXF-2026-1003",
                    studentId = "stu_oxf_04",
                    studentName = "Lucas Vance",
                    rollNumber = "OXF-09B-004",
                    gradeLevel = "Grade 9",
                    section = "Section B",
                    academicTerm = "Term 1 (Fall 2026)",
                    dueDate = "2026-03-01",
                    items = listOf(
                        FeeItem("fi_07", FeeCategory.TUITION, "Grade 9 Core Tuition", 1150.0),
                        FeeItem("fi_08", FeeCategory.TRANSPORT, "West Route 2", 300.0)
                    ),
                    totalAmount = 1450.0,
                    discountAmount = 0.0,
                    fineAmount = 50.0,
                    paidAmount = 0.0,
                    dueAmount = 1500.0,
                    status = InvoiceStatus.OVERDUE,
                    transactions = emptyList()
                ),
                FeeInvoice.createInvoice(
                    id = "inv_oxf_04",
                    schoolId = schoolId,
                    invoiceNumber = "INV-OXF-2026-1004",
                    studentId = "stu_oxf_03",
                    studentName = "Sophia Chen",
                    rollNumber = "OXF-11S-003",
                    gradeLevel = "Grade 11",
                    section = "Science",
                    academicTerm = "Term 1 (Fall 2026)",
                    dueDate = "2026-04-15",
                    items = listOf(
                        FeeItem("fi_09", FeeCategory.TUITION, "Grade 11 Honors Tuition", 1400.0),
                        FeeItem("fi_10", FeeCategory.LABORATORY, "Advanced Chemistry Lab", 300.0),
                        FeeItem("fi_11", FeeCategory.DEVELOPMENT, "STEM Lab Endowment", 100.0)
                    ),
                    discountAmount = 100.0, // Academic scholarship discount
                    fineAmount = 0.0,
                    paidAmount = 0.0,
                    transactions = emptyList()
                ),
                FeeInvoice.createInvoice(
                    id = "inv_oxf_05",
                    schoolId = schoolId,
                    invoiceNumber = "INV-OXF-2026-1005",
                    studentId = "stu_oxf_05",
                    studentName = "Maya Patel",
                    rollNumber = "OXF-12A-005",
                    gradeLevel = "Grade 12",
                    section = "Section A",
                    academicTerm = "Term 1 (Fall 2026)",
                    dueDate = "2026-03-10",
                    items = listOf(
                        FeeItem("fi_12", FeeCategory.TUITION, "Grade 12 Advanced Tuition", 1450.0),
                        FeeItem("fi_13", FeeCategory.EXAMINATION, "Board Exam Registration Fee", 300.0)
                    ),
                    discountAmount = 0.0,
                    fineAmount = 0.0,
                    paidAmount = 1750.0,
                    transactions = listOf(
                        PaymentTransaction(
                            transactionId = "tx_oxf_103",
                            amount = 1750.0,
                            paymentDate = System.currentTimeMillis() - 1200000000L,
                            paymentMethod = PaymentMethod.CASH,
                            receiptNumber = "RCP-OXF-8803",
                            recordedById = "usr_accountant",
                            recordedByName = "Robert Vance",
                            notes = "Full settlement cash counter deposit"
                        )
                    )
                )
            )
        } else {
            listOf(
                FeeInvoice.createInvoice(
                    id = "inv_cam_01",
                    schoolId = schoolId,
                    invoiceNumber = "INV-CAM-2026-2001",
                    studentId = "stu_cam_01",
                    studentName = "Liam Davies",
                    rollNumber = "CAM-10C-101",
                    gradeLevel = "Year 10",
                    section = "Section A",
                    academicTerm = "Michaelmas Term 2026",
                    dueDate = "2026-03-20",
                    items = listOf(
                        FeeItem("fi_c01", FeeCategory.TUITION, "Year 10 GCSE Tuition", 1000.0),
                        FeeItem("fi_c02", FeeCategory.LIBRARY, "Cambridge Library Consortium", 200.0)
                    ),
                    paidAmount = 1200.0,
                    transactions = listOf(
                        PaymentTransaction(
                            transactionId = "tx_cam_201",
                            amount = 1200.0,
                            paymentDate = System.currentTimeMillis() - 700000000L,
                            paymentMethod = PaymentMethod.BANK_TRANSFER,
                            receiptNumber = "RCP-CAM-9001",
                            recordedById = "usr_bursar",
                            recordedByName = "Bursar Department",
                            notes = "Wire transfer verified"
                        )
                    )
                ),
                FeeInvoice.createInvoice(
                    id = "inv_cam_02",
                    schoolId = schoolId,
                    invoiceNumber = "INV-CAM-2026-2002",
                    studentId = "stu_cam_02",
                    studentName = "Emma Watson",
                    rollNumber = "CAM-11A-102",
                    gradeLevel = "Year 11",
                    section = "Section B",
                    academicTerm = "Michaelmas Term 2026",
                    dueDate = "2026-03-31",
                    items = listOf(
                        FeeItem("fi_c03", FeeCategory.TUITION, "Year 11 GCSE Tuition", 1100.0),
                        FeeItem("fi_c04", FeeCategory.SPORTS, "Rowing & Tennis Pass", 250.0)
                    ),
                    paidAmount = 500.0,
                    transactions = listOf(
                        PaymentTransaction(
                            transactionId = "tx_cam_202",
                            amount = 500.0,
                            paymentDate = System.currentTimeMillis() - 300000000L,
                            paymentMethod = PaymentMethod.CARD,
                            receiptNumber = "RCP-CAM-9002",
                            recordedById = "usr_bursar",
                            recordedByName = "Bursar Department",
                            notes = "Debit card payment partial"
                        )
                    )
                ),
                FeeInvoice.createInvoice(
                    id = "inv_cam_03",
                    schoolId = schoolId,
                    invoiceNumber = "INV-CAM-2026-2003",
                    studentId = "stu_cam_03",
                    studentName = "Noah Wilson",
                    rollNumber = "CAM-12S-103",
                    gradeLevel = "Year 12 (Sixth Form)",
                    section = "Science",
                    academicTerm = "Michaelmas Term 2026",
                    dueDate = "2026-04-10",
                    items = listOf(
                        FeeItem("fi_c05", FeeCategory.TUITION, "A-Level Natural Sciences Tuition", 1300.0),
                        FeeItem("fi_c06", FeeCategory.LABORATORY, "Cavendish Laboratory Pass", 200.0)
                    ),
                    paidAmount = 0.0
                )
            )
        }
    }
}
