package com.example.glucosetracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.ui.theme.AppColors
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun GlucoseChart(
    glucoseList: List<GlucoseEntry>,
    modifier: Modifier = Modifier
) {
    val lineColor = AppColors.PrimaryGreen.toArgb()
    val textColor = AppColors.TextSecondary.toArgb()
    val gridColor = AppColors.Background.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                setNoDataText("Нет данных для графика")
                setTouchEnabled(false)
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                legend.isEnabled = false
                description.isEnabled = false
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                axisLeft.axisMinimum = 2f
                axisLeft.axisMaximum = 14f
                axisLeft.setDrawAxisLine(false)
                axisLeft.setDrawGridLines(true)
            }
        },
        update = { chart ->
            val entries = glucoseList.mapIndexed { index, item ->
                Entry(index.toFloat(), item.glucoseLevel)
            }
            val dataSet = LineDataSet(entries, "Глюкоза").apply {
                color = lineColor
                valueTextColor = textColor
                lineWidth = 3f
                circleRadius = 4f
                setCircleColor(lineColor)
                setDrawCircleHole(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.xAxis.textColor = textColor
            chart.axisLeft.textColor = textColor
            chart.axisLeft.gridColor = gridColor
            chart.axisLeft.granularity = 2f
            chart.axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            chart.setNoDataTextColor(textColor)
            chart.description.isEnabled = false
            chart.invalidate()
        }
    )
}