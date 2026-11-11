package com.example.features.fees.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.fees.data.FeeRepository
import com.example.features.fees.domain.FeeCategory
import com.example.features.fees.domain.FeeInvoice
import com.example.features.fees.domain.FeeItem
import com.example.features.fees.domain.InvoiceStatus
import com.example.features.fees.domain.PaymentMethod
import com.example.features.fees.domain.PaymentTransaction
import com.example.features.students.data.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class FeeUiState(
    val invoices: List<FeeInvoice> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: InvoiceStatus? = null,
    val selectedGradeFilter: String? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showCreateInvoiceDialog: Boolean = false,
    val showPaymentDialog: Boolean = false,
    val showReceiptDialog: Boolean = false,
    val selectedInvoiceForPayment: FeeInvoice? = null,
    val selectedInvoiceForReceipt: FeeInvoice? = null,
    val selectedTransactionForReceipt: PaymentTransaction? = null,
    val totalBilled: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val overdueCount: Int = 0,
    val currencySymbol: String = "$"
) {
    val filteredInvoices: List<FeeInvoice>
        get() = invoices.filter { inv ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                inv.invoiceNumber.lowercase().contains(q) ||
                inv.studentName.lowercase().contains(q) ||
                inv.rollNumber.lowercase().contains(q) ||
                inv.gradeLevel.lowercase().contains(q)
            }
            val matchesStatus = selectedStatusFilter == null || inv.status == selectedStatusFilter
            val matchesGrade = selectedGradeFilter == null || inv.gradeLevel.contains(selectedGradeFilter, ignoreCase = true)
            matchesSearch && matchesStatus && matchesGrade
        }
}

