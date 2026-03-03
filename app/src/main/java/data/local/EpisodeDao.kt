package com.example.tutorial.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EpisodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes ORDER BY startTimestamp DESC LIMIT :n")
    suspend fun recent(n: Int = 10): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE synced = 0")
    suspend fun unsynced(): List<EpisodeEntity>

    @Query("UPDATE episodes SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Int>)

    @Query("DELETE FROM episodes")
    suspend fun clearAll()

    @Query(
        """
      SELECT * FROM episodes
      WHERE startTimestamp BETWEEN :from AND :to
      ORDER BY startTimestamp ASC
    """
    )
    suspend fun getRange(from: Long, to: Long): List<EpisodeEntity>

}
