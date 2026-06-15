package com.example.glucosetracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InjectionEntry
import com.example.glucosetracker.data.local.entities.MealEntry
import com.example.glucosetracker.data.local.entities.InsulinEntry
import com.example.glucosetracker.ui.components.CurrentGlucoseCard
import com.example.glucosetracker.ui.components.GlucoseChart
import com.example.glucosetracker.ui.components.StatsCard
import com.example.glucosetracker.ui.components.TodayEvent
import com.example.glucosetracker.ui.components.TodayEventType
import com.example.glucosetracker.ui.components.TodayEventsCard
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.ui.theme.AppDimens
import com.example.glucosetracker.viewmodel.HomeViewModel
import com.example.glucosetracker.viewmodel.SyncState
import com.example.glucosetracker.viewmodel.SyncStatus
import java.text.SimpleDateFormat
import java.util.Calendar
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
    val injectionsList by viewModel.injectionsList.collectAsState()
    val insulinList by viewModel.insulinList.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val sortedGlucose = glucoseList.sortedBy { it.timestamp }
    val currentEntry = sortedGlucose.lastOrNull()
    val currentGlucose = currentEntry?.glucoseMmolL
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
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TopBar() }
        item { SyncStatusCard(syncState = syncState, onSyncClick = viewModel::syncGlucose) }
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
                events = todayEvents(
                    mealsList = mealsList,
                    injectionsList = injectionsList,
                    insulinList = insulinList
                ),
                onAddClick = { isAddSheetVisible = true }
            )
        }
    }

    if (isAddSheetVisible) {
        AddEventBottomSheet(
            sheetState = sheetState,
            onDismiss = { isAddSheetVisible = false },
            onAddGlucose = viewModel::addGlucose,
            onAddMeal = viewModel::addMeal,
            onAddInsulin = viewModel::addInsulin
        )
    }
}

@Composable
private fun SyncStatusCard(syncState: SyncState, onSyncClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Синхронизация: ${syncState.status.homeLabel()}",
                    color = AppColors.TextDark,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = syncState.errorText ?: syncState.lastSyncAt?.let { "Последняя: ${formatTime(it)}" } ?: "Еще не выполнялась",
                    color = if (syncState.status == SyncStatus.Error) AppColors.Danger else AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = onSyncClick,
                enabled = syncState.status != SyncStatus.Loading
            ) {
                Text(if (syncState.status == SyncStatus.Loading) "…" else "Синхронизировать")
            }
        }
    }
}

private fun SyncStatus.homeLabel(): String = when (this) {
    SyncStatus.Idle -> "idle"
    SyncStatus.Loading -> "loading"
    SyncStatus.Success -> "success"
    SyncStatus.Error -> "error"
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircleIcon(text = "≡")
        Text(
            text = "CGM",
            color = AppColors.TextDark,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        CircleIcon(text = "•")
    }
}

