package com.example.glucosetracker.ui.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun GlucoseChart(
    glucoseList: List<GlucoseEntry>,
    modifier: Modifier = Modifier
) {

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context)
        },
        update = { chart ->

            val entries = glucoseList.mapIndexed { index, item ->
                Entry(index.toFloat(), item.glucoseLevel)
            }

            val dataSet = LineDataSet(entries, "Глюкоза")

            dataSet.color = Color.GREEN
            dataSet.valueTextColor = Color.BLACK
            dataSet.lineWidth = 3f
            dataSet.circleRadius = 5f

            val lineData = LineData(dataSet)

            chart.data = lineData

            chart.description.isEnabled = false
            chart.invalidate()
        }
    )
}