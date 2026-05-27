package com.example.glucosetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glucosetracker.ui.theme.AppColors

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
    val progress = glucose?.let { ((it - LOW_GLUCOSE) / (HIGH_GLUCOSE - LOW_GLUCOSE)).coerceIn(0f, 1f) } ?: 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(132.dp)) {
                    val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    val inset = stroke.width / 2
                    val arcSize = Size(size.width - stroke.width, size.height - stroke.width)

                    drawArc(
                        color = AppColors.Background,
                        startAngle = 140f,
                        sweepAngle = 280f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = stroke
                    )
                    drawArc(
                        color = statusColor,
                        startAngle = 140f,
                        sweepAngle = 280f * progress,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = stroke
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = glucose?.let { "%.1f".format(it) } ?: "--",
                        color = AppColors.TextDark,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ммоль/л",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Текущая глюкоза",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, CircleShape)
                    )
                    Text(
                        text = trend,
                        modifier = Modifier.padding(start = 8.dp),
                        color = AppColors.TextDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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

private const val LOW_GLUCOSE = 3.9f
private const val HIGH_GLUCOSE = 10f