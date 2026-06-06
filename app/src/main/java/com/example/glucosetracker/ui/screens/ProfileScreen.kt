package com.example.glucosetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.ui.theme.AppColors

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    var units by remember { mutableStateOf("ммоль/л") }
    var dataSource by remember { mutableStateOf("Nightscout API") }
    var targetRange by remember { mutableStateOf("3.9–10.0") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(contentPadding),
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Профиль",
                    color = AppColors.TextDark,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Настройки диапазона, единиц и источника данных",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        item {
            SettingsCard(title = "Целевой диапазон", subtitle = "Диапазон для отчетов и карточек") {
                ChipRow(
                    options = listOf("3.9–10.0", "4.0–9.0", "4.4–8.5"),
                    selected = targetRange,
                    onSelected = { targetRange = it }
                )
            }
        }
        item {
            SettingsCard(title = "Единицы измерения", subtitle = "Как отображать значения глюкозы") {
                ChipRow(
                    options = listOf("ммоль/л", "mg/dL"),
                    selected = units,
                    onSelected = { units = it }
                )
            }
        }
        item {
            SettingsCard(title = "Источник данных", subtitle = "CGM/API для синхронизации") {
                ChipRow(
                    options = listOf("Nightscout API", "Room demo", "CGM вручную"),
                    selected = dataSource,
                    onSelected = { dataSource = it }
                )
            }
        }
        item {
            SettingsCard(title = "Текущее состояние", subtitle = "Временная state-модель профиля") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileRow(label = "Цель", value = "$targetRange $units")
                    ProfileRow(label = "Источник", value = dataSource)
                    ProfileRow(label = "Синхронизация", value = "При запуске приложения")
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    color = AppColors.TextDark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = subtitle, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            content()
        }
    }
}

@Composable
private fun ChipRow(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            Text(
                text = option,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AppColors.PrimaryGreen else AppColors.Background)
                    .clickable { onSelected(option) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                color = if (isSelected) AppColors.Card else AppColors.TextSecondary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = AppColors.TextSecondary)
        Text(text = value, color = AppColors.TextDark, fontWeight = FontWeight.Bold)
    }
}