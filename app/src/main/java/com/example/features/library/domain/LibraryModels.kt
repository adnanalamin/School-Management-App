package com.example.features.library.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Classification categories for institutional library catalog items.
 */
enum class BookCategory(val displayName: String) {
    TEXTBOOK("Academic Textbook"),
    SCIENCE("Natural Sciences"),
    MATHEMATICS("Mathematics"),
    LITERATURE("Literature & Arts"),
    COMPUTER_SCIENCE("Computer Science"),
    HISTORY("History & Civics"),
    REFERENCE("Reference & Encyclopedias"),
    FICTION("General Fiction");

    companion object {
        fun fromString(value: String?): BookCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TEXTBOOK
        }
    }
}

/**
 * Lending lifecycle status of an individual circulation record.
 */
enum class LoanStatus(val displayName: String) {
    ISSUED("Currently Issued"),
    RETURNED("Returned"),
    OVERDUE("Overdue"),
    LOST("Reported Lost");

    companion object {
        fun fromString(value: String?): LoanStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ISSUED
        }
    }
}

/**
 * Individual circulation/lending record for a library book copy.
 */
data class BookLoanRecord(
    val loanId: String,
    val borrowerId: String,
    val borrowerName: String,
    val borrowerRole: String = "Student",
    val rollOrStaffId: String,
    val issueDate: String,
    val dueDate: String,
    val returnDate: String = "",
    val status: LoanStatus = LoanStatus.ISSUED,
    val fineAmount: Double = 0.0,
    val notes: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "loanId" to loanId,
        "borrowerId" to borrowerId,
        "borrowerName" to borrowerName,
        "borrowerRole" to borrowerRole,
        "rollOrStaffId" to rollOrStaffId,
        "issueDate" to issueDate,
        "dueDate" to dueDate,
        "returnDate" to returnDate,
        "status" to status.name,
        "fineAmount" to fineAmount,
        "notes" to notes
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): BookLoanRecord {
            return BookLoanRecord(
                loanId = data["loanId"] as? String ?: "",
                borrowerId = data["borrowerId"] as? String ?: "",
                borrowerName = data["borrowerName"] as? String ?: "",
                borrowerRole = data["borrowerRole"] as? String ?: "Student",
                rollOrStaffId = data["rollOrStaffId"] as? String ?: "",
                issueDate = data["issueDate"] as? String ?: "",
                dueDate = data["dueDate"] as? String ?: "",
                returnDate = data["returnDate"] as? String ?: "",
                status = LoanStatus.fromString(data["status"] as? String),
                fineAmount = (data["fineAmount"] as? Number)?.toDouble() ?: 0.0,
                notes = data["notes"] as? String ?: ""
            )
        }
    }
}

/**
 * School library catalog book & circulation ledger entity.
 * Scoped to `schools/{schoolId}/library/{bookId}` (Subcollection 12).
 */
data class LibraryBook(
    override val id: String,
    override val schoolId: String,
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String = "",
    val category: BookCategory = BookCategory.TEXTBOOK,
    val shelfLocation: String = "Rack A-1",
    val totalCopies: Int = 5,
    val availableCopies: Int = 5,
    val publicationYear: Int = 2024,
    val description: String = "",
    val loans: List<BookLoanRecord> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val issuedCopiesCount: Int
        get() = (totalCopies - availableCopies).coerceAtLeast(0)

    val activeLoansCount: Int
        get() = loans.count { it.status == LoanStatus.ISSUED || it.status == LoanStatus.OVERDUE }

    val overdueLoansCount: Int
        get() = loans.count { it.status == LoanStatus.OVERDUE }

    val isAvailable: Boolean
        get() = availableCopies > 0

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "isbn" to isbn,
        "title" to title,
        "author" to author,
        "publisher" to publisher,
        "category" to category.name,
        "shelfLocation" to shelfLocation,
        "totalCopies" to totalCopies,
        "availableCopies" to availableCopies,
        "publicationYear" to publicationYear,
        "description" to description,
        "loans" to loans.map { it.toFirestoreMap() },
        "createdAt" to createdAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): LibraryBook {
            val rawLoans = data["loans"] as? List<Map<String, Any?>> ?: emptyList()
            val parsedLoans = rawLoans.map { BookLoanRecord.fromFirestoreMap(it) }

            return LibraryBook(
                id = id,
                schoolId = schoolId,
                isbn = data["isbn"] as? String ?: "",
                title = data["title"] as? String ?: "",
                author = data["author"] as? String ?: "",
                publisher = data["publisher"] as? String ?: "",
                category = BookCategory.fromString(data["category"] as? String),
                shelfLocation = data["shelfLocation"] as? String ?: "Rack A-1",
                totalCopies = (data["totalCopies"] as? Number)?.toInt() ?: 1,
                availableCopies = (data["availableCopies"] as? Number)?.toInt() ?: 1,
                publicationYear = (data["publicationYear"] as? Number)?.toInt() ?: 2024,
                description = data["description"] as? String ?: "",
                loans = parsedLoans,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
