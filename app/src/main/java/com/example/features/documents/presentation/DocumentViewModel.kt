package com.example.features.documents.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.documents.data.DocumentRepository
import com.example.features.documents.domain.DocumentCategory
import com.example.features.documents.domain.DocumentVerificationStatus
import com.example.features.documents.domain.SchoolDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DocumentUiState(
    val documents: List<SchoolDocument> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilter: DocumentCategory? = null,
    val selectedStatusFilter: DocumentVerificationStatus? = null,
    val onlyPinnedFilter: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showCreateDocumentDialog: Boolean = false,
    val showDocumentDetailDialog: Boolean = false,
    val selectedDocumentForAction: SchoolDocument? = null
) {
    val filteredDocuments: List<SchoolDocument>
        get() = documents.filter { doc ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                doc.title.lowercase().contains(q) ||
                    doc.documentCode.lowercase().contains(q) ||
                    doc.description.lowercase().contains(q) ||
                    doc.fileName.lowercase().contains(q) ||
                    doc.targetGradeOrDepartment.lowercase().contains(q) ||
                    doc.issuedToName.lowercase().contains(q) ||
                    doc.issuedToId.lowercase().contains(q) ||
                    doc.issuedBy.lowercase().contains(q)
            }
            val matchesCategory = selectedCategoryFilter == null || doc.category == selectedCategoryFilter
            val matchesStatus = selectedStatusFilter == null || doc.verificationStatus == selectedStatusFilter
            val matchesPinned = !onlyPinnedFilter || doc.isPinned
            matchesSearch && matchesCategory && matchesStatus && matchesPinned
        }.sortedByDescending { it.isPinned }

    val totalDocumentsCount: Int
        get() = documents.size

    val verifiedDocumentsCount: Int
        get() = documents.count { it.verificationStatus == DocumentVerificationStatus.VERIFIED }

    val credentialsCount: Int
        get() = documents.count { it.isOfficialCredential }

    val totalDownloadsCount: Int
        get() = documents.sumOf { it.downloadCount }
}

