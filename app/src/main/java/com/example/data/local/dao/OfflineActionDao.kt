package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.OfflineActionEntity

@Dao
interface OfflineActionDao {
    @Query("SELECT * FROM offline_actions ORDER BY createdAt ASC")
    suspend fun getAllPendingActions(): List<OfflineActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: OfflineActionEntity)

    @Query("DELETE FROM offline_actions WHERE id = :actionId")
    suspend fun deleteAction(actionId: Int)
}
