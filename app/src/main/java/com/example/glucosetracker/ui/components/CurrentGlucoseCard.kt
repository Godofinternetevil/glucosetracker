package com.example.glucosetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.ui.theme.AppDimens

@Composable
fun CurrentGlucoseCard(
    glucose: Float?,
    trend: String,
    updatedAt: String,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        glucose == null -> AppColors.TextSecondary
        glucose < LOW_GLUCOSE -> AppColors.Danger
        glucose > HIGH_GLUCOSE -> AppColors.HighGlucose
        else -> AppColors.PrimaryGreen
    }
    val statusTitle = when {
        glucose == null -> "Нет данных"
        glucose < LOW_GLUCOSE -> "Ниже цели"
        glucose > HIGH_GLUCOSE -> "Выше цели"
        else -> "Стабильно"
    }
    val rangeSubtitle = when {
        glucose == null -> "Добавьте измерение, чтобы увидеть статус"
        glucose in LOW_GLUCOSE..HIGH_GLUCOSE -> "Глюкоза в пределах целевого диапазона"
        else -> trend
    }
    val progress = glucose?.let { (it / MAX_RING_GLUCOSE).coerceIn(0f, 1f) } ?: 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(AppDimens.SoftBorderWidth, AppColors.Border), RoundedCornerShape(AppDimens.CardRadius)),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.CardElevation),
        shape = RoundedCornerShape(AppDimens.CardRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlucoseRing(
                glucose = glucose,
                progress = progress,
                statusColor = statusColor,
                modifier = Modifier.size(136.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(statusColor, CircleShape)
                    )
                    Text(
                        text = statusTitle,
                        modifier = Modifier.padding(start = 8.dp),
                        color = AppColors.TextDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(AppColors.PrimaryGreenSoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trendArrow(glucose),
                            color = statusColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Тренд",
                        modifier = Modifier.padding(start = 10.dp),
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = rangeSubtitle,
                    color = AppColors.TextDark,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = updatedAt,
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun GlucoseRing(
    glucose: Float?,
    progress: Float,
    statusColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)

            drawArc(
                color = AppColors.ChartGrid,
                startAngle = -220f,
                sweepAngle = 320f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
            drawArc(
                color = statusColor,
                startAngle = -220f,
                sweepAngle = 320f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = glucose?.let { formatGlucoseValue(it) } ?: "--",
                color = AppColors.TextDark,
                fontSize = 36.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "ммоль/л",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatGlucoseValue(value: Float): String = "%.1f".format(value).replace('.', ',')

private fun trendArrow(glucose: Float?): String = when {
    glucose == null -> "—"
    glucose < LOW_GLUCOSE -> "↘"
    glucose > HIGH_GLUCOSE -> "↗"
    else -> "→"
}

private const val LOW_GLUCOSE = 3.9f
private const val HIGH_GLUCOSE = 10f
private const val MAX_RING_GLUCOSE = 16f