class DocumentViewModel(
    private val documentRepository: DocumentRepository = DocumentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()

    init {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val seeded = documentRepository.getPreSeededDocuments(schoolId)
        _uiState.update { it.copy(documents = seeded) }
        loadDocuments()
    }

    fun loadDocuments() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = documentRepository.getDocuments(schoolId = schoolId)
                _uiState.update { it.copy(documents = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load institutional documents"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: DocumentCategory?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun setStatusFilter(status: DocumentVerificationStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun toggleOnlyPinnedFilter() {
        _uiState.update { it.copy(onlyPinnedFilter = !it.onlyPinnedFilter) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedCategoryFilter = null,
                selectedStatusFilter = null,
                onlyPinnedFilter = false
            )
        }
    }

    fun openCreateDocumentDialog() {
        if (!TenantContext.hasPermission(Permission.DOCUMENTS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires DOCUMENTS_MANAGE permission") }
            return
        }
        _uiState.update { it.copy(showCreateDocumentDialog = true, errorMessage = null) }
    }

    fun closeCreateDocumentDialog() {
        _uiState.update { it.copy(showCreateDocumentDialog = false) }
    }

    fun openDocumentDetail(document: SchoolDocument) {
        _uiState.update {
            it.copy(
                selectedDocumentForAction = document,
                showDocumentDetailDialog = true
            )
        }
    }

    fun closeDocumentDetail() {
        _uiState.update {
            it.copy(
                showDocumentDetailDialog = false,
                selectedDocumentForAction = null
            )
        }
    }

    fun publishDocument(
        documentCode: String,
        title: String,
        description: String,
        category: DocumentCategory,
        targetGradeOrDepartment: String,
        issuedToName: String,
        issuedToId: String,
        fileName: String,
        fileSizeLabel: String,
        isPinned: Boolean
    ) {
        if (!TenantContext.hasPermission(Permission.DOCUMENTS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires DOCUMENTS_MANAGE permission") }
            return
        }

        if (title.isBlank() || fileName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Document title and file name are required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date())
        val issuerRole = TenantContext.activeUserRole.value.displayName
        val safeCode = documentCode.ifBlank {
            "DOC-${UUID.randomUUID().toString().take(6).uppercase()}"
        }.trim().uppercase()

        val newDoc = SchoolDocument(
            id = "doc_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            documentCode = safeCode,
            title = title.trim(),
            description = description.trim(),
            category = category,
            verificationStatus = DocumentVerificationStatus.VERIFIED,
            targetGradeOrDepartment = targetGradeOrDepartment.ifBlank { "All Grades" }.trim(),
            issuedToName = issuedToName.trim(),
            issuedToId = issuedToId.trim().uppercase(),
            fileName = fileName.trim(),
            fileSizeLabel = fileSizeLabel.ifBlank { "1.0 MB" }.trim(),
            academicYear = "2026-2027",
            issuedBy = issuerRole,
            issuedDate = todayStr,
            downloadCount = 0,
            isPinned = isPinned,
            createdAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                documents = listOf(newDoc) + state.documents,
                showCreateDocumentDialog = false,
                statusMessage = "Published '${newDoc.title}' ($safeCode).",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            documentRepository.saveDocument(newDoc)
        }
    }

    fun recordDownload(docId: String) {
        if (!TenantContext.hasPermission(Permission.DOCUMENTS_VIEW)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires DOCUMENTS_VIEW permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        var downloadedFile = ""
        _uiState.update { state ->
            val updatedDocs = state.documents.map { doc ->
                if (doc.id == docId) {
                    downloadedFile = doc.fileName
                    doc.copy(downloadCount = doc.downloadCount + 1)
                } else doc
            }
            val updatedSelected = updatedDocs.firstOrNull { it.id == docId }
            state.copy(
                documents = updatedDocs,
                selectedDocumentForAction = if (state.showDocumentDetailDialog) updatedSelected else state.selectedDocumentForAction,
                statusMessage = if (downloadedFile.isNotEmpty()) "Downloaded '$downloadedFile' safely." else state.statusMessage,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            documentRepository.recordDownload(schoolId, docId)
        }
    }

    fun updateVerificationStatus(docId: String, newStatus: DocumentVerificationStatus) {
        if (!TenantContext.hasPermission(Permission.DOCUMENTS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires DOCUMENTS_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            val updatedDocs = state.documents.map { doc ->
                if (doc.id == docId) doc.copy(verificationStatus = newStatus) else doc
            }
            val updatedSelected = updatedDocs.firstOrNull { it.id == docId }
            state.copy(
                documents = updatedDocs,
                selectedDocumentForAction = if (state.showDocumentDetailDialog) updatedSelected else state.selectedDocumentForAction,
                statusMessage = "Document status updated to ${newStatus.displayName}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            documentRepository.updateVerificationStatus(schoolId, docId, newStatus)
        }
    }

    fun togglePin(docId: String) {
        if (!TenantContext.hasPermission(Permission.DOCUMENTS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires DOCUMENTS_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        var nowPinned = false
        _uiState.update { state ->
            val updatedDocs = state.documents.map { doc ->
                if (doc.id == docId) {
                    nowPinned = !doc.isPinned
                    doc.copy(isPinned = nowPinned)
                } else doc
            }
            val updatedSelected = updatedDocs.firstOrNull { it.id == docId }
            state.copy(
                documents = updatedDocs,
                selectedDocumentForAction = if (state.showDocumentDetailDialog) updatedSelected else state.selectedDocumentForAction,
                statusMessage = if (nowPinned) "Document pinned to top of vault." else "Document unpinned.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            documentRepository.togglePin(schoolId, docId)
        }
    }

    fun deleteDocument(docId: String) {
        if (!TenantContext.hasPermission(Permission.DOCUMENTS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires DOCUMENTS_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                documents = state.documents.filterNot { it.id == docId },
                statusMessage = "Document removed from institutional vault."
            )
        }

        viewModelScope.launch {
            documentRepository.deleteDocument(schoolId, docId)
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
