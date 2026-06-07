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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class HistoryEvent(
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val marker: String
)

@Composable
fun HistoryScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val glucoseList by viewModel.glucoseList.collectAsState()
    val mealsList by viewModel.mealsList.collectAsState()
    val injectionsList by viewModel.injectionsList.collectAsState()
    val events = remember(glucoseList, mealsList, injectionsList) {
        val glucoseEvents = glucoseList.map { entry ->
            HistoryEvent(
                title = "Глюкоза ${"%.1f".format(entry.glucoseLevel)} ммоль/л",
                subtitle = glucoseStatus(entry.glucoseLevel),
                timestamp = entry.timestamp,
                marker = "🩸"
            )
        }
        val mealEvents = mealsList.map { meal ->
            HistoryEvent(
                title = meal.mealName,
                subtitle = "${meal.carbs} г углеводов",
                timestamp = meal.timestamp,
                marker = "🍽"
            )
        }
        val injectionEvents = injectionsList.map { injection ->
            HistoryEvent(
                title = "Инсулин ${injection.insulinUnits.formatUnits()} ед.",
                subtitle = listOf(injection.injectionType, injection.insulinType, injection.notes)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                timestamp = injection.timestamp,
                marker = "💉"
            )
        }
        (glucoseEvents + mealEvents + injectionEvents).sortedByDescending { it.timestamp }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(contentPadding),
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                title = "История",
                subtitle = "Хронология значений глюкозы, приемов пищи и инсулина"
            )
        }

        if (events.isEmpty()) {
            item {
                EmptyCard(
                    title = "Пока нет событий",
                    subtitle = "Добавьте глюкозу, прием пищи или инъекцию на главном экране, чтобы увидеть их в истории."
                )
            }
        } else {
            items(events) { event ->
                HistoryEventCard(event = event)
            }
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = AppColors.TextDark,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = AppColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun HistoryEventCard(event: HistoryEvent, showTime: Boolean = true) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = event.marker, style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = event.title,
                    color = AppColors.TextDark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.subtitle,
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (showTime) {
                Text(
                    text = formatHistoryTime(event.timestamp),
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
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
                text = title,
                color = AppColors.TextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = subtitle, color = AppColors.TextSecondary)
        }
    }
}

private fun glucoseStatus(glucose: Float): String = when {
    glucose < 3.9f -> "Ниже целевого диапазона"
    glucose > 10f -> "Выше целевого диапазона"
    else -> "В целевом диапазоне"
}

private fun formatHistoryTime(timestamp: Long): String =
    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun Float.formatUnits(): String = if (this % 1f == 0f) {
    toInt().toString()
} else {
    "%.1f".format(this)
}