class FeeViewModel(
    private val feeRepository: FeeRepository = FeeRepository(),
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeeUiState())
    val uiState: StateFlow<FeeUiState> = _uiState.asStateFlow()

    init {
        // Synchronously initialize state with seeded data for immediate JVM testing
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val currency = if (schoolId.contains("cambridge") || schoolId.startsWith("sch_cam")) "£" else "$"
        val initialInvoices = feeRepository.getPreSeededInvoices(schoolId)
        val metrics = computeMetrics(initialInvoices)

        _uiState.value = FeeUiState(
            invoices = initialInvoices,
            totalBilled = metrics.totalBilled,
            totalCollected = metrics.totalCollected,
            totalOutstanding = metrics.totalOutstanding,
            overdueCount = metrics.overdueCount,
            currencySymbol = currency
        )

        loadInvoicesAsync()
    }

    fun loadInvoicesAsync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
                val currency = if (schoolId.contains("cambridge") || schoolId.startsWith("sch_cam")) "£" else "$"
                val list = feeRepository.getInvoices(schoolId)
                val metrics = computeMetrics(list)

                _uiState.update {
                    it.copy(
                        invoices = list,
                        totalBilled = metrics.totalBilled,
                        totalCollected = metrics.totalCollected,
                        totalOutstanding = metrics.totalOutstanding,
                        overdueCount = metrics.overdueCount,
                        currencySymbol = currency,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onStatusFilterChange(status: InvoiceStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun onGradeFilterChange(grade: String?) {
        _uiState.update { it.copy(selectedGradeFilter = grade) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedStatusFilter = null,
                selectedGradeFilter = null
            )
        }
    }

    fun openCreateInvoiceDialog() {
        if (!TenantContext.hasPermission(Permission.FEES_CREATE)) {
            _uiState.update { it.copy(errorMessage = "Permission denied: Creating invoices requires FEES_CREATE authorization.") }
            return
        }
        _uiState.update { it.copy(showCreateInvoiceDialog = true) }
    }

    fun closeCreateInvoiceDialog() {
        _uiState.update { it.copy(showCreateInvoiceDialog = false) }
    }

    fun openPaymentDialog(invoice: FeeInvoice) {
        if (!TenantContext.hasPermission(Permission.FEES_COLLECT)) {
            _uiState.update { it.copy(errorMessage = "Permission denied: Collecting payments requires FEES_COLLECT authorization.") }
            return
        }
        _uiState.update {
            it.copy(
                showPaymentDialog = true,
                selectedInvoiceForPayment = invoice
            )
        }
    }

    fun closePaymentDialog() {
        _uiState.update {
            it.copy(
                showPaymentDialog = false,
                selectedInvoiceForPayment = null
            )
        }
    }

    fun openReceiptDialog(invoice: FeeInvoice, transaction: PaymentTransaction? = null) {
        _uiState.update {
            it.copy(
                showReceiptDialog = true,
                selectedInvoiceForReceipt = invoice,
                selectedTransactionForReceipt = transaction ?: invoice.transactions.lastOrNull()
            )
        }
    }

    fun closeReceiptDialog() {
        _uiState.update {
            it.copy(
                showReceiptDialog = false,
                selectedInvoiceForReceipt = null,
                selectedTransactionForReceipt = null
            )
        }
    }

    fun createInvoice(
        studentId: String,
        studentName: String,
        rollNumber: String,
        gradeLevel: String,
        section: String,
        academicTerm: String,
        dueDate: String,
        tuitionFee: Double,
        labFee: Double = 0.0,
        libraryFee: Double = 0.0,
        transportFee: Double = 0.0,
        sportsFee: Double = 0.0,
        discountAmount: Double = 0.0
    ) {
        if (!TenantContext.hasPermission(Permission.FEES_CREATE)) {
            _uiState.update { it.copy(errorMessage = "Permission denied: Creating invoices requires FEES_CREATE role.") }
            return
        }

        if (studentName.isBlank() || dueDate.isBlank() || tuitionFee <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Student Name, Due Date, and Tuition amount are required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val invoiceNumber = feeRepository.generateInvoiceNumber(schoolId)
        val invoiceId = "inv_${UUID.randomUUID().toString().take(8)}"

        val items = mutableListOf<FeeItem>()
        items.add(FeeItem("fi_tui_${invoiceId}", FeeCategory.TUITION, "Tuition Fee - $academicTerm", tuitionFee))
        if (labFee > 0.0) items.add(FeeItem("fi_lab_${invoiceId}", FeeCategory.LABORATORY, "Science & STEM Lab", labFee))
        if (libraryFee > 0.0) items.add(FeeItem("fi_lib_${invoiceId}", FeeCategory.LIBRARY, "Library & Media Access", libraryFee))
        if (transportFee > 0.0) items.add(FeeItem("fi_trn_${invoiceId}", FeeCategory.TRANSPORT, "School Bus Service", transportFee))
        if (sportsFee > 0.0) items.add(FeeItem("fi_spt_${invoiceId}", FeeCategory.SPORTS, "Athletics Facility Fee", sportsFee))

        val newInvoice = FeeInvoice.createInvoice(
            id = invoiceId,
            schoolId = schoolId,
            invoiceNumber = invoiceNumber,
            studentId = studentId.ifBlank { "stu_${UUID.randomUUID().toString().take(6)}" },
            studentName = studentName,
            rollNumber = rollNumber.ifBlank { "ROLL-${(100..999).random()}" },
            gradeLevel = gradeLevel.ifBlank { "Grade 10" },
            section = section.ifBlank { "Section A" },
            academicTerm = academicTerm.ifBlank { "Term 1 (2026)" },
            dueDate = dueDate,
            items = items,
            discountAmount = discountAmount,
            fineAmount = 0.0,
            paidAmount = 0.0
        )

        // Update state synchronously for instant UI and test verification
        val updatedList = _uiState.value.invoices + newInvoice
        val metrics = computeMetrics(updatedList)
        _uiState.update {
            it.copy(
                invoices = updatedList,
                totalBilled = metrics.totalBilled,
                totalCollected = metrics.totalCollected,
                totalOutstanding = metrics.totalOutstanding,
                overdueCount = metrics.overdueCount,
                showCreateInvoiceDialog = false,
                statusMessage = "Invoice ${newInvoice.invoiceNumber} created successfully."
            )
        }

        viewModelScope.launch {
            feeRepository.saveInvoice(newInvoice)
        }
    }

    fun recordPayment(
        invoiceId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        notes: String = ""
    ) {
        if (!TenantContext.hasPermission(Permission.FEES_COLLECT)) {
            _uiState.update { it.copy(errorMessage = "Permission denied: Recording fee payments requires FEES_COLLECT role.") }
            return
        }

        val target = _uiState.value.invoices.firstOrNull { it.id == invoiceId }
        if (target == null) {
            _uiState.update { it.copy(errorMessage = "Invoice not found.") }
            return
        }

        if (amount <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Payment amount must be greater than zero.") }
            return
        }

        val schoolId = target.schoolId
        val receiptNumber = feeRepository.generateReceiptNumber(schoolId)
        val actorId = TenantContext.activeUserId.value ?: "usr_accountant"
        val actorName = TenantContext.activeUserRole.value.name

        val newTx = PaymentTransaction(
            transactionId = "tx_${UUID.randomUUID().toString().take(8)}",
            amount = amount,
            paymentDate = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            receiptNumber = receiptNumber,
            recordedById = actorId,
            recordedByName = actorName,
            notes = notes.ifBlank { "Payment collected via ${paymentMethod.displayName}" }
        )

        val updatedPaid = target.paidAmount + amount
        val updatedDue = (target.totalAmount - updatedPaid).coerceAtLeast(0.0)
        val updatedStatus = if (updatedDue <= 0.0) InvoiceStatus.PAID else InvoiceStatus.PARTIAL

        val updatedInvoice = target.copy(
            paidAmount = updatedPaid,
            dueAmount = updatedDue,
            status = updatedStatus,
            transactions = target.transactions + newTx
        )

        // Update state synchronously
        val updatedList = _uiState.value.invoices.map { if (it.id == invoiceId) updatedInvoice else it }
        val metrics = computeMetrics(updatedList)

        _uiState.update {
            it.copy(
                invoices = updatedList,
                totalBilled = metrics.totalBilled,
                totalCollected = metrics.totalCollected,
                totalOutstanding = metrics.totalOutstanding,
                overdueCount = metrics.overdueCount,
                showPaymentDialog = false,
                selectedInvoiceForPayment = null,
                showReceiptDialog = true,
                selectedInvoiceForReceipt = updatedInvoice,
                selectedTransactionForReceipt = newTx,
                statusMessage = "Payment of ${_uiState.value.currencySymbol}$amount recorded. Receipt #$receiptNumber generated."
            )
        }

        viewModelScope.launch {
            feeRepository.saveInvoice(updatedInvoice)
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private data class Metrics(
        val totalBilled: Double,
        val totalCollected: Double,
        val totalOutstanding: Double,
        val overdueCount: Int
    )

    private fun computeMetrics(invoices: List<FeeInvoice>): Metrics {
        val totalBilled = invoices.sumOf { it.totalAmount }
        val totalCollected = invoices.sumOf { it.paidAmount }
        val totalOutstanding = invoices.sumOf { it.dueAmount }
        val overdueCount = invoices.count { it.status == InvoiceStatus.OVERDUE }
        return Metrics(totalBilled, totalCollected, totalOutstanding, overdueCount)
    }
}
