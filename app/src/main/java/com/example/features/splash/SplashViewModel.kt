package com.example.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.TenantContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _targetRoute = MutableStateFlow("onboarding")
    val targetRoute: StateFlow<String> = _targetRoute.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Simulate session & tenant initialization
            delay(1500)
            val hasActiveTenant = TenantContext.activeSchoolId.value != null
            _targetRoute.value = if (hasActiveTenant) "main_dashboard" else "onboarding"
            _isReady.value = true
        }
    }
}
