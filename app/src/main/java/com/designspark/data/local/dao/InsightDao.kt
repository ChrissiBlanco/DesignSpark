package com.designspark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.designspark.data.local.entity.InsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    @Query("SELECT * FROM insights WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun getByProject(projectId: String): Flow<List<InsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<InsightEntity>)

    @Query("DELETE FROM insights WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: String)
}
