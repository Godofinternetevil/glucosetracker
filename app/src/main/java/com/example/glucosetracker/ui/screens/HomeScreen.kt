package com.example.glucosetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.ui.components.GlucoseChart
import com.example.glucosetracker.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val glucoseList by viewModel.glucoseList.collectAsState()
    val mealsList by viewModel.mealsList.collectAsState()

    var glucoseInput by remember { mutableStateOf("") }
    var mealName by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }

    val currentGlucose = glucoseList.lastOrNull()?.glucoseLevel

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "CGM",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentGlucose?.let { "%.1f ммоль/л".format(it) } ?: "Нет данных",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentGlucose?.let { glucoseStatusText(it) } ?: "Добавьте или синхронизируйте значения"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            GlucoseChart(
                glucoseList = glucoseList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Добавить уровень глюкозы")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = glucoseInput,
                        onValueChange = { glucoseInput = it },
                        label = { Text("ммоль/л") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val value = glucoseInput.toFloatOrNull()
                            if (value != null) {
                                viewModel.addGlucose(value)
                                glucoseInput = ""
                            }
                        }
                    ) {
                        Text("Сохранить")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Добавить прием пищи")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mealName,
                        onValueChange = { mealName = it },
                        label = { Text("Название еды") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = carbsInput,
                        onValueChange = { carbsInput = it },
                        label = { Text("Углеводы") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val carbs = carbsInput.toIntOrNull()
                            if (mealName.isNotBlank() && carbs != null) {
                                viewModel.addMeal(mealName.trim(), carbs)
                                mealName = ""
                                carbsInput = ""
                            }
                        }
                    ) {
                        Text("Добавить")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Последние приемы пищи",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(mealsList) { meal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = meal.mealName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = "${meal.carbs} г углеводов")
                    }
                }
            }
        }
    }
}

private fun glucoseStatusText(glucose: Float): String = when {
    glucose < 3.9f -> "Глюкоза ниже диапазона"
    glucose > 10f -> "Глюкоза выше диапазона"
    else -> "Глюкоза в нормальном диапазоне"
}