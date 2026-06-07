package com.example.glucosetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.ui.theme.AppDimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayEventsCard(
    events: List<TodayEvent>,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                    text = "Сегодняшние события",
                    color = AppColors.TextDark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.BlueAccent),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Добавить +")
                }
            }

            if (events.isEmpty()) {
                Text(
                    text = "Пока нет событий еды или инъекций",
                    modifier = Modifier.padding(top = 16.dp),
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                events.sortedByDescending { it.timestamp }.take(5).forEach { event ->
                    EventRow(
                        event = event,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: TodayEvent, modifier: Modifier = Modifier) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val tint = if (event.type == TodayEventType.Injection) AppColors.PrimaryGreen else AppColors.BlueAccent
    val tintBackground = if (event.type == TodayEventType.Injection) AppColors.PrimaryGreenSoft else AppColors.BlueAccentSoft

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(tintBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = event.type.icon,
                color = tint,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeFormatter.format(Date(event.timestamp)),
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "  •  ${event.type.label}",
                    color = tint,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = event.title,
                color = AppColors.TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = event.subtitle,
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "›",
            color = AppColors.TextMuted,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

data class TodayEvent(
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val type: TodayEventType
)

enum class TodayEventType(val label: String, val icon: String) {
    Meal("Еда", "🍽"),
    Injection("Инъекция", "💉")
}