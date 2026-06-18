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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.viewmodel.HomeViewModel
import com.example.glucosetracker.viewmodel.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val config by viewModel.dataSourceConfig.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val configError by viewModel.configError.collectAsState()

    var units by remember { mutableStateOf(DataSourceConfig.UNIT_MMOL_L) }
    var dataSource by remember { mutableStateOf(DataSourceConfig.SOURCE_MANUAL) }
    var targetRange by remember { mutableStateOf("3.9–10.0") }
    var nightscoutBaseUrl by remember { mutableStateOf("") }
    var nightscoutToken by remember { mutableStateOf("") }
    var xDripBaseUrl by remember { mutableStateOf("") }
    var xDripToken by remember { mutableStateOf("") }
    var otherApiBaseUrl by remember { mutableStateOf("") }
    var otherApiToken by remember { mutableStateOf("") }
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var exportPreview by remember { mutableStateOf("") }

    LaunchedEffect(config) {
        units = config.sourceUnits
        dataSource = config.sourceType.ifBlank { DataSourceConfig.SOURCE_MANUAL }
        targetRange = targetRange.ifBlank { "3.9–10.0" }
        nightscoutBaseUrl = config.nightscoutBaseUrl.ifBlank { config.baseUrl }
        nightscoutToken = config.nightscoutToken.ifBlank { config.apiSecret }
        xDripBaseUrl = config.xDripBaseUrl
        xDripToken = config.xDripToken
        otherApiBaseUrl = config.otherApiBaseUrl
        otherApiToken = config.otherApiToken
        autoSyncEnabled = config.autoSyncEnabled
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
            SettingsCard(title = "Единицы источника", subtitle = "В UI значения всегда отображаются в ммоль/л") {
                ChipRow(
                    options = listOf(DataSourceConfig.UNIT_MMOL_L, DataSourceConfig.UNIT_MG_DL),
                    selected = units,
                    onSelected = { units = it }
                )
            }
        }
        item {
            SettingsCard(title = "Режим подключения", subtitle = "Manual, Nightscout, xDrip bridge или O") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChipRow(
                        options = listOf(
                            DataSourceConfig.SOURCE_MANUAL,
                            DataSourceConfig.SOURCE_NIGHTSCOUT,
                            DataSourceConfig.SOURCE_XDRIP_BRIDGE,
                            DataSourceConfig.SOURCE_OTHER_API
                        ),
                        selected = dataSource,
                        onSelected = { dataSource = it }
                    )

                    when (dataSource) {
                        DataSourceConfig.SOURCE_NIGHTSCOUT -> SourceSettingsFields(
                            label = "Nightscout",
                            url = nightscoutBaseUrl,
                            token = nightscoutToken,
                            onUrlChange = { nightscoutBaseUrl = it },
                            onTokenChange = { nightscoutToken = it }
                        )

                        DataSourceConfig.SOURCE_XDRIP_BRIDGE -> SourceSettingsFields(
                            label = "xDrip bridge",
                            url = xDripBaseUrl,
                            token = xDripToken,
                            onUrlChange = { xDripBaseUrl = it },
                            onTokenChange = { xDripToken = it }
                        )

                        DataSourceConfig.SOURCE_OTHER_API -> SourceSettingsFields(
                            label = "Other API",
                            url = otherApiBaseUrl,
                            token = otherApiToken,
                            onUrlChange = { otherApiBaseUrl = it },
                            onTokenChange = { otherApiToken = it }
                        )

                        else -> Text(
                            text = "Manual режим не требует URL и токена. Используйте его для локального ввода значений.",
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "URL должен начинаться с http:// или https://, а токен нужен для удаленной синхронизации.",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Автосинхронизация",
                            color = AppColors.TextDark,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(checked = autoSyncEnabled, onCheckedChange = { autoSyncEnabled = it })
                    }
                }
            }
        }
        item {
            SettingsCard(title = "Синхронизация", subtitle = "Ручной запуск и состояние последней попытки") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileRow(label = "Статус", value = syncState.status.label())
                    ProfileRow(
                        label = "Последняя синхронизация",
                        value = syncState.lastSyncAt?.let { formatDateTime(it) } ?: "—"
                    )
                    syncState.errorText?.let { error ->
                        Text(text = error, color = AppColors.Danger, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = {
                            viewModel.saveDataSourceConfig(
                                config.copy(
                                    sourceType = dataSource,
                                    sourceUnits = units,
                                    autoSyncEnabled = autoSyncEnabled,
                                    nightscoutBaseUrl = nightscoutBaseUrl.trim(),
                                    nightscoutToken = nightscoutToken.trim(),
                                    xDripBaseUrl = xDripBaseUrl.trim(),
                                    xDripToken = xDripToken.trim(),
                                    otherApiBaseUrl = otherApiBaseUrl.trim(),
                                    otherApiToken = otherApiToken.trim()
                                )
                            )
                        },
                        enabled = syncState.status != SyncStatus.Loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сохранить источник")
                    }
                    Button(
                        onClick = { viewModel.syncGlucose(force = true) },
                        enabled = syncState.status != SyncStatus.Loading && dataSource != DataSourceConfig.SOURCE_MANUAL,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Синхронизировать сейчас")
                    }
                    configError?.let { error ->
                        Text(text = error, color = AppColors.Danger, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            SettingsCard(title = "Экспорт", subtitle = "CSV с расширенными полями еды для ML") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { exportPreview = viewModel.exportCsv() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сформировать CSV")
                    }
                    if (exportPreview.isNotBlank()) {
                        Text(
                            text = exportPreview.lineSequence().take(6).joinToString("\n"),
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        item {
            SettingsCard(title = "Текущее состояние", subtitle = "Сохраненная конфигурация профиля") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileRow(label = "Цель", value = "$targetRange ммоль/л")
                    ProfileRow(label = "Источник", value = config.sourceType)
                    ProfileRow(label = "Режим", value = config.connectionMode.ifBlank { dataSource })
                    ProfileRow(label = "Единицы источника", value = config.sourceUnits)
                    ProfileRow(label = "URL", value = config.activeBaseUrl().ifBlank { "—" })
                }
            }
        }
    }
}

@Composable
private fun SourceSettingsFields(
    label: String,
    url: String,
    token: String,
    onUrlChange: (String) -> Unit,
    onTokenChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "$label connection",
            color = AppColors.TextDark,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL") },
            placeholder = { Text("https://example.com") },
            singleLine = true
        )
        OutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Token / API secret") },
            placeholder = { Text("••••••••••••") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
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

private fun SyncStatus.label(): String = when (this) {
    SyncStatus.Idle -> "idle"
    SyncStatus.Loading -> "loading"
    SyncStatus.Success -> "success"
    SyncStatus.Error -> "error"
}

private fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}