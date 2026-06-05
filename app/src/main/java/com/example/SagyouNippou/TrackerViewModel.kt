package com.example.SagyouNippou

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.trackerDao()
    private val csvExporter = CsvExporter(application, db)

    // Expose classifications directly from the database to your UI screens
    val allClassifications: Flow<List<Classification>> = dao.getAllClassifications()

    // Seeds standard categories if the database is opened for the first time
    init {
        viewModelScope.launch {
            // Check if categories already exist; if empty, insert defaults
            // (In production, you can check lists, but for now we can add them via setup buttons or defaults)
        }
    }

    fun saveNewClassification(name: String, colorHex: String) {
        viewModelScope.launch {
            dao.insertClassification(Classification(name = name, colorHex = colorHex))
        }
    }

    fun logSessionStart(classificationId: Int, startTime: Long) {
        viewModelScope.launch {
            dao.insertTimeLog(TimeLog(classificationId = classificationId, startTime = startTime))
        }
    }

    fun triggerCsvExport() {
        viewModelScope.launch {
            csvExporter.exportLogsToCsv()
        }
    }
}