package com.example.features.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val highlightBadge: String
)

data class OnboardingUiState(
    val currentPage: Int = 0,
    val pages: List<OnboardingPageData> = listOf(
        OnboardingPageData(
            title = "Multi-School SaaS Cloud",
            subtitle = "Complete zero-trust tenant data isolation for multiple schools, campuses, and academic institutions.",
            highlightBadge = "ENTERPRISE SAAS"
        ),
        OnboardingPageData(
            title = "Unified Academics & Operations",
            subtitle = "Real-time student attendance, automated grading & GPA calculations, fee invoicing, and exam scheduling.",
            highlightBadge = "ALL-IN-ONE PORTAL"
        ),
        OnboardingPageData(
            title = "Connected School Community",
            subtitle = "Dedicated access control and instant communication channels for administrators, faculty, parents, and students.",
            highlightBadge = "ROLE-BASED ACCESS"
        )
    )
)

class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setPage(page: Int) {
        _uiState.update { it.copy(currentPage = page.coerceIn(0, it.pages.size - 1)) }
    }

    fun nextPage(): Boolean {
        val next = _uiState.value.currentPage + 1
        return if (next < _uiState.value.pages.size) {
            _uiState.update { it.copy(currentPage = next) }
            true
        } else {
            false
        }
    }
}
