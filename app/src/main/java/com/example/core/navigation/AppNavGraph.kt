package com.example.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.auth.LoginScreen
import com.example.features.auth.LoginViewModel
import com.example.features.auth.RegisterScreen
import com.example.features.auth.RegisterViewModel
import com.example.features.schools.presentation.SchoolRegistrationScreen
import com.example.features.schools.presentation.SchoolRegistrationViewModel
import com.example.features.dashboard.DashboardScreen
import com.example.features.dashboard.DashboardViewModel
import com.example.features.designsystem.DesignSystemScreen
import com.example.features.designsystem.DesignSystemViewModel
import com.example.features.foundation.FoundationScreen
import com.example.features.foundation.FoundationViewModel
import com.example.features.onboarding.OnboardingScreen
import com.example.features.onboarding.OnboardingViewModel
import com.example.features.profile.ProfileScreen
import com.example.features.profile.ProfileViewModel
import com.example.features.settings.SettingsScreen
import com.example.features.settings.SettingsViewModel
import com.example.features.tenant.TenantArchitectureScreen
import com.example.features.tenant.TenantArchitectureViewModel
import com.example.features.rbac.RbacMatrixScreen
import com.example.features.rbac.RbacMatrixViewModel
import com.example.features.students.presentation.StudentListScreen
import com.example.features.students.presentation.StudentListViewModel
import com.example.features.teachers.presentation.TeacherListScreen
import com.example.features.teachers.presentation.TeacherListViewModel
import com.example.features.splash.SplashScreen
import com.example.features.splash.SplashViewModel
import com.example.shared.components.EduBottomNavBar
import com.example.shared.components.NavItem
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    foundationViewModel: FoundationViewModel = viewModel(),
    designSystemViewModel: DesignSystemViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Determine if bottom navigation should be visible
    val isBottomBarVisible = currentRoute in listOf(
        AppScreen.MainDashboard.route,
        AppScreen.StudentsList.route,
        AppScreen.TeachersList.route,
        AppScreen.Attendance.route,
        AppScreen.Fees.route,
        AppScreen.Settings.route
    )

    // Helper function to safely navigate with RouteGuard evaluation
    val safeNavigate: (AppScreen) -> Unit = { destination ->
        when (val result = RouteGuard.evaluate(destination)) {
            is RouteGuard.GuardResult.Authorized -> {
                navController.navigate(destination.route) {
                    popUpTo(AppScreen.MainDashboard.route) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            is RouteGuard.GuardResult.Redirect -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(result.reason)
                }
                navController.navigate(result.targetScreen.route) {
                    launchSingleTop = true
                }
            }
        }
    }

    // Dynamic Bottom Nav items based on current active role permissions
    val activeRole = TenantContext.activeUserRole.value
    val bottomNavItems = mutableListOf(
        NavItem(AppScreen.MainDashboard.route, "Home", Icons.Default.Dashboard, "nav_item_dashboard")
    )
    if (TenantContext.hasPermission(Permission.STUDENTS_VIEW)) {
        bottomNavItems.add(NavItem(AppScreen.StudentsList.route, "Students", Icons.Default.School, "nav_item_students"))
    }
    if (TenantContext.hasPermission(Permission.TEACHERS_VIEW)) {
        bottomNavItems.add(NavItem(AppScreen.TeachersList.route, "Faculty", Icons.Default.Group, "nav_item_teachers"))
    }
    if (TenantContext.hasPermission(Permission.ATTENDANCE_VIEW)) {
        bottomNavItems.add(NavItem(AppScreen.Attendance.route, "Attendance", Icons.Default.CheckCircle, "nav_item_attendance"))
    }
    if (TenantContext.hasPermission(Permission.FEES_VIEW)) {
        bottomNavItems.add(NavItem(AppScreen.Fees.route, "Fees", Icons.Default.AttachMoney, "nav_item_fees"))
    }
    bottomNavItems.add(NavItem(AppScreen.Settings.route, "Settings", Icons.Default.Settings, "nav_item_settings"))

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isBottomBarVisible) {
                EduBottomNavBar(
                    items = bottomNavItems,
                    selectedId = currentRoute ?: AppScreen.MainDashboard.route,
                    onItemSelected = { route ->
                        val screen = AppScreen.fromRoute(route)
                        safeNavigate(screen)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Splash Screen
            composable(AppScreen.Splash.route) {
                val splashViewModel: SplashViewModel = viewModel()
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateNext = { route ->
                        navController.navigate(route) {
                            popUpTo(AppScreen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding Screen
            composable(AppScreen.Onboarding.route) {
                val onboardingViewModel: OnboardingViewModel = viewModel()
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onFinishOnboarding = {
                        navController.navigate(AppScreen.Login.route) {
                            popUpTo(AppScreen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Login Screen
            composable(AppScreen.Login.route) {
                val loginViewModel: LoginViewModel = viewModel()
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate(AppScreen.MainDashboard.route) {
                            popUpTo(AppScreen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(AppScreen.Register.route)
                    }
                )
            }

            // Register School Destination (Phase 5 School Registration & Onboarding)
            composable(AppScreen.Register.route) {
                val schoolRegistrationViewModel: SchoolRegistrationViewModel = viewModel()
                BackHandler {
                    if (schoolRegistrationViewModel.uiState.value.currentStep > 0) {
                        schoolRegistrationViewModel.previousStep()
                    } else {
                        navController.popBackStack()
                    }
                }
                SchoolRegistrationScreen(
                    viewModel = schoolRegistrationViewModel,
                    onRegistrationSuccess = {
                        navController.navigate(AppScreen.MainDashboard.route) {
                            popUpTo(AppScreen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Main Dashboard Screen
            composable(AppScreen.MainDashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToStudents = { safeNavigate(AppScreen.StudentsList) },
                    onNavigateToTeachers = { safeNavigate(AppScreen.TeachersList) },
                    onNavigateToAttendance = { safeNavigate(AppScreen.Attendance) },
                    onNavigateToFees = { safeNavigate(AppScreen.Fees) },
                    onNavigateToProfile = { navController.navigate(AppScreen.Profile.route) }
                )
            }

            // Students Directory Route (Phase 8 Student Management & Information System - SIS)
            composable(AppScreen.StudentsList.route) {
                BackHandler { safeNavigate(AppScreen.MainDashboard) }
                val studentListViewModel: StudentListViewModel = viewModel()
                StudentListScreen(
                    viewModel = studentListViewModel,
                    onNavigateBack = { safeNavigate(AppScreen.MainDashboard) }
                )
            }

            // Faculty & Staff Directory Route (Phase 9 Teacher & Staff Management)
            composable(AppScreen.TeachersList.route) {
                BackHandler { safeNavigate(AppScreen.MainDashboard) }
                val teacherListViewModel: TeacherListViewModel = viewModel()
                TeacherListScreen(
                    viewModel = teacherListViewModel,
                    onNavigateBack = { safeNavigate(AppScreen.MainDashboard) }
                )
            }

            // Attendance Route
            composable(AppScreen.Attendance.route) {
                BackHandler { safeNavigate(AppScreen.MainDashboard) }
                val dashboardViewModel: DashboardViewModel = viewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToStudents = { safeNavigate(AppScreen.StudentsList) },
                    onNavigateToAttendance = { safeNavigate(AppScreen.Attendance) },
                    onNavigateToFees = { safeNavigate(AppScreen.Fees) },
                    onNavigateToProfile = { navController.navigate(AppScreen.Profile.route) }
                )
            }

            // Fees Route
            composable(AppScreen.Fees.route) {
                BackHandler { safeNavigate(AppScreen.MainDashboard) }
                val dashboardViewModel: DashboardViewModel = viewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToStudents = { safeNavigate(AppScreen.StudentsList) },
                    onNavigateToAttendance = { safeNavigate(AppScreen.Attendance) },
                    onNavigateToFees = { safeNavigate(AppScreen.Fees) },
                    onNavigateToProfile = { navController.navigate(AppScreen.Profile.route) }
                )
            }

            // User Profile Screen
            composable(AppScreen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel()
                BackHandler {
                    navController.popBackStack()
                }
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSignedOut = {
                        navController.navigate(AppScreen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Settings Screen
            composable(AppScreen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel()
                BackHandler { safeNavigate(AppScreen.MainDashboard) }
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToFoundation = { navController.navigate(AppScreen.Foundation.route) },
                    onNavigateToDesignSystem = { navController.navigate(AppScreen.DesignSystem.route) },
                    onNavigateToTenantArchitecture = { navController.navigate(AppScreen.TenantArchitecture.route) },
                    onNavigateToRbacMatrix = { navController.navigate(AppScreen.RbacMatrix.route) }
                )
            }

            // Foundation Hub (Phase 1)
            composable(AppScreen.Foundation.route) {
                BackHandler { navController.popBackStack() }
                FoundationScreen(viewModel = foundationViewModel)
            }

            // Design System (Phase 2)
            composable(AppScreen.DesignSystem.route) {
                BackHandler { navController.popBackStack() }
                DesignSystemScreen(
                    viewModel = designSystemViewModel,
                    onBackToFoundation = { navController.popBackStack() }
                )
            }

            // Multi-Tenant Architecture Explorer (Phase 6)
            composable(AppScreen.TenantArchitecture.route) {
                val tenantViewModel: TenantArchitectureViewModel = viewModel()
                BackHandler { navController.popBackStack() }
                TenantArchitectureScreen(
                    viewModel = tenantViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Role-Based Access Control (RBAC) & Permission Engine (Phase 7)
            composable(AppScreen.RbacMatrix.route) {
                val rbacViewModel: RbacMatrixViewModel = viewModel()
                BackHandler { navController.popBackStack() }
                RbacMatrixScreen(
                    viewModel = rbacViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
