package com.example.SagyouNippou

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// We inject the dispatcher here with a default value so it satisfies SonarQube
// while remaining perfectly easy to use normally.
class CsvExporter(
    private val context: Context,
    private val db: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun exportLogsToCsv(): File? = withContext(ioDispatcher) {
        val dao = db.trackerDao()

        // 1. Fetch classifications to map IDs to readable names
        val classificationsList = dao.getAllClassifications().first()
        val classificationMap = classificationsList.associate { it.id to it.name }

        // 2. Set up the export directory and file name
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "TimeTracker_Export_$timestamp.csv"

        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val csvFile = File(baseDir, fileName)

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        try {
            val writer = FileWriter(csvFile)

            // Write CSV Header row
            writer.append("Log ID,Classification Name,Start Time,End Time,Duration (Seconds)\n")

            // Dummy logic representing row structure
            val sampleStartTime = System.currentTimeMillis() - 3600000
            val sampleEndTime = System.currentTimeMillis()
            val sampleClassificationName = classificationMap[1] ?: "Unknown Category"
            val durationSeconds = (sampleEndTime - sampleStartTime) / 1000

            writer.append("1,")
            writer.append("$sampleClassificationName,")
            writer.append("${dateFormatter.format(Date(sampleStartTime))},")
            writer.append("${dateFormatter.format(Date(sampleEndTime))},")
            writer.append("$durationSeconds\n")

            writer.flush()
            writer.close()
            csvFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}