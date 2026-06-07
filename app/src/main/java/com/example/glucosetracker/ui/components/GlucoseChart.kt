package com.example.glucosetracker.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private const val MILLIS_IN_HOUR = 60L * 60L * 1000L
private const val MIN_CHART_GLUCOSE = 2f
private const val MAX_CHART_GLUCOSE = 14f

@Composable
fun GlucoseChart(
    glucoseList: List<GlucoseEntry>,
    modifier: Modifier = Modifier,
    targetLow: Float = 3.9f,
    targetHigh: Float = 10.0f,
    selectedRangeHours: Int,
    onRangeChanged: (Int) -> Unit
) {
    val ranges = remember { listOf(3, 6, 12, 24) }
    val sortedGlucose = remember(glucoseList) { glucoseList.sortedBy { it.timestamp } }
    val now = System.currentTimeMillis()
    val rangeDuration = selectedRangeHours.toLong() * MILLIS_IN_HOUR
    val rangeStart = now - rangeDuration
    val visibleGlucose = remember(sortedGlucose, rangeStart, now) {
        sortedGlucose.filter { it.timestamp in rangeStart..now }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ranges.forEach { range ->
                ChartRangeChip(
                    text = "$range ч",
                    selected = range == selectedRangeHours,
                    onClick = { onRangeChanged(range) }
                )
            }
        }
        GlucoseChartCanvas(
            glucoseList = visibleGlucose,
            rangeStart = rangeStart,
            rangeEnd = now,
            targetLow = targetLow,
            targetHigh = targetHigh,
            modifier = Modifier
                .fillMaxWidth()
                .height(202.dp)
        )
    }
}

@Composable
private fun ChartRangeChip(text: String, selected: Boolean, onClick: () -> Unit) {
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
private fun GlucoseChartCanvas(
    glucoseList: List<GlucoseEntry>,
    rangeStart: Long,
    rangeEnd: Long,
    targetLow: Float,
    targetHigh: Float,
    modifier: Modifier = Modifier
) {
    val lineColor = AppColors.PrimaryGreen
    val targetColor = AppColors.PrimaryGreen.copy(alpha = 0.12f)
    val lowColor = AppColors.Danger
    val highColor = AppColors.HighGlucose
    val textColor = AppColors.TextSecondary
    val gridColor = AppColors.Background
    val darkText = AppColors.TextDark
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor.toArgb()
            textSize = 11.sp.toPx()
            textAlign = Paint.Align.CENTER
        }
        val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkText.toArgb()
            textSize = 14.sp.toPx()
            textAlign = Paint.Align.CENTER
        }

        val plotLeft = 6.dp.toPx()
        val plotRight = size.width - 6.dp.toPx()
        val plotTop = 4.dp.toPx()
        val plotBottom = size.height - 28.dp.toPx()
        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop
        val glucoseMin = min(MIN_CHART_GLUCOSE, targetLow - 0.5f)
        val glucoseMax = max(MAX_CHART_GLUCOSE, targetHigh + 0.5f)
        val glucoseRange = glucoseMax - glucoseMin
        val duration = (rangeEnd - rangeStart).coerceAtLeast(1L).toFloat()
        val thresholdStrokeWidth = 1.5.dp.toPx()
        val thresholdDashEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 7.dp.toPx()), 0f)

        fun xFor(timestamp: Long): Float {
            val progress = ((timestamp - rangeStart).toFloat() / duration).coerceIn(0f, 1f)
            return plotLeft + progress * plotWidth
        }

        fun yFor(glucose: Float): Float {
            val progress = ((glucose - glucoseMin) / glucoseRange).coerceIn(0f, 1f)
            return plotBottom - progress * plotHeight
        }

        repeat(4) { index ->
            val y = plotTop + (plotHeight / 3f) * index
            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val targetTop = yFor(targetHigh)
        val targetBottom = yFor(targetLow)
        drawRoundRect(
            color = targetColor,
            topLeft = Offset(plotLeft, targetTop),
            size = Size(plotWidth, targetBottom - targetTop),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )
        drawLine(
            color = lowColor,
            start = Offset(plotLeft, targetBottom),
            end = Offset(plotRight, targetBottom),
            strokeWidth = thresholdStrokeWidth,
            cap = StrokeCap.Round,
            pathEffect = thresholdDashEffect
        )
        drawLine(
            color = highColor,
            start = Offset(plotLeft, targetTop),
            end = Offset(plotRight, targetTop),
            strokeWidth = thresholdStrokeWidth,
            cap = StrokeCap.Round,
            pathEffect = thresholdDashEffect
        )

        if (glucoseList.isEmpty()) {
            drawContext.canvas.nativeCanvas.drawText(
                "Нет данных для графика",
                size.width / 2f,
                plotTop + plotHeight / 2f,
                emptyPaint
            )
        } else {
            val points = glucoseList.map { entry ->
                Offset(xFor(entry.timestamp), yFor(entry.glucoseLevel))
            }
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point -> lineTo(point.x, point.y) }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }

        val labelTimestamps = listOf(
            rangeStart,
            rangeStart + (rangeEnd - rangeStart) / 3,
            rangeStart + (rangeEnd - rangeStart) * 2 / 3
        )
        labelTimestamps.forEach { timestamp ->
            drawContext.canvas.nativeCanvas.drawText(
                timeFormatter.format(Date(timestamp)),
                xFor(timestamp),
                size.height - 8.dp.toPx(),
                labelPaint
            )
        }
        drawContext.canvas.nativeCanvas.drawText(
            "Сейчас",
            plotRight,
            size.height - 8.dp.toPx(),
            labelPaint.apply { textAlign = Paint.Align.RIGHT }
        )
    }
}