package com.example.features.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.library.data.LibraryRepository
import com.example.features.library.domain.BookCategory
import com.example.features.library.domain.BookLoanRecord
import com.example.features.library.domain.LibraryBook
import com.example.features.library.domain.LoanStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class LibraryUiState(
    val books: List<LibraryBook> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilter: BookCategory? = null,
    val onlyAvailableFilter: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showAddBookDialog: Boolean = false,
    val showIssueBookDialog: Boolean = false,
    val showBookDetailDialog: Boolean = false,
    val selectedBookForAction: LibraryBook? = null
) {
    val filteredBooks: List<LibraryBook>
        get() = books.filter { book ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                book.title.lowercase().contains(q) ||
                book.author.lowercase().contains(q) ||
                book.isbn.lowercase().contains(q) ||
                book.shelfLocation.lowercase().contains(q) ||
                book.loans.any { it.borrowerName.lowercase().contains(q) || it.rollOrStaffId.lowercase().contains(q) }
            }
            val matchesCategory = selectedCategoryFilter == null || book.category == selectedCategoryFilter
            val matchesAvailability = !onlyAvailableFilter || book.isAvailable
            matchesSearch && matchesCategory && matchesAvailability
        }

    val totalTitlesCount: Int
        get() = books.size

    val totalPhysicalCopies: Int
        get() = books.sumOf { it.totalCopies }

    val availableCopiesCount: Int
        get() = books.sumOf { it.availableCopies }

    val activeIssuedCount: Int
        get() = books.sumOf { it.activeLoansCount }

    val overdueLoansCount: Int
        get() = books.sumOf { it.overdueLoansCount }
}

