package com.example.features.tenant

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.AppError
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.core.tenant.StudentEntity
import com.example.core.tenant.TeacherEntity
import com.example.core.tenant.FeeEntity
import com.example.core.tenant.NoticeEntity
import com.example.core.tenant.TenantDataRepository
import com.example.core.tenant.TenantPathProvider
import com.example.shared.models.TenantSchool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TenantOption(
    val id: String,
    val name: String,
    val code: String,
    val location: String
)

data class TenantArchitectureUiState(
    val activeTenantId: String = "sch_oxford_01",
    val availableTenants: List<TenantOption> = listOf(
        TenantOption("sch_oxford_01", "Oxford International Academy", "OXF-101", "Boston, USA"),
        TenantOption("sch_cambridge_101", "Cambridge High School", "CHS-101", "Cambridge, UK"),
        TenantOption("sch_stjudes_202", "St. Jude's International School", "STJ-202", "Toronto, Canada")
    ),
    val selectedSubcollection: String = "students",
    val subcollectionsList: List<String> = listOf(
        "students", "teachers", "parents", "classes", "sections",
        "attendance", "fees", "exams", "results", "notices",
        "homework", "library", "transport", "events", "documents"
    ),
    val currentPath: String = "schools/sch_oxford_01/students",
    val students: List<StudentEntity> = emptyList(),
    val teachers: List<TeacherEntity> = emptyList(),
    val fees: List<FeeEntity> = emptyList(),
    val notices: List<NoticeEntity> = emptyList(),
    val totalRecordsInSubcollection: Int = 0,
    val firewallStatus: String = "STRICT ISOLATION ACTIVE",
    val simulatedAttackMessage: String? = null,
    val isAttackBlocked: Boolean = false,
    val isLoading: Boolean = false
)

class TenantArchitectureViewModel(
    private val repositoryProvider: ((Context) -> TenantDataRepository)? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantArchitectureUiState())
    val uiState: StateFlow<TenantArchitectureUiState> = _uiState.asStateFlow()

    init {
        loadSubcollectionData()
    }

    fun switchActiveTenant(tenantId: String) {
        TenantContext.setTenant(
            schoolId = tenantId,
            role = UserRole.SCHOOL_ADMIN,
            userId = "usr_admin_${tenantId.takeLast(4)}"
        )
        _uiState.update {
            it.copy(
                activeTenantId = tenantId,
                currentPath = "schools/$tenantId/${it.selectedSubcollection}",
                simulatedAttackMessage = null,
                isAttackBlocked = false
            )
        }
        loadSubcollectionData()
    }

    fun selectSubcollection(collection: String) {
        _uiState.update {
            it.copy(
                selectedSubcollection = collection,
                currentPath = "schools/${it.activeTenantId}/$collection",
                simulatedAttackMessage = null
            )
        }
        loadSubcollectionData()
    }

    fun loadSubcollectionData(context: Context? = null) {
        val state = _uiState.value
        val repo = if (context != null) repositoryProvider?.invoke(context) ?: TenantDataRepository(context) else TenantDataRepository()
        val schoolId = state.activeTenantId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val path = when (state.selectedSubcollection) {
                "students" -> TenantPathProvider.studentsPath(schoolId)
                "teachers" -> TenantPathProvider.teachersPath(schoolId)
                "parents" -> TenantPathProvider.parentsPath(schoolId)
                "classes" -> TenantPathProvider.classesPath(schoolId)
                "sections" -> TenantPathProvider.sectionsPath(schoolId)
                "attendance" -> TenantPathProvider.attendancePath(schoolId)
                "fees" -> TenantPathProvider.feesPath(schoolId)
                "exams" -> TenantPathProvider.examsPath(schoolId)
                "results" -> TenantPathProvider.resultsPath(schoolId)
                "notices" -> TenantPathProvider.noticesPath(schoolId)
                "homework" -> TenantPathProvider.homeworkPath(schoolId)
                "library" -> TenantPathProvider.libraryPath(schoolId)
                "transport" -> TenantPathProvider.transportPath(schoolId)
                "events" -> TenantPathProvider.eventsPath(schoolId)
                "documents" -> TenantPathProvider.documentsPath(schoolId)
                else -> TenantPathProvider.studentsPath(schoolId)
            }

            val studentList = repo.getStudents(schoolId)
            val teacherList = repo.getTeachers(schoolId)
            val feeList = repo.getFees(schoolId)
            val noticeList = repo.getNotices(schoolId)

            val count = when (state.selectedSubcollection) {
                "students" -> studentList.size
                "teachers" -> teacherList.size
                "fees" -> feeList.size
                "notices" -> noticeList.size
                else -> 4 // Baseline items for other subcollections
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentPath = path,
                    students = studentList,
                    teachers = teacherList,
                    fees = feeList,
                    notices = noticeList,
                    totalRecordsInSubcollection = count
                )
            }
        }
    }

    fun simulateCrossTenantBreachAttempt() {
        val currentActive = _uiState.value.activeTenantId
        val targetForeignTenant = if (currentActive == "sch_oxford_01") "sch_cambridge_101" else "sch_oxford_01"

        try {
            // Attempting to evaluate or access foreign tenant boundary
            TenantPathProvider.verifyTenantBoundary(targetForeignTenant)
            _uiState.update {
                it.copy(
                    isAttackBlocked = false,
                    simulatedAttackMessage = "Breach succeeded (UNEXPECTED)"
                )
            }
        } catch (e: AppError.SecurityViolation) {
            _uiState.update {
                it.copy(
                    isAttackBlocked = true,
                    simulatedAttackMessage = "BLOCKED: ${e.message}"
                )
            }
        }
    }
}
