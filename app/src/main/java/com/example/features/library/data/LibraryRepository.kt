package com.example.features.library.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.library.domain.BookCategory
import com.example.features.library.domain.BookLoanRecord
import com.example.features.library.domain.LibraryBook
import com.example.features.library.domain.LoanStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LibraryRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("LibraryRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getBooks(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        category: BookCategory? = null,
        onlyAvailable: Boolean = false
    ): List<LibraryBook> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.libraryPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededBooks(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    LibraryBook.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterBooks(list, category, onlyAvailable)
        } catch (e: Exception) {
            Log.w("LibraryRepository", "Error retrieving library catalog from Firestore, fallback to preseeded", e)
            val seeded = getPreSeededBooks(schoolId)
            filterBooks(seeded, category, onlyAvailable)
        }
    }

    private fun filterBooks(
        books: List<LibraryBook>,
        category: BookCategory?,
        onlyAvailable: Boolean
    ): List<LibraryBook> {
        return books.filter { book ->
            (category == null || book.category == category) &&
            (!onlyAvailable || book.isAvailable)
        }
    }

    suspend fun saveBook(book: LibraryBook): Result<LibraryBook> {
        return try {
            TenantPathProvider.verifyTenantBoundary(book.schoolId)
            val path = TenantPathProvider.libraryPath(book.schoolId)
            db.collection(path).document(book.id).set(book.toFirestoreMap()).await()
            Result.success(book)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun issueBook(
        schoolId: String,
        bookId: String,
        loan: BookLoanRecord
    ): Result<LibraryBook> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val books = getBooks(schoolId)
            val target = books.firstOrNull { it.id == bookId }
                ?: return Result.failure(Exception("Book record not found in catalog"))

            if (target.availableCopies <= 0) {
                return Result.failure(Exception("No copies currently available to issue"))
            }

            val updatedBook = target.copy(
                availableCopies = (target.availableCopies - 1).coerceAtLeast(0),
                loans = listOf(loan) + target.loans
            )
            saveBook(updatedBook)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun returnBook(
        schoolId: String,
        bookId: String,
        loanId: String,
        returnDate: String,
        fineAmount: Double = 0.0
    ): Result<LibraryBook> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val books = getBooks(schoolId)
            val target = books.firstOrNull { it.id == bookId }
                ?: return Result.failure(Exception("Book record not found in catalog"))

            var foundActiveLoan = false
            val updatedLoans = target.loans.map { loan ->
                if (loan.loanId == loanId && (loan.status == LoanStatus.ISSUED || loan.status == LoanStatus.OVERDUE)) {
                    foundActiveLoan = true
                    loan.copy(
                        returnDate = returnDate,
                        status = LoanStatus.RETURNED,
                        fineAmount = fineAmount
                    )
                } else loan
            }

            if (!foundActiveLoan) {
                return Result.failure(Exception("Active loan record not found"))
            }

            val updatedBook = target.copy(
                availableCopies = (target.availableCopies + 1).coerceAtMost(target.totalCopies),
                loans = updatedLoans
            )
            saveBook(updatedBook)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteBook(schoolId: String, bookId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.libraryPath(schoolId)
            db.collection(path).document(bookId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededBooks(schoolId: String): List<LibraryBook> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                LibraryBook(
                    id = "lib_oxf_01",
                    schoolId = schoolId,
                    isbn = "978-0-321-50349-7",
                    title = "University Physics with Modern Physics",
                    author = "Hugh D. Young & Roger A. Freedman",
                    publisher = "Pearson Academic",
                    category = BookCategory.SCIENCE,
                    shelfLocation = "Wing B • Rack S-12",
                    totalCopies = 12,
                    availableCopies = 10,
                    publicationYear = 2023,
                    description = "Comprehensive calculus-based mechanics, electromagnetism, thermodynamics, and quantum optics reference.",
                    loans = listOf(
                        BookLoanRecord(
                            loanId = "ln_oxf_101",
                            borrowerId = "stu_oxf_01",
                            borrowerName = "Amina Rahman",
                            borrowerRole = "Student",
                            rollOrStaffId = "OXF-10A-001",
                            issueDate = "2026-03-10",
                            dueDate = "2026-03-24",
                            status = LoanStatus.ISSUED,
                            notes = "Midterm physics lab prep"
                        ),
                        BookLoanRecord(
                            loanId = "ln_oxf_102",
                            borrowerId = "stu_oxf_02",
                            borrowerName = "Oliver Smith",
                            borrowerRole = "Student",
                            rollOrStaffId = "OXF-10A-002",
                            issueDate = "2026-02-25",
                            dueDate = "2026-03-11",
                            status = LoanStatus.OVERDUE,
                            fineAmount = 4.50,
                            notes = "Reminder sent for overdue return"
                        )
                    )
                ),
                LibraryBook(
                    id = "lib_oxf_02",
                    schoolId = schoolId,
                    isbn = "978-0-262-03384-8",
                    title = "Introduction to Algorithms (CLRS 4th Ed.)",
                    author = "Thomas H. Cormen et al.",
                    publisher = "MIT Press",
                    category = BookCategory.COMPUTER_SCIENCE,
                    shelfLocation = "Wing C • Rack CS-04",
                    totalCopies = 8,
                    availableCopies = 7,
                    publicationYear = 2022,
                    description = "Canonical guide to graph theory, dynamic programming, amortized analysis, and data structures.",
                    loans = listOf(
                        BookLoanRecord(
                            loanId = "ln_oxf_103",
                            borrowerId = "tea_oxf_01",
                            borrowerName = "Dr. Sarah Jenkins",
                            borrowerRole = "Faculty",
                            rollOrStaffId = "FAC-OXF-01",
                            issueDate = "2026-03-14",
                            dueDate = "2026-04-14",
                            status = LoanStatus.ISSUED,
                            notes = "Faculty robotics club seminar"
                        )
                    )
                ),
                LibraryBook(
                    id = "lib_oxf_03",
                    schoolId = schoolId,
                    isbn = "978-0-521-60602-8",
                    title = "Pure Mathematics for Senior Secondary",
                    author = "Hugh Neill & Douglas Quadling",
                    publisher = "Cambridge University Press",
                    category = BookCategory.MATHEMATICS,
                    shelfLocation = "Wing A • Rack M-02",
                    totalCopies = 15,
                    availableCopies = 15,
                    publicationYear = 2024,
                    description = "Core textbook covering complex numbers, differential equations, and vector spaces.",
                    loans = listOf(
                        BookLoanRecord(
                            loanId = "ln_oxf_104",
                            borrowerId = "stu_oxf_03",
                            borrowerName = "Sophia Chen",
                            borrowerRole = "Student",
                            rollOrStaffId = "OXF-10A-003",
                            issueDate = "2026-03-01",
                            dueDate = "2026-03-15",
                            returnDate = "2026-03-14",
                            status = LoanStatus.RETURNED,
                            fineAmount = 0.0,
                            notes = "Returned in pristine condition"
                        )
                    )
                ),
                LibraryBook(
                    id = "lib_oxf_04",
                    schoolId = schoolId,
                    isbn = "978-0-141-43951-8",
                    title = "The Complete Works of William Shakespeare",
                    author = "William Shakespeare",
                    publisher = "Oxford Clarendon Edition",
                    category = BookCategory.LITERATURE,
                    shelfLocation = "Wing A • Rack L-09",
                    totalCopies = 6,
                    availableCopies = 5,
                    publicationYear = 2021,
                    description = "Annotated academic edition including Macbeth, Hamlet, and The Tempest.",
                    loans = listOf(
                        BookLoanRecord(
                            loanId = "ln_oxf_105",
                            borrowerId = "stu_oxf_03",
                            borrowerName = "Sophia Chen",
                            borrowerRole = "Student",
                            rollOrStaffId = "OXF-10A-003",
                            issueDate = "2026-03-18",
                            dueDate = "2026-04-01",
                            status = LoanStatus.ISSUED,
                            notes = "Essay research reference"
                        )
                    )
                )
            )
        } else {
            listOf(
                LibraryBook(
                    id = "lib_cam_01",
                    schoolId = schoolId,
                    isbn = "978-0-140-43072-1",
                    title = "Lyrical Ballads & Romantic Criticism",
                    author = "William Wordsworth & S.T. Coleridge",
                    publisher = "Penguin Classics",
                    category = BookCategory.LITERATURE,
                    shelfLocation = "Main Hall • Rack L-01",
                    totalCopies = 10,
                    availableCopies = 9,
                    publicationYear = 2023,
                    description = "Annotated critical volume for Year 10 British Literature.",
                    loans = listOf(
                        BookLoanRecord(
                            loanId = "ln_cam_201",
                            borrowerId = "stu_cam_01",
                            borrowerName = "Elizabeth Bennet",
                            borrowerRole = "Student",
                            rollOrStaffId = "CAM-10A-001",
                            issueDate = "2026-03-19",
                            dueDate = "2026-04-02",
                            status = LoanStatus.ISSUED
                        )
                    )
                ),
                LibraryBook(
                    id = "lib_cam_02",
                    schoolId = schoolId,
                    isbn = "978-0-691-15091-8",
                    title = "Classical Mechanics: A Modern Perspective",
                    author = "V.D. Barger & M.G. Olsson",
                    publisher = "Princeton Academic",
                    category = BookCategory.SCIENCE,
                    shelfLocation = "Science Wing • Rack P-03",
                    totalCopies = 7,
                    availableCopies = 7,
                    publicationYear = 2024,
                    description = "Lagrangian and Hamiltonian formulations for honors cohorts."
                )
            )
        }
    }
}
