package com.example.tutorial.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "episodes",
    indices = [Index(value = ["startTimestamp", "endTimestamp"], unique = true)]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val baselineHr: Int,
    val peakHr: Int,
    val delta: Int,
    val synced: Boolean = false
)
