package com.designspark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.designspark.data.local.entity.GeneratedInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedInsightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<GeneratedInsightEntity>)

    @Query("SELECT * FROM generated_insights WHERE project_id = :projectId ORDER BY order_index ASC")
    fun getByProjectId(projectId: String): Flow<List<GeneratedInsightEntity>>

    @Query(
        "SELECT * FROM generated_insights WHERE project_id = :projectId AND type = :type ORDER BY order_index ASC"
    )
    fun getByProjectIdAndType(projectId: String, type: String): Flow<List<GeneratedInsightEntity>>

    @Query("DELETE FROM generated_insights WHERE project_id = :projectId")
    suspend fun deleteByProjectId(projectId: String)

    @Query("DELETE FROM generated_insights WHERE project_id = :projectId AND type = :type")
    suspend fun deleteByProjectIdAndType(projectId: String, type: String)
}
