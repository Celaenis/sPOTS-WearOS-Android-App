package com.example.tutorial.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SymptomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<SymptomEntity>)

    @Query("SELECT * FROM symptoms WHERE day = :day")
    suspend fun getByDay(day: Int): List<SymptomEntity>

    @Query("SELECT DISTINCT day FROM symptoms WHERE day BETWEEN :from AND :to")
    suspend fun getDaysWithSymptoms(from: Int, to: Int): List<Int>

    @Query("SELECT * FROM symptoms WHERE synced = 0")
    suspend fun getUnsynced(): List<SymptomEntity>

    @Query("UPDATE symptoms SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Int>)

    @Query("DELETE FROM symptoms WHERE day = :day")
    suspend fun deleteDay(day: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<SymptomEntity>)

    @Query("DELETE FROM symptoms")
    suspend fun clearAll()

    @Query(
        """
      SELECT * FROM symptoms
      WHERE millis BETWEEN :from AND :to
      ORDER BY millis ASC
    """
    )
    suspend fun getRange(from: Long, to: Long): List<SymptomEntity>
}
