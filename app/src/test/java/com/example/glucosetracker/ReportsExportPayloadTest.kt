package com.example.glucosetracker

import com.example.glucosetracker.data.export.DatasetExporter
import com.example.glucosetracker.domain.ml.MlFeatureExporter
import com.example.glucosetracker.ui.screens.ExportRange
import com.example.glucosetracker.ui.screens.ExportType
import com.example.glucosetracker.ui.screens.buildReportsExportPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportsExportPayloadTest {
    @Test
    fun mlTrainingFeaturesSelectionUsesMlFeatureExporterHeaderInsteadOfRawDatasetHeader() {
        val payload = buildReportsExportPayload(
            exportType = ExportType.MlTrainingFeatures,
            exportFormat = DatasetExporter.Format.CSV,
            glucose = emptyList(),
            meals = emptyList(),
            insulin = emptyList(),
            startTimestamp = 0L,
            endTimestamp = 0L
        )

        val header = payload.content.lineSequence().first()
        assertEquals(MlFeatureExporter.CSV_HEADER.joinToString(","), header)
        assertTrue(header.contains("current_glucose_mmol_l"))
        assertTrue(header.contains("target_glucose_120_min_mg_dl"))
        assertFalse(header.contains("event_type"))
        assertTrue(payload.missingTargets)
    }

    @Test
    fun mlTrainingFeaturesSelectionUsesDistinctFileNamePrefix() {
        assertEquals(
            "glucose_ml_features_7d.csv",
            ExportType.MlTrainingFeatures.fileName(ExportRange.Last7Days, DatasetExporter.Format.CSV)
        )
    }
}