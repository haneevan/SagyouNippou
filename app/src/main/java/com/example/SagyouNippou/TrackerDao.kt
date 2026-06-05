package com.example.SagyouNippou

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Insert
    suspend fun insertClassification(classification: Classification)

    @Query("SELECT * FROM classifications")
    fun getAllClassifications(): Flow<List<Classification>>

    @Insert
    suspend fun insertTimeLog(timeLog: TimeLog): Long

    @Query("UPDATE time_logs SET endTime = :endTime WHERE id = :logId")
    suspend fun stopTimeLog(logId: Long, endTime: Long)

    @Query("SELECT * FROM time_logs WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveTimeLog(): TimeLog?
}