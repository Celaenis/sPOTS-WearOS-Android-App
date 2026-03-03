package com.example.tutorial.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "symptoms",
    indices = [Index(value = ["day", "symptomId"], unique = true)]
)
data class SymptomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val day: Int,
    val millis: Long,
    val symptomId: String,
    val severity: Int,
    val synced: Boolean = false
)
