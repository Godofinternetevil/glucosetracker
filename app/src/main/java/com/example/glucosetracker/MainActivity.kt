package com.example.glucosetracker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.example.glucosetracker.ui.screens.HomeScreen
import com.example.glucosetracker.viewmodel.HomeViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val composeView = findViewById<ComposeView>(R.id.composeView)

        composeView.setContent {
            HomeScreen(viewModel)
        }
    }
}