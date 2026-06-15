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
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.data.export.DatasetExporter
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.viewmodel.HomeViewModel
import java.util.Calendar
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exportFormat by remember { mutableStateOf(DatasetExporter.Format.CSV) }
    var exportRange by remember { mutableStateOf(ExportRange.Last7Days) }
    val createExportDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(exportFormat.mimeType)
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val (start, end) = exportRange.toMillisRange()
        scope.launch {
            val glucose = glucoseList.filter { it.timestamp in start..end }
            val meals = viewModel.mealsList.value.filter { it.timestamp in start..end }
            val insulin = viewModel.insulinList.value.filter { it.timestamp in start..end }
            val payload = when (exportFormat) {
                DatasetExporter.Format.CSV -> DatasetExporter().exportCsv(glucose, meals, insulin)
                DatasetExporter.Format.JSONL -> DatasetExporter().exportJsonl(glucose, meals, insulin)
            }
            val message: CharSequence = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(payload.toByteArray(Charsets.UTF_8))
                } ?: error("Не удалось открыть файл для записи")
            }.fold(
                onSuccess = { "Данные экспортированы" },
                onFailure = { error -> "Ошибка экспорта: ${error.localizedMessage ?: error.message}" }
            )
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

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
            ExportDataCard(
                selectedFormat = exportFormat,
                onFormatSelected = { exportFormat = it },
                selectedRange = exportRange,
                onRangeSelected = { exportRange = it },
                onExportClick = {
                    val fileName = "glucose_dataset_${exportRange.fileSuffix}.${exportFormat.extension}"
                    createExportDocument.launch(fileName)
                }
            )
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

private enum class ExportRange(val label: String, val fileSuffix: String) {
    Today("Сегодня", "today"),
    Last7Days("7 дней", "7d"),
    Last30Days("30 дней", "30d"),
    All("Все данные", "all");

    fun toMillisRange(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val start = when (this) {
            Today -> startOfDayMillis()
            Last7Days -> now - 7.daysInMillis()
            Last30Days -> now - 30.daysInMillis()
            All -> 0L
        }
        return start to now
    }
}

@Composable
private fun ExportDataCard(
    selectedFormat: DatasetExporter.Format,
    onFormatSelected: (DatasetExporter.Format) -> Unit,
    selectedRange: ExportRange,
    onRangeSelected: (ExportRange) -> Unit,
    onExportClick: () -> Unit
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
            Text(
                text = "Экспорт данных",
                color = AppColors.TextDark,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Сохраните объединённый датасет glucose/meal/insulin через системный выбор файла.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatasetExporter.Format.entries.forEach { format ->
                    FilterChip(
                        selected = selectedFormat == format,
                        onClick = { onFormatSelected(format) },
                        label = { Text(format.name) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportRange.entries.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onRangeSelected(range) },
                        label = { Text(range.label) }
                    )
                }
            }
            Button(onClick = onExportClick, modifier = Modifier.fillMaxWidth()) {
                Text("Экспорт данных")
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
    val values = entries.map { it.glucoseMmolL }
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