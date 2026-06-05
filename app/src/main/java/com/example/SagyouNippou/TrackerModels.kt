package com.example.SagyouNippou

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "classifications")
data class Classification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String
)

@Entity(
    tableName = "time_logs",
    foreignKeys = [
        ForeignKey(
            entity = Classification::class,
            parentColumns = ["id"],
            childColumns = ["classificationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TimeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classificationId: Int,
    val startTime: Long,
    val endTime: Long? = null
)