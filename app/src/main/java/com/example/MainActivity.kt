package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.core.navigation.AppNavGraph
import com.example.core.theme.EduManageTheme
import com.example.features.designsystem.DesignSystemViewModel
import com.example.features.foundation.FoundationViewModel

class MainActivity : ComponentActivity() {
    private val foundationViewModel: FoundationViewModel by viewModels()
    private val designSystemViewModel: DesignSystemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val foundationState by foundationViewModel.uiState.collectAsState()
            val designState by designSystemViewModel.uiState.collectAsState()
            val isDark = foundationState.isDarkMode || designState.isDarkMode || isSystemInDarkTheme()

            EduManageTheme(darkTheme = isDark) {
                AppNavGraph(
                    navController = navController,
                    foundationViewModel = foundationViewModel,
                    designSystemViewModel = designSystemViewModel
                )
            }
        }
    }
}