class LibraryViewModel(
    private val libraryRepository: LibraryRepository = LibraryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val seeded = libraryRepository.getPreSeededBooks(schoolId)
        _uiState.update { it.copy(books = seeded) }
        loadBooks()
    }

    fun loadBooks() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = libraryRepository.getBooks(schoolId = schoolId)
                _uiState.update { it.copy(books = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load library catalog"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: BookCategory?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun toggleOnlyAvailableFilter() {
        _uiState.update { it.copy(onlyAvailableFilter = !it.onlyAvailableFilter) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedCategoryFilter = null,
                onlyAvailableFilter = false
            )
        }
    }

    fun openAddBookDialog() {
        if (!TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LIBRARY_MANAGE permission") }
            return
        }
        _uiState.update { it.copy(showAddBookDialog = true, errorMessage = null) }
    }

    fun closeAddBookDialog() {
        _uiState.update { it.copy(showAddBookDialog = false) }
    }

    fun openIssueBookDialog(book: LibraryBook) {
        if (!TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LIBRARY_MANAGE permission") }
            return
        }
        if (!book.isAvailable) {
            _uiState.update { it.copy(errorMessage = "All copies of '${book.title}' are currently checked out.") }
            return
        }
        _uiState.update {
            it.copy(
                selectedBookForAction = book,
                showIssueBookDialog = true,
                errorMessage = null
            )
        }
    }

    fun closeIssueBookDialog() {
        _uiState.update {
            it.copy(
                showIssueBookDialog = false,
                selectedBookForAction = null
            )
        }
    }

    fun openBookDetail(book: LibraryBook) {
        _uiState.update {
            it.copy(
                selectedBookForAction = book,
                showBookDetailDialog = true
            )
        }
    }

    fun closeBookDetail() {
        _uiState.update {
            it.copy(
                showBookDetailDialog = false,
                selectedBookForAction = null
            )
        }
    }

    fun addBook(
        isbn: String,
        title: String,
        author: String,
        publisher: String,
        category: BookCategory,
        shelfLocation: String,
        totalCopies: Int,
        publicationYear: Int,
        description: String
    ) {
        if (!TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LIBRARY_MANAGE permission") }
            return
        }

        if (title.isBlank() || author.isBlank() || isbn.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Book title, author, and ISBN are required.") }
            return
        }

        val copies = totalCopies.coerceAtLeast(1)
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val newBook = LibraryBook(
            id = "lib_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            isbn = isbn.trim(),
            title = title.trim(),
            author = author.trim(),
            publisher = publisher.trim(),
            category = category,
            shelfLocation = shelfLocation.ifBlank { "Rack A-1" }.trim(),
            totalCopies = copies,
            availableCopies = copies,
            publicationYear = publicationYear,
            description = description.trim(),
            loans = emptyList(),
            createdAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                books = listOf(newBook) + state.books,
                showAddBookDialog = false,
                statusMessage = "'${newBook.title}' ($copies copies) cataloged in ${newBook.shelfLocation}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            libraryRepository.saveBook(newBook)
        }
    }

    fun issueBook(
        bookId: String,
        borrowerName: String,
        rollOrStaffId: String,
        borrowerRole: String,
        dueDate: String,
        notes: String
    ) {
        if (!TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LIBRARY_MANAGE permission") }
            return
        }

        if (borrowerName.isBlank() || rollOrStaffId.isBlank() || dueDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Borrower name, ID, and due date are required.") }
            return
        }

        val target = _uiState.value.books.firstOrNull { it.id == bookId } ?: return
        if (target.availableCopies <= 0) {
            _uiState.update { it.copy(errorMessage = "No copies available to issue for '${target.title}'.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date())

        val loanRecord = BookLoanRecord(
            loanId = "ln_${UUID.randomUUID().toString().take(8)}",
            borrowerId = "brw_${UUID.randomUUID().toString().take(6)}",
            borrowerName = borrowerName.trim(),
            borrowerRole = borrowerRole.ifBlank { "Student" }.trim(),
            rollOrStaffId = rollOrStaffId.trim().uppercase(),
            issueDate = todayStr,
            dueDate = dueDate.trim(),
            status = LoanStatus.ISSUED,
            notes = notes.trim()
        )

        _uiState.update { state ->
            val updatedBooks = state.books.map { book ->
                if (book.id == bookId) {
                    book.copy(
                        availableCopies = (book.availableCopies - 1).coerceAtLeast(0),
                        loans = listOf(loanRecord) + book.loans
                    )
                } else book
            }
            val updatedSelected = updatedBooks.firstOrNull { it.id == bookId }
            state.copy(
                books = updatedBooks,
                showIssueBookDialog = false,
                selectedBookForAction = if (state.showBookDetailDialog) updatedSelected else null,
                statusMessage = "Issued '${target.title}' to ${loanRecord.borrowerName} (Due: ${loanRecord.dueDate}).",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            libraryRepository.issueBook(schoolId, bookId, loanRecord)
        }
    }

    fun returnBook(
        bookId: String,
        loanId: String,
        fineAmount: Double = 0.0
    ) {
        if (!TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LIBRARY_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val returnDateStr = dateFormat.format(Date())

        var returnedTitle = ""
        _uiState.update { state ->
            val updatedBooks = state.books.map { book ->
                if (book.id == bookId) {
                    returnedTitle = book.title
                    var matched = false
                    val updatedLoans = book.loans.map { loan ->
                        if (loan.loanId == loanId && (loan.status == LoanStatus.ISSUED || loan.status == LoanStatus.OVERDUE)) {
                            matched = true
                            loan.copy(
                                returnDate = returnDateStr,
                                status = LoanStatus.RETURNED,
                                fineAmount = fineAmount.coerceAtLeast(0.0)
                            )
                        } else loan
                    }
                    if (matched) {
                        book.copy(
                            availableCopies = (book.availableCopies + 1).coerceAtMost(book.totalCopies),
                            loans = updatedLoans
                        )
                    } else book
                } else book
            }
            val updatedSelected = updatedBooks.firstOrNull { it.id == bookId }
            state.copy(
                books = updatedBooks,
                selectedBookForAction = if (state.showBookDetailDialog) updatedSelected else state.selectedBookForAction,
                statusMessage = if (returnedTitle.isNotEmpty()) "Copy of '$returnedTitle' checked back in." else state.statusMessage,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            libraryRepository.returnBook(schoolId, bookId, loanId, returnDateStr, fineAmount)
        }
    }

    fun deleteBook(bookId: String) {
        if (!TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LIBRARY_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                books = state.books.filterNot { it.id == bookId },
                statusMessage = "Book removed from library catalog."
            )
        }

        viewModelScope.launch {
            libraryRepository.deleteBook(schoolId, bookId)
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
