package com.example.glucosetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glucosetracker.ui.screens.HomeScreen
import com.example.glucosetracker.ui.theme.GlucoseTrackerTheme
import com.example.glucosetracker.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlucoseTrackerApp()
        }
    }
}

@androidx.compose.runtime.Composable
private fun GlucoseTrackerApp() {
    val homeViewModel: HomeViewModel = viewModel()

    GlucoseTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(viewModel = homeViewModel)
        }
    }
}