@Composable
private fun CircleIcon(text: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(AppColors.Card, CircleShape)
            .border(BorderStroke(AppDimens.SoftBorderWidth, AppColors.Border), CircleShape),
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(AppDimens.SoftBorderWidth, AppColors.Border), RoundedCornerShape(AppDimens.SmallCardRadius)),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.CardElevation),
        shape = RoundedCornerShape(AppDimens.SmallCardRadius)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Динамика глюкозы",
                    color = AppColors.TextDark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Цель 3.9–10.0",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            GlucoseChart(
                glucoseList = glucoseList,
                selectedRangeHours = selectedRange,
                onRangeChanged = onRangeSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAddGlucose: (Float) -> Unit,
    onAddMeal: (String, Float, Float?, Float?, Int?, String, String, String) -> Unit,
    onAddInsulin: (Float, String, String, String) -> Unit
) {
    val tabs = listOf("Глюкоза", "Еда", "Инсулин")
    var selectedTab by remember { mutableStateOf(0) }
    var glucoseInput by remember { mutableStateOf("") }
    var mealName by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(MealEntry.TYPE_SNACK) }
    var mealNote by remember { mutableStateOf("") }
    var insulinUnitsInput by remember { mutableStateOf("") }
    var insulinType by remember { mutableStateOf(InsulinEntry.TYPE_RAPID) }
    var notes by remember { mutableStateOf("") }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    SectionTab(
                        title = title,
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when (selectedTab) {
                0 -> GlucoseSection(
                    glucoseInput = glucoseInput,
                    onGlucoseInputChange = { glucoseInput = it },
                    onSave = {
                        glucoseInput.toFloatOrNull()?.let { level ->
                            onAddGlucose(level)
                            glucoseInput = ""
                            onDismiss()
                        }
                    }
                )

                1 -> MealSection(
                    mealName = mealName,
                    onMealNameChange = { mealName = it },
                    carbsInput = carbsInput,
                    onCarbsInputChange = { carbsInput = it },
                    proteinInput = proteinInput,
                    onProteinInputChange = { proteinInput = it },
                    fatInput = fatInput,
                    onFatInputChange = { fatInput = it },
                    caloriesInput = caloriesInput,
                    onCaloriesInputChange = { caloriesInput = it },
                    mealType = mealType,
                    onMealTypeChange = { mealType = it },
                    note = mealNote,
                    onNoteChange = { mealNote = it },
                    onSave = {
                        val carbs = carbsInput.toFloatOrNull()
                        val protein = proteinInput.toFloatOrNull()
                        val fat = fatInput.toFloatOrNull()
                        val calories = caloriesInput.toIntOrNull()
                        if (mealName.isNotBlank() && carbs != null) {
                            onAddMeal(
                                mealName.trim(),
                                carbs,
                                protein,
                                fat,
                                calories,
                                mealType,
                                mealNote.trim(),
                                DataSourceConfig.SOURCE_MANUAL
                            )
                            mealName = ""
                            carbsInput = ""
                            proteinInput = ""
                            fatInput = ""
                            caloriesInput = ""
                            mealType = MealEntry.TYPE_SNACK
                            mealNote = ""
                            onDismiss()
                        }
                    }
                )

                2 -> InsulinSection(
                    insulinUnitsInput = insulinUnitsInput,
                    onInsulinUnitsInputChange = { insulinUnitsInput = it },
                    insulinType = insulinType,
                    onInsulinTypeChange = { insulinType = it },
                    notes = notes,
                    onNotesChange = { notes = it },
                    onSave = {
                        val units = insulinUnitsInput.toFloatOrNull()
                        if (units != null && insulinType.isNotBlank()) {
                            onAddInsulin(
                                units,
                                insulinType,
                                notes.trim(),
                                DataSourceConfig.SOURCE_MANUAL
                            )
                            insulinUnitsInput = ""
                            insulinType = InsulinEntry.TYPE_RAPID
                            notes = ""
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) AppColors.BlueAccent else AppColors.BlueAccentSoft,
            contentColor = if (selected) AppColors.Card else AppColors.BlueAccent
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(title)
    }
}

@Composable
private fun GlucoseSection(
    glucoseInput: String,
    onGlucoseInputChange: (String) -> Unit,
    onSave: () -> Unit
) {
    OutlinedTextField(
        value = glucoseInput,
        onValueChange = onGlucoseInputChange,
        label = { Text("Глюкоза, ммоль/л") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Сохранить глюкозу")
    }
}

@Composable
private fun MealSection(
    mealName: String,
    onMealNameChange: (String) -> Unit,
    carbsInput: String,
    onCarbsInputChange: (String) -> Unit,
    proteinInput: String,
    onProteinInputChange: (String) -> Unit,
    fatInput: String,
    onFatInputChange: (String) -> Unit,
    caloriesInput: String,
    onCaloriesInputChange: (String) -> Unit,
    mealType: String,
    onMealTypeChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    OutlinedTextField(
        value = mealName,
        onValueChange = onMealNameChange,
        label = { Text("Еда") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = carbsInput,
        onValueChange = onCarbsInputChange,
        label = { Text("Углеводы, г") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = "Тип приема пищи",
        color = AppColors.TextSecondary,
        style = MaterialTheme.typography.bodySmall
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MealEntry.SUPPORTED_TYPES.take(2).forEach { type ->
            SectionTab(
                title = type.mealTypeLabel(),
                selected = mealType == type,
                onClick = { onMealTypeChange(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MealEntry.SUPPORTED_TYPES.drop(2).forEach { type ->
            SectionTab(
                title = type.mealTypeLabel(),
                selected = mealType == type,
                onClick = { onMealTypeChange(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
    OutlinedTextField(
        value = carbsInput,
        onValueChange = onCarbsInputChange,
        label = { Text("Углеводы, г") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = proteinInput,
            onValueChange = onProteinInputChange,
            label = { Text("Белки, г") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = fatInput,
            onValueChange = onFatInputChange,
            label = { Text("Жиры, г") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
    OutlinedTextField(
        value = caloriesInput,
        onValueChange = onCaloriesInputChange,
        label = { Text("Калории") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text("Заметка") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Добавить прием пищи")
    }
}

@Composable
private fun InsulinSection(
    insulinUnitsInput: String,
    onInsulinUnitsInputChange: (String) -> Unit,
    insulinType: String,
    onInsulinTypeChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    OutlinedTextField(
        value = insulinUnitsInput,
        onValueChange = onInsulinUnitsInputChange,
        label = { Text("Единицы инсулина") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = "Тип инсулина",
        color = AppColors.TextSecondary,
        style = MaterialTheme.typography.bodySmall
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InsulinEntry.SUPPORTED_TYPES.take(3).forEach { type ->
            SectionTab(
                title = type.insulinTypeLabel(),
                selected = insulinType == type,
                onClick = { onInsulinTypeChange(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InsulinEntry.SUPPORTED_TYPES.drop(3).forEach { type ->
            SectionTab(
                title = type.insulinTypeLabel(),
                selected = insulinType == type,
                onClick = { onInsulinTypeChange(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Заметки") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Добавить инъекцию")
    }
}

private fun todayEvents(
    mealsList: List<MealEntry>,
    injectionsList: List<InjectionEntry>,
    insulinList: List<InsulinEntry>
): List<TodayEvent> {
    val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val mealEvents = mealsList.filter { it.timestamp >= startOfDay }.map { meal ->
        TodayEvent(
            title = meal.mealName,
            subtitle = meal.nutritionSummary(),
            timestamp = meal.timestamp,
            type = TodayEventType.Meal
        )
    }
    val injectionEvents = injectionsList.filter { it.timestamp >= startOfDay }.map { injection ->
        TodayEvent(
            title = "${injection.insulinUnits.formatUnits()} ед. • ${injection.injectionType}",
            subtitle = listOf(injection.insulinType, injection.notes)
                .filter { it.isNotBlank() }
                .joinToString(" • "),
            timestamp = injection.timestamp,
            type = TodayEventType.Injection
        )
    }

    val insulinEvents = insulinList.filter { it.timestamp >= startOfDay }.map { insulin ->
        TodayEvent(
            title = "${insulin.units.formatUnits()} ед. • ${insulin.insulinType.insulinTypeLabel()}",
            subtitle = listOf(insulin.source, insulin.note)
                .filter { it.isNotBlank() }
                .joinToString(" • "),
            timestamp = insulin.timestamp,
            type = TodayEventType.Injection
        )
    }

    return mealEvents + injectionEvents + insulinEvents
}

private fun String.insulinTypeLabel(): String = when (this) {
    InsulinEntry.TYPE_RAPID -> "Быстрый"
    InsulinEntry.TYPE_SHORT -> "Короткий"
    InsulinEntry.TYPE_LONG -> "Длинный"
    InsulinEntry.TYPE_BASAL -> "Базальный"
    InsulinEntry.TYPE_CORRECTION -> "Коррекция"
    else -> this
}

private fun Float.formatUnits(): String = if (this % 1f == 0f) {
    toInt().toString()
} else {
    "%.1f".format(this)
}

private fun glucoseStatusText(glucose: Float): String = when {
    glucose < 3.9f -> "Ниже диапазона"
    glucose > 10f -> "Выше диапазона"
    else -> "В целевом диапазоне"
}

private fun formatTime(timestamp: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun averageGlucoseText(glucoseList: List<GlucoseEntry>): String {
    if (glucoseList.isEmpty()) return "--"
    return "%.1f".format(glucoseList.map { it.glucoseMmolL }.average())
}

private fun timeInRangeText(glucoseList: List<GlucoseEntry>): String {
    if (glucoseList.isEmpty()) return "--"
    val inRangeCount = glucoseList.count { it.glucoseMmolL in 3.9f..10f }
    return "${((inRangeCount.toFloat() / glucoseList.size) * 100).roundToInt()}%"
}

private fun gmiEstimateText(glucoseList: List<GlucoseEntry>): String {
    if (glucoseList.isEmpty()) return "--"
    val averageMgDl = glucoseList.map { it.glucoseMmolL }.average() * 18.0182
    val gmi = 3.31 + (0.02392 * averageMgDl)
    return "%.1f%%".format(gmi)
}

private fun Int.hoursInMillis(): Long = this * 60L * 60L * 1000L

private fun String.mealTypeLabel(): String = when (this) {
    MealEntry.TYPE_BREAKFAST -> "Завтрак"
    MealEntry.TYPE_LUNCH -> "Обед"
    MealEntry.TYPE_DINNER -> "Ужин"
    MealEntry.TYPE_SNACK -> "Перекус"
    else -> this
}

private fun MealEntry.nutritionSummary(): String = listOfNotNull(
    mealType.mealTypeLabel(),
    "${carbsGrams.formatGrams()} г углеводов",
    proteinGrams?.let { "${it.formatGrams()} г белков" },
    fatGrams?.let { "${it.formatGrams()} г жиров" },
    calories?.let { "$it ккал" },
    note.takeIf { it.isNotBlank() },
    source.takeIf { it.isNotBlank() }
).joinToString(" • ")

private fun Float.formatGrams(): String = if (this % 1f == 0f) {
    toInt().toString()
} else {
    "%.1f".format(this)
}