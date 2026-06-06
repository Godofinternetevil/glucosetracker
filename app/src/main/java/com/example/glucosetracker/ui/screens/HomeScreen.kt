package com.example.glucosetracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.ui.components.CurrentGlucoseCard
import com.example.glucosetracker.ui.components.GlucoseChart
import com.example.glucosetracker.ui.components.StatsCard
import com.example.glucosetracker.ui.components.TodayEvent
import com.example.glucosetracker.ui.components.TodayEventType
import com.example.glucosetracker.ui.components.TodayEventsCard
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val glucoseList by viewModel.glucoseList.collectAsState()
    val mealsList by viewModel.mealsList.collectAsState()
    val sortedGlucose = glucoseList.sortedBy { it.timestamp }
    val currentEntry = sortedGlucose.lastOrNull()
    val currentGlucose = currentEntry?.glucoseLevel
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isAddSheetVisible by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(6) }
    val chartGlucose = remember(sortedGlucose, selectedRange) {
        val since = System.currentTimeMillis() - selectedRange.hoursInMillis()
        sortedGlucose.filter { it.timestamp >= since }.ifEmpty { sortedGlucose.takeLast(24) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(contentPadding),
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TopBar() }
        item {
            CurrentGlucoseCard(
                glucose = currentGlucose,
                trend = currentGlucose?.let { glucoseStatusText(it) } ?: "Данные не синхронизированы",
                updatedAt = currentEntry?.timestamp?.let { "Обновлено ${formatTime(it)}" } ?: "Добавьте или синхронизируйте значения"
            )
        }
        item {
            GlucoseChartCard(
                glucoseList = chartGlucose,
                selectedRange = selectedRange,
                onRangeSelected = { selectedRange = it }
            )
        }
        item {
            StatsCard(
                averageGlucose = averageGlucoseText(sortedGlucose),
                timeInRange = timeInRangeText(sortedGlucose),
                gmiEstimate = gmiEstimateText(sortedGlucose)
            )
        }
        item {
            TodayEventsCard(
                events = mealsList.map { meal ->
                    TodayEvent(
                        title = meal.mealName,
                        subtitle = "${meal.carbs} г углеводов",
                        timestamp = meal.timestamp,
                        type = TodayEventType.Meal
                    )
                },
                onAddClick = { isAddSheetVisible = true }
            )
        }
    }

    if (isAddSheetVisible) {
        AddEventBottomSheet(
            sheetState = sheetState,
            onDismiss = { isAddSheetVisible = false },
            onAddGlucose = viewModel::addGlucose,
            onAddMeal = viewModel::addMeal
        )
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircleIcon(text = "☰")
        Text(
            text = "CGM",
            color = AppColors.TextDark,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        CircleIcon(text = "🔔")
    }
}

@Composable
private fun CircleIcon(text: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(AppColors.Card, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = AppColors.TextDark)
    }
}

@Composable
private fun GlucoseChartCard(
    glucoseList: List<GlucoseEntry>,
    selectedRange: Int,
    onRangeSelected: (Int) -> Unit
) {
    val ranges = listOf(3, 6, 12, 24)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Сегодня",
                    color = AppColors.TextDark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Цель 3.9–10",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ranges.forEach { range ->
                    RangeChip(
                        text = "$range ч",
                        selected = range == selectedRange,
                        onClick = { onRangeSelected(range) }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .padding(top = 16.dp)
            ) {
                TargetRangeBand(modifier = Modifier.matchParentSize())
                GlucoseChart(
                    glucoseList = glucoseList,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
private fun RangeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) AppColors.BlueAccent else AppColors.Background
    val content = if (selected) AppColors.Card else AppColors.TextSecondary
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        color = content,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun TargetRangeBand(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val top = size.height * 0.28f
        val height = size.height * 0.44f
        drawRoundRect(
            color = AppColors.PrimaryGreen.copy(alpha = 0.12f),
            topLeft = Offset(0f, top),
            size = Size(size.width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )
        drawLine(
            color = AppColors.PrimaryGreen.copy(alpha = 0.5f),
            start = Offset(0f, top),
            end = Offset(size.width, top),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = AppColors.PrimaryGreen.copy(alpha = 0.5f),
            start = Offset(0f, top + height),
            end = Offset(size.width, top + height),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAddGlucose: (Float) -> Unit,
    onAddMeal: (String, Int) -> Unit
) {
    var glucoseInput by remember { mutableStateOf("") }
    var mealName by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Добавить событие",
                color = AppColors.TextDark,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = glucoseInput,
                onValueChange = { glucoseInput = it },
                label = { Text("Глюкоза, ммоль/л") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    glucoseInput.toFloatOrNull()?.let(onAddGlucose)
                    glucoseInput = ""
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить глюкозу")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = mealName,
                onValueChange = { mealName = it },
                label = { Text("Еда") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = carbsInput,
                onValueChange = { carbsInput = it },
                label = { Text("Углеводы, г") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val carbs = carbsInput.toIntOrNull()
                    if (mealName.isNotBlank() && carbs != null) {
                        onAddMeal(mealName.trim(), carbs)
                        mealName = ""
                        carbsInput = ""
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить прием пищи")
            }
        }
    }
}

private fun glucoseStatusText(glucose: Float): String = when {
    glucose < 3.9f -> "Ниже диапазона"
    glucose > 10f -> "Выше диапазона"
    else -> "В целевом диапазоне"
}

private fun formatTime(timestamp: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun averageGlucoseText(glucoseList: List<GlucoseEntry>): String {
    if (glucoseList.isEmpty()) return "--"
    return "%.1f".format(glucoseList.map { it.glucoseLevel }.average())
}

private fun timeInRangeText(glucoseList: List<GlucoseEntry>): String {
    if (glucoseList.isEmpty()) return "--"
    val inRangeCount = glucoseList.count { it.glucoseLevel in 3.9f..10f }
    return "${((inRangeCount.toFloat() / glucoseList.size) * 100).roundToInt()}%"
}

private fun gmiEstimateText(glucoseList: List<GlucoseEntry>): String {
    if (glucoseList.isEmpty()) return "--"
    val averageMgDl = glucoseList.map { it.glucoseLevel }.average() * 18.0182
    val gmi = 3.31 + (0.02392 * averageMgDl)
    return "%.1f%%".format(gmi)
}

private fun Int.hoursInMillis(): Long = this * 60L * 60L * 1000L