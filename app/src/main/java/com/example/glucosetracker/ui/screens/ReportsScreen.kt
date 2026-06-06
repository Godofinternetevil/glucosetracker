package com.example.glucosetracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.viewmodel.HomeViewModel
import java.util.Calendar
import kotlin.math.roundToInt

private data class ReportSummary(
    val average: String,
    val timeInRange: String,
    val minMax: String,
    val count: String
)

@Composable
fun ReportsScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val glucoseList by viewModel.glucoseList.collectAsState()
    val dayStart = remember { startOfDayMillis() }
    val weekStart = remember { System.currentTimeMillis() - 7.daysInMillis() }
    val daySummary = remember(glucoseList, dayStart) { glucoseList.summarySince(dayStart) }
    val weekSummary = remember(glucoseList, weekStart) { glucoseList.summarySince(weekStart) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(contentPadding),
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ReportsHeader()
        }
        item {
            ReportCard(title = "Сегодня", summary = daySummary)
        }
        item {
            ReportCard(title = "Последние 7 дней", summary = weekSummary)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.Card),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Целевой диапазон",
                        color = AppColors.TextDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Time-in-range считается для 3.9–10.0 ммоль/л и обновляется из локальных данных Room.",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportsHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Отчёты",
            color = AppColors.TextDark,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Средняя глюкоза, time-in-range, минимум и максимум",
            color = AppColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ReportCard(title: String, summary: ReportSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                color = AppColors.TextDark,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(label = "Средняя", value = summary.average, modifier = Modifier.weight(1f))
                MetricTile(label = "В диапазоне", value = summary.timeInRange, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(label = "Мин / макс", value = summary.minMax, modifier = Modifier.weight(1f))
                MetricTile(label = "Измерений", value = summary.count, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(AppColors.Background, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = label, color = AppColors.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value,
            color = AppColors.TextDark,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun List<GlucoseEntry>.summarySince(since: Long): ReportSummary {
    val entries = filter { it.timestamp >= since }
    if (entries.isEmpty()) {
        return ReportSummary(average = "--", timeInRange = "--", minMax = "--", count = "0")
    }
    val values = entries.map { it.glucoseLevel }
    val average = values.average()
    val inRange = values.count { it in 3.9f..10f }
    val min = values.minOrNull() ?: 0f
    val max = values.maxOrNull() ?: 0f
    return ReportSummary(
        average = "%.1f".format(average),
        timeInRange = "${((inRange.toFloat() / values.size) * 100).roundToInt()}%",
        minMax = "%.1f / %.1f".format(min, max),
        count = values.size.toString()
    )
}

private fun startOfDayMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun Int.daysInMillis(): Long = this * 24L * 60L * 60L * 1000L