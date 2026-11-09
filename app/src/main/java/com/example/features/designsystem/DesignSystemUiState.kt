package com.example.features.designsystem

data class DesignSystemUiState(
    val isDarkMode: Boolean = false,
    val isShimmerLoading: Boolean = false,
    val searchQuery: String = "",
    val textInputValue: String = "John Doe",
    val textInputError: String? = null,
    val passwordValue: String = "SecretPass123",
    val selectedClass: String = "Grade 10 - Science",
    val availableClasses: List<String> = listOf(
        "Grade 6 - General",
        "Grade 7 - General",
        "Grade 8 - General",
        "Grade 9 - Science",
        "Grade 9 - Commerce",
        "Grade 10 - Science",
        "Grade 10 - Humanities"
    ),
    val showConfirmDialog: Boolean = false,
    val showPromptDialog: Boolean = false,
    val showFilterBottomSheet: Boolean = false,
    val selectedStatusFilter: String = "All",
    val selectedCategoryTab: Int = 0, // 0: Cards & Metrics, 1: Form Inputs, 2: Dialogs & Sheets, 3: Shimmer & Feedback
    val lastActionLog: String = "Design system initialized"